<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>eGovFrame 4.3 AI Portal</title>
    <style>
        body { margin: 0; padding: 0; font-family: 'Malgun Gothic', sans-serif; display: flex; flex-direction: column; min-height: 100vh; }
        header { background-color: #1f2937; color: white; padding: 15px 30px; }
        footer { background-color: #f3f4f6; padding: 15px; text-align: center; margin-top: auto; border-top: 1px solid #e5e7eb; }
        main { flex-grow: 1; padding: 30px; box-sizing: border-box; }
    </style>
</head>
<body>

<!-- 헤더 삽입 영역 -->
<header>
    <tiles:insertAttribute name="header" />
</header>

<!-- 동적 본문(Body) 삽입 영역 -->
<main>
    <tiles:insertAttribute name="body" />
</main>

<!-- 푸터 삽입 영역 -->
<footer>
    <tiles:insertAttribute name="footer" />
</footer>

</body>
</html>
