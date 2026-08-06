<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>

    <style>
        #gridList {
            width: 100%;
            height: 500px; /* 명시적인 높이값 필수 */
        }

        .grid-pagination ul.pagination {
            list-style: none;
            margin: 20px 0;
            padding: 0;
            display: flex;
            justify-content: center;
            gap: 4px;
        }

        .grid-pagination ul.pagination li a {
            display: inline-block;
            padding: 6px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            color: #333;
            text-decoration: none;
            background: #fff;
        }

        .grid-pagination ul.pagination li a:hover {
            background: #f0f0f0;
        }

        .grid-pagination ul.pagination li.active a {
            background: #3f51b5;
            color: #fff;
            border-color: #3f51b5;
        }

        .grid-pagination ul.pagination li.disabled a {
            color: #ccc;
            cursor: not-allowed;
            border-color: #eee;
        }
    </style>

</head>

<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">
<form name="form1" id="form1" action="${pageContext.request.contextPath}/board/insert" method="POST" onsubmit="return false;">

    <button id="btn-alert" onsubmit="return false;">alert</button>
    <button id="btn-confirm" onsubmit="return false;">confirm</button>
    <button id="btn-success" onsubmit="return false;">success</button>
    <button id="btn-error" onsubmit="return false;">error</button>
    <button id="btn-toast" onsubmit="return false;">toast</button>


    권한: <sec:authentication property="principal.authorities" />
    결재 권한 여부: <sec:authentication property="principal.approvalYn" />
    발송 권한 여부: <sec:authentication property="principal.sendYn" />
    문서 권한 여부: <sec:authentication property="principal.docYn" />
    조회 권한 여부: <sec:authentication property="principal.selectYn" />

    authList:<sec:authentication property="principal.authList" />

    <%-- 전체 병합 권한 체크 (역할 무관) --%>
    <c:if test="${loginUser.sendYn}">
        <button onclick="sendDoc()">발신</button>
    </c:if>

    <%-- 특정 role 권한 체크 --%>
    <c:if test="${loginUser.hasAuthList('A', 'PERM_SEND')}">
        <button onclick="sendAsA()">A역할로 발신</button>
    </c:if>

    <%-- Spring Security 표준 hasAuthority --%>
    <sec:authorize access="hasAuthority('PERM_SEND')">
        <button>발신 (통합 권한 체크)</button>
    </sec:authorize>
</form>
<script>

    $(document).ready(function() {
        // 버튼 동작들
        $('#btn-alert').click(function() {
            event.preventDefault();
            ComMsg.alert('저장되었습니다.');
        });
        $('#btn-confirm').click(function() {
            event.preventDefault();
            ComMsg.confirm('삭제하시겠습니까?', '삭제');
        });
        $('#btn-success').click(function() {
            event.preventDefault();
            ComMsg.success('승인 완료.');
        });
        $('#btn-error').click(function() {
            event.preventDefault();
            ComMsg.error('오류 발생.');
        });
        $('#btn-toast').click(function() {
            event.preventDefault();
            ComMsg.toast('저장되었습니다.');
        })
    });
</script>
</body>
</html>
