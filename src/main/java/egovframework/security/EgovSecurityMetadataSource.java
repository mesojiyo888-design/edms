package egovframework.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import egovframework.security.service.ResourceRoleCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class EgovSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    @Autowired
    private ResourceRoleCacheService resourceRoleCacheService;

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object){
        HttpServletRequest request = ((FilterInvocation) object).getRequest();

        Map<AntPathRequestMatcher, Collection<ConfigAttribute>> resourceMap = resourceRoleCacheService.getResourceMap();

        for(Map.Entry<AntPathRequestMatcher, Collection<ConfigAttribute>> entry : resourceMap.entrySet()){
            if(entry.getKey().matches(request)){
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes(){
        Set<ConfigAttribute> all = new HashSet<ConfigAttribute>();
        resourceRoleCacheService.getResourceMap().values().forEach(all::addAll);
        return all;
    }

    @Override
    public boolean supports(Class<?> clazz){
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    // 관리자 화면에서 수동 리로드 시 호출 (이 서버의 캐시만 즉시 비움)
    public void reload(){
        resourceRoleCacheService.evictResourceMapCache();
    }
}