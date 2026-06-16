package edms.com.toastEditor.service;

import org.springframework.web.multipart.MultipartFile;

public interface EdmsToastEditorService {
    void insertBoard(EdmsToastEditorBoardVO boardVO) throws Exception;

    Long insertImage(MultipartFile image) throws Exception;

    EdmsToastEditorImageVO selectImage(Long imageId) throws Exception;
}
