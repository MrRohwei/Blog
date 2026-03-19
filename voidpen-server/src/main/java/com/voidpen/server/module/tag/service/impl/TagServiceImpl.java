package com.voidpen.server.module.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.tag.entity.TTag;
import com.voidpen.server.module.tag.mapper.TagMapper;
import com.voidpen.server.module.tag.model.request.CreateTagRequest;
import com.voidpen.server.module.tag.model.request.TagQueryRequest;
import com.voidpen.server.module.tag.model.request.UpdateTagRequest;
import com.voidpen.server.module.tag.model.response.TagVO;
import com.voidpen.server.module.tag.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    public List<TagVO> listPublicTags() {
        return tagMapper.selectPublicListWithBlogCount();
    }

    @Override
    public PageResult<TagVO> listAdminTags(TagQueryRequest request) {
        Page<TagVO> page = new Page<>(request.getPage(), request.getSize());
        IPage<TagVO> pageData = tagMapper.selectAdminPageWithBlogCount(page, request.getKeyword());
        return PageResult.of(pageData);
    }

    @Override
    public void createTag(CreateTagRequest request) {
        checkDuplicateTagName(request.getName(), null);
        TTag tag = new TTag().setName(request.getName()).setColor(request.getColor());
        tagMapper.insert(tag);
    }

    @Override
    public void updateTag(Long id, UpdateTagRequest request) {
        getRequiredTag(id);
        checkDuplicateTagName(request.getName(), id);
        TTag toUpdate = new TTag().setId(id).setName(request.getName()).setColor(request.getColor());
        tagMapper.updateById(toUpdate);
    }

    @Override
    public void deleteTag(Long id) {
        getRequiredTag(id);
        tagMapper.deleteBlogTagRelationsByTagId(id);
        tagMapper.deleteById(id);
    }

    private TTag getRequiredTag(Long id) {
        TTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
        return tag;
    }

    private void checkDuplicateTagName(String name, Long excludeId) {
        LambdaQueryWrapper<TTag> wrapper = new LambdaQueryWrapper<TTag>().eq(TTag::getName, name);
        if (excludeId != null) {
            wrapper.ne(TTag::getId, excludeId);
        }
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.TAG_DUPLICATE);
        }
    }
}
