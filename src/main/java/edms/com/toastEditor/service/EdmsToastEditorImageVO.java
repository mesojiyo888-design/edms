package edms.com.toastEditor.service;

import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;

@Data
public class EdmsToastEditorImageVO {
    private Long imageId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private byte[] fileData;



    @Override
    public String toString() {
        return "EdmsToastEditorImageVO{" +
                "imageId=" + imageId +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileSize=" + fileSize +
                ", fileData=" + Arrays.toString(fileData) +
                '}';
    }
}
