package com.houseKeeping.logModel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houseKeeping.logModel.pojo.LogBefore;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LogBeforeMapper extends BaseMapper<LogBefore> {
}
