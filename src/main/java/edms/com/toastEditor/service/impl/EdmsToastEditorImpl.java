package edms.com.toastEditor.service.impl;

import edms.com.toastEditor.service.EdmsToastEditorBoardVO;
import edms.com.toastEditor.service.EdmsToastEditorImageVO;
import edms.com.toastEditor.service.EdmsToastEditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Service("EdmsToastEditorService")
public class EdmsToastEditorImpl implements EdmsToastEditorService {

    @Autowired
    private EdmsToastEditorMapper edmsToastEditorMapper;

    @Override
    public void insertBoard(EdmsToastEditorBoardVO boardVO) throws Exception {
        System.out.println("test boardVO : " + boardVO);
        edmsToastEditorMapper.insertBoard(boardVO);
    }

    @Override
    public Long insertImage(MultipartFile image) throws Exception {
        EdmsToastEditorImageVO imageVO = new EdmsToastEditorImageVO();

        imageVO.setFileName(image.getOriginalFilename());
        imageVO.setContentType(image.getContentType());
        imageVO.setFileSize(image.getSize());
        imageVO.setFileData(image.getBytes());

        edmsToastEditorMapper.insertImage(imageVO);

        return imageVO.getImageId();
    }

    @Override
    public EdmsToastEditorImageVO selectImage(Long imageId) throws Exception {
        return edmsToastEditorMapper.selectImage(imageId);
    }
}