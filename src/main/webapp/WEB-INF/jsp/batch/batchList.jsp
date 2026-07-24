<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <style>
        #batchGrid {
            width: 100%;
            height: 560px; /* 명시적인 높이값 필수 */
        }
        .batch-toolbar {
            margin: 10px 0;
            display: flex;
            gap: 8px;
        }
        .batch-toolbar button {
            padding: 6px 14px;
            cursor: pointer;
        }
        .grid-pagination ul.pagination {
            list-style: none;
            margin: 20px 0;
            padding: 0;
            display: flex;
            justify-content: center;
            gap: 4px;
        }
        .grid-pagination ul.pagination li a {
            display: inline-block;
            padding: 6px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            color: #333;
            text-decoration: none;
            background: #fff;
        }
        .grid-pagination ul.pagination li a:hover { background: #f0f0f0; }
        .grid-pagination ul.pagination li.active a {
            background: #3f51b5;
            color: #fff;
            border-color: #3f51b5;
        }
        .grid-pagination ul.pagination li.disabled a {
            color: #ccc;
            cursor: not-allowed;
            border-color: #eee;
        }
        /* 수동 실행 - 배치서버 선택 팝업 */
        .run-server-form { text-align: left; }
        .run-server-form label { display: block; margin-top: 10px; font-weight: bold; font-size: 13px; }
        .run-server-form select, .run-server-form input[type="text"] { width: 100%; box-sizing: border-box; }
        .run-server-form .hint { color: #888; font-size: 12px; margin-top: 4px; }
    </style>
    <%-- select2 4.1.0-rc.0 (로컬 vendoring, 버전 정보는 /js/lib/select2/VERSION.txt 참고) --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/select2/select2.min.css" />
    <script src="${pageContext.request.contextPath}/js/lib/select2/select2.min.js"></script>
</head>

<body>
<form name="batchForm1" id="batchForm1" onsubmit="return false;">

    <div class="batch-toolbar">
        <button type="button" id="btnNew">신규등록</button>
        <button type="button" id="btnAllHistory">전체 이력조회</button>
        <%--
            useYn 저장버튼은 USE_YN_MODE === 'batch' 일 때만 노출됨
            (batchList.js 상단 참고: 'instant' = 즉시반영 / 'batch' = 일괄저장)
        --%>
        <button type="button" id="btnSaveUseYn" style="display:none;">사용여부 저장</button>
    </div>

    <div id="batchGrid"></div>
    <div class="grid-pagination" data-grid="batchGrid"></div>

</form>

<script src="${pageContext.request.contextPath}/js/batch/batchList.js"></script>
</body>
</html>
