<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>로그인</title>
</head>
<body style="background-color: #f5f5f5; font-family: sans-serif;">
<div style="margin: 150px auto; width: 350px; background: white; border: 1px solid #ddd; padding: 30px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); border-radius: 8px;">
    <h2 style="text-align: center; color: #333; margin-bottom: 20px;">로그인</h2>
    <form action="/dummy-login-process" method="post">
        <div style="margin-bottom: 15px;">
            <input type="text" name="userId" placeholder="ID 입력" required
                   style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
        </div>
        <button type="submit" style="width: 100%; padding: 12px; background-color: #007bff; color: white; border: none; border-radius: 4px; font-weight: bold; cursor: pointer;">
            로그인
        </button>
    </form>
</div>
</body>
</html>