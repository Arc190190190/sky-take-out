package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
public interface ShoppingCartService {

    /**
     * 查询购物车
     * @return
     */
    List<ShoppingCart> list();

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 清空购物车
     */
    void clean();

    /**
     * 删除购物车
     * @param shoppingCartDTO
     */
    void sub(ShoppingCartDTO shoppingCartDTO);
}
