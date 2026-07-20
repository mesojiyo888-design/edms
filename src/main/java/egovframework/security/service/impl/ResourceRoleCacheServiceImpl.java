package egovframework.security.service.impl;

import egovframework.security.service.ResourceRoleCacheService;
import egovframework.security.service.impl.RoleResourceMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Service;

@Service("resourceRoleCacheService")
public class ResourceRoleCacheServiceImpl implements ResourceRoleCacheService {


    @Autowired
    private RoleResourceMapper roleResourceMapper;

    @Cacheable(value = "resourceRoleCache", key = "'resourceMap'", cacheManager = "cacheManager")
    public Map<AntPathRequestMatcher, Collection<ConfigAttribute>> getResourceMap(){
        Map<AntPathRequestMatcher, Collection<ConfigAttribute>> newMap = new LinkedHashMap<AntPathRequestMatcher, Collection<ConfigAttribute>>();

        // 임시로 롤권한 부여
        newMap.put(new AntPathRequestMatcher("/admin/**"),
                Collections.singletonList(new SecurityConfig("ROLE_ADMIN")));
        newMap.put(new AntPathRequestMatcher("/doc/**"),
                Collections.singletonList(new SecurityConfig("ROLE_USER")));

        newMap.put(new AntPathRequestMatcher("/doc/send/**"),
                Collections.singletonList(new SecurityConfig("PERM_SEND")));
        newMap.put(new AntPathRequestMatcher("/doc/approval/**"),
                Collections.singletonList(new SecurityConfig("PERM_APPROVAL")));

        return newMap;
    }

    @CacheEvict(value = "resourceRoleCache", key = "'resourceMap'", cacheManager = "cacheManager")
    @Scheduled(fixedDelay = 30000)
    public void evictResourceMapCache(){
        // 비우는 동작만 수행, 내용 없음
    }
}