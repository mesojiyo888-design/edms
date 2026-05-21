<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Spring Boot + Vue 3 CDN</title>

    <!-- 1. Vue 3 global 빌드 CDN 로드 -->
    <script src="${pageContext.request.contextPath}/js/lib/vue/dist/vue.global-3.5.34.js"></script>

    <!-- 필요시 axios(ajax 라이브러리)도 CDN으로 로드 -->
    <script src="${pageContext.request.contextPath}/js/lib/axios/dist/axios.min-1.16.1.js"></script>
</head>
<body>

<div style="padding: 20px;">
    <h2>Spring Boot 2.7 + Vue 3 (CDN) 연동 테스트</h2>
    <hr>

    <!-- 2. Vue가 제어할 HTML 영역 (반드시 id="app" 내부여야 함) -->
    <div id="app">
        <h3>{{ message }}</h3>

        <!-- 클릭 이벤트 핸들링 -->
        <button @click="count++">클릭 횟수: {{ count }}</button>
        <button @click="fetchServerData" style="margin-left: 10px;">서버 데이터 가져오기</button>

        <!-- 비동기 데이터 출력 영역 -->
        <div v-if="serverInfo" style="margin-top: 15px; background: #f4f4f4; padding: 10px;">
            <p><strong>서버 응답 결과:</strong> {{ serverInfo }}</p>
        </div>
        <div v-else="serverInfo" style="margin-top: 15px; background: #f4f4f4; padding: 10px;">
            <p><strong>서버 응답 결과:</strong> 기다리는중입니다.</p>
        </div>
    </div>

    <hr>
    <!-- 3. JSP 본연의 기능(EL 변수 표현)도 같은 페이지에서 정상 작동합니다 -->
    <h4>Spring 백엔드가 보낸 Model 데이터: ${serverMessage}</h4>
</div>

<!-- 4. Vue 인스턴스 초기화 스크립트 -->
<script>
    // 전역 Vue 객체에서 사용할 함수들 구조분해할당
    const { createApp, ref, onMounted } = Vue;

    createApp({
        setup() {
            // Vue의 반응형 데이터 선언 (state)
            const message = ref('안녕하세요! Vue 3 가 정상적으로 로드되었습니다.');
            const count = ref(0);
            const serverInfo = ref(null);

            // 라이프사이클 훅: 페이지 로드 직후 실행
            onMounted(() => {
                console.log('Vue 컴포넌트 마운트 완료');
            });

            // 서버와 통신하는 함수 (Axios 활용)
            const fetchServerData = () => {
                axios.get('${pageContext.request.contextPath}/api/axois/data')
                    .then(response => {
                        serverInfo.value = response.data;
                    })
                    .catch(error => {
                        console.error('에러 발생:', error);
                        alert('데이터를 가져오는데 실패했습니다.');
                    });
            };

            // HTML 템플릿(id="app" 내부)에서 사용할 수 있도록 리턴
            return {
                message,
                count,
                serverInfo,
                fetchServerData
            };
        }
    }).mount('#app'); // id="app"을 가진 div에 Vue를 바인딩
</script>
</body>
</html>