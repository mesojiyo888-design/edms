package egovframework.security.service.impl;

import lombok.Data;

@Data
public class RoleResourceVO {
    private String resourceId;
    private String resourceUrl;
    private String roleCd;
    private int sortOrder;
    // getter/setter
}