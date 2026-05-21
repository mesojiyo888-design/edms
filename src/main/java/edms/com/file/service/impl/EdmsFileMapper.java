package edms.com.file.service.impl;

import edms.com.file.service.EdmsFileVo;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface EdmsFileMapper {

    // 특정 파일 ID 내에서 다음으로 생성될 순번(MAX+1) 조회
    int selectNextSeq(String fileId) throws Exception;

    // 신규 파일 단건 저장
    int insertFile(EdmsFileVo vo) throws Exception;

    // 파일 ID에 속한 전체 파일 목록 조회
    List<EdmsFileVo> selectFileListByFileId(String fileId) throws Exception;

    // 단건 조회 (다운로드 및 물리파일 추적용 - 파라미터: fileId, fileSeq)
    EdmsFileVo selectFileDetail(Map<String, Object> param) throws Exception;

    // 특정 순번 파일 삭제 (파라미터: fileId, fileSeq)
    int deleteFileBySeq(Map<String, Object> param) throws Exception;
}