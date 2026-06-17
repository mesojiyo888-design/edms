<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>LLM Chat</title>
<style>
  /* ============================================================
     Reset & Base
  ============================================================ */
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

  :root {
    --bg:           #0f1117;
    --surface:      #1a1d27;
    --surface-2:    #22263a;
    --border:       #2e3248;
    --accent:       #5b7fff;
    --accent-dim:   #3a52cc;
    --user-bubble:  #1e3a5f;
    --ai-bubble:    #1a1d27;
    --text:         #e2e4f0;
    --text-muted:   #7880a0;
    --text-label:   #a0a8c8;
    --danger:       #ff5f6d;
    --success:      #3ecf8e;
    --radius:       12px;
    --radius-sm:    8px;
    --font-mono:    'Consolas', 'Menlo', monospace;
  }

  html, body {
    height: 100%;
    background: var(--bg);
    color: var(--text);
    font-family: 'Segoe UI', 'Apple SD Gothic Neo', sans-serif;
    font-size: 14px;
    line-height: 1.6;
  }

  /* ============================================================
     Layout
  ============================================================ */
  .app {
    display: flex;
    flex-direction: column;
    height: 100vh;
    max-width: 900px;
    margin: 0 auto;
  }

  /* ============================================================
     Header
  ============================================================ */
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid var(--border);
    background: var(--surface);
    flex-shrink: 0;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .logo {
    width: 32px; height: 32px;
    background: linear-gradient(135deg, var(--accent), #a78bfa);
    border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    font-size: 16px;
  }

  .header h1 {
    font-size: 16px;
    font-weight: 600;
    color: var(--text);
  }

  .provider-badge {
    font-size: 11px;
    padding: 3px 8px;
    border-radius: 20px;
    background: var(--surface-2);
    border: 1px solid var(--border);
    color: var(--text-muted);
    font-family: var(--font-mono);
  }

  .provider-badge.active {
    border-color: var(--accent);
    color: var(--accent);
  }

  /* ============================================================
     Mode Tabs
  ============================================================ */
  .tabs {
    display: flex;
    gap: 4px;
    padding: 12px 20px 0;
    background: var(--surface);
    border-bottom: 1px solid var(--border);
    flex-shrink: 0;
  }

  .tab-btn {
    padding: 8px 18px;
    border: none;
    border-radius: var(--radius-sm) var(--radius-sm) 0 0;
    background: transparent;
    color: var(--text-muted);
    cursor: pointer;
    font-size: 13px;
    font-weight: 500;
    transition: color .15s, background .15s;
    border-bottom: 2px solid transparent;
    margin-bottom: -1px;
  }

  .tab-btn:hover { color: var(--text); }

  .tab-btn.active {
    color: var(--accent);
    border-bottom-color: var(--accent);
    background: var(--bg);
  }

  /* ============================================================
     System Prompt Panel (chat mode only)
  ============================================================ */
  .sys-panel {
    background: var(--surface);
    border-bottom: 1px solid var(--border);
    flex-shrink: 0;
    overflow: hidden;
    transition: max-height .25s ease;
    max-height: 0;
  }

  .sys-panel.open { max-height: 160px; }

  .sys-panel-inner {
    padding: 12px 20px;
  }

  .sys-label {
    font-size: 11px;
    color: var(--text-label);
    font-weight: 600;
    letter-spacing: .06em;
    text-transform: uppercase;
    margin-bottom: 6px;
  }

  .sys-textarea {
    width: 100%;
    height: 72px;
    resize: none;
    background: var(--surface-2);
    border: 1px solid var(--border);
    border-radius: var(--radius-sm);
    color: var(--text);
    font-size: 13px;
    padding: 8px 12px;
    line-height: 1.5;
    outline: none;
    font-family: inherit;
    transition: border-color .15s;
  }

  .sys-textarea:focus { border-color: var(--accent); }

  /* ============================================================
     Chat Area
  ============================================================ */
  .chat-area {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    scroll-behavior: smooth;
  }

  .chat-area::-webkit-scrollbar { width: 6px; }
  .chat-area::-webkit-scrollbar-track { background: transparent; }
  .chat-area::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }

  /* ============================================================
     Message Bubbles
  ============================================================ */
  .msg-row {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    animation: fadeUp .2s ease;
  }

  @keyframes fadeUp {
    from { opacity: 0; transform: translateY(8px); }
    to   { opacity: 1; transform: translateY(0); }
  }

  .msg-row.user { flex-direction: row-reverse; }

  .avatar {
    width: 32px; height: 32px;
    border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    font-size: 14px;
    flex-shrink: 0;
  }

  .avatar.user-av {
    background: var(--user-bubble);
    border: 1px solid #2a4a7f;
  }

  .avatar.ai-av {
    background: linear-gradient(135deg, var(--accent), #a78bfa);
  }

  .bubble {
    max-width: 72%;
    padding: 10px 14px;
    border-radius: var(--radius);
    font-size: 14px;
    line-height: 1.65;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .msg-row.user .bubble {
    background: var(--user-bubble);
    border: 1px solid #2a4a7f;
    border-radius: var(--radius) 4px var(--radius) var(--radius);
  }

  .msg-row.ai .bubble {
    background: var(--ai-bubble);
    border: 1px solid var(--border);
    border-radius: 4px var(--radius) var(--radius) var(--radius);
  }

  .msg-meta {
    font-size: 11px;
    color: var(--text-muted);
    margin-top: 4px;
    padding: 0 2px;
  }

  .msg-row.user .msg-meta { text-align: right; }

  /* 타이핑 커서 */
  .typing-cursor::after {
    content: '▋';
    animation: blink .7s step-end infinite;
    color: var(--accent);
    font-size: .9em;
  }

  @keyframes blink {
    50% { opacity: 0; }
  }

  /* 로딩 dots */
  .loading-dots span {
    display: inline-block;
    width: 6px; height: 6px;
    background: var(--text-muted);
    border-radius: 50%;
    animation: dot .8s ease-in-out infinite;
    margin: 0 2px;
  }

  .loading-dots span:nth-child(2) { animation-delay: .15s; }
  .loading-dots span:nth-child(3) { animation-delay: .3s; }

  @keyframes dot {
    0%, 80%, 100% { transform: scale(.6); opacity: .4; }
    40% { transform: scale(1); opacity: 1; }
  }

  /* 에러 메시지 */
  .msg-row.error .bubble {
    background: #2a1215;
    border-color: #4a2020;
    color: #ff9494;
  }

  /* ============================================================
     Empty State
  ============================================================ */
  .empty-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    color: var(--text-muted);
    text-align: center;
    padding: 40px 20px;
  }

  .empty-icon {
    width: 56px; height: 56px;
    background: var(--surface-2);
    border: 1px solid var(--border);
    border-radius: 16px;
    display: flex; align-items: center; justify-content: center;
    font-size: 26px;
    margin-bottom: 4px;
  }

  .empty-state p { font-size: 13px; color: var(--text-muted); }

  .empty-hints {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-top: 8px;
    width: 100%;
    max-width: 320px;
  }

  .hint-chip {
    background: var(--surface-2);
    border: 1px solid var(--border);
    border-radius: var(--radius-sm);
    padding: 8px 14px;
    font-size: 12px;
    color: var(--text-label);
    cursor: pointer;
    transition: border-color .15s, color .15s;
    text-align: left;
  }

  .hint-chip:hover {
    border-color: var(--accent);
    color: var(--text);
  }

  /* ============================================================
     Input Area
  ============================================================ */
  .input-area {
    padding: 14px 20px 18px;
    background: var(--surface);
    border-top: 1px solid var(--border);
    flex-shrink: 0;
  }

  .input-row {
    display: flex;
    gap: 8px;
    align-items: flex-end;
  }

  .input-wrap {
    flex: 1;
    position: relative;
  }

  #userInput {
    width: 100%;
    min-height: 44px;
    max-height: 160px;
    resize: none;
    background: var(--surface-2);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    color: var(--text);
    font-size: 14px;
    padding: 10px 44px 10px 14px;
    line-height: 1.5;
    outline: none;
    font-family: inherit;
    overflow-y: auto;
    transition: border-color .15s;
  }

  #userInput:focus { border-color: var(--accent); }
  #userInput::placeholder { color: var(--text-muted); }

  .char-count {
    position: absolute;
    bottom: 9px; right: 10px;
    font-size: 10px;
    color: var(--text-muted);
    font-family: var(--font-mono);
    pointer-events: none;
  }

  .char-count.warn { color: var(--danger); }

  .send-btn {
    width: 44px; height: 44px;
    border: none;
    border-radius: var(--radius);
    background: var(--accent);
    color: #fff;
    font-size: 18px;
    cursor: pointer;
    display: flex; align-items: center; justify-content: center;
    transition: background .15s, transform .1s;
    flex-shrink: 0;
  }

  .send-btn:hover:not(:disabled) { background: var(--accent-dim); }
  .send-btn:active:not(:disabled) { transform: scale(.94); }
  .send-btn:disabled { background: var(--border); cursor: not-allowed; }

  /* ============================================================
     Toolbar (sys-prompt toggle, clear)
  ============================================================ */
  .toolbar {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
  }

  .tool-btn {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 4px 10px;
    border: 1px solid var(--border);
    border-radius: 20px;
    background: transparent;
    color: var(--text-muted);
    font-size: 12px;
    cursor: pointer;
    transition: border-color .15s, color .15s;
    white-space: nowrap;
  }

  .tool-btn:hover { border-color: var(--accent); color: var(--text); }
  .tool-btn.active { border-color: var(--accent); color: var(--accent); }

  .tool-sep {
    flex: 1;
  }

  /* ============================================================
     Responsive
  ============================================================ */
  @media (max-width: 600px) {
    .bubble { max-width: 90%; }
    .header h1 { font-size: 14px; }
  }
