package com.voidpen.server.module.system.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemSchemaInitializer {

    private static final String CREATE_SYSTEM_CONFIG_SQL = """
        CREATE TABLE IF NOT EXISTS t_system_config (
            id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
            config_group VARCHAR(50) NOT NULL,
            config_key VARCHAR(100) NOT NULL,
            config_value TEXT,
            value_type VARCHAR(20) NOT NULL DEFAULT 'string',
            description VARCHAR(255),
            editable TINYINT NOT NULL DEFAULT 1,
            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            UNIQUE KEY uniq_group_key (config_group, config_key)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """;

    private static final String CREATE_SYSTEM_OPERATION_LOG_SQL = """
        CREATE TABLE IF NOT EXISTS t_system_operation_log (
            id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
            operator_id BIGINT,
            operation_type VARCHAR(50) NOT NULL,
            target_scope VARCHAR(200) NOT NULL,
            detail VARCHAR(500),
            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_created_at (created_at)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """;

    private static final String INSERT_DEFAULT_CONFIG_SQL = """
        INSERT INTO t_system_config (config_group, config_key, config_value, value_type, description, editable)
        SELECT ?, ?, ?, ?, ?, ?
        WHERE NOT EXISTS (
            SELECT 1 FROM t_system_config WHERE config_group = ? AND config_key = ?
        )
        """;

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initSchema() {
        jdbcTemplate.execute(CREATE_SYSTEM_CONFIG_SQL);
        jdbcTemplate.execute(CREATE_SYSTEM_OPERATION_LOG_SQL);
        initDefaultConfigs();
    }

    private void initDefaultConfigs() {
        List<DefaultConfig> defaults = List.of(
            new DefaultConfig("site", "title", "Voidpen Blog", "string", "站点标题", 1),
            new DefaultConfig("site", "subtitle", "记录与分享", "string", "站点副标题", 1),
            new DefaultConfig("seo", "keywords", "Voidpen,博客,技术分享", "string", "SEO 关键词", 1),
            new DefaultConfig("comment", "autoAudit", "false", "boolean", "评论自动审核", 1),
            new DefaultConfig("upload", "maxSizeMb", "10", "number", "上传文件大小限制（MB）", 0),
            new DefaultConfig("feature", "enableRegister", "true", "boolean", "开启前台注册功能", 1)
        );

        for (DefaultConfig config : defaults) {
            jdbcTemplate.update(
                INSERT_DEFAULT_CONFIG_SQL,
                config.group(),
                config.key(),
                config.value(),
                config.type(),
                config.description(),
                config.editable(),
                config.group(),
                config.key()
            );
        }
    }

    private record DefaultConfig(
        String group,
        String key,
        String value,
        String type,
        String description,
        int editable
    ) {}
}
