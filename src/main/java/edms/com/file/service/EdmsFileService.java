package edms.com.file.service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface EdmsFileService {

    // 신규 추가된 파일들을 현재 시퀀스 이후로 순번을 매겨 업로드
    List<EdmsFileVo> uploadFiles(String fileId, List<MultipartFile> files) throws Exception;

    // 파일 ID 기반 목록 조회
    List<EdmsFileVo> getFileListByFileId(String fileId) throws Exception;

    // 다운로드용 단건 조회
    EdmsFileVo getFileDetail(String fileId, int fileSeq) throws Exception;

    // 선택 삭제된 순번(Seq) 리스트 일괄 가공 및 삭제
    void deleteFiles(String fileId, List<Integer> deleteSeqs) throws Exception;
}