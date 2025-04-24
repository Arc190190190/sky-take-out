package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */


public interface CategoryService {

    /**
     * 修改分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);


    /**
     * 根据类型查询
     * @param type
     * @return
     */
    List<Category> getByType(Integer type);

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 启用禁用
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 删除指定分类
     * @param id
     */
    void delete(Long id);
}
