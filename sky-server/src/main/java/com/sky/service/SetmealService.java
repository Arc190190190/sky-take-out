package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * 套餐业务
 * @author Arc
 * @version v1.0
 */
public interface SetmealService {

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveSetmealWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 套餐详情查询
     * @param id
     * @return
     */
    SetmealVO selectById(Long id);

    /**
     * 批量删除套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    void updateSetmealDish(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void batchDelete(List<Long> ids);
}
