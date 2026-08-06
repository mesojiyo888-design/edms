package egovframework.security.service.impl;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper {

    List<String> selectRoleIdsByUserId(String userId);
}