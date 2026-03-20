package com.voidpen.server.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@JsonComponent
public class LocalDateTimeJsonComponent {

    private static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter[] SUPPORTED_INPUT_FORMATTERS = new DateTimeFormatter[] {
        DATETIME_FORMATTER,
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    };

    public static class Serializer extends JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeString(value.format(DATETIME_FORMATTER));
        }
    }

    public static class Deserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (!StringUtils.hasText(text)) {
                return null;
            }

            String rawValue = text.trim();
            for (DateTimeFormatter formatter : SUPPORTED_INPUT_FORMATTERS) {
                try {
                    return LocalDateTime.parse(rawValue, formatter);
                } catch (DateTimeParseException ignored) {
                    // Try next supported format
                }
            }

            try {
                return OffsetDateTime.parse(rawValue).toLocalDateTime();
            } catch (DateTimeParseException ignored) {
                // Fall through
            }

            throw InvalidFormatException.from(
                p,
                "时间格式错误，支持 yyyy-MM-dd HH:mm:ss",
                rawValue,
                LocalDateTime.class
            );
        }
    }
}
