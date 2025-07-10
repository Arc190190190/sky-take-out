package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        // 先从Redis中获取缓存数据
        String key = "dish_" + categoryId;
        List<DishVO> dishList= (List<DishVO>) redisTemplate.opsForValue().get(key);
        // 如果缓存数据存在，则直接返回，不用查询数据库
        if (dishList != null && dishList.size() > 0){
            return Result.success(dishList);
        }

        // 如果缓存数据不存在，则查询数据库，将查询到的数据放入Redis缓存中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        dishList = dishService.listWithFlavor(dish);
        // 将查询到的菜品数据放入Redis缓存中
        redisTemplate.opsForValue().set(key,dishList);

        return Result.success(dishList);
    }

}
