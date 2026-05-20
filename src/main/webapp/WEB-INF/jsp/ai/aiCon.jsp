<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>eGovFrame AI Chat (JSP)</title>
    <!-- 스타일은 가볍게 구성했습니다. 기존 전자정부 CSS와 겹치지 않습니다. -->
    <style>
        .ai-container { max-width: 700px; margin: 30px auto; font-family: 'Malgun Gothic', sans-serif; }
        .input-box { width: 100%; height: 80px; padding: 10px; box-sizing: border-box; resize: none; }
        .btn-submit { width: 100%; padding: 12px; background-color: #0056b3; color: white; border: none; cursor: pointer; font-weight: bold; margin-top: 5px; }
        .btn-submit:disabled { background-color: #cccccc; cursor: not-allowed; }
        .result-zone { margin-top: 20px; border-top: 2px solid #0056b3; padding-top: 15px; }
        .chat-display { background-color: #f8f9fa; border: 1px solid #dee2e6; padding: 15px; min-height: 150px; border-radius: 4px; white-space: pre-wrap; word-break: break-all; line-height: 1.6; }
        .blinking-cursor { animation: blink 1s infinite; color: #0056b3; font-weight: bold; }
        @keyframes blink { 0%, 100% { opacity: 0; } 50% { opacity: 1; } }
    </style>
</head>
<body>

<div class="ai-container">
    <h2>AI 실시간 스트리밍 답변 테스트</h2>

    <!-- 질문 입력 폼 -->
    <div>
        <textarea id="promptInput" class="input-box" placeholder="AI에게 질문할 내용을 입력하세요..."></textarea>
        <button id="submitBtn" class="btn-submit" onclick="startAiStream()">AI에게 질문하기</button>
    </div>

    <!-- 결과 출력 영역 -->
    <div class="result-zone">
        <h3>AI 답변:</h3>
        <div id="chatDisplay" class="chat-display">질문을 입력하시면 실시간으로 답변이 작성됩니다.</div>
    </div>
</div>

<script type="text/javascript">
    async function startAiStream() {
        const promptInput = document.getElementById("promptInput");
        const submitBtn = document.getElementById("submitBtn");
        const chatDisplay = document.getElementById("chatDisplay");

        const promptText = promptInput.value.trim();
        if (!promptText) {
            alert("질문을 입력해주세요.");
            return;
        }

        // UI 상태를 '로딩 중'으로 변경
        submitBtn.disabled = true;
        submitBtn.innerText = "답변 생성 중...";
        chatDisplay.innerHTML = '<span class="blinking-cursor">|</span>'; // 초기화 및 커서 표시

        try {
            // 1. WebFlux 서버의 스트리밍 API 호출
            const response = await fetch('/api/v1/stream', {
                method: 'POST',
                headers: {
                    'Content-Type': 'text/plain; charset=UTF-8'
                },
                body: promptText
            });

            if (!response.ok) {
                throw new Error("서ver 응답 오류");
            }

            // 2. 스트림 리더 및 디코더 준비
            const reader = response.body.getReader();
            const decoder = new TextDecoder('utf-8');

            // 텍스트를 누적할 임시 변수
            let fullText = "";

            // 3. 서버에서 들어오는 조각(Chunk) 데이터를 무한 루프로 실시간 인지
            while (true) {
                const { value, done } = await reader.read();
                if (done) break; // 데이터 스트림이 완전히 끝나면 종료

                // 바이너리 데이터를 글자로 변환
                const chunkText = decoder.decode(value, { stream: true });

                // 글자 누적
                fullText += chunkText;

                // 4. JSP 화면에 실시간으로 반영 (타다닥 효과)
                // HTML 특수문자 깨짐을 방지하고 줄바꿈을 유지하기 위해 pre-wrap 스타일 지정 상태
                chatDisplay.innerText = fullText;

                // 글자 맨 뒤에 깜빡이는 커서 효과 추가
                const cursorSpan = document.createElement("span");
                cursorSpan.className = "blinking-cursor";
                cursorSpan.innerText = "|";
                chatDisplay.appendChild(cursorSpan);
            }

            // 스트림이 완전히 끝나면 커서 제거
            const cursor = chatDisplay.querySelector(".blinking-cursor");
            if(cursor) cursor.remove();

        } catch (error) {
            console.error("Streaming 에러 발생:", error);
            chatDisplay.innerText = "⚠️ AI와 통신 중 에러가 발생했거나 연결이 끊어졌습니다.";
        } finally {
            // UI 상태 원복
            submitBtn.disabled = false;
            submitBtn.innerText = "AI에게 질문하기";
        }
    }
</script>

</body>
</html>