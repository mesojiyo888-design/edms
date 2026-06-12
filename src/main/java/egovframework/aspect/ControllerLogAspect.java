package egovframework.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class ControllerLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLogAspect.class);

    @Pointcut("execution(* egovframework..*Controller.*(..)) || execution(*  edms..*Controller.*(..))")
    public void controllerPoints() {}

    @Before("controllerPoints()")
    public void doBefore(JoinPoint joinPoint) {
        log.info("#####################################ControllerLogAspect start ##############################################");
        // 1. 컨트롤러 클래스명, 메서드명 추출
        String controllerName = joinPoint.getTarget().getClass().getSimpleName();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        // 💡 [핵심] 2. 메서드로 넘어간 파라미터 이름과 값 싹 다 긁어모으기
        String[] parameterNames = signature.getParameterNames(); // 파라미터 변수명들
        Object[] parameterValues = joinPoint.getArgs();          // 파라미터 실제 값들

        StringBuilder paramsBuilder = new StringBuilder();
        if (parameterNames != null && parameterValues != null) {
            for (int i = 0; i < parameterValues.length; i++) {
                // HttpServletRequest, HttpServletResponse 같은 서블릿 내장 객체는 로깅에서 제외 (에러 방지)
                if (parameterValues[i] instanceof javax.servlet.ServletRequest ||
                        parameterValues[i] instanceof javax.servlet.ServletResponse ||
                        parameterValues[i] instanceof org.springframework.ui.Model) {
                    continue;
                }

                if (paramsBuilder.length() > 0) paramsBuilder.append(", ");

                String paramName = (parameterNames.length > i) ? parameterNames[i] : "param" + i;
                paramsBuilder.append(paramName).append("=").append(parameterValues[i]);
            }
        }
        String finalParams = paramsBuilder.length() > 0 ? paramsBuilder.toString() : "없음";

        // 3. 현재 요청 정보 추출 (URI, HTTP Method)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String requestURI = request.getRequestURI();
            String httpMethod = request.getMethod();

            // 4. 로그인 사용자 정보 추출
            String userId = "ANONYMOUS";
            if (SecurityContextHolder.getContext().getAuthentication() != null
                    && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                userId = SecurityContextHolder.getContext().getAuthentication().getName();
            }

            // 5. 통합 로그 출력
            log.info("[AOP LOG] 유저: {} | 호출: {}.{}() | 데이터: [{}] | HTTP: {} | 주소: {}",
                    userId, controllerName, methodName, finalParams, httpMethod, requestURI);
            log.info("#####################################ControllerLogAspect end ##############################################");
            /*
             * 여기에 오차 없이 완전히 바인딩된 finalParams 데이터를 DB에 INSERT 하시면 됩니다.
             */
        }
    }
}