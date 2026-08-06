package edms.common.service;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

public interface CommonJobConfigService {
    public List<EgovMap> getJobConfig();
    public List<EgovMap> updateJobConfig();
    public void clearAllJobConfigCache();
}
