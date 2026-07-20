<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<meta name="_csrf" content="${_csrf.token}"/>
<meta name="_csrf_header" content="${_csrf.headerName}"/>

<script>
    var _CONTEXT_PATH = "${pageContext.request.contextPath}" || "";

    window.csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    window.csrfHeaderName = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
    window.csrfParameterName = "_csrf";
</script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/jquery/jquery-ui-1.14.2/jquery-ui.css">

<script src="${pageContext.request.contextPath}/js/lib/jquery/jquery.min-3.6.1.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/jquery/jquery-ui-1.14.2/jquery-ui.min.js"></script>
<script src="${pageContext.request.contextPath}/js/common/datepicker-utils.js"></script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/toast/toast-date.picker/tui-date-picker.min-4.3.3.css" />
<script src="${pageContext.request.contextPath}/js/lib/toast/toast-date.picker/tui-date-picker.min-4.3.3.js"></script>

<script src="${pageContext.request.contextPath}/js/common/toastDatepicker.js"></script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/toast/toast-ui.grid/tui-grid.min-4.21.22.css" />
<script src="${pageContext.request.contextPath}/js/lib/toast/toast-ui.grid/tui-grid.min-4.21.22.js"></script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/sweetalert/sweetalert2.min.css">
<script src="${pageContext.request.contextPath}/js/lib/sweetalert/sweetalert2.min.js"></script>

<script src="${pageContext.request.contextPath}/js/common/fileUpload.js"></script>
<script src="${pageContext.request.contextPath}/js/common/pagination.js"></script>
<script src="${pageContext.request.contextPath}/js/common/toastGrid.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/handlebars/handlebars-4.7.7.js"></script>
<script src="${pageContext.request.contextPath}/js/common/commonMsg.js"></script>
<script src="${pageContext.request.contextPath}/js/common/commonAjax.js"></script>

<script>
    (function ($) {

        /* ========================
         * jQuery $.ajax CSRF 토큰 자동 헤더 추가
         * ======================== */
        $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
            if (window.csrfToken && window.csrfHeaderName) {
                options.headers = options.headers || {};
                options.headers[window.csrfHeaderName] = window.csrfToken;
            }
        });

        /* ========================
         * XMLHttpRequest CSRF
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
         * fetch CSRF
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

    <sec:authorize access="isAuthenticated()">
        <span style="margin-left: 20px; color: #007bff;">접속자 [<sec:authentication property="principal.username" />]</span>

    </sec:authorize>
    <sec:authorize access="isAuthenticated()">
        <button onclick="location.href='/logout'">Logout</button>
    </sec:authorize>

</div>
