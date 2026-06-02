package egovframework;

import egovframework.config.*;
import egovframework.filter.AccessLogFilter;
import egovframework.filter.XssFilter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import java.util.List;

public class WebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    // 1. Root Context 설정 (데이터베이스, 트랜잭션, 공통 서비스 등)
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { EgovConfigWeb.class };
    }

    // 2. DispatcherServlet 설정 (웹 관련 설정)
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { EgovConfigWeb.class };
    }

    // 3. DispatcherServlet 매핑
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected Filter[] getServletFilters() {

        // 여기서 필터를 스프링 컨텍스트의 빈과 연결하여 등록합니다!
        return new Filter[] {
                new CharacterEncodingFilter("UTF-8", true),
                new AccessLogFilter(),
                new XssFilter(),
                new DelegatingFilterProxy("springSecurityFilterChain")
        };
    }

}