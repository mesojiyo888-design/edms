<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<meta name="_csrf" content="${_csrf.token}"/>
<meta name="_csrf_header" content="${_csrf.headerName}"/>

<script src="${pageContext.request.contextPath}/js/egovframework/jquery-3.6.1.min.js"></script>

<script>
    var _CONTEXT_PATH = "${pageContext.request.contextPath}" || "";

    $(function () {

            // window 객체에 바인딩하여 전역 변수(Global Variable)화 시킵니다.
            window.csrfToken = $("meta[name='_csrf']").attr("content");
            window.csrfHeaderName = $("meta[name='_csrf_header']").attr("content");
            window.csrfParameterName = "_csrf"; // 스프링 시큐리티 기본 파라미터명

            // 기존 모든 AJAX 통신 처리 로직 유지
            $(document).ajaxSend(function(e, xhr, options) {
                if (window.csrfToken && window.csrfHeaderName) {
                    xhr.setRequestHeader(window.csrfHeaderName, window.csrfToken);
                }
            });
        });
</script>


<div style="display: flex; justify-content: space-between; align-items: center;">
    <h1 style="margin: 0; font-size: 20px;">Portal</h1>
    <span>접속자: 관리자(Admin)</span>
</div>
