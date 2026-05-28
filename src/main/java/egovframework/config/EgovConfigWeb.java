package egovframework.config;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.annotation.PostConstruct;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"edms", "egovframework"}) // 핵심 컨트롤러와 서비스 스캔
@Import({
        EgovConfigAspect.class,
        EgovConfigCommon.class,
        EgovConfigDatasource.class,
        EgovConfigIdGeneration.class,
        EgovConfigMapper.class,
        EgovConfigProperties.class,
        EgovConfigTransaction.class,
        EgovConfigValidation.class,
        SsoIntegratedConfig.class, // 추가
        AccessLogFilter.class,     // 추가
        TilesConfig.class,         // 추가
        P6SpyConfig.class          // 추가
})
public class EgovConfigWeb implements WebMvcConfigurer, ApplicationContextAware {

	private ApplicationContext applicationContext;

	public void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Bean
	public InternalResourceViewResolver jspViewResolver() {
	    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
	    
	    // JSP 표준 태그 라이브러리(JSTL)를 사용할 수 있도록 뷰 클래스 지정
	    viewResolver.setViewClass(JstlView.class);
	    
	    // 실제 경로를 WEB-INF 밑으로 지정
	    viewResolver.setPrefix("/WEB-INF/jsp/"); // 상황에 맞게 /WEB-INF/views/ 등으로 수정 가능
	    
	    // 확장자를 .jsp로 지정
	    viewResolver.setSuffix(".jsp");
	    
	    return viewResolver;
	}


	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
	}

	@Bean
	public SessionLocaleResolver localeResolver() {
		return new SessionLocaleResolver();
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
		interceptor.setParamName("language");
		return interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
		Properties prop = new Properties();
		prop.setProperty("org.springframework.dao.DataAccessException", "egovSampleError");
		prop.setProperty("org.springframework.transaction.TransactionException", "egovSampleError");
		prop.setProperty("org.egovframe.rte.fdl.cmmn.exception.EgovBizException", "egovSampleError");
		prop.setProperty("org.springframework.security.AccessDeniedException", "egovSampleError");
		prop.setProperty("java.lang.Throwable", "egovSampleError");

		Properties statusCode = new Properties();
		statusCode.setProperty("egovSampleError", "400");
		statusCode.setProperty("egovSampleError", "500");

		SimpleMappingExceptionResolver smer = new SimpleMappingExceptionResolver();
		smer.setDefaultErrorView("egovSampleError");
		smer.setExceptionMappings(prop);
		smer.setStatusCodes(statusCode);
		resolvers.add(smer);
	}

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(1024 * 1024 * 100); // 100MB
        resolver.setDefaultEncoding("UTF-8");
        return resolver;
    }
}
