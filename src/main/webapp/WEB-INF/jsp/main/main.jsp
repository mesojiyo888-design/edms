<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
</div>
</body>
</html>