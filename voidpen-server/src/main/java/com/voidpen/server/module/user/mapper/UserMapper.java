package com.voidpen.server.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voidpen.server.module.user.entity.TUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<TUser> {
}
