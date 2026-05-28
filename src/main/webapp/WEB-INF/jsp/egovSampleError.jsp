<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>접근 권한 없음 (Access Denied)</title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, Roboto, sans-serif;
        }

        body {
            background-color: #f5f7fa;
            color: #333d4b;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
            width: 100vw;
            overflow: hidden;
        }

        .container {
            text-align: center;
            padding: 40px 24px;
            background: #ffffff;
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
            max-width: 480px;
            width: 90%;
            animation: fadeInUp 0.6s ease-out;
        }

        .icon-box {
            position: relative;
            width: 100px;
            height: 100px;
            margin: 0 auto 24px;
            background: #feeef2;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .lock-icon {
            width: 44px;
            height: 44px;
            fill: #f04452;
            animation: shake 2s infinite ease-in-out;
        }

        h1 {
            font-size: 24px;
            font-weight: 700;
            color: #191f28;
            margin-bottom: 12px;
            letter-spacing: -0.5px;
        }

        p {
            font-size: 15px;
            color: #4e5968;
            line-height: 1.6;
            margin-bottom: 32px;
        }

        .highlight {
            color: #f04452;
            font-weight: 600;
        }

        .btn-group {
            display: flex;
            gap: 12px;
            justify-content: center;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 24px;
            font-size: 15px;
            font-weight: 600;
            border-radius: 12px;
            text-decoration: none;
            transition: all 0.2s ease;
            cursor: pointer;
            border: none;
            width: 100%;
        }

        .btn-primary {
            background-color: #3182f6;
            color: #ffffff;
        }

        .btn-primary:hover {
            background-color: #1b64da;
        }

        .btn-secondary {
            background-color: #e5e8eb;
            color: #4e5968;
        }

        .btn-secondary:hover {
            background-color: #d1d6db;
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        @keyframes shake {
            0%, 100% { transform: rotate(0deg); }
            20%, 60% { transform: rotate(-8deg); }
            40%, 80% { transform: rotate(8deg); }
        }
    </style>
</head>
<body>

<div class="container">
    <div class="icon-box">
        <svg class="lock-icon" viewBox="0 0 24 24">
            <path d="M18,8H17V6A5,5,0,0,0,7,6V8H6a3,3,0,0,0,-3,3v8a3,3,0,0,0,3,3H18a3,3,0,0,0,3,-3V11A3,3,0,0,0,18,8ZM9,6a3,3,0,0,1,6,0V8H9ZM13,16.72V18a1,1,0,0,1,-2,0V16.72a2,2,0,1,1,2,0Z"/>
        </svg>
    </div>

    <h1>접근 권한이 없습니다</h1>
    <p>
        인증 세션이 없거나 페이지 접근 권한이 없습니다.<br>
        지속적으로 문제가 발생할 경우 <span class="highlight">EDMS 시스템 관리자</span>에게 문의해 주세요.
    </p>

    <div class="btn-group">
        <button onclick="history.back();" class="btn btn-secondary">이전 화면</button>
        <!-- 💡 JSP 스타일의 컨텍스트 패스 보장 로그인 주소 세팅 -->
        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">로그인 페이지로</a>
    </div>
</div>

</body>
</html>