package edms.com.file.web;

import edms.com.file.service.EdmsFileService;
import edms.com.file.service.EdmsFileVo;
import egovframework.exception.ApiResponse;
import egovframework.exception.EdmsException;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class EdmsFileController {

    @Autowired
    private EdmsFileService edmsFileService;

    @GetMapping("/file-page")
    public String filePage() {
        return "test/sampleFileUpload";
    }

    /**
     * 1. 파일 ID 값이 넘어왔을 때만 기존 파일 목록을 조회해서 반환
     */
    @GetMapping("/file/list")
    @ResponseBody
    public List<EdmsFileVo> getFileList(@RequestParam(value = "fileId", required = false) String fileId) {
        List<EdmsFileVo> list = new ArrayList<>();
        if (fileId != null && !fileId.trim().isEmpty() && !"null".equals(fileId)) {
            try {
                list = edmsFileService.getFileListByFileId(fileId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 2. 수정/저장 통합 처리 프로세스 (차분 관리)
     * - 기존 파일 유지 + 삭제된 순번 제거 + 신규 파일만 순번 누적 추가
     */
    @PostMapping("/file/save-process")
    @ResponseBody
    public ApiResponse<?> saveProcess(MultipartHttpServletRequest request) {
        try {

            System.out.println("이미지 파일 ID: " + request.getParameter("imgFileId"));
            System.out.println("이미지 파일 개수: " + request.getFiles("imgFile").size());

            System.out.println("서류 파일 ID: " + request.getParameter("docFileId"));
            System.out.println("서류 파일 개수: " + request.getFiles("docFile").size());
            // ==========================================

            // 1. 이미지 첨부파일 파트 처리
            // ==========================================
            String imgFileId = request.getParameter("imgFileId"); // 'imgFile' + 'Id'
            String[] imgDelSeqs = request.getParameterValues("imgFileDeleteSeqs");

            if (imgDelSeqs != null && imgDelSeqs.length > 0) {
                List<Integer> delList = new ArrayList<>();
                for (String s : imgDelSeqs) delList.add(Integer.parseInt(s));
                edmsFileService.deleteFiles(imgFileId, delList); // 이미지 DB/물리 삭제
            }

            List<MultipartFile> imgFile = request.getFiles("imgFile"); // 이미지 파일 스트림 배열
            if (imgFile != null && !imgFile.isEmpty()) {
                edmsFileService.uploadFiles(imgFileId, imgFile); // 이미지 신규 저장
            }

            // ==========================================
            // 2. 서류 첨부파일 파트 처리
            // ==========================================
            String docFileId = request.getParameter("docFileId"); // 'docFile' + 'Id'
            String[] docDelSeqs = request.getParameterValues("docFileDeleteSeqs");

            if (docDelSeqs != null && docDelSeqs.length > 0) {
                List<Integer> delList = new ArrayList<>();
                for (String s : docDelSeqs) delList.add(Integer.parseInt(s));
                edmsFileService.deleteFiles(docFileId, delList); // 서류 DB/물리 삭제
            }

            List<MultipartFile> docFile = request.getFiles("docFile"); // 서류 파일 스트림 배열
            if (docFile != null && !docFile.isEmpty()) {
                edmsFileService.uploadFiles(docFileId, docFile); // 서류 신규 저장
            }

            // 3. 최종 업무 테이블에 매핑ID 최종 저장 업데이트
            //boardService.updateBoardFileIds(title, imgFileId, docFileId);
            System.out.println("디비 매핑 완료 -> 이미지ID: " + imgFileId + " | 서류ID: " + docFileId);


            return ApiResponse.success("다운로드 시작");
        } catch (Exception e) {
            throw new EdmsException("파일 저장 중 오류가 발생했습니다.", "ERR_500_FILE_SAVE", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * 3. 복합키(fileId, fileSeq) 기반 다운로드 처리
     */
    @GetMapping("/file/download/{fileId}/{fileSeq}")
    public void download(@PathVariable("fileId") String fileId, @PathVariable("fileSeq") int fileSeq, HttpServletResponse response) {
        try {
            EdmsFileVo fileVo = edmsFileService.getFileDetail(fileId, fileSeq);
            if (fileVo == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            File file = new File(fileVo.getFilePath() + File.separator + fileVo.getSaveFileName());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            try (InputStream is = new FileInputStream(file)) {
                String encodedName = URLEncoder.encode(fileVo.getOrgFileName(), "UTF-8").replaceAll("\\+", "%20");
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedName + "\"");
                response.setContentLengthLong(file.length());
                StreamUtils.copy(is, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}