package edms.com.toastTree.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EdmsToastTreeController {

    //@Resource(name = "EdmsToastTreeService")
    //private EdmsToastTreeService edmsToastTreeService;

    @GetMapping("/test/testToastTree")
    public String toastTreePage() {
        return "test/testToastTree";
    }
}
