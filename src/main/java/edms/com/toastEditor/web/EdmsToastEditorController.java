package edms.com.toastEditor.web;

import edms.com.toastEditor.service.EdmsToastEditorBoardVO;
import edms.com.toastEditor.service.EdmsToastEditorImageVO;
import edms.com.toastEditor.service.EdmsToastEditorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EdmsToastEditorController {

    @Resource(name = "EdmsToastEditorService")
    private EdmsToastEditorService edmsToastEditorService;

    @GetMapping("/test/toastEditor")
    public String toastEditorPage() {
        return "test/testToastEditer";
    }

    @RequestMapping(value = "/test/toastEditorInsertImages", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> insertWithImages(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "blobUrls", required = false) List<String> blobUrls,
            HttpServletRequest request
    ) throws Exception {

        /*
         * 1. 이미지가 있으면 DB BLOB에 저장
         * 2. 본문 안의 blob URL을 실제 이미지 조회 URL로 치환
         */
        if (images != null && blobUrls != null) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);

                if (image == null || image.isEmpty()) {
                    continue;
                }

                Long imageId = edmsToastEditorService.insertImage(image);

                String imageUrl = request.getContextPath() + "/test/editorImage/" + imageId;

                if (i < blobUrls.size()) {
                    content = content.replace(blobUrls.get(i), imageUrl);
                }
            }
        }

        /*
         * 3. 최종 게시글 저장
         */
        EdmsToastEditorBoardVO boardVO = new EdmsToastEditorBoardVO();
        boardVO.setTitle(title);
        boardVO.setContent(content);

        edmsToastEditorService.insertBoard(boardVO);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);
        result.put("boardId", boardVO.getBoardId());

        return result;
    }

    @RequestMapping(value = "/test/editorImage/{imageId}", method = RequestMethod.GET)
    public void viewImage(
            @PathVariable("imageId") Long imageId,
            HttpServletResponse response
    ) throws Exception {

        EdmsToastEditorImageVO imageVO = edmsToastEditorService.selectImage(imageId);

        if (imageVO == null || imageVO.getFileData() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(imageVO.getContentType());
        response.setContentLengthLong(imageVO.getFileSize());

        response.setHeader(
                "Content-Disposition",
                "inline; filename=\"" + imageVO.getFileName() + "\""
        );

        ServletOutputStream out = response.getOutputStream();
        out.write(imageVO.getFileData());
        out.flush();
    }
}