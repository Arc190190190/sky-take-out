package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Arc
 * @version v1.0
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端-购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 返回购物车信息
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("返回购物车信息")
    public Result<List<ShoppingCart>> list(){
        log.info("查询购物车");
        return Result.success(shoppingCartService.list());
    }

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车:{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 清空购物车
     * @return
     */
    @ApiOperation("清空购物车中的商品")
    @DeleteMapping("/clean")
    public Result clean(){
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }

    @ApiOperation("删除购物车中的商品")
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中的商品:{}", shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

}
