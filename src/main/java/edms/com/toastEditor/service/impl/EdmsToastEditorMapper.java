package edms.com.toastEditor.service.impl;

import edms.com.toastEditor.service.EdmsToastEditorBoardVO;
import edms.com.toastEditor.service.EdmsToastEditorImageVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EdmsToastEditorMapper {
    void insertBoard(EdmsToastEditorBoardVO boardVO) throws Exception;

    void insertImage(EdmsToastEditorImageVO imageVO) throws Exception;

    EdmsToastEditorImageVO selectImage(Long imageId) throws Exception;
}