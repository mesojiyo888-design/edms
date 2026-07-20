package egovframework.security.service;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.Map;

/**
 * @author snet
 * @version 1.0
 * @Class Name : ResourceRoleCacheService.java
 * @Description :
 * @Modification Information
 * <p>
 * 수정일        수정자           수정내용
 * -------    --------    ---------------------------
 * 26. 7. 14.      snet            최초 생성
 * @see Copyright (C) by snetsystems All right reserved.
 * @since 26. 7. 14.
 */
public interface ResourceRoleCacheService {
    public Map<AntPathRequestMatcher, Collection<ConfigAttribute>> getResourceMap();
    public void evictResourceMapCache();
}
