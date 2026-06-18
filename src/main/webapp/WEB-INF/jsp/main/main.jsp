<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>메인 시스템 페이지</title>
</head>
<body style="padding: 50px; font-family: sans-serif;">
<div style="border-left: 5px solid #28a745; padding-left: 20px; margin-bottom: 30px;">
    <h1>[Boot 2.7 / Java 8] SSO 인증 연동 성공</h1>
    <p style="color: #555;">스프링 시큐리티 컨텍스트에 사용자 정보가 정상 탑재되었습니다.</p>
</div>
<div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; display: inline-block;">
    <p>현재 로그인된 유저 사번/ID: <strong style="color: #dc3545; font-size: 18px;">${userId}</strong></p>
    <ul>
        <li><a href="/file-page" style="color: #007bff; text-decoration: none;" target="_blank">파일첨부 예제</a></li>
        <li><a href="/sample/excel" style="color: #007bff; text-decoration: none;" target="_blank">엑셀업/다운로드 예제</a></li>
        <li><a href="/test/list" style="color: #007bff; text-decoration: none;" target="_blank">핸들바 예제</a></li>
        <li><a href="/test/vue" style="color: #007bff; text-decoration: none;" target="_blank">vue 화면 예제</a></li>
        <li><a href="/test/gridlist" style="color: #007bff; text-decoration: none;" target="_blank">그리드 리스트 예제</a></li>
        <li><a href="<c:url value='/test/commonValidator' />" style="color: #007bff; text-decoration: none;" target="_blank">JS Validator 예제</a></li>
        <li><a href="/ai/aiChat" style="color: #007bff; text-decoration: none;" target="_blank">ai chat 예제</a></li>
        <li><a href="<c:url value='/llm/chat' />" style="color: #007bff; text-decoration: none;" target="_blank">LLM 예제</a></li>
    </ul>
</div>
</body>
</html>