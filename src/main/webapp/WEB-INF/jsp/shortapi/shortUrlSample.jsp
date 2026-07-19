<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>단축 URL 샘플 테스트 페이지</title>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <style>
        *, *::before, *::after { box-sizing: border-box; }
        body {
            font-family: 'Malgun Gothic', sans-serif;
            background: #f0f2f5;
            margin: 0; padding: 20px 24px;
            color: #222;
        }
        h1 { font-size: 19px; margin: 0 0 2px; }
        .subtitle { font-size: 12px; color: #888; margin: 0 0 20px; }

        /* ── 섹션 카드 ── */
        .card {
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.07);
            padding: 20px 22px;
            margin-bottom: 16px;
        }
        .card-title {
            font-size: 13px; font-weight: 700; color: #333;
            border-left: 3px solid #1a73e8; padding-left: 9px;
            margin: 0 0 14px;
        }

        /* ── 듀얼 패널 ── */
        .dual {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 14px;
        }
        @media (max-width: 900px) { .dual { grid-template-columns: 1fr; } }

        .panel {
            border-radius: 8px;
            padding: 14px 16px;
            border: 1px solid #e5e7eb;
        }
        .panel-db   { background: #fafafa; border-color: #d1d5db; }
        .panel-kutt { background: #f0f7ff; border-color: #bfdbfe; }

        .panel-label {
            display: flex; align-items: center; gap: 6px;
            font-size: 11px; font-weight: 700;
            margin-bottom: 10px; padding-bottom: 8px;
            border-bottom: 1px solid;
        }
        .panel-db   .panel-label { color: #6b7280; border-color: #e5e7eb; }
        .panel-kutt .panel-label { color: #1d4ed8; border-color: #bfdbfe; }

        .badge-db   { background: #e5e7eb; color: #374151; padding: 2px 7px; border-radius: 10px; font-size: 10px; }
        .badge-kutt { background: #dbeafe; color: #1d4ed8; padding: 2px 7px; border-radius: 10px; font-size: 10px; }
        .badge-warn { background: #fef3c7; color: #92400e; padding: 2px 7px; border-radius: 10px; font-size: 10px; }

        /* ── 폼 필드 ── */
        .field { margin-bottom: 8px; }
        .field label {
            display: block; font-size: 11px; color: #555; margin-bottom: 3px; font-weight: 600;
        }
        .field input, .field select, .field textarea {
            width: 100%; padding: 7px 9px;
            border: 1px solid #d1d5db; border-radius: 4px;
            font-size: 12px; font-family: inherit;
            background: #fff;
        }
        .field input:disabled, .field select:disabled {
            background: #f3f4f6; color: #9ca3af; cursor: not-allowed;
        }

        /* ── 버튼 ── */
        .btn {
            padding: 7px 16px; border: none; border-radius: 4px;
            font-size: 12px; font-weight: 700; cursor: pointer; color: #fff;
            transition: opacity .15s;
        }
        .btn:hover { opacity: .85; }
        .btn:disabled { opacity: .4; cursor: not-allowed; }
        .btn-blue   { background: #1a73e8; }
        .btn-gray   { background: #6b7280; }
        .btn-green  { background: #16a34a; }
        .btn-orange { background: #f59e0b; }
        .btn-red    { background: #dc2626; }
        .btn-indigo { background: #4f46e5; }

        .btn-row { display: flex; justify-content: flex-end; gap: 6px; margin-top: 10px; }

        /* ── 인라인 응답 박스 ── */
        .res-box {
            margin-top: 10px;
            border-radius: 6px;
            font-family: monospace;
            font-size: 11px;
            line-height: 1.5;
            white-space: pre-wrap;
            word-break: break-all;
            display: none;
            padding: 10px 12px;
            border: 1px solid;
            max-height: 220px;
            overflow-y: auto;
        }
        .res-ok   { background: #f0fdf4; border-color: #bbf7d0; color: #15803d; }
        .res-err  { background: #fef2f2; border-color: #fecaca; color: #b91c1c; }
        .res-info { background: #eff6ff; border-color: #bfdbfe; color: #1d4ed8; }

        /* ── 모달 시나리오 버튼 그룹 ── */
        .scenario-wrap { display: flex; flex-wrap: wrap; gap: 8px; }
        .btn-scenario {
            padding: 8px 16px; border: none; border-radius: 6px;
            font-size: 12px; font-weight: 600; cursor: pointer; color: #fff;
        }

        /* ── 구분선 ── */
        hr.sec { border: none; border-top: 1px solid #e5e7eb; margin: 14px 0; }

        /* ── 엔드포인트 태그 ── */
        .ep {
            display: inline-block; font-size: 11px; font-family: monospace;
            background: #1e1e2e; color: #cdd6f4;
            padding: 2px 8px; border-radius: 4px; margin-bottom: 10px;
        }
        .ep .m-post   { color: #89b4fa; }
        .ep .m-get    { color: #a6e3a1; }
        .ep .m-patch  { color: #f9e2af; }
        .ep .m-delete { color: #f38ba8; }
    </style>
</head>
<body>

<h1>🔗 단축 URL — 샘플 테스트 페이지</h1>
<p class="subtitle">
    <span style="color:#16a34a; font-weight:700;">● DB 조회</span> = 실제 비즈니스 로직 (DB 연동 후 활성화) &nbsp;|&nbsp;
    <span style="color:#1d4ed8; font-weight:700;">● KUTT 테스트</span> = kuttId 직접 호출 (현재 사용 가능)
</p>

<%-- ============================================================
     0. 모달 시나리오
============================================================ --%>
<div class="card">
    <p class="card-title">📋 모달 열기 시나리오</p>
    <div class="scenario-wrap">
        <button class="btn-scenario btn-blue"   onclick="openBasic()">① 기본 (현재 URL)</button>
        <button class="btn-scenario btn-gray"   onclick="openWithDesc()">② 설명 포함</button>
        <button class="btn-scenario btn-green"  onclick="openWithCallback()">③ onSuccess 콜백</button>
        <button class="btn-scenario btn-orange" onclick="openCustomUrl()">④ 커스텀 URL</button>
        <button class="btn-scenario btn-red"    onclick="openMissingId()">⑤ programId 없음 (오류)</button>
    </div>
    <div id="res-modal" class="res-box res-info" style="display:none;"></div>
</div>

<%-- ============================================================
     1. 생성 (Kutt API 실호출 — DB 저장 skip)
============================================================ --%>
<div class="card">
    <p class="card-title">✨ 단축 URL 생성</p>
    <div class="ep"><span class="m-post">POST</span> /api/short-url/create</div>
    <div class="field"><label>programId</label><input id="c_programId" value="SAMPLE_001" /></div>
    <div class="field"><label>programUrl</label><input id="c_programUrl" value="https://example.com/test-page" /></div>
    <div class="field"><label>description</label><input id="c_desc" value="샘플 테스트용 URL" /></div>
    <div class="field"><label>expireIn <small style="color:#aaa;">(예: 7 days — 빈칸=무기한)</small></label>
        <input id="c_expireIn" placeholder="7 days" /></div>
    <div class="btn-row">
        <button class="btn btn-blue" onclick="apiCreate()">🚀 생성 요청</button>
    </div>
    <div id="res-create" class="res-box"></div>
</div>

<%-- ============================================================
     2. 단건 조회
============================================================ --%>
<div class="card">
    <p class="card-title">🔍 단건 조회</p>
    <div class="dual">

        <%-- DB 패널 --%>
        <div class="panel panel-db">
            <div class="panel-label">
                🗄 DB 조회 (실제 로직)
                <span class="badge-warn">DB 연동 후 사용 가능</span>
            </div>
            <div class="ep"><span class="m-get">GET</span> /api/short-url/{programId}</div>
            <div class="field"><label>programId</label><input id="g_db_programId" value="SAMPLE_001" /></div>
            <div class="btn-row">
                <button class="btn btn-gray" onclick="apiGetDb()">조회</button>
            </div>
            <div id="res-get-db" class="res-box"></div>
        </div>

        <%-- KUTT 패널 --%>
        <div class="panel panel-kutt">
            <div class="panel-label">
                🔗 KUTT 직접 테스트
                <span class="badge-kutt">현재 사용 가능</span>
            </div>
            <div class="ep"><span class="m-get">GET</span> /api/short-url/test/kutt/link?kuttId=</div>
            <div class="field">
                <label>kuttId <small style="color:#60a5fa;">(생성 응답의 kuttId 입력)</small></label>
                <input id="g_kutt_kuttId" placeholder="예: accefebc-f139-4f08-8b6a-e45b89ce0873" />
            </div>
            <div class="btn-row">
                <button class="btn btn-indigo" onclick="apiGetKutt()">조회</button>
            </div>
            <div id="res-get-kutt" class="res-box"></div>
        </div>
    </div>
</div>

<%-- ============================================================
     3. 목록 조회
============================================================ --%>
<div class="card">
    <p class="card-title">📋 목록 조회</p>
    <div class="dual">

        <%-- DB 패널 --%>
        <div class="panel panel-db">
            <div class="panel-label">
                🗄 DB 조회 (실제 로직)
                <span class="badge-warn">DB 연동 후 사용 가능</span>
            </div>
            <div class="ep"><span class="m-get">GET</span> /api/short-url/list</div>
            <div class="field"><label>programId <small style="color:#aaa;">(빈칸=전체)</small></label>
                <input id="l_db_programId" placeholder="전체 조회 시 빈칸" /></div>
            <div class="field"><label>useYn</label>
                <select id="l_db_useYn">
                    <option value="Y">Y (사용)</option>
                    <option value="N">N (미사용)</option>
                </select>
            </div>
            <div class="btn-row">
                <button class="btn btn-gray" onclick="apiListDb()">목록 조회</button>
            </div>
            <div id="res-list-db" class="res-box"></div>
        </div>

        <%-- KUTT 패널 --%>
        <div class="panel panel-kutt">
            <div class="panel-label">
                🔗 KUTT 직접 테스트
                <span class="badge-kutt">현재 사용 가능</span>
            </div>
            <div class="ep"><span class="m-get">GET</span> /api/short-url/test/kutt/links</div>
            <div class="field"><label>limit</label><input id="l_kutt_limit" type="number" value="10" /></div>
            <div class="field"><label>skip (offset)</label><input id="l_kutt_skip" type="number" value="0" /></div>
            <div class="btn-row">
                <button class="btn btn-indigo" onclick="apiListKutt()">목록 조회</button>
            </div>
            <div id="res-list-kutt" class="res-box"></div>
        </div>
    </div>
</div>

<%-- ============================================================
     4. 수정
============================================================ --%>
<div class="card">
    <p class="card-title">✏️ 수정</p>
    <div class="dual">

        <%-- DB 패널 --%>
        <div class="panel panel-db">
            <div class="panel-label">
                🗄 DB 조회 (실제 로직)
                <span class="badge-warn">DB 연동 후 사용 가능</span>
            </div>
            <div class="ep"><span class="m-patch">PATCH</span> /api/short-url/{programId}</div>
            <div class="field"><label>programId</label><input id="u_db_programId" value="SAMPLE_001" /></div>
            <div class="field"><label>keepTarget</label>
                <select id="u_db_keepTarget" onchange="toggleNewTarget('db')">
                    <option value="true">true (원본 URL 유지)</option>
                    <option value="false">false (새 URL로 변경)</option>
                </select>
            </div>
            <div class="field" id="u_db_newTargetWrap" style="display:none;">
                <label>newTarget</label><input id="u_db_newTarget" placeholder="변경할 URL" />
            </div>
            <div class="field"><label>description</label><input id="u_db_desc" value="수정된 설명" /></div>
            <div class="field"><label>expireIn</label><input id="u_db_expireIn" placeholder="예: 14 days" /></div>
            <div class="btn-row">
                <button class="btn btn-gray" onclick="apiUpdateDb()">수정 요청</button>
            </div>
            <div id="res-update-db" class="res-box"></div>
        </div>

        <%-- KUTT 패널 --%>
        <div class="panel panel-kutt">
            <div class="panel-label">
                🔗 KUTT 직접 테스트
                <span class="badge-kutt">현재 사용 가능</span>
            </div>
            <div class="ep"><span class="m-patch">PATCH</span> /api/short-url/test/kutt/{kuttId}</div>
            <div class="field">
                <label>kuttId <small style="color:#60a5fa;">(생성 응답의 kuttId 입력)</small></label>
                <input id="u_kutt_kuttId" placeholder="예: accefebc-f139-4f08-8b6a-e45b89ce0873" />
            </div>
            <div class="field"><label>keepTarget</label>
                <select id="u_kutt_keepTarget" onchange="toggleNewTarget('kutt')">
                    <option value="true">true (원본 URL 유지)</option>
                    <option value="false">false (새 URL로 변경)</option>
                </select>
            </div>
            <div class="field" id="u_kutt_newTargetWrap" style="display:none;">
                <label>newTarget</label><input id="u_kutt_newTarget" placeholder="변경할 URL" />
            </div>
            <div class="field"><label>description</label><input id="u_kutt_desc" value="수정된 설명" /></div>
            <div class="field"><label>expireIn</label><input id="u_kutt_expireIn" placeholder="예: 14 days" /></div>
            <div class="btn-row">
                <button class="btn btn-indigo" onclick="apiUpdateKutt()">수정 요청</button>
            </div>
            <div id="res-update-kutt" class="res-box"></div>
        </div>
    </div>
</div>

<%-- ============================================================
     5. 삭제
============================================================ --%>
<div class="card">
    <p class="card-title">🗑 삭제</p>
    <div class="dual">

        <%-- DB 패널 --%>
        <div class="panel panel-db">
            <div class="panel-label">
                🗄 DB 조회 (실제 로직)
                <span class="badge-warn">DB 연동 후 사용 가능</span>
            </div>
            <div class="ep"><span class="m-post">POST</span> /api/short-url/{programId}/delete</div>
            <div class="field"><label>programId</label><input id="d_db_programId" value="SAMPLE_001" /></div>
            <div class="btn-row">
                <button class="btn btn-gray" onclick="apiDeleteDb()">삭제 요청</button>
            </div>
            <div id="res-delete-db" class="res-box"></div>
        </div>

        <%-- KUTT 패널 --%>
        <div class="panel panel-kutt">
            <div class="panel-label">
                🔗 KUTT 직접 테스트
                <span class="badge-kutt">현재 사용 가능</span>
            </div>
            <div class="ep"><span class="m-delete">DELETE</span> /api/short-url/test/kutt/{kuttId}/delete</div>
            <div class="field">
                <label>kuttId <small style="color:#60a5fa;">(생성 응답의 kuttId 입력)</small></label>
                <input id="d_kutt_kuttId" placeholder="예: accefebc-f139-4f08-8b6a-e45b89ce0873" />
            </div>
            <div class="btn-row">
                <button class="btn btn-red" onclick="apiDeleteKutt()">삭제 요청</button>
            </div>
            <div id="res-delete-kutt" class="res-box"></div>
        </div>
    </div>
</div>

<%-- ============================================================
     단축 URL 모달 include
============================================================ --%>
<%@ include file="/WEB-INF/jsp/shortapi/shortUrlModal.jsp" %>

<%-- ============================================================
     JavaScript
============================================================ --%>
<script>
var CTX = '<%=request.getContextPath()%>';

/* ── 응답 렌더 유틸 ─────────────────────────────────────── */
function showRes(id, data, isOk) {
    var el  = document.getElementById(id);
    var cls = isOk ? 'res-ok' : 'res-err';
    el.className = 'res-box ' + cls;
    el.textContent = JSON.stringify(data, null, 2);
    el.style.display = 'block';
}
function showResInfo(id, text) {
    var el = document.getElementById(id);
    el.className = 'res-box res-info';
    el.textContent = text;
    el.style.display = 'block';
}

/* ── 모달 시나리오 ──────────────────────────────────────── */
function openBasic() {
    showResInfo('res-modal', '모달 열기 → programId: SAMPLE_BASIC');
    ShortUrl.open({ programId: 'SAMPLE_BASIC', programUrl: location.href });
}
function openWithDesc() {
    showResInfo('res-modal', '모달 열기 → programId: SAMPLE_DESC, description: "샘플 설명"');
    ShortUrl.open({ programId: 'SAMPLE_DESC', programUrl: location.href, description: '샘플 설명 포함 테스트' });
}
function openWithCallback() {
    showResInfo('res-modal', '모달 열기 → onSuccess 콜백 등록됨. 생성 완료 시 아래 업데이트됩니다.');
    ShortUrl.open({
        programId: 'SAMPLE_CB',
        programUrl: location.href,
        onSuccess: function(shortUrl) {
            showResInfo('res-modal', 'onSuccess 콜백 호출됨!\n생성된 단축 URL: ' + shortUrl);
        }
    });
}
function openCustomUrl() {
    var url = 'https://example.com/internal/report?year=2026&dept=IT';
    showResInfo('res-modal', '모달 열기 → 커스텀 URL: ' + url);
    ShortUrl.open({ programId: 'SAMPLE_CUSTOM', programUrl: url, description: '내부 보고서 페이지' });
}
function openMissingId() {
    showResInfo('res-modal', '모달 열기 → programId 없음 (오류 메시지 확인)');
    ShortUrl.open({ programId: '', programUrl: location.href });
}

/* ── 1. 생성 ────────────────────────────────────────────── */
function apiCreate() {
    var payload = {
        programId  : $('#c_programId').val(),
        programUrl : $('#c_programUrl').val(),
        description: $('#c_desc').val(),
        expireIn   : $('#c_expireIn').val() || null
    };
    $.ajax({
        url: CTX + '/api/short-url/create', type: 'POST',
        contentType: 'application/json', data: JSON.stringify(payload), dataType: 'json',
        success: function(res) {
            showRes('res-create', res, res.success);
            // 생성된 kuttId를 KUTT 테스트 패널 입력란에 자동 채우기
            if (res.success && res.kuttId) {
                $('#g_kutt_kuttId').val(res.kuttId);
                $('#u_kutt_kuttId').val(res.kuttId);
                $('#d_kutt_kuttId').val(res.kuttId);
            }
        },
        error: function(xhr) {
            showRes('res-create', { status: xhr.status, error: xhr.responseText }, false);
        }
    });
}

/* ── 2. 단건 조회 ───────────────────────────────────────── */
function apiGetDb() {
    var id = $('#g_db_programId').val();
    $.ajax({
        url: CTX + '/api/short-url/' + encodeURIComponent(id), type: 'GET', dataType: 'json',
        success: function(res) { showRes('res-get-db', res, res.success); },
        error:   function(xhr) { showRes('res-get-db', { status: xhr.status, error: xhr.responseText }, false); }
    });
}
function apiGetKutt() {
    var kuttId = $('#g_kutt_kuttId').val();
    if (!kuttId) { showRes('res-get-kutt', { error: 'kuttId를 입력하세요.' }, false); return; }
    $.ajax({
        url: CTX + '/api/short-url/test/kutt/link?kuttId=' + encodeURIComponent(kuttId),
        type: 'GET', dataType: 'json',
        success: function(res) { showRes('res-get-kutt', res, res.success); },
        error:   function(xhr) { showRes('res-get-kutt', { status: xhr.status, error: xhr.responseText }, false); }
    });
}

/* ── 3. 목록 조회 ───────────────────────────────────────── */
function apiListDb() {
    var id    = $('#l_db_programId').val();
    var useYn = $('#l_db_useYn').val();
    var params = '?useYn=' + useYn + (id ? '&programId=' + encodeURIComponent(id) : '');
    $.ajax({
        url: CTX + '/api/short-url/list' + params, type: 'GET', dataType: 'json',
        success: function(res) { showRes('res-list-db', res, res.success); },
        error:   function(xhr) { showRes('res-list-db', { status: xhr.status, error: xhr.responseText }, false); }
    });
}
function apiListKutt() {
    var limit = $('#l_kutt_limit').val() || 10;
    var skip  = $('#l_kutt_skip').val()  || 0;
    $.ajax({
        url: CTX + '/api/short-url/test/kutt/links?limit=' + limit + '&skip=' + skip,
        type: 'GET', dataType: 'json',
        success: function(res) { showRes('res-list-kutt', res, res.success); },
        error:   function(xhr) { showRes('res-list-kutt', { status: xhr.status, error: xhr.responseText }, false); }
    });
}

/* ── 4. 수정 ────────────────────────────────────────────── */
function toggleNewTarget(type) {
    var val  = $('#u_' + type + '_keepTarget').val();
    var wrap = document.getElementById('u_' + type + '_newTargetWrap');
    wrap.style.display = (val === 'false') ? 'block' : 'none';
}
function apiUpdateDb() {
    var id = $('#u_db_programId').val();
    var payload = {
        keepTarget : ($('#u_db_keepTarget').val() === 'true'),
        newTarget  : $('#u_db_newTarget').val() || null,
        description: $('#u_db_desc').val(),
        expireIn   : $('#u_db_expireIn').val() || null
    };
    $.ajax({
        url: CTX + '/api/short-url/' + encodeURIComponent(id), type: 'POST',
        contentType: 'application/json',
        headers: { 'X-HTTP-Method-Override': 'PATCH' },
        data: JSON.stringify(payload), dataType: 'json',
        success: function(res) { showRes('res-update-db', res, res.success); },
        error:   function(xhr) { showRes('res-update-db', { status: xhr.status, error: xhr.responseText }, false); }
    });
}
function apiUpdateKutt() {
    var kuttId = $('#u_kutt_kuttId').val();
    if (!kuttId) { showRes('res-update-kutt', { error: 'kuttId를 입력하세요.' }, false); return; }
    var payload = {
        keepTarget : ($('#u_kutt_keepTarget').val() === 'true'),
        newTarget  : $('#u_kutt_newTarget').val() || null,
        description: $('#u_kutt_desc').val(),
        expireIn   : $('#u_kutt_expireIn').val() || null
    };
    $.ajax({
        url: CTX + '/api/short-url/test/kutt/' + encodeURIComponent(kuttId), type: 'POST',
        contentType: 'application/json',
        headers: { 'X-HTTP-Method-Override': 'PATCH' },
        data: JSON.stringify(payload), dataType: 'json',
        success: function(res) { showRes('res-update-kutt', res, res.success); },
        error:   function(xhr) { showRes('res-update-kutt', { status: xhr.status, error: xhr.responseText }, false); }
    });
}

/* ── 5. 삭제 ────────────────────────────────────────────── */
function apiDeleteDb() {
    var id = $('#d_db_programId').val();
    if (!confirm('[DB] ' + id + ' 를 삭제하시겠습니까?')) return;
    $.ajax({
        url: CTX + '/api/short-url/' + encodeURIComponent(id) + '/delete', type: 'POST', dataType: 'json',
        success: function(res) { showRes('res-delete-db', res, res.success); },
        error:   function(xhr) { showRes('res-delete-db', { status: xhr.status, error: xhr.responseText }, false); }
    });
}
function apiDeleteKutt() {
    var kuttId = $('#d_kutt_kuttId').val();
    if (!kuttId) { showRes('res-delete-kutt', { error: 'kuttId를 입력하세요.' }, false); return; }
    if (!confirm('[KUTT] ' + kuttId + ' 를 삭제하시겠습니까?')) return;
    $.ajax({
        url: CTX + '/api/short-url/test/kutt/' + encodeURIComponent(kuttId) + '/delete', type: 'POST', dataType: 'json',
        success: function(res) { showRes('res-delete-kutt', res, res.success); },
        error:   function(xhr) { showRes('res-delete-kutt', { status: xhr.status, error: xhr.responseText }, false); }
    });
}
</script>
</body>
</html>
