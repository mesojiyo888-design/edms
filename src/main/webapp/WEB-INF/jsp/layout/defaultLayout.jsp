<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>eGovFrame 4.3 AI Portal</title>
    <link rel="stylesheet" href="/css/comTab.css">
    <style>
        body { margin: 0; padding: 0; font-family: 'Malgun Gothic', sans-serif; display: flex; flex-direction: column; min-height: 100vh; }
        header { background-color: #1f2937; color: white; padding: 15px 30px; }
        footer { background-color: #f3f4f6; padding: 15px; text-align: center; margin-top: auto; border-top: 1px solid #e5e7eb; }
        main { flex-grow: 1; padding: 30px; box-sizing: border-box; }
    </style>
</head>
<body>

<!-- 헤더 삽입 영역 (GNB는 header.jsp 내부에 포함되어 있다고 가정) -->
<header>
    <tiles:insertAttribute name="header" />
</header>

<tiles:insertAttribute name="lnb" />

<div class="breadcrumb-area">
    <span class="page-title"></span>
    <ol class="breadcrumb"></ol>
</div>

<!-- 추가로 열리는 탭들이 표시되는 영역 -->
<ul class="tab-bar"></ul>
<div class="tab-content"></div>

<!-- 동적 본문(Body) 삽입 영역: 현재 요청된 페이지 자체 -->
<main>
    <tiles:insertAttribute name="body" />
</main>

<footer>
</footer>

<script id="menuData" type="application/json">${menuListJson}</script>

<script src="/js/lib/jquery-3.7.1.min.js"></script>
<script src="/js/common/ComMenu.js"></script>
<script src="/js/common/ComTab.js"></script>
<script src="/js/common/ComMsg.js"></script>
<script>
    $(function() {
        var menuData = JSON.parse($('#menuData').text());
        ComMenu.init('.gnb-list', '.lnb-list', '.breadcrumb', '.page-title', menuData);
        ComTab.init('.tab-bar', '.tab-content');
    });
</script>

</body>
</html>