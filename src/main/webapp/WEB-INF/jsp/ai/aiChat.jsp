<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<div class="chat-wrap">

    <!-- 추론 레벨 선택 -->
    <div class="chat-options">
        <label>추론 레벨</label>
        <select id="reasoningLevel">
            <option value="low">Low (빠름)</option>
            <option value="medium" selected>Medium (기본)</option>
            <option value="high">High (정확, 느림)</option>
        </select>
    </div>

    <!-- 대화 영역 -->
    <div id="chatHistory" class="chat-history"></div>

    <!-- 입력 영역 -->
    <div class="chat-input-wrap">
        <textarea id="promptInput" rows="3" placeholder="질문을 입력하세요..."></textarea>
        <div class="chat-btn-wrap">
            <button id="sendBtn" type="button">전송</button>
            <button id="clearBtn" type="button">초기화</button>
        </div>
    </div>

</div>

<script>
    (function () {

        var eventSource = null;
        var currentAssistantDiv = null;

        /* ========================
         * 전송 버튼
         * ======================== */
        document.getElementById('sendBtn').addEventListener('click', function () {
            sendMessage();
        });

        /* 엔터(Shift+Enter 제외) */
        document.getElementById('promptInput').addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        /* 초기화 버튼 */
        document.getElementById('clearBtn').addEventListener('click', function () {
            if (eventSource) {
                eventSource.close();
                eventSource = null;
            }
            document.getElementById('chatHistory').innerHTML = '';
            currentAssistantDiv = null;
        });

        /* ========================
         * 메시지 전송
         * ======================== */
        function sendMessage() {
            var prompt = document.getElementById('promptInput').value.trim();
            if (!prompt) return;
            if (eventSource) return; // 스트리밍 중 중복 방지

            var reasoning = document.getElementById('reasoningLevel').value;

            appendMessage('user', prompt);
            document.getElementById('promptInput').value = '';

            currentAssistantDiv = appendMessage('assistant', '');
            setLoading(true);

            /* SSE 연결 */
            var url = '<c:url value="/ai/stream"/>?prompt=' + encodeURIComponent(prompt)
                    + '&reasoning=' + encodeURIComponent(reasoning);

            eventSource = new EventSource(url);

            eventSource.onmessage = function (e) {
                setLoading(false);
                currentAssistantDiv.innerHTML += escapeHtml(e.data);
                scrollToBottom();
            };

            eventSource.addEventListener('done', function () {
                closeStream();
            });

            eventSource.onerror = function () {
                closeStream();
                if (currentAssistantDiv && currentAssistantDiv.innerHTML === '') {
                    currentAssistantDiv.innerHTML = '<span class="error">[오류] 응답을 받지 못했습니다.</span>';
                }
            };
        }

        /* ========================
         * 문서 요약 (POST + SSE)
         * ======================== */
        function summarizeDocument(content) {
            if (!content) return;

            currentAssistantDiv = appendMessage('assistant', '');
            setLoading(true);

            /* SSE는 GET만 지원 → 요약은 fetch로 토큰 스트리밍 */
            fetch('<c:url value="/ai/summarize"/>', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content: content })
            }).then(function (response) {
                var reader = response.body.getReader();
                var decoder = new TextDecoder('utf-8');
                var buffer = '';

                function read() {
                    reader.read().then(function (result) {
                        if (result.done) {
                            setLoading(false);
                            return;
                        }

                        buffer += decoder.decode(result.value, { stream: true });

                        /* SSE 포맷 파싱: "data: ...\n\n" */
                        var lines = buffer.split('\n');
                        buffer = lines.pop(); // 마지막 미완성 라인 보관

                        lines.forEach(function (line) {
                            if (line.indexOf('data:') === 0) {
                                var token = line.substring(5).trim();
                                if (token === '[DONE]') {
                                    setLoading(false);
                                    return;
                                }
                                setLoading(false);
                                currentAssistantDiv.innerHTML += escapeHtml(token);
                                scrollToBottom();
                            }
                        });

                        read();
                    });
                }

                read();
            }).catch(function (e) {
                setLoading(false);
                currentAssistantDiv.innerHTML = '<span class="error">[오류] ' + e.message + '</span>';
            });
        }

        /* ========================
         * 관련 문서 추천 (POST + SSE)
         * ======================== */
        function recommendDocuments(currentDoc, candidates) {
            currentAssistantDiv = appendMessage('assistant', '');
            setLoading(true);

            fetch('<c:url value="/ai/recommend"/>', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    currentDoc: currentDoc,
                    candidates: candidates
                })
            }).then(function (response) {
                var reader = response.body.getReader();
                var decoder = new TextDecoder('utf-8');
                var buffer = '';

                function read() {
                    reader.read().then(function (result) {
                        if (result.done) {
                            setLoading(false);
                            return;
                        }

                        buffer += decoder.decode(result.value, { stream: true });

                        var lines = buffer.split('\n');
                        buffer = lines.pop();

                        lines.forEach(function (line) {
                            if (line.indexOf('data:') === 0) {
                                var token = line.substring(5).trim();
                                if (token === '[DONE]') {
                                    setLoading(false);
                                    return;
                                }
                                setLoading(false);
                                currentAssistantDiv.innerHTML += escapeHtml(token);
                                scrollToBottom();
                            }
                        });

                        read();
                    });
                }

                read();
            }).catch(function (e) {
                setLoading(false);
                currentAssistantDiv.innerHTML = '<span class="error">[오류] ' + e.message + '</span>';
            });
        }

        /* ========================
         * 공통 유틸
         * ======================== */
        function appendMessage(role, text) {
            var history = document.getElementById('chatHistory');
            var div = document.createElement('div');
            div.className = 'chat-message chat-message--' + role;

            var label = document.createElement('span');
            label.className = 'chat-label';
            label.textContent = role === 'user' ? '나' : 'AI';

            var content = document.createElement('div');
            content.className = 'chat-content';
            content.innerHTML = escapeHtml(text);

            div.appendChild(label);
            div.appendChild(content);
            history.appendChild(div);
            scrollToBottom();

            return content; // 스트리밍 토큰을 content에 추가
        }

        function closeStream() {
            if (eventSource) {
                eventSource.close();
                eventSource = null;
            }
            setLoading(false);
            currentAssistantDiv = null;
        }

        function setLoading(flag) {
            var btn = document.getElementById('sendBtn');
            btn.disabled = flag;
            btn.textContent = flag ? '응답 중...' : '전송';
        }

        function scrollToBottom() {
            var history = document.getElementById('chatHistory');
            history.scrollTop = history.scrollHeight;
        }

        function escapeHtml(str) {
            if (!str) return '';
            return str
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/\n/g, '<br>');
        }

        /* 외부에서 호출 가능하도록 노출 */
        window.AiChat = {
            summarize: summarizeDocument,
            recommend: recommendDocuments
        };
        // ex)
        // 문서 요약
        //window.AiChat.summarize('요약할 문서 내용...');
        // 관련 문서 추천
        //window.AiChat.recommend('현재 문서 제목', ['후보1', '후보2', '후보3']);

    }());
