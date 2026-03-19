package com.voidpen.server.common.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;

    private Long total;

    private Long page;

    private Long size;

    private Long pages;

    public static <T> PageResult<T> of(IPage<T> pageData) {
        return new PageResult<>(
            pageData.getRecords(),
            pageData.getTotal(),
            pageData.getCurrent(),
            pageData.getSize(),
            pageData.getPages()
        );
    }
}
