package com.voidpen.server.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import java.util.List;
import org.junit.jupiter.api.Test;

class MybatisPlusConfigTests {

    @Test
    void shouldRegisterMysqlPaginationInterceptor() {
        MybatisPlusConfig config = new MybatisPlusConfig();

        MybatisPlusInterceptor interceptor = config.mybatisPlusInterceptor();
        List<InnerInterceptor> innerInterceptors = interceptor.getInterceptors();

        assertThat(innerInterceptors).hasSize(1);
        assertThat(innerInterceptors.get(0)).isInstanceOf(PaginationInnerInterceptor.class);

        PaginationInnerInterceptor paginationInterceptor =
            (PaginationInnerInterceptor) innerInterceptors.get(0);
        assertThat(paginationInterceptor.getDbType()).isEqualTo(DbType.MYSQL);
    }
}