</script>

<style>
    .chat-wrap {
        display: flex;
        flex-direction: column;
        height: 600px;
        border: 1px solid #ddd;
        border-radius: 8px;
        overflow: hidden;
    }

    .chat-options {
        padding: 8px 12px;
        background: #f5f5f5;
        border-bottom: 1px solid #ddd;
    }

    .chat-options select {
        margin-left: 8px;
    }

    .chat-history {
        flex: 1;
        overflow-y: auto;
        padding: 16px;
        background: #fafafa;
    }

    .chat-message {
        display: flex;
        gap: 8px;
        margin-bottom: 16px;
    }

    .chat-message--user {
        flex-direction: row-reverse;
    }

    .chat-label {
        font-size: 12px;
        font-weight: bold;
        color: #666;
        min-width: 24px;
        text-align: center;
    }

    .chat-content {
        max-width: 75%;
        padding: 10px 14px;
        border-radius: 8px;
        font-size: 14px;
        line-height: 1.6;
        white-space: pre-wrap;
        word-break: break-word;
    }

    .chat-message--user .chat-content {
        background: #1976d2;
        color: #fff;
    }

    .chat-message--assistant .chat-content {
        background: #fff;
        border: 1px solid #e0e0e0;
        color: #333;
    }

    .chat-input-wrap {
        padding: 12px;
        border-top: 1px solid #ddd;
        background: #fff;
    }

    .chat-input-wrap textarea {
        width: 100%;
        resize: none;
        padding: 8px;
        border: 1px solid #ccc;
        border-radius: 4px;
        font-size: 14px;
        box-sizing: border-box;
    }

    .chat-btn-wrap {
        display: flex;
        justify-content: flex-end;
        gap: 8px;
        margin-top: 8px;
    }

    .chat-btn-wrap button {
        padding: 6px 16px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
    }

    #sendBtn {
        background: #1976d2;
        color: #fff;
    }

    #sendBtn:disabled {
        background: #90caf9;
        cursor: not-allowed;
    }

    #clearBtn {
        background: #e0e0e0;
        color: #333;
    }

    span.error {
        color: #d32f2f;
    }
</style>