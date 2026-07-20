package egovframework.security.service.impl;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RolePermissionMapper {

    List<Map<String, String>> selectPermissionListByUserId(String userId);
}