<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <title>Markdown View</title>
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.2.0/github-markdown.min.css">
</head>
<body class="markdown-body" style="padding: 20px;">

    <div id="content">로딩 중...</div>

    <script>
        // static 폴더가 루트(/) 경로가 되므로 static은 주소에서 제외합니다.
        // 파일명이 대소문자까지 일치하는지 꼭 확인하세요 (README.md)

        // 현재 시간을 밀리초 단위 숫자로 가져옴 (예: 1719822131000)
        const cacheBuster = new Date().getTime();

        fetch("<c:url value='/md/README.md' />?v=" + cacheBuster, { cache: 'no-store' })
            .then(response => {
                if (!response.ok) throw new Error('파일을 찾을 수 없습니다.');
                return response.text();
            })
            .then(text => {
                document.getElementById('content').innerHTML = marked.parse(text);
            })
            .catch(err => {
                document.getElementById('content').innerHTML = '에러 발생: ' + err.message;
            });
    </script>
</body>
</html>