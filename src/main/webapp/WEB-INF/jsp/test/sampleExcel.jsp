<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">
    <title>Spring Boot Excel Upload/Download</title>
    <style>
        body { font-family: Arial, sans-serif; padding: 20px; }
        .box { border: 1px solid #ccc; padding: 20px; margin-bottom: 20px; border-radius: 5px; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f4f4f4; }
    </style>

</head>
<body>

<h2>Apache POI 엑셀 업로드 / 다운로드 예제</h2>

<!-- 1. 다운로드 영역 -->
<div class="box">
    <h3>1. 엑셀 데이터 다운로드</h3>
    <p>서버에 저장된 대용량 데이터를 SXSSF 방식으로 안전하게 다운로드합니다.</p>
    <!-- 단순 링크 이동으로 다운로드 실행 -->
    <a href="${pageContext.request.contextPath}/sample/excel/download">
        <button type="button" style="padding: 10px 20px; cursor: pointer;">엑셀 다운로드 (.xlsx)</button>
    </a>
    <a href="${pageContext.request.contextPath}/sample/excel/xmldownload">
        <button type="button" style="padding: 10px 20px; cursor: pointer;">XML 다운로드 (.xml)</button>
    </a>
    <a href="${pageContext.request.contextPath}/sample/excel/downloadCsv">
        <button type="button" style="padding: 10px 20px; cursor: pointer;">CSV 다운로드 (.csv)</button>
    </a>
</div>

<!-- 2. 업로드 영역 -->
<div class="box">
    <h3>2. 엑셀 파일 업로드</h3>
    <p>양식에 맞춘 엑셀 파일을 선택하고 업로드 버튼을 누르세요.</p>

    <form id="uploadForm">
        <input type="file" id="excelFile" name="file" accept=".xlsx" required/>
        <button type="button" onclick="uploadFile()" style="padding: 5px 15px; cursor: pointer;">업로드</button>
    </form>

    <!-- 결과 출력 테이블 -->
    <table id="resultTable" style="display: none;">
        <thead>
        <tr>
            <th>번호</th>
            <th>이름</th>
            <th>이메일</th>
        </tr>
        </thead>
        <tbody id="resultBody">
        <!-- 자바스크립트로 동적 행 생성 -->
        </tbody>
    </table>
</div>
<div>
    <table>
        <tr>
            <td><textarea id="brdContent" name="brdContent" rows="4" cols="50"></textarea></td>
        </tr>
    </table>

</div>

<script>
    function uploadFile() {
        const fileInput = document.getElementById('excelFile');
        if (!fileInput.files[0]) {
          alert("파일을 선택해 주세요.");
          return;
        }

        // 파일 데이터를 다루기 위한 FormData 객체 생성
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);
        formData.append("brdContent", $("#brdContent").val()); // 추가 데이터 예시

      $.ajax({
          url: '${pageContext.request.contextPath}/sample/excel/upload',
          type: 'POST',
          data: formData,
          processData: false,  // 쿼리 스트링 변환 방지 (파일 전송 시 필수)
          contentType: false,  // 브라우저가 자동으로 multipart/form-data 헤더를 세팅하도록 설정 (파일 전송 시 필수)
          beforeSend: function(xhr) {

          },
          success: function(data) {
                console.log("AJAX Success:", JSON.stringify(data));
              // jQuery는 서버 응답이 성공(200 OK)이고 JSON 포맷이면 자동으로 파싱해 줍니다.
              const tbody = $('#resultBody');
              tbody.empty(); // 기존 테이블 데이터 초기화

              if (!data || data.length === 0) {
                  alert("읽어온 데이터가 없거나 올바르지 않은 엑셀 양식입니다.");
                  return;
              }

              // 기존 자바스크립트의 data.forEach 에러 해결 (안전하게 루프 실행)
              $.each(data, function(index, row) {

                    const tr = `
                        <tr>
                            <td>\${row.cell_0 != null ? row.cell_0 : ''}</td>
                            <td>\${row.cell_1 != null ? row.cell_1 : ''}</td>
                            <td>\${row.cell_2 != null ? row.cell_2 : ''}</td>
                        </tr>
                    `;
                    tbody.append(tr);
                });

              // 결과 테이블 노출
              $('#resultTable').show();
              alert("업로드 및 데이터 파싱 성공!");
          },
          error: function(xhr, status, error) {
              // 403 등 에러가 나면 success가 아닌 이곳으로 들어오므로 안전함
              console.error("AJAX Error:", xhr, status, error);

              if (xhr.status === 403) {
                  alert("업로드 권한이 없거나 보안 토큰(CSRF)이 만료되었습니다. (403 Forbidden)");
              } else {
                  alert("업로드 중 서버 오류가 발생했습니다. (에러 코드: " + xhr.status + ")");
              }
          }
      });
  }
</script>
</body>
</html>