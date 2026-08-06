package egovframework.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class EgovConfigDatasource {

    @Value("${spring.profiles.active}")
    private String profile;
    @Value("${spring.config.datasource.jndi-name}")
    private String jndiName;
    // 로컬 환경을 위한 변수 추가
    @Value("${spring.config.datasource.driver-class-name}")
    private String driver;
    @Value("${spring.config.datasource.url}")
    private String url;
    @Value("${spring.config.datasource.username}")
    private String username;
    @Value("${spring.config.datasource.password}")
    private String password;

    @Bean(name="dataSource")
    public DataSource dataSource() {
        log.debug("@@@@@@@@@@driver: " + driver);
        log.debug("@@@@@@@@@@username: " + username);
        log.debug("@@@@@@@@@@password: " + password);
        // 프로필 읽기
        if (!"oper".equals(profile)) {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(driver);
            ds.setUrl(url);
            ds.setUsername(username);
            ds.setPassword(password);
            return ds;
        } else {
            // JNDI 방식
            org.springframework.jndi.JndiObjectFactoryBean jndi = new org.springframework.jndi.JndiObjectFactoryBean();
            jndi.setJndiName(jndiName);
            jndi.setResourceRef(true);
            try {
                jndi.afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException("JNDI 설정 오류", e);
            }
            return (DataSource) jndi.getObject();
        }
    }

}
