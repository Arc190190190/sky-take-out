package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 返回购物车
     * @return
     */
    public List<ShoppingCart> list() {
        Long currentId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.list(currentId);
        return list;
    }

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //得到当前用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //查询要插入的商品是否已经在购物车中
        ShoppingCart cart = shoppingCartMapper.getByDishIdAndSetmealId(shoppingCart);
        //已经在，则修改数量
        if (cart != null) {
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
        }else{
            //不在，则插入
            //判断是否是套餐还是菜品
            if (shoppingCart.getSetmealId() != null) {
                Setmeal setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
            } else {
                Dish dish = dishMapper.getById(shoppingCart.getDishId());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 清空购物车
     */
    public void clean() {
       shoppingCartMapper.deleteBatchByUserId(BaseContext.getCurrentId());
    }

    /**
     * 删除购物车中的商品
     * @param shoppingCartDTO
     */
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //得到当前要删除商品在表中的信息
        ShoppingCart cart = shoppingCartMapper.getByDishIdAndSetmealId(shoppingCart);
        //如果购物车中该商品数量大于1
        if (cart.getNumber() > 1) {
            //则数量减1
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.update(cart);
        } else {
            //如果购物车中该商品数量等于1
            //则删除该商品
            shoppingCartMapper.delete(cart);
        }

    }
}
