package com.houseKeeping.systemUser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houseKeeping.systemUser.pojo.pojo.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SystemUser extends BaseMapper<SysUser> {
}
