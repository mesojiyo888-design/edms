<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
<style>

    .tui-datepicker {
        z-index: 9999 !important;
    }

    .datepicker-wrapper {
        position: relative;
    }
</style>
</head>

<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">
<form action="${pageContext.request.contextPath}/board/insert" method="POST">

    <table border="1">
        <c:forEach var="dataList" items="${dataList}" varStatus="resultListStatus">
            <tr>
                <td align="left">
                    <c:out value="${dataList.name}"/>
                </td>
                <td align="left">
                    <c:out value="${dataList.email}"/>
                </td>
                <td>
                    <input type="text" name="regDate" id="regDate_${resultListStatus.index}" value="<c:out value="${dataList.regDate}"/>" class="datepicker">
                </td>
                <td align="center">
                    <c:out value="${dataList.useYn}"/>
                </td>
            </tr>
        </c:forEach>
    </table>

    <div id="paging-area"></div>

    <!-- 데이터가 뿌려질 타겟 요소 -->
    <ul id="userList"></ul>

    <!-- 2. 핸들바 템플릿 정의 -->
    <script id="user-template" type="text/x-handlebars-template">
        {{#each this}}
        <li>이름: {{name}}, 나이: {{age}}</li>
        {{/each}}
    </script>
</form>
<script>

    $(document).ready(function() {
        //jQuery UI Datepicker 방식
        //$(".datepicker").setDatepicker();

        //Toast UI Datepicker 방식
        ToastDatepicker.initAll();

        var data = [{name: '홍길동', age: 20}, {name: '이순신', age: 30}]

        //핸들바 뼈대 가져오기
        var source = $("#user-template").html();

        //핸들바 컴파일 및 데이터 주입
        var template = Handlebars.compile(source);
        var resultHtml = template(data);

        //화면에 꽂아넣기
        $("#userList").html(resultHtml);

        // 사용법
        var pager = new PaginationManager('paging-area', 50, 10, 'page');

        // 데이터 처리 콜백 설정
        pager.onPageChange = function(page) {
            console.log("현재 페이지 데이터 로드: " + page);
        };

        pager.render();

        // 나중에 필요할 때 모드 전환
        // pager.updateConfig('loadMore', 10);

    });
</script>
</body>
</html>
