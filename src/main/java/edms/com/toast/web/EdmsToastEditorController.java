package edms.com.toast.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class EdmsToastEditorController {

    @GetMapping("/test/toastEditor")
    public String toastEditorPage() {
        return "test/testToastEditer";
    }
}