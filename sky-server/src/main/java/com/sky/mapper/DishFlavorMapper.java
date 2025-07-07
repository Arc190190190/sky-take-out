package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入
      * @param dishFlavors
     */
    void insertBatch(List<DishFlavor> dishFlavors);

    /**
     * 根据菜品id批量删除
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);
}
