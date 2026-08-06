<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
    단축 URL 공통 모달 (shortUrlModal.jsp)
    위치: /WEB-INF/jsp/shortapi/shortUrlModal.jsp

    [사용법] 단축 URL이 필요한 어느 JSP에서나 아래처럼 include:

    <%@ include file="/WEB-INF/jsp/shortapi/shortUrlModal.jsp" %>

    또는 (동적 include):
    <jsp:include page="/WEB-INF/jsp/shortapi/shortUrlModal.jsp"/>

    [호출 방법 - JavaScript]
    // 단축 URL 모달 열기
    ShortUrl.open({
        programId  : 'MENU_001',                      // 필수: 프로그램 식별자
        programUrl : location.href,                   // 필수: 단축할 URL (현재 페이지)
        description: '페이지 설명',                    // 선택
        onSuccess  : function(shortUrl) {             // 선택: 생성 성공 콜백
            console.log('생성된 단축 URL:', shortUrl);
        }
    });
--%>

<!-- ============================================================
     단축 URL 모달 HTML
============================================================ -->
<div id="shortUrlModal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%;
     background:rgba(0,0,0,0.5); z-index:9999; justify-content:center; align-items:center;">
    <div style="background:#fff; border-radius:8px; width:520px; max-width:95%; padding:28px 30px;
                box-shadow:0 8px 32px rgba(0,0,0,0.18); position:relative;">

        <!-- 헤더 -->
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
            <h3 style="margin:0; font-size:16px; font-weight:700; color:#222;">&#128279; 단축 URL 관리</h3>
            <button onclick="ShortUrl.close()"
                    style="background:none; border:none; font-size:20px; cursor:pointer; color:#888;">&#x2715;</button>
        </div>

        <!-- 탭 -->
        <div id="suTabBar" style="display:flex; border-bottom:2px solid #eee; margin-bottom:20px;">
            <button class="su-tab active" onclick="ShortUrl.switchTab('view')"
                    style="padding:8px 18px; border:none; background:none; cursor:pointer; font-size:13px;
                           font-weight:600; color:#1a73e8; border-bottom:2px solid #1a73e8; margin-bottom:-2px;">
                조회/생성
            </button>
            <button class="su-tab" onclick="ShortUrl.switchTab('edit')"
                    style="padding:8px 18px; border:none; background:none; cursor:pointer; font-size:13px;
                           color:#888;">
                수정
            </button>
            <button class="su-tab" onclick="ShortUrl.switchTab('delete')"
                    style="padding:8px 18px; border:none; background:none; cursor:pointer; font-size:13px;
                           color:#888;">
                삭제
            </button>
        </div>

        <!-- ==================== 탭: 조회/생성 ==================== -->
        <div id="suTab_view">
            <div style="margin-bottom:14px;">
                <label style="font-size:12px; color:#666; display:block; margin-bottom:4px;">원본 URL</label>
                <input id="suProgramUrl" type="text" readonly
                       style="width:100%; padding:8px 10px; border:1px solid #ddd; border-radius:4px;
                              font-size:13px; background:#f9f9f9; box-sizing:border-box;" />
            </div>
            <div style="margin-bottom:14px;">
                <label style="font-size:12px; color:#666; display:block; margin-bottom:4px;">설명 (선택)</label>
                <input id="suDescription" type="text" placeholder="단축 URL에 대한 설명을 입력하세요"
                       style="width:100%; padding:8px 10px; border:1px solid #ddd; border-radius:4px;
                              font-size:13px; box-sizing:border-box;" />
            </div>
            <div style="margin-bottom:20px;">
                <label style="font-size:12px; color:#666; display:block; margin-bottom:4px;">
                    만료 기간 (선택 — 미입력 시 만료 없음)
                </label>
                <div style="display:flex; gap:8px;">
                    <input id="suExpireNum" type="number" min="1" placeholder="숫자"
                           style="width:80px; padding:8px 10px; border:1px solid #ddd; border-radius:4px; font-size:13px;" />
                    <select id="suExpireUnit"
                            style="flex:1; padding:8px 10px; border:1px solid #ddd; border-radius:4px; font-size:13px;">
                        <option value="minutes">분 (minutes)</option>
                        <option value="hours">시간 (hours)</option>
                        <option value="days" selected>일 (days)</option>
                        <option value="weeks">주 (weeks)</option>
                        <option value="months">월 (months)</option>
                    </select>
                </div>
            </div>

            <!-- 결과 표시 영역 -->
            <div id="suResultArea" style="display:none; background:#f0f7ff; border:1px solid #c2d9f5;
                  border-radius:6px; padding:14px; margin-bottom:16px;">
                <div style="font-size:12px; color:#555; margin-bottom:6px;">생성된 단축 URL</div>
                <div style="display:flex; align-items:center; gap:8px;">
                    <input id="suShortUrlResult" type="text" readonly
                           style="flex:1; padding:7px 10px; border:1px solid #b0c9e8; border-radius:4px;
                                  font-size:13px; font-weight:600; background:#fff; color:#1a73e8;" />
                    <button onclick="ShortUrl.copyToClipboard()"
                            style="padding:7px 14px; background:#1a73e8; color:#fff; border:none;
                                   border-radius:4px; font-size:12px; cursor:pointer; white-space:nowrap;">
                        복사
                    </button>
                </div>
            </div>

            <div style="display:flex; gap:8px; justify-content:flex-end;">
                <button onclick="ShortUrl.load()"
                        style="padding:9px 20px; background:#6c757d; color:#fff; border:none;
                               border-radius:4px; font-size:13px; cursor:pointer;">
                    기존 조회
                </button>
                <button onclick="ShortUrl.create()"
                        style="padding:9px 20px; background:#1a73e8; color:#fff; border:none;
                               border-radius:4px; font-size:13px; cursor:pointer; font-weight:600;">
                    단축 URL 생성
                </button>
            </div>
        </div>

        <!-- ==================== 탭: 수정 ==================== -->
        <div id="suTab_edit" style="display:none;">
            <div style="margin-bottom:14px;">
                <label style="font-size:12px; color:#666; display:block; margin-bottom:4px;">
                    URL 유지 여부
                </label>
                <div style="display:flex; gap:20px; align-items:center;">
                    <label style="font-size:13px; cursor:pointer;">
                        <input type="radio" name="suKeepTarget" value="true" checked /> 원본 URL 유지
                    </label>
                    <label style="font-size:13px; cursor:pointer;">
                        <input type="radio" name="suKeepTarget" value="false" /> 새 URL로 변경
                    </label>
                </div>
            </div>
            <div id="suNewTargetArea" style="margin-bottom:14px; display:none;">
                <label style="font-size:12px; color:#666; display:block; margin-bottom:4px;">새 대상 URL</label>
                <input id="suNewTarget" type="text" placeholder="변경할 URL 입력"
                       style="width:100%; padding:8px 10px; border:1px solid #ddd; border-radius:4px;
                              font-size:13px; box-sizing:border-box;" />
            </div>
            <div style="margin-bottom:14px;">
                <label style="font-size:12px; color:#666; display:block; margin-bottom:4px;">설명 수정</label>
                <input id="suEditDescription" type="text" placeholder="설명 입력"
                       style="width:100%; padding:8px 10px; border:1px solid #ddd; border-radius:4px;
                              font-size:13px; box-sizing:border-box;" />
            </div>
            <div style="margin-bottom:20px;">
                <label style="font-size:12px; color:#666; display:block; margin-bottom:4px;">
                    만료 기간 수정 (미입력 시 만료 제거)
                </label>
                <div style="display:flex; gap:8px;">
                    <input id="suEditExpireNum" type="number" min="1" placeholder="숫자"
                           style="width:80px; padding:8px 10px; border:1px solid #ddd; border-radius:4px; font-size:13px;" />
                    <select id="suEditExpireUnit"
                            style="flex:1; padding:8px 10px; border:1px solid #ddd; border-radius:4px; font-size:13px;">
                        <option value="minutes">분 (minutes)</option>
                        <option value="hours">시간 (hours)</option>
                        <option value="days" selected>일 (days)</option>
                        <option value="weeks">주 (weeks)</option>
                        <option value="months">월 (months)</option>
                    </select>
                </div>
            </div>
            <div style="display:flex; justify-content:flex-end;">
                <button onclick="ShortUrl.update()"
                        style="padding:9px 20px; background:#f59e0b; color:#fff; border:none;
                               border-radius:4px; font-size:13px; cursor:pointer; font-weight:600;">
                    수정 저장
                </button>
            </div>
        </div>

        <!-- ==================== 탭: 삭제 ==================== -->
        <div id="suTab_delete" style="display:none;">
            <div style="background:#fff4f4; border:1px solid #fecaca; border-radius:6px;
                  padding:16px; margin-bottom:20px;">
                <p style="margin:0 0 8px; font-size:13px; color:#dc2626; font-weight:600;">&#9888; 삭제 주의사항</p>
                <p style="margin:0; font-size:12px; color:#7f1d1d; line-height:1.6;">
                    삭제 시 Kutt 서버와 자체 DB에서 모두 제거됩니다.<br />
                    삭제된 단축 URL로 접근하면 <strong>404 오류</strong>가 발생합니다.<br />
                    삭제 후 복구가 불가능합니다.
                </p>
            </div>
            <div id="suDeleteInfo" style="background:#f9f9f9; border-radius:6px; padding:12px;
                  margin-bottom:20px; font-size:13px; color:#555; display:none;">
                <!-- 삭제 대상 정보 표시 -->
            </div>
            <div style="display:flex; gap:8px; justify-content:flex-end;">
                <button onclick="ShortUrl.close()"
                        style="padding:9px 20px; background:#6c757d; color:#fff; border:none;
                               border-radius:4px; font-size:13px; cursor:pointer;">
                    취소
                </button>
                <button onclick="ShortUrl.delete()"
                        style="padding:9px 20px; background:#dc2626; color:#fff; border:none;
                               border-radius:4px; font-size:13px; cursor:pointer; font-weight:600;">
                    삭제 확인
                </button>
            </div>
        </div>

        <!-- 로딩/메시지 영역 -->
        <div id="suMessage" style="display:none; margin-top:12px; padding:10px 12px; border-radius:4px;
              font-size:13px; text-align:center;"></div>
    </div>
