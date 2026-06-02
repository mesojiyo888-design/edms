package edms.com.service;
import lombok.Data;
import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "root") // 최상단 이름 지정
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MyXmlDataListVO {

    // @XmlElementWrapper는 <item>들을 감싸는 상위 태그를 제어함
    // 하지만 단순히 <item>만 반복하고 싶다면 아래와 같이 작성
    @XmlElement(name = "item")
    private List<MyXmlDataVO> items;
}

