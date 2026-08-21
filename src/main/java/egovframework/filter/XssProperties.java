package egovframework.filter;

/**
 * @author snet
 * @version 1.0
 * @Class Name : XssProperties.java
 * @Description :
 * @Modification Information
 * <p>
 * 수정일        수정자           수정내용
 * -------    --------    ---------------------------
 * 26. 8. 11.      snet            최초 생성
 * @see Copyright (C) by snetsystems All right reserved.
 * @since 26. 8. 11.
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XssProperties
{
    @Value("#{'${xss.exclude-params:}'.split(',')}")
    private List<String> excludeParams;

    public List<String> getExcludeParams()
    {
        return excludeParams;
    }
}