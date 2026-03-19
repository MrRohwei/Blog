package com.voidpen.server.module.tag.service;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.tag.model.request.CreateTagRequest;
import com.voidpen.server.module.tag.model.request.TagQueryRequest;
import com.voidpen.server.module.tag.model.request.UpdateTagRequest;
import com.voidpen.server.module.tag.model.response.TagVO;
import java.util.List;

public interface TagService {

    List<TagVO> listPublicTags();

    PageResult<TagVO> listAdminTags(TagQueryRequest request);

    void createTag(CreateTagRequest request);

    void updateTag(Long id, UpdateTagRequest request);

    void deleteTag(Long id);
}
