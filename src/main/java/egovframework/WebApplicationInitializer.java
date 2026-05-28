package egovframework;

import egovframework.config.*;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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
                new AccessLogFilter()
        };
    }


}