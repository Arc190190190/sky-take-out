package com.sky.service.impl;

import cn.hutool.db.sql.Order;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.TakeawayOrderGenerator;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Arc
 * @version v1.0
 */
@Service
@Slf4j
public class OderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;
    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        // 获取地址簿 信息
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        //判断异常信息(地址簿为空，购物车为空)
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //检查用户收货地址是否超过配送范围
        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        //获取购物车信息
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(userId);
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 生成订单编号
        String orderNumber = TakeawayOrderGenerator.generate();
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(orderNumber);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setAddress(addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());

        //插入一条订单数据
        orderMapper.insert(orders);

        //插入多条订单明细数据
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderId(orders.getId())
                    .dishId(shoppingCart.getDishId())
                    .setmealId(shoppingCart.getSetmealId())
                    .name(shoppingCart.getName())
                    .image(shoppingCart.getImage())
                    .number(shoppingCart.getNumber())
                    .amount(shoppingCart.getAmount())
                    .dishFlavor(shoppingCart.getDishFlavor())
                    .build();
            orderDetailMapper.insert(orderDetail);
        }

        //删除购物车数据
        shoppingCartMapper.deleteBatchByUserId(BaseContext.getCurrentId());
        //封装VO数据并返回
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        // 根据用户id查询用户
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED; //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        //获取订单号码
        String orderNumber = ordersPaymentDTO.getOrderNumber();

        log.info("调用updateStatus, 用于替换微信支付更新数据库状态的问题");
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderNumber);
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //查询数据
        Page<OrderVO> orderVOS = orderMapper.pageQuery(ordersPageQueryDTO);
        long total = orderVOS.getTotal();
        List<OrderVO> result = orderVOS.getResult();

        //封装订单详情信息到DTO
        for (OrderVO orderVO: result) {
            List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(orderVO.getId());
            orderVO.setOrderDetailList(orderDetail);
        }
        //返回结果
        return new PageResult(total, result);
    }

    /**
     * 订单详情
     * @param id
     * @return
     */
    public OrderVO getById(Long id) {
        OrderVO orderVO = new OrderVO();
        //订单
        Orders order = orderMapper.getById(id);
        BeanUtils.copyProperties(order, orderVO);
        //订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    public void cancel(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        //判断订单状态,订单状态为3或者4不能取消
        if (ordersDB == null || ordersDB.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //如果已经支付待接单，则需要退款

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Transactional
    public void repetition(Long id) {
        List <OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        //构造购物车数据
        List <ShoppingCart> shoppingCartList = orderDetails.stream().map(x ->
                {
                    ShoppingCart shoppingCart = new ShoppingCart();
                    BeanUtils.copyProperties(x, shoppingCart);
                    shoppingCart.setUserId(BaseContext.getCurrentId());
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    return shoppingCart;
                }).collect(Collectors.toList());
        for (ShoppingCart shoppingCart : shoppingCartList) {
            //判断购物车商品还是否在售或者存在，如果菜品或套餐已下架，则抛出业务异常
            if (shoppingCart.getSetmealId() == null){
                Dish dish = dishMapper.getById(shoppingCart.getDishId());
                if (dish == null || !dish.getStatus().equals(1)){
                    throw new OrderBusinessException(MessageConstant.DISH_OFF_SALE);
                }
            }else if (shoppingCart.getDishId() == null){
                Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                if (setmeal == null || !setmeal.getStatus().equals(1)){
                    throw new OrderBusinessException(MessageConstant.SETMEAL_OFF_SALE);
                }
            }
        }
        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    /**
     * 条件搜索
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.conditionSearch(ordersPageQueryDTO);
        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 统计订单数据
     * @return
     */
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        //获取各个订单状态的统计
        Integer tobeConfirmed = orderMapper.getStatusCount(Orders.TO_BE_CONFIRMED);
        Integer Confirmed = orderMapper.getStatusCount(Orders.CONFIRMED);
        Integer delivery = orderMapper.getStatusCount(Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setConfirmed(Confirmed);
        orderStatisticsVO.setToBeConfirmed(tobeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(delivery);

        return orderStatisticsVO;
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        Orders order = new Orders();
        //判断订单是否已经支付，如果已经支付则需退款
        if (orders.getPayStatus() == Orders.PAID){
            order.setPayStatus(Orders.REFUND);
        }
        order.setId(ordersCancelDTO.getId());
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 拒单 rejection
     * @param ordersRejectionDTO
     */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        Orders order = new Orders();
        //只有订单状态为待接单时，才可拒单
        if (orders == null && !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //判断订单是否已经支付，如果已经支付则需退款
        if (orders.getPayStatus() == Orders.PAID){
            order.setPayStatus(Orders.REFUND);
        }
        order.setId(ordersRejectionDTO.getId());
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersRejectionDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 确认订单
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders build = Orders.builder().id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(build);
    }


    /**
     * 派送订单
     *
     * @param id
     */
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }


    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);
        log.info("shopaddress:{}",shopAddress);
        log.info("ak:{}",ak);
        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }
}
