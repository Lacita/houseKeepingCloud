package com.houseKeeping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houseKeeping.pojo.PhoneUsers;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ReceiveManager extends BaseMapper<PhoneUsers> {
}
