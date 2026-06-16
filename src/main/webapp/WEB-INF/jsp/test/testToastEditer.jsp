<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>

    <script>
        // 초기값 텍스트 세팅
        var sampleContent = `
테스트입니다.
# Headings입니다.
**Bold입니다.**
*Italic입니다.*
~~strice입니다.~~
***
↘↘↘line입니다.

> Blockquote입니다.
* Unordered list입니다.
0. Ordered list입니다.

|table입니다.1  |2  |
| --- | --- |
|3  |4  |

\`Inline 코드입니다.\`

\`\`\`
Insert CodeBlock입니다.
\`\`\`
        `;

        $(document).ready(function() {
            ToastEditorManager.init('#editor', '#sampleForm', {
                height: '500px',
                previewStyle: 'tab',
                initialEditType: 'markdown',
                initialValue: sampleContent
            });

            $('#btn-save').click(function(e) {
                e.preventDefault();

                ToastEditorManager.saveWithImages(
                    '#editor',
                    '#content',
                    '${pageContext.request.contextPath}/test/toastEditorInsertImages'
                );
            });
        });
    </script>
</head>

<body>
    <form id="sampleForm" method="post" enctype="multipart/form-data" onsubmit="return false;">
        <div id="editor"></div>

        <!-- Editor Markdown 저장용 -->
        <input type="hidden" name="content" id="content">

        <!-- Spring Security CSRF 사용 시 필요. 이미 ajaxSend에서 header로 넣고 있으면 없어도 됨. -->
        <c:if test="${not empty _csrf}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        </c:if>

        <button type="button" id="btn-save">저장 테스트</button>
    </form>

    <textarea id="insertData" style="width:100%; height:150px;"></textarea>
</body>
</html>