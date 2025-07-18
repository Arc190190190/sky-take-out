package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
@Mapper
public interface ShoppingCartMapper {

    /**
     * 条件查询是否已经存在这条数据
     * @param shoppingCart
     * @return
     */
    ShoppingCart getByDishIdAndSetmealId(ShoppingCart shoppingCart);

    /**
     * 修改购物车数据
     * @param cart
     */
    void update(ShoppingCart cart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    /**
     * 查询当前用户的购物车数据
     * @param currentId
     * @return
     */
    List<ShoppingCart> list(Long currentId);

    /**
     * 清空当前用户的购物车数据
     * @param currentId
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void deleteBatchByUserId(Long currentId);

    /**
     * 删除购物车数据
     * @param shoppingCart
     */
    void delete(ShoppingCart shoppingCart);

    /**
     * 批量插入
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
