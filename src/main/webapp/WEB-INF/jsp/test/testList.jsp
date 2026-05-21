<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<script src="${pageContext.request.contextPath}/js/egovframework/jquery-3.6.1.min.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/handlebars/handlebars-4.7.7.js"></script>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>

</head>

<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">
<form action="${pageContext.request.contextPath}/board/insert" method="POST">

    <table border="1">
        <c:forEach var="resultList" items="${resultList}" varStatus="resultListStatus">
            <tr>
                <td align="center">
                    <c:out value="${resultList.id}"/>
                </td>
                <td align="left">
                    <c:out value="${resultList.name}"/>
                </td>
                <td align="left">
                    <c:out value="${resultList.description}"/>
                </td>
                <td align="center">
                    <c:out value="${resultList.regUser}"/>
                </td>
                <td align="center">
                    <c:out value="${resultList.useYn}"/>
                </td>
            </tr>
        </c:forEach>
    </table>


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
        var csrfToken = $("meta[name='_csrf']").attr("content");

        // 💡 시스템 내부의 모든 POST 폼 태그에 CSRF 토큰 히든 필드 자동 생성 및 삽입!
        if(csrfToken) {
            $('form[method="POST"], form[method="post"]').each(function() {
                // 이미 토큰이 담겨있는지 확인 후 없으면 주입
                if ($(this).find('input[name="_csrf"]').length === 0) {
                    $(this).append('<input type="hidden" name="_csrf" value="' + csrfToken + '" />');
                }
            });
        }
    });

    $(document).ready(function() {
        var data = [{name: '홍길동', age: 20}, {name: '이순신', age: 30}]

        //핸들바 뼈대 가져오기
        var source = $("#user-template").html();

        //핸들바 컴파일 및 데이터 주입
        var template = Handlebars.compile(source);
        var resultHtml = template(data);

        //화면에 꽂아넣기
        $("#userList").html(resultHtml);

    });
</script>
</body>
</html>
