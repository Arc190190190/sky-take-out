package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> getByOrderId(Long id);
}
