package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Arc
 * @version v1.0
 */
@Mapper
public interface OrderDetailMapper {
    /**
     * 插入订单详情数据
     * @param orderDetail
     */
    void insert(OrderDetail orderDetail);
}