</div>

<!-- ============================================================
     단축 URL JavaScript 유틸리티 (ShortUrl 네임스페이스)
============================================================ -->
<script type="text/javascript">
var ShortUrl = (function () {

    var _programId  = '';
    var _programUrl = '';
    var _onSuccess  = null;
    var _contextPath = '<%=request.getContextPath()%>';

    var BASE_URL = _contextPath + '/api/short-url';

    // ============================================================
    // 공개 API
    // ============================================================

    /**
     * 모달 열기
     * @param {Object} options
     *   programId  {string} 필수 - 프로그램 식별자
     *   programUrl {string} 필수 - 단축할 원본 URL
     *   description{string} 선택
     *   onSuccess  {function} 선택 - 생성 성공 콜백(shortUrl 전달)
     */
    function open(options) {
        _programId  = options.programId  || '';
        _programUrl = options.programUrl || location.href;
        _onSuccess  = options.onSuccess  || null;

        $('#suProgramUrl').val(_programUrl);
        $('#suDescription').val(options.description || '');
        $('#suExpireNum').val('');
        $('#suResultArea').hide();
        _hideMessage();

        // 기존 등록 여부 확인 후 표시
        _loadExisting(function (data) {
            if (data && data.shortUrl) {
                _showResult(data.shortUrl);
            }
        });

        // 삭제 탭: 대상 정보 표시
        if (_programId) {
            $('#suDeleteInfo').html(
                '<b>프로그램 ID:</b> ' + _escHtml(_programId) +
                '<br/><b>원본 URL:</b> ' + _escHtml(_programUrl)
            ).show();
        }

        switchTab('view');
        $('#shortUrlModal').css('display', 'flex');
    }

    function close() {
        $('#shortUrlModal').hide();
    }

    function switchTab(tab) {
        $('#suTab_view, #suTab_edit, #suTab_delete').hide();
        $('#suTab_' + tab).show();
        $('.su-tab').css({ 'color': '#888', 'border-bottom': 'none', 'font-weight': 'normal' });
        $('.su-tab[onclick="ShortUrl.switchTab(\'' + tab + '\')"]').css({
            'color': '#1a73e8',
            'border-bottom': '2px solid #1a73e8',
            'margin-bottom': '-2px',
            'font-weight': '600'
        });
        _hideMessage();
    }

    // ── 조회 ─────────────────────────────────────────────────

    function load() {
        if (!_programId) { _showMsg('programId가 없습니다.', 'error'); return; }
        _loadExisting(function (data) {
            if (data && data.shortUrl) {
                _showResult(data.shortUrl);
                _showMsg('기존 단축 URL을 불러왔습니다.', 'success');
            } else {
                _showMsg('등록된 단축 URL이 없습니다. 새로 생성해주세요.', 'info');
            }
        });
    }

    // ── 생성 ─────────────────────────────────────────────────

    function create() {
        if (!_programId) { _showMsg('programId가 없습니다.', 'error'); return; }
        if (!_programUrl) { _showMsg('programUrl이 없습니다.', 'error'); return; }

        var expireIn = _buildExpireIn($('#suExpireNum').val(), $('#suExpireUnit').val());

        _ajax('POST', BASE_URL + '/create', {
            programId  : _programId,
            programUrl : _programUrl,
            description: $('#suDescription').val(),
            expireIn   : expireIn
        }, function (res) {
            if (res.success) {
                _showResult(res.shortUrl);
                _showMsg('단축 URL이 생성되었습니다.', 'success');
                if (typeof _onSuccess === 'function') _onSuccess(res.shortUrl);
            } else {
                _showMsg(res.message || '생성 실패', 'error');
            }
        });
    }

    // ── 수정 ─────────────────────────────────────────────────

    function update() {
        if (!_programId) { _showMsg('programId가 없습니다.', 'error'); return; }

        var keepTarget = $('input[name="suKeepTarget"]:checked').val() === 'true';
        var expireIn   = _buildExpireIn($('#suEditExpireNum').val(), $('#suEditExpireUnit').val());
        var payload    = {
            keepTarget  : keepTarget,
            description : $('#suEditDescription').val(),
            expireIn    : expireIn
        };
        if (!keepTarget) {
            payload.newTarget = $('#suNewTarget').val();
        }

        _ajax('PATCH', BASE_URL + '/' + _programId, payload, function (res) {
            if (res.success) {
                _showMsg('수정이 완료되었습니다.', 'success');
                switchTab('view');
                _loadExisting(function(data) {
                    if (data && data.shortUrl) _showResult(data.shortUrl);
                });
            } else {
                _showMsg(res.message || '수정 실패', 'error');
            }
        });
    }

    // ── 삭제 ─────────────────────────────────────────────────

    function deleteUrl() {
        if (!_programId) { _showMsg('programId가 없습니다.', 'error'); return; }
        if (!confirm('단축 URL을 삭제하면 복구할 수 없습니다. 삭제하시겠습니까?')) return;

        $.ajax({
            url        : BASE_URL + '/' + _programId + '/delete',
            type       : 'POST',
            contentType: 'application/json',
            dataType   : 'json',
            success    : function (res) {
                if (res.success) {
                    _showMsg('삭제가 완료되었습니다.', 'success');
                    $('#suResultArea').hide();
                    switchTab('view');
                } else {
                    _showMsg(res.message || '삭제 실패', 'error');
                }
            },
            error: function () { _showMsg('서버 오류가 발생했습니다.', 'error'); }
        });
    }

    // ── 클립보드 복사 ─────────────────────────────────────────

    function copyToClipboard() {
        var val = $('#suShortUrlResult').val();
        if (!val) return;
        if (navigator.clipboard) {
            navigator.clipboard.writeText(val).then(function () {
                _showMsg('클립보드에 복사되었습니다!', 'success');
            });
        } else {
            $('#suShortUrlResult').select();
            document.execCommand('copy');
            _showMsg('클립보드에 복사되었습니다!', 'success');
        }
    }

    // ============================================================
    // 내부 유틸
    // ============================================================

    function _loadExisting(callback) {
        if (!_programId) { callback(null); return; }
        $.ajax({
            url     : BASE_URL + '/' + _programId,
            type    : 'GET',
            dataType: 'json',
            success : function (res) { callback(res.success ? res : null); },
            error   : function () { callback(null); }
        });
    }

    function _ajax(method, url, data, callback) {
        // PATCH는 일부 환경 미지원 → POST + X-HTTP-Method-Override
        var actualMethod = (method === 'PATCH') ? 'POST' : method;
        var headers = { 'Content-Type': 'application/json' };
        if (method === 'PATCH') headers['X-HTTP-Method-Override'] = 'PATCH';

        $.ajax({
            url        : url,
            type       : actualMethod,
            contentType: 'application/json',
            headers    : headers,
            data       : JSON.stringify(data),
            dataType   : 'json',
            success    : callback,
            error      : function () { _showMsg('서버 오류가 발생했습니다.', 'error'); }
        });
    }

    function _buildExpireIn(num, unit) {
        if (!num || num <= 0) return null;
        return num + ' ' + unit;
    }

    function _showResult(shortUrl) {
        $('#suShortUrlResult').val(shortUrl);
        $('#suResultArea').show();
    }

    function _showMsg(msg, type) {
        var colors = {
            success: { bg: '#f0fdf4', border: '#bbf7d0', color: '#15803d' },
            error  : { bg: '#fef2f2', border: '#fecaca', color: '#dc2626' },
            info   : { bg: '#eff6ff', border: '#bfdbfe', color: '#1d4ed8' }
        };
        var c = colors[type] || colors['info'];
        $('#suMessage')
            .css({ background: c.bg, border: '1px solid ' + c.border, color: c.color })
            .text(msg)
            .show();
        setTimeout(function () { $('#suMessage').fadeOut(); }, 3500);
    }

    function _hideMessage() { $('#suMessage').hide(); }

    function _escHtml(str) {
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    // keepTarget 라디오 변경 시 새 URL 입력 필드 토글
    $(document).on('change', 'input[name="suKeepTarget"]', function () {
        if ($(this).val() === 'false') {
            $('#suNewTargetArea').show();
        } else {
            $('#suNewTargetArea').hide();
        }
    });

    // 모달 외부 클릭 시 닫기
    $(document).on('click', '#shortUrlModal', function (e) {
        if ($(e.target).is('#shortUrlModal')) close();
    });

    return {
        open           : open,
        close          : close,
        switchTab      : switchTab,
        load           : load,
        create         : create,
        update         : update,
        'delete'       : deleteUrl,
        copyToClipboard: copyToClipboard
    };

})();
</script>
