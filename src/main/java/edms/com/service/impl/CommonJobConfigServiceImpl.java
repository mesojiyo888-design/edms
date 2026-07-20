package edms.com.service.impl;

import edms.com.service.CommonJobConfigService;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service(value = "commonJobConfigService")
public class CommonJobConfigServiceImpl implements CommonJobConfigService {

    private static final Logger log = LoggerFactory.getLogger(CommonJobConfigServiceImpl.class);

    /**
     * 1. 캐싱 처리된 jobConfig 공통코드 조회
     * 'jobConfig' 캐시에 'jobId'를 Key로 저장합니다.
     */
    @Cacheable(value = "jobConfig")
    @Override
    public List<EgovMap> getJobConfig() {
        log.debug("@@@ CommonJobConfigServiceImpl.getJobConfig : ");

        return Collections.emptyList();
    }

    /**
     * 2. 값이 변경되었을 때 캐시를 즉시 비우는 메서드 (수정/삭제 시 호출)
     * 해당 jobId의 캐시만 제거하여 다음 조회 시 DB에서 갱신되도록 합니다.
     */
    @CacheEvict(value = "jobConfig")
    @Override
    public List<EgovMap> updateJobConfig() {
        log.debug("@@@ CommonJobConfigServiceImpl.updateJobConfig : ");

        return Collections.emptyList();
    }

    /**
     * 3. 특정 주기에 따라 캐시를 전체를 비우는 메서드 (스케줄러용)
     * allEntries = true 설정으로 'jobConfig' 캐시 안의 모든 데이터를 비웁니다.
     */
    @CacheEvict(value = "jobConfig", allEntries = true)
    @Override
    public void clearAllJobConfigCache() {
        log.debug("@@@ CommonJobConfigServiceImpl.clearAllJobConfigCache : ");

    }
}
