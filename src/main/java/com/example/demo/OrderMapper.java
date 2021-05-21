package com.example.demo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Mapper
@Repository
public interface OrderMapper extends BaseMapper<Order> {

    public Integer addOrder(Order order);

    public BigDecimal selectSumsByOid(@Param("oid") Long oid);

    public Order selectStatusById(@Param("id")Long id);

}
