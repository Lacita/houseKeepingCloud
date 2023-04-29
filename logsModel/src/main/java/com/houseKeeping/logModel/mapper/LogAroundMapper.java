package com.houseKeeping.logModel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houseKeeping.logModel.pojo.LogAround;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LogAroundMapper extends BaseMapper<LogAround> {
}
