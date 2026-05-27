<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>첨부파일 컴포넌트</title>

    <style>
        body { font-family: 'Malgun Gothic', sans-serif; padding: 20px; background-color: #f8f9fa; }
        .biz-card { background: #fff; border: 1px solid #dee2e6; border-radius: 6px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
        .section-title { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #eaeaea; padding-bottom: 10px; margin-bottom: 15px; }
        .section-title h4 { margin: 0; font-size: 16px; }
        .btn-add { background-color: #28a745; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 12px; }
        .file-list-wrapper { background: #fff; padding: 5px; border: 1px solid #ced4da; border-radius: 4px; margin-bottom: 15px; min-height: 40px; }
        .file-list-wrapper ul { list-style: none; padding: 0; margin: 0; }
        .btn-submit { display: block; width: 100%; max-width: 300px; margin: 30px auto; padding: 12px; background: #007bff; color: #fff; border: none; border-radius: 5px; font-size: 16px; font-weight: bold; cursor: pointer; text-align: center; }
    </style>
</head>
<body>

<h2 style="text-align: center; margin-bottom:30px; color:#333;">EDMS 파일 실시간 적재 검증</h2>

<form id="boardWriteForm">

    <div id="imgFileArea"></div>

    <div id="docFileArea"></div>

    <button type="button" class="btn-submit" onclick="executeIntegratedSave()">💾 전체 데이터 통합 저장</button>
</form>

<script>
    $(document).ready(function() {
        CommonFile.createAndAppend('#imgFileArea', 'imgFile', '1');
        CommonFile.createAndAppend('#docFileArea', 'docFile', '2');
    });


    let zoneIndex = 0;

    function addNewFileZone() {
        zoneIndex++;
        // 예: 커스텀파라미터1, 커스텀파라미터2 형식으로 파라미터명 동적 생성
        const paramName = "customParam_" + zoneIndex;

        // ⭐️ [신규 추가된 엔진 호출] 원하는 부모 영역안에 실시간으로 UI를 빌드하고 객체를 등록합니다.
        window.EdmsAutoBuilder.createAndAppend('#dynamicFileArea', paramName, '');
    }


    function executeIntegratedSave() {
        const formData = new FormData();

        CommonFile.appendDataFromArea("#imgFileArea", formData);
        CommonFile.appendDataFromArea("#docFileArea", formData);

        for (var pair of formData.entries()) {
            console.log(pair[0] + ', ' + pair[1]);
        }

        $.ajax({
            url: '${pageContext.request.contextPath}/file/save-process',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                alert("성공적으로 서버에 통합 매핑 처리되었습니다.");
            },
            error: function() {
                alert("네트워크 통신 오류 혹은 매핑 주소를 확인하세요.");
            }
        });
    }
</script>
</body>
</html>