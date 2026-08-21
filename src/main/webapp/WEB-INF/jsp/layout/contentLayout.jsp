<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { margin: 0; padding: 15px; font-family: 'Malgun Gothic', sans-serif; box-sizing: border-box; }
    </style>
</head>
<body class="tab-content-body">

<tiles:insertAttribute name="body" />

<script src="/js/lib/jquery-3.7.1.min.js"></script>
<script src="/js/common/commonAjax.js"></script>
<script src="/js/common/ComMsg.js"></script>

</body>
</html>