</style>
</head>
<body>

<div class="app">

  <!-- ── Header ─────────────────────────────────────────── -->
  <header class="header">
    <div class="header-left">
      <div class="logo">🤖</div>
      <h1>LLM Chat</h1>
    </div>
    <span class="provider-badge" id="providerBadge">연결 중…</span>
  </header>

  <!-- ── Mode Tabs ───────────────────────────────────────── -->
  <div class="tabs">
    <button class="tab-btn active" id="tabComplete" onclick="switchMode('complete')">
      ✦ Complete
    </button>
    <button class="tab-btn" id="tabChat" onclick="switchMode('chat')">
      💬 Chat
    </button>
  </div>

  <!-- ── System Prompt Panel (chat mode) ────────────────── -->
  <div class="sys-panel" id="sysPanel">
    <div class="sys-panel-inner">
      <div class="sys-label">System Prompt</div>
      <textarea class="sys-textarea" id="sysPromptInput"
                placeholder="AI 의 역할과 행동 방식을 지정합니다. 비워두면 기본 프롬프트가 사용됩니다."></textarea>
    </div>
  </div>

  <!-- ── Chat Area ───────────────────────────────────────── -->
  <div class="chat-area" id="chatArea">
    <div class="empty-state" id="emptyState">
      <div class="empty-icon">💡</div>
      <p>무엇이든 물어보세요</p>
      <div class="empty-hints">
        <div class="hint-chip" onclick="setHint('Spring MVC와 Spring Boot의 차이점을 설명해줘')">
          Spring MVC와 Spring Boot의 차이점은?
        </div>
        <div class="hint-chip" onclick="setHint('Java 8 Stream API 사용 예제를 코드로 보여줘')">
          Java 8 Stream API 예제 코드
        </div>
        <div class="hint-chip" onclick="setHint('eGovFrame을 사용하는 이유가 뭐야?')">
          eGovFrame을 사용하는 이유
        </div>
      </div>
    </div>
  </div>

  <!-- ── Input Area ──────────────────────────────────────── -->
  <div class="input-area">
    <div class="toolbar">
      <button class="tool-btn" id="sysBtnWrap" style="display:none"
              onclick="toggleSysPanel()" id="sysToggleBtn">
        ⚙ 시스템 프롬프트
      </button>
      <div class="tool-sep"></div>
      <button class="tool-btn" onclick="clearHistory()">
        🗑 대화 초기화
      </button>
    </div>
    <div class="input-row">
      <div class="input-wrap">
        <textarea
          id="userInput"
          rows="1"
          placeholder="메시지를 입력하세요… (Shift+Enter: 줄바꿈 / Enter: 전송)"
          maxlength="4000"
          oninput="onInputChange()"
          onkeydown="onKeyDown(event)"
        ></textarea>
        <span class="char-count" id="charCount">0/4000</span>
      </div>
      <button class="send-btn" id="sendBtn" onclick="sendMessage()" title="전송">
        ➤
      </button>
    </div>
  </div>

