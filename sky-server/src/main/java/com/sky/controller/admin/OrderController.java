package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.lettuce.core.output.ListOfGenericMapsOutput;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

/**
 * @author Arc
 * @version v1.0
 */

@RestController("adminOrderController")
@Slf4j
@Api(tags = "订单管理接口")
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        log.info("查询订单：{}", id);
        OrderVO orderVO = orderService.getById(id);
        return Result.success(orderVO);
    }

    @ApiOperation("订单搜索,分页返回")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(@ModelAttribute OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("分页查询订单：{}", ordersPageQueryDTO);
        PageResult page = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 统计订单各个状态数据
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("统计订单各个状态数据")
    public Result<OrderStatisticsVO> statistics(){
        log.info("统计订单各个状态数据");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 取消订单
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单：{}", ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 拒单
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("取消订单：{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 确认订单
     * @return
     */
    @ApiOperation("确认订单")
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("确认订单：{}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 派送订单
     *
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable("id") Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     *
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
