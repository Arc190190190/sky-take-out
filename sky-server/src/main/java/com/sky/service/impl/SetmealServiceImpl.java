package com.sky.service.impl;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐和套餐菜品关系
     * @param setmealDTO
     */
    @Transactional
    public void saveSetmealWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 插入套餐数据
        setmealMapper.insert(setmeal);
        Long id = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 批量插入套餐菜品关系数据
        if (setmealDishes != null && setmealDishes.size() > 0){
            setmealDishes.forEach(dish -> dish.setSetmealId(id));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //使用PageHelper插件进行分页,只有效于下条sql
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        //查询分页数据
        Page<SetmealVO> setmeals = setmealMapper.pageQuery(setmealPageQueryDTO);
        //返回结果
        return new PageResult(setmeals.getTotal(), setmeals.getResult());
    }

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    @Transactional
    public SetmealVO selectById(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        Setmeal setmeal = setmealMapper.selectById(id);
        //将套餐信息拷贝到setmealVO中
        BeanUtils.copyProperties(setmeal, setmealVO);
        //根据id查询套餐菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 启用禁用套餐
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status == StatusConstant.ENABLE){
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Transactional
    public void updateSetmealDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //更新套餐数据
        setmealMapper.update(setmeal);
        //删除套餐菜品关系数据
        Long id = setmealDTO.getId();
        setmealDishMapper.deleteByDishId(id);
        //插入新的套餐菜品关系数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0){
            setmealDishes.forEach(dish -> dish.setSetmealId(id));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    public void batchDelete(List<Long> ids) {
        List<Setmeal> setmeals = setmealMapper.selectByIds(ids);
        for (Setmeal setmeal : setmeals) {
            //判断当前套餐是否在售
            if (setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //批量删除套餐数据
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteByDishIds(ids);
    }


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
