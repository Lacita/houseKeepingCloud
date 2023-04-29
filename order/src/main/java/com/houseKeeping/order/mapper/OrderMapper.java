package com.houseKeeping.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houseKeeping.order.entity.TaskInfo;
import com.houseKeeping.order.entity.TaskInfoShow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OrderMapper extends BaseMapper<TaskInfo> {

    List<TaskInfo> selectTaskByPhoneUsers(@Param("phoneUser") String phoneUser, @Param("start") Integer start, @Param("size") Integer size);

    List<TaskInfo> selectFinishByPhoneUser(@Param("phoneUser") String phoneUser, @Param("start") Integer start, @Param("size") Integer size);

    List<TaskInfoShow> selectTaskDetailList(@Param("offSet") int offSet, @Param("size") Integer size);

    int SearchTaskInfoFinishCount();


}
