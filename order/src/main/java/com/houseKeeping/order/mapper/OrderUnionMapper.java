package com.houseKeeping.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houseKeeping.order.entity.TaskUnionPhoneUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderUnionMapper extends BaseMapper<TaskUnionPhoneUser> {
}
