package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param DishIds
     * @return
     */
    List<Long> getSetmealIdByDishId(List<Long> DishIds);

    /**
     * 批量保存套餐和菜品的关联关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据菜品id删除关联关系
     * @param id
     */
    @Delete("delete from setmeal_dish where dish_id = #{id}")
    void deleteByDishId(Long id);

}
