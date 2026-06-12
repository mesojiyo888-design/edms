package edms.com.toast.web;

import edms.com.file.service.EdmsFileService;
import edms.com.file.service.EdmsFileVo;
import egovframework.exception.ApiResponse;
import egovframework.exception.EdmsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class EdmsToastEditorController {

    @Autowired
    private EdmsFileService edmsFileService;

    @GetMapping("/test/toastEditor")
    public String filePage() {
        return "test/testToastEditer";
    }
}