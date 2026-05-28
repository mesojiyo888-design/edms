package egovframework.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
public class YamlConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();

        // 1. 먼저 공통 application.yml만 읽기
        YamlPropertiesFactoryBean commonYaml = new YamlPropertiesFactoryBean();
        commonYaml.setResources(new ClassPathResource("application.yml"));
        Properties commonProps = commonYaml.getObject();

        // 2. application.yml에서 spring.profiles.active 값을 추출
        String profile = commonProps.getProperty("spring.profiles.active", "local");

        // 3. 공통 설정과 프로필 설정을 모두 합치기
        YamlPropertiesFactoryBean combinedYaml = new YamlPropertiesFactoryBean();

        List<ClassPathResource> resources = new ArrayList<>();
        resources.add(new ClassPathResource("application.yml"));

        ClassPathResource profileResource = new ClassPathResource("application-" + profile + ".yml");
        if (profileResource.exists()) {
            resources.add(profileResource);
        }

        combinedYaml.setResources(resources.toArray(new ClassPathResource[0]));

        configurer.setProperties(combinedYaml.getObject());
        configurer.setLocalOverride(true); // 프로필 설정이 우선순위 갖도록 함

        return configurer;
    }
}