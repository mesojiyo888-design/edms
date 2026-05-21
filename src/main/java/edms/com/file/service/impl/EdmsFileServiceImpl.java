package edms.com.file.service.impl;

import edms.com.file.service.EdmsFileService;
import edms.com.file.service.EdmsFileVo;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("edmsFileService")
public class EdmsFileServiceImpl implements EdmsFileService {

    @Autowired
    private EdmsFileMapper edmsFileMapper;

    private final String uploadDir = System.getProperty("user.home") + File.separator + "edms_uploads";

    private static final Map<String, String> MIME_EXTENSION_MAP = new HashMap<>();

    static {
        MIME_EXTENSION_MAP.put("application/pdf", "pdf");
        MIME_EXTENSION_MAP.put("image/png", "png");
        MIME_EXTENSION_MAP.put("image/jpeg", "jpg");
        MIME_EXTENSION_MAP.put("image/gif", "gif");
        MIME_EXTENSION_MAP.put("text/plain", "txt");
        MIME_EXTENSION_MAP.put("application/zip", "zip");
        MIME_EXTENSION_MAP.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        MIME_EXTENSION_MAP.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<EdmsFileVo> uploadFiles(String fileId, List<MultipartFile> files) throws Exception {
        List<EdmsFileVo> resultList = new ArrayList<>();
        if (files == null || files.isEmpty()) return resultList;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        int nextSeq = edmsFileMapper.selectNextSeq(fileId);

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String orgFileName = file.getOriginalFilename();
            String realExt = getRealFileExtension(file);

            if ("unknown".equals(realExt)) {
                if (orgFileName.contains(".")) {
                    realExt = orgFileName.substring(orgFileName.lastIndexOf(".")).replace(".", "").toLowerCase();
                } else {
                    realExt = "bin";
                }
            }

            String saveFileName = fileId + "_" + nextSeq + "." + realExt;
            File targetFile = new File(uploadDir + File.separator + saveFileName);

            // 파일 복사 후 스트림을 즉시 100% 닫아주므로 톰캣이 임시 tmp 파일을 바로 삭제할 수 있도록 함
            try (InputStream is = file.getInputStream();
                 OutputStream os = Files.newOutputStream(targetFile.toPath())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            EdmsFileVo fileVo = new EdmsFileVo(
                    fileId, nextSeq, orgFileName, saveFileName, uploadDir, file.getSize(), realExt
            );

            edmsFileMapper.insertFile(fileVo);
            resultList.add(fileVo);
            nextSeq++;
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiles(String fileId, List<Integer> deleteSeqs) throws Exception {
        if (deleteSeqs == null || deleteSeqs.isEmpty()) return;

        for (int seq : deleteSeqs) {
            Map<String, Object> param = new HashMap<>();
            param.put("fileId", fileId);
            param.put("fileSeq", seq);

            EdmsFileVo fileVo = edmsFileMapper.selectFileDetail(param);
            if (fileVo != null) {
                File file = new File(fileVo.getFilePath() + File.separator + fileVo.getSaveFileName());
                if (file.exists()) {
                    file.delete();
                }
                edmsFileMapper.deleteFileBySeq(param);
            }
        }
    }

    @Override
    public List<EdmsFileVo> getFileListByFileId(String fileId) throws Exception {
        return edmsFileMapper.selectFileListByFileId(fileId);
    }

    @Override
    public EdmsFileVo getFileDetail(String fileId, int fileSeq) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("fileId", fileId);
        param.put("fileSeq", fileSeq);
        return edmsFileMapper.selectFileDetail(param);
    }

    private String getRealFileExtension(MultipartFile file) {
        String mimeType = "application/octet-stream";
        //Tika가 사용하는 파일 InputStream도 완벽히 열고 자동 close
        try (InputStream is = file.getInputStream()) {
            Tika tika = new Tika();
            mimeType = tika.detect(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return MIME_EXTENSION_MAP.getOrDefault(mimeType, "unknown");
    }
}
