package com.pasan.config.jackson;

import com.alibaba.fastjson.serializer.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JsonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder.serializerByType(Long.class, com.fasterxml.jackson.databind.ser.std.ToStringSerializer.instance)
                .serializerByType(Long.TYPE, com.fasterxml.jackson.databind.ser.std.ToStringSerializer.instance);
    }
}
