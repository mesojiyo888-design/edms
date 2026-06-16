<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<meta name="_csrf" content="${_csrf.token}"/>
<meta name="_csrf_header" content="${_csrf.headerName}"/>

<script>
    var _CONTEXT_PATH = "${pageContext.request.contextPath}" || "";
</script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/jquery/jquery-ui-1.14.2/jquery-ui.css">

<script src="${pageContext.request.contextPath}/js/lib/jquery/jquery.min-3.6.1.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/jquery/jquery-ui-1.14.2/jquery-ui.min.js"></script>
<!--<script src="${pageContext.request.contextPath}/js/common/datepicker-utils.js"></script>-->

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/toast/toast-date.picker/tui-date-picker.min-4.3.3.css" />
<script src="${pageContext.request.contextPath}/js/lib/toast/toast-date.picker/tui-date-picker.min-4.3.3.js"></script>

<script src="${pageContext.request.contextPath}/js/common/toastDatepicker.js"></script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/toast/toast-ui.grid/tui-grid.min-4.21.22.css" />
<script src="${pageContext.request.contextPath}/js/lib/toast/toast-ui.grid/tui-grid.min-4.21.22.js"></script>


<!-- <link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/toast/toast-editor/toastui-editor.css" /> -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/toast/toast-editor/toastui-editor.min.css" />
<!-- <script src="${pageContext.request.contextPath}/js/lib/toast/toast-editor/toastui-editor-all.js"></script> -->
<script src="${pageContext.request.contextPath}/js/lib/toast/toast-editor/toastui-editor-all.min.js"></script>
<!--<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/toast/toast-editor/theme/toastui-editor-dark.css" /> -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/toast/toast-editor/theme/toastui-editor-dark.min.css" />


<!-- <link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/jquery/jstree/jstree.min-3.3.17.css" /> -->
<!-- <script src="${pageContext.request.contextPath}/js/lib/jquery/jstree/jstree.min-3.3.17.js" ></script> -->
<!-- <link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/jquery/jstree/tui-tree-4.0.9.css" /> -->
<!-- <script src="${pageContext.request.contextPath}/js/lib/jquery/jstree/tui-tree-4.0.9.js" ></script> -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/jquery/jstree/tui-tree.min-4.0.9.css" />
<script src="${pageContext.request.contextPath}/js/lib/jquery/jstree/tui-tree.min-4.0.9.js" ></script>

<script src="${pageContext.request.contextPath}/js/common/fileUpload.js"></script>
<script src="${pageContext.request.contextPath}/js/common/pagination.js"></script>
<script src="${pageContext.request.contextPath}/js/common/toastGrid.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/handlebars/handlebars-4.7.7.js"></script>
<script src="${pageContext.request.contextPath}/js/common/toastEditor.js"></script>
<script src="${pageContext.request.contextPath}/js/common/jsTree.js"></script>



<script>
    (function ($) {

        // window 객체에 바인딩하여 전역 변수(Global Variable)화 시킵니다.
        window.csrfToken = $("meta[name='_csrf']").attr("content");
        window.csrfHeaderName = $("meta[name='_csrf_header']").attr("content");
        window.csrfParameterName = "_csrf"; // 스프링 시큐리티 기본 파라미터명

        /* ========================
         * 1. jQuery $.ajax CSRF
         * ======================== */
        $(document).ajaxSend(function (e, xhr, options) {
            if (window.csrfToken && window.csrfHeaderName) {
                xhr.setRequestHeader(window.csrfHeaderName, window.csrfToken);
            }
        });

        /* ========================
         * 2. XMLHttpRequest CSRF
         * ======================== */
        var originalXhrOpen = XMLHttpRequest.prototype.open;
        var originalXhrSend = XMLHttpRequest.prototype.send;

        XMLHttpRequest.prototype.open = function (method) {
            this._xhrMethod = method;
            originalXhrOpen.apply(this, arguments);
        };

        XMLHttpRequest.prototype.send = function () {
            if (this._xhrMethod && this._xhrMethod.toUpperCase() !== 'GET') {
                if (window.csrfToken && window.csrfHeaderName) {
                    try {
                        this.setRequestHeader(window.csrfHeaderName, window.csrfToken);
                    } catch (e) {
                        // 이미 전송된 경우 무시
                    }
                }
            }
            originalXhrSend.apply(this, arguments);
        };

        /* ========================
         * 3. fetch CSRF
         * ======================== */
        var originalFetch = window.fetch;

        window.fetch = function (url, options) {
            options = options || {};

            var method = (options.method || 'GET').toUpperCase();
            if (method !== 'GET') {
                if (window.csrfToken && window.csrfHeaderName) {
                    options.headers = options.headers || {};
                    options.headers[window.csrfHeaderName] = window.csrfToken;
                }
            }

            return originalFetch.call(this, url, options);
        };
        /*
        axios.interceptors.request.use(function (config) {
            // 모든 요청 헤더에 CSRF 토큰을 자동으로 추가
            if (window.csrfToken && window.csrfHeaderName) {
                config.headers[window.csrfHeaderName] = window.csrfToken;
            }
            return config;
        }, function (error) {
            return Promise.reject(error);
        });
        */

    }(jQuery));
</script>


<div style="display: flex; justify-content: space-between; align-items: center;">
    <h1 style="margin: 0; font-size: 20px;">Portal</h1>

    <security:authorize access="isAuthenticated()">
        <span style="margin-left: 20px; color: #007bff;">접속자 [${principal}]</span>
    </security:authorize>
    <security:authorize access="isAuthenticated()">
        <button onclick="location.href='/logout'">Logout</button>
    </security:authorize>
</div>
