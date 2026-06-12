<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>Validator 샘플</title>
<style>
  body { font-family: sans-serif; padding: 2rem; max-width: 400px; }
  label { display: block; margin-top: 1rem; font-weight: bold; }
  input { width: 100%; padding: 6px; box-sizing: border-box; }
  .error { color: red; font-size: 12px; margin-top: 2px; min-height: 14px; }
  button { margin-top: 1.5rem; padding: 8px 16px; }
</style>
</head>
<body>

  <h2>회원 정보 입력</h2>

  <label for="name">이름 (필수, 2~10자)</label>
  <input type="text" id="name">
  <div class="error" id="nameMsg"></div>

  <label for="age">나이 (숫자, 1~3자리)</label>
  <input type="text" id="age">
  <div class="error" id="ageMsg"></div>

  <label for="email">이메일 (필수)</label>
  <input type="text" id="email">
  <div class="error" id="emailMsg"></div>

  <label for="phone">전화번호</label>
  <input type="text" id="phone">
  <div class="error" id="phoneMsg"></div>

  <button id="submitBtn">제출</button>

  <script src="<c:url value='/js/common/commonValidator.js' />"></script>
  <script>
    const schema = [
      { id: 'name',  type: 'text',   opts: { required: true, min: 2, max: 10 }, msgId: 'nameMsg' },
      { id: 'age',   type: 'number', opts: { required: true, min: 1, max: 3 }, msgId: 'ageMsg' },
      { id: 'email', type: 'email',  opts: { required: true }, msgId: 'emailMsg' },
      { id: 'phone', type: 'tel',    opts: { required: false }, msgId: 'phoneMsg' },
    ];

    document.getElementById('submitBtn').addEventListener('click', () => {
      const { isValid } = validateForm(schema);

      if (!isValid) {
        // focusOnError 기본값이 true이므로 첫 invalid 요소로 자동 focus 됨
        return;
      }

      alert('검증 통과! 제출 진행');
      // 실제 form.submit() 또는 ajax 호출 등
    });
  </script>
</body>
</html>