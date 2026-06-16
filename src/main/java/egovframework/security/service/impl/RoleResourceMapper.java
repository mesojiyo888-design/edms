package egovframework.security.service.impl;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleResourceMapper {
    List<RoleResourceVO> selectAll();
}