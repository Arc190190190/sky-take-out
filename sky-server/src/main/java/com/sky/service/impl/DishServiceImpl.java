package com.sky.service.impl;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和口味
     *
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //插入菜品数据
        dishMapper.insert(dish);
        //获取菜品id
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //如果 flavors不为空，则批量插入
        if (flavors != null && flavors.size() > 0){
            //给每个口味设置菜品id
           flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            //批量插入口味
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<Dish> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 启用停用菜品
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder().
                status(status).
                id(id).
                build();
        dishMapper.update(dish);
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Transactional
    public DishVO selectById(Long id) {
        DishVO dishVO = new DishVO();
        //根据id查询菜品
        Dish dish = dishMapper.getById(id);
        //将菜品信息拷贝到dishVO中
        BeanUtils.copyProperties(dish, dishVO);
        //根据id查询菜品口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getDishFlavorsByDishId(id);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 批量删除菜品
     * @param DishIds
     */
    @Transactional
    public void batchDelete(List<Long> DishIds) {
        //判断当前菜品是否在售中
        for (Long id : DishIds) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == 1){
                //如果菜品在售则抛出异常
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断当前菜品是否关联了套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishId(DishIds);
        if (setmealIds != null && setmealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品
        dishMapper.deleteByIds(DishIds);
        //删除菜品关联口味
        dishFlavorMapper.deleteByDishIds(DishIds);

    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    public void updateDishWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品数据
        dishMapper.update(dish);
        //删除菜品对应的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //修改当前菜品对应的口味数据
        if (flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch( flavors);
        }
    }
}
