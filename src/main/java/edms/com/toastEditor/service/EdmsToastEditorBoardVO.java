package edms.com.toastEditor.service;

import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

@Data
public class EdmsToastEditorBoardVO {
    private Long boardId;
    private String title;
    private String content;

    @Override
    public String toString() {
        return "EdmsToastEditorBoardVO{" +
                "boardId=" + boardId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
