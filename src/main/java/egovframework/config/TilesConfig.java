package egovframework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;

@Configuration
public class TilesConfig {

    /**
     * Tiles 설정 및 정의 파일 위치를 등록하는 환경설정 빈입니다.
     */
    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer configurer = new TilesConfigurer();
        // 타일즈 정의 XML 파일 위치 지정
        configurer.setDefinitions(new String[] { "/WEB-INF/tiles/tiles-defs.xml" });
        configurer.setCheckRefresh(true); // 개발 시 XML 수정 반영 여부
        return configurer;
    }

    /**
     * 컨트롤러가 리턴한 문자열을 바탕으로 Tiles 레이아웃을 완성해주는 뷰 리졸버입니다.
     */
    @Bean
    public UrlBasedViewResolver tilesViewResolver() {
        UrlBasedViewResolver resolver = new UrlBasedViewResolver();
        resolver.setViewClass(TilesView.class);
        // 기존 일반 JSP 뷰리졸버(InternalResourceViewResolver)보다 우선순위를 높여줍니다.
        resolver.setOrder(1);
        return resolver;
    }
}
