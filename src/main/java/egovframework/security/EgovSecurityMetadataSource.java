package egovframework.security;

import egovframework.security.service.impl.RoleResourceMapper;
import egovframework.security.service.impl.RoleResourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class EgovSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    @Autowired
    private RoleResourceMapper roleResourceMapper;

    private volatile Map<AntPathRequestMatcher, Collection<ConfigAttribute>> resourceMap = new LinkedHashMap<>();

    @PostConstruct
    public void loadResourceMap() {
        Map<AntPathRequestMatcher, Collection<ConfigAttribute>> newMap = new LinkedHashMap<>();
        /*
        List<RoleResourceVO> list = roleResourceMapper.selectAll();
        list.forEach(vo -> {
            AntPathRequestMatcher matcher = new AntPathRequestMatcher(vo.getResourceUrl());
            newMap.put(matcher, Collections.singletonList(new SecurityConfig(vo.getRoleCd())));
        });
         */
        
        //임시로 롤권한 부여
        newMap.put(new AntPathRequestMatcher("/admin/**"),
                Collections.singletonList(new SecurityConfig("ROLE_ADMIN")));
        newMap.put(new AntPathRequestMatcher("/doc/**"),
                Collections.singletonList(new SecurityConfig("ROLE_USER")));
        this.resourceMap = newMap;
    }

    public void reload() {
        loadResourceMap();
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) {
        HttpServletRequest request = ((FilterInvocation) object).getRequest();
        for (Map.Entry<AntPathRequestMatcher, Collection<ConfigAttribute>> entry : resourceMap.entrySet()) {
            if (entry.getKey().matches(request)) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> all = new HashSet<>();
        resourceMap.values().forEach(all::addAll);
        return all;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}