package com.voidpen.server.module.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voidpen.server.module.comment.entity.TComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<TComment> {
}
