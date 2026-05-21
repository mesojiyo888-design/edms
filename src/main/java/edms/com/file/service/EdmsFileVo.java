package edms.com.file.service;

import java.io.Serializable;

public class EdmsFileVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileId;
    private int fileSeq; // [변경] 순번
    private String orgFileName;
    private String saveFileName;
    private String filePath;
    private long fileSize;
    private String fileExt;

    public EdmsFileVo() {}

    public EdmsFileVo(String fileId, int fileSeq, String orgFileName, String saveFileName, String filePath, long fileSize, String fileExt) {
        this.fileId = fileId;
        this.fileSeq = fileSeq;
        this.orgFileName = orgFileName;
        this.saveFileName = saveFileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileExt = fileExt;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public int getFileSeq() { return fileSeq; }
    public void setFileSeq(int fileSeq) { this.fileSeq = fileSeq; }
    public String getOrgFileName() { return orgFileName; }
    public void setOrgFileName(String orgFileName) { this.orgFileName = orgFileName; }
    public String getSaveFileName() { return saveFileName; }
    public void setSaveFileName(String saveFileName) { this.saveFileName = saveFileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getFileExt() { return fileExt; }
    public void setFileExt(String fileExt) { this.fileExt = fileExt; }
}