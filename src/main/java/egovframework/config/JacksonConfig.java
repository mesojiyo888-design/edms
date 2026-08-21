package egovframework.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edms.common.util.HtmlEscapeUtils;
import egovframework.filter.XssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

@Configuration
public class JacksonConfig {
    @Autowired
    private XssProperties xssProperties;

    @Bean
    public ObjectMapper objectMapper()
    {
        SimpleModule xssModule = new SimpleModule();

        xssModule.addDeserializer(String.class, new JsonDeserializer<String>()
        {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
            {
                String fieldName = p.getCurrentName();
                String value     = p.getValueAsString();

                if(fieldName != null && xssProperties.getExcludeParams().contains(fieldName))
                {
                    return value;   // 이스케이프 스킵
                }

                return HtmlEscapeUtils.escape(value);
            }
        });

        return Jackson2ObjectMapperBuilder.json().modules(xssModule).build();
    }
}