package egovframework.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class LoginUserInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof EgovUserDetails) {
            EgovUserDetails userDetails = (EgovUserDetails) authentication.getPrincipal();
            request.setAttribute("loginUserId", userDetails.getUsername());
            request.setAttribute("loginUser", userDetails);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView) throws Exception {

        if(modelAndView == null) {
            return;
        }

        String viewName = modelAndView.getViewName();
        boolean isTabMode = "Y".equals(request.getParameter("_tabMode"));
        boolean isAjax = fnIsAjaxRequest(request, handler);

        // 1) 탭 콘텐츠 레이아웃 뷰 경로 치환 (기존 로직)
        if(isTabMode && viewName != null
                && !viewName.startsWith("redirect:")
                && !viewName.startsWith("content/")) {
            modelAndView.setViewName("content/" + viewName);
        }

        // 2) 메뉴 데이터는 tabMode/ajax가 아닐 때만 모델에 태움
        if(isTabMode || isAjax) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof EgovUserDetails) {
            EgovUserDetails userDetails = (EgovUserDetails) authentication.getPrincipal();

            /**Todo: 메뉴 캐시 매니저를 주입받아야 함. 현재는 null로 가정하고 있음. 실제 코드에서는 @Autowired 또는 생성자 주입으로 menuCacheManager를 받아야 함.*/
        /*
        List<String> userRoleList = userDetails.getRoleList();
        List<MenuVO> filteredMenuList = menuCacheManager.getMenuList().stream()
                .filter(menu -> fnHasAccessRole(menu, userRoleList))
                .collect(Collectors.toList());

        String menuJson = objectMapper.writeValueAsString(filteredMenuList);
        modelAndView.addObject("menuData", menuJson);

        */
        }
    }

    private boolean fnIsAjaxRequest(HttpServletRequest request, Object handler) {
        if("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            return handlerMethod.hasMethodAnnotation(ResponseBody.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(RestController.class);
        }
        return false;
    }
}