</div><!-- /.app -->

<script>
(function () {
  'use strict';

  /* ============================================================
     상태
  ============================================================ */
  var mode = 'complete';       // 'complete' | 'chat'
  var isLoading = false;

  /* ============================================================
     초기화
  ============================================================ */
  window.addEventListener('DOMContentLoaded', function () {
    fetchProvider();
    autoResizeTextarea(document.getElementById('userInput'));
  });

  /** 현재 활성 provider 를 헤더 배지에 표시 */
  function fetchProvider() {
    fetch('/api/llm/service')
      .then(function (res) { return res.json(); })
      .then(function (data) {
        var badge = document.getElementById('providerBadge');
        if (data && data.provider) {
          badge.textContent = data.provider.toUpperCase();
          badge.classList.add('active');
        }
      })
      .catch(function () {
        document.getElementById('providerBadge').textContent = '오프라인';
      });
  }

  /* ============================================================
     모드 전환
  ============================================================ */
  window.switchMode = function (m) {
    mode = m;
    document.getElementById('tabComplete').classList.toggle('active', m === 'complete');
    document.getElementById('tabChat').classList.toggle('active', m === 'chat');

    var sysBtn = document.getElementById('sysBtnWrap');
    sysBtn.style.display = (m === 'chat') ? '' : 'none';

    if (m !== 'chat') closeSysPanel();

    document.getElementById('userInput').placeholder =
      m === 'complete'
        ? '프롬프트를 입력하세요… (Enter: 전송)'
        : '메시지를 입력하세요… (Shift+Enter: 줄바꿈 / Enter: 전송)';
  };

  /* ============================================================
     System Prompt 패널 토글
  ============================================================ */
  window.toggleSysPanel = function () {
    var panel = document.getElementById('sysPanel');
    var btn   = document.getElementById('sysBtnWrap');
    if (panel.classList.contains('open')) {
      closeSysPanel();
    } else {
      panel.classList.add('open');
      btn.classList.add('active');
      document.getElementById('sysPromptInput').focus();
    }
  };

  function closeSysPanel() {
    document.getElementById('sysPanel').classList.remove('open');
    var btn = document.getElementById('sysBtnWrap');
    if (btn) btn.classList.remove('active');
  }

  /* ============================================================
     힌트 칩 클릭
  ============================================================ */
  window.setHint = function (text) {
    var input = document.getElementById('userInput');
    input.value = text;
    onInputChange();
    input.focus();
  };

  /* ============================================================
     입력 이벤트
  ============================================================ */
  window.onInputChange = function () {
    var val   = document.getElementById('userInput').value;
    var len   = val.length;
    var el    = document.getElementById('charCount');
    el.textContent = len + '/4000';
    el.classList.toggle('warn', len > 3800);
    document.getElementById('sendBtn').disabled = (len === 0 || isLoading);
    autoResizeTextarea(document.getElementById('userInput'));
  };

  window.onKeyDown = function (e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      if (!isLoading) sendMessage();
    }
  };

  function autoResizeTextarea(el) {
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 160) + 'px';
  }

  /* ============================================================
     메시지 전송
  ============================================================ */
  window.sendMessage = function () {
    var userMsg = document.getElementById('userInput').value.trim();
    if (!userMsg || isLoading) return;

    hideEmptyState();
    appendUserBubble(userMsg);

    document.getElementById('userInput').value = '';
    onInputChange();

    isLoading = true;
    document.getElementById('sendBtn').disabled = true;

    var aiRow = appendAiLoadingBubble();

    if (mode === 'complete') {
      callComplete(userMsg, aiRow);
    } else {
      var sysPrompt = document.getElementById('sysPromptInput').value.trim();
      callChat(sysPrompt, userMsg, aiRow);
    }
  };

  /* ============================================================
     API 호출
  ============================================================ */
  function callComplete(prompt, aiRow) {
    fetch('/api/llm/complete', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json; charset=UTF-8' },
      body:    JSON.stringify({ prompt: prompt })
    })
      .then(handleResponse)
      .then(function (data) { finishAiBubble(aiRow, data.result, data.provider); })
      .catch(function (err)  { finishAiBubbleError(aiRow, err.message); });
  }

  function callChat(systemPrompt, userMessage, aiRow) {
    var body = { userMessage: userMessage };
    if (systemPrompt) body.systemPrompt = systemPrompt;

    fetch('/api/llm/chat', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json; charset=UTF-8' },
      body:    JSON.stringify(body)
    })
      .then(handleResponse)
      .then(function (data) { finishAiBubble(aiRow, data.result, data.provider); })
      .catch(function (err)  { finishAiBubbleError(aiRow, err.message); });
  }

  function handleResponse(res) {
    return res.json().then(function (data) {
      if (!res.ok) {
        var msg = (data && data.errorMessage) ? data.errorMessage : '서버 오류 (' + res.status + ')';
        throw new Error(msg);
      }
      return data;
    });
  }

  /* ============================================================
     DOM 조작 - 버블 생성
  ============================================================ */
  function hideEmptyState() {
    var el = document.getElementById('emptyState');
    if (el) el.style.display = 'none';
  }

  function appendUserBubble(text) {
    var area = document.getElementById('chatArea');
    var row  = document.createElement('div');
    row.className = 'msg-row user';
    row.innerHTML =
      '<div class="avatar user-av">👤</div>' +
      '<div>' +
        '<div class="bubble">' + escapeHtml(text) + '</div>' +
        '<div class="msg-meta">' + formatTime() + '</div>' +
      '</div>';
    area.appendChild(row);
    scrollToBottom();
  }

  function appendAiLoadingBubble() {
    var area = document.getElementById('chatArea');
    var row  = document.createElement('div');
    row.className = 'msg-row ai';
    row.innerHTML =
      '<div class="avatar ai-av">🤖</div>' +
      '<div>' +
        '<div class="bubble" id="aiActiveBubble">' +
          '<div class="loading-dots">' +
            '<span></span><span></span><span></span>' +
          '</div>' +
        '</div>' +
        '<div class="msg-meta" id="aiActiveMeta"></div>' +
      '</div>';
    area.appendChild(row);
    scrollToBottom();
    return row;
  }

  function finishAiBubble(row, text, provider) {
    isLoading = false;
    document.getElementById('sendBtn').disabled = false;

    var bubble = row.querySelector('#aiActiveBubble');
    var meta   = row.querySelector('#aiActiveMeta');

    if (bubble) {
      bubble.removeAttribute('id');
      bubble.innerHTML = '';
      bubble.classList.add('typing-cursor');
      typeText(bubble, text, function () {
        bubble.classList.remove('typing-cursor');
      });
    }

    if (meta) {
      meta.removeAttribute('id');
      meta.textContent = formatTime() + (provider ? ' · ' + provider : '');
    }

    scrollToBottom();
  }

  function finishAiBubbleError(row, message) {
    isLoading = false;
    document.getElementById('sendBtn').disabled = false;

    var bubble = row.querySelector('#aiActiveBubble');
    var meta   = row.querySelector('#aiActiveMeta');

    row.classList.add('error');
    if (bubble) {
      bubble.removeAttribute('id');
      bubble.textContent = '⚠ ' + (message || '알 수 없는 오류가 발생했습니다.');
    }
    if (meta) {
      meta.removeAttribute('id');
      meta.textContent = formatTime();
    }

    scrollToBottom();
  }

  /* ============================================================
     타이핑 효과
  ============================================================ */
  function typeText(el, text, onDone) {
    var i   = 0;
    var len = text.length;
    // 텍스트가 짧으면 빠르게, 길면 스킵
    var delay = len > 800 ? 0 : len > 300 ? 4 : 10;

    if (delay === 0) {
      el.textContent = text;
      if (onDone) onDone();
      return;
    }

    (function tick() {
      if (i < len) {
        el.textContent = text.slice(0, ++i);
        scrollToBottom();
        setTimeout(tick, delay);
      } else if (onDone) {
        onDone();
      }
    })();
  }

  /* ============================================================
     대화 초기화
  ============================================================ */
  window.clearHistory = function () {
    var area = document.getElementById('chatArea');
    // emptyState 를 제외한 모든 msg-row 제거
    var rows = area.querySelectorAll('.msg-row');
    for (var i = 0; i < rows.length; i++) {
      area.removeChild(rows[i]);
    }
    var emptyState = document.getElementById('emptyState');
    if (emptyState) emptyState.style.display = '';
  };

  /* ============================================================
     유틸
  ============================================================ */
  function scrollToBottom() {
    var area = document.getElementById('chatArea');
    area.scrollTop = area.scrollHeight;
  }

  function formatTime() {
    var d = new Date();
    var h = String(d.getHours()).padStart(2, '0');
    var m = String(d.getMinutes()).padStart(2, '0');
    return h + ':' + m;
  }

  function escapeHtml(str) {
    return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  // 초기 전송 버튼 비활성화
  document.getElementById('sendBtn').disabled = true;

})();
</script>
</body>
</html>
