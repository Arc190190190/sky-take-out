package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Arc
 * @version v1.0
 * 管理菜品接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品接口")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 新增菜品和口味
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增菜品和口味")
    public Result<String> save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavors(dishDTO);
        return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询菜品")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品：{}",dishPageQueryDTO);
        PageResult result = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(result);
    }

    /**
     * 启用禁用菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用禁用菜品")
    public Result startOrStop(@PathVariable Integer status,Long id) {
        log.info("菜品起售停售：{}",id);
        dishService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dish = dishService.selectById(id);
        return Result.success(dish);
    }

    /**
     * 批量删除菜品
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "批量删除菜品")
    public Result batchDelete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);
        dishService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改菜品")
    public Result updateDishWithFlavors(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateDishWithFlavors(dishDTO);
        return Result.success();
    }

}
