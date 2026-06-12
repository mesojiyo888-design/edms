package edms.com.toast.web;

import edms.com.file.service.EdmsFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class EdmsToastEditorController {

    @GetMapping("/test/toastEditor")
    public String toastEditorPage() {
        return "test/testToastEditer";
    }
}