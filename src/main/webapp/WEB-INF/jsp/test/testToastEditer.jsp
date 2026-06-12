<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>

    <script>
        var content = '초기 내용';

         $(function() {
             const editor = new toastui.Editor({
                    el: document.querySelector('#editor'),
                    previewStyle: 'vertical',
                    height: '500px',
                    initialValue: content
                  });
         });
    </script>
</head>
<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">
<div id="editor"></div>
</body>
</html>
