package egovframework.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class LoginUserInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception{

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof EgovUserDetails){
            EgovUserDetails userDetails = (EgovUserDetails) authentication.getPrincipal();
            request.setAttribute("loginUserId", userDetails.getUsername());
            request.setAttribute("loginUser", userDetails);
        }

        return true;
    }
}