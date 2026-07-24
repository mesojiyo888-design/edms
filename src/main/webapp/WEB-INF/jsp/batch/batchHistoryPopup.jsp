<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <style>
        #historyGrid {
            width: 100%;
            height: 380px; /* 명시적인 높이값 필수 */
        }
        .batch-hist-search { margin-bottom: 10px; display: flex; gap: 6px; align-items: center; flex-wrap: wrap; }
        .batch-hist-search input[type="text"], .batch-hist-search select { padding: 4px 6px; }
        .error { color: red; font-size: 12px; }
        /* 오류메시지 셀 - 말줄임 처리 + 클릭 가능 표시 (전체 내용은 hover 툴팁 또는 클릭 시 팝업) */
        .err-msg-cell {
            display: block; width: 100%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
            cursor: pointer; color: #c0392b;
        }
        .err-msg-cell:hover { text-decoration: underline; }

        /*
         * 오류메시지 전체보기 - ComMsg.alert()(SweetAlert2) 대신 순수 CSS 오버레이 사용.
         * SweetAlert2는 인스턴스가 1개뿐이라 이력 팝업(모달) 위에서 Swal.fire()를 또 띄우면
         * 기존 Swal(이력 팝업 자체)이 교체돼버려서, 상세보기를 닫을 때 이력 팝업까지 같이 닫혀버린다.
         * 그래서 이 오버레이는 Swal과 무관하게 별도 div로 그려서 이력 팝업 위에 그냥 얹는 방식으로 만든다.
         */
        #h_errDetailOverlay {
            display: none; position: fixed; inset: 0; z-index: 100000;
            align-items: center; justify-content: center;
        }
        #h_errDetailOverlay .err-detail-backdrop {
            position: absolute; inset: 0; background: rgba(0,0,0,0.45);
        }
        #h_errDetailOverlay .err-detail-box {
            position: relative; background: #fff; border-radius: 6px; padding: 20px;
            width: 480px; max-width: 90vw; max-height: 70vh; overflow-y: auto;
            box-shadow: 0 4px 20px rgba(0,0,0,0.3);
        }
        #h_errDetailOverlay .err-detail-header {
            display: flex; justify-content: space-between; align-items: center;
            font-weight: bold; margin-bottom: 10px;
        }
        #h_errDetailOverlay .err-detail-close {
            border: none; background: none; font-size: 18px; cursor: pointer; line-height: 1;
        }
        #h_errDetailOverlay .err-detail-body {
            white-space: pre-wrap; word-break: break-all; font-size: 13px; color: #333;
        }
        #h_errDetailOverlay .err-detail-actions { text-align: right; margin-top: 16px; }
        #h_errDetailOverlay .err-detail-actions button {
            padding: 6px 16px; cursor: pointer;
        }

        .grid-pagination ul.pagination {
            list-style: none;
            margin: 20px 0;
            padding: 0;
            display: flex;
            justify-content: center;
            gap: 4px;
        }
        .grid-pagination ul.pagination li a {
            display: inline-block;
            padding: 6px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            color: #333;
            text-decoration: none;
            background: #fff;
        }
        .grid-pagination ul.pagination li a:hover { background: #f0f0f0; }
        .grid-pagination ul.pagination li.active a {
            background: #3f51b5;
            color: #fff;
            border-color: #3f51b5;
        }
        .grid-pagination ul.pagination li.disabled a {
            color: #ccc;
            cursor: not-allowed;
            border-color: #eee;
        }
    </style>
</head>

<body>
<c:set var="fixedJobName" value="${jobName}" />

<form id="historyForm" onsubmit="return false;">
    <div class="batch-hist-search">
        <c:if test="${not empty fixedJobName}">
            <b>Job명: ${fixedJobName}</b>
            <input type="hidden" id="h_jobName" value="${fixedJobName}" />
        </c:if>
        <%-- 전체 이력조회(jobName 파라미터 없음) 팝업에서만 Job명 검색조건 노출 --%>
        <c:if test="${empty fixedJobName}">
            <label>Job명 <input type="text" id="h_jobNameSearch" name="jobName" placeholder="전체" /></label>
        </c:if>
        <label>시작일
            <input type="text" id="h_startDt" class="datepicker" placeholder="yyyy-MM-dd" readonly style="width:100px;" />
        </label>
        <input type="text" id="h_startTime" placeholder="00:00:00" style="width:70px;" maxlength="8" />
        <input type="hidden" id="h_startDtHidden" name="startDt" />

        <label>종료일
            <input type="text" id="h_endDt" class="datepicker" placeholder="yyyy-MM-dd" readonly style="width:100px;" />
        </label>
        <input type="text" id="h_endTime" placeholder="23:59:59" style="width:70px;" maxlength="8" />
        <input type="hidden" id="h_endDtHidden" name="endDt" />

        <%-- 공통 검색조건: 실행 상태 (BatchStatus + 등록 직후의 커스텀 상태 RUNNING) --%>
        <label>상태
            <select id="h_status" name="status">
                <option value="">전체</option>
                <option value="RUNNING">RUNNING</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="FAILED">FAILED</option>
                <option value="STOPPED">STOPPED</option>
                <option value="ABANDONED">ABANDONED</option>
                <option value="UNKNOWN">UNKNOWN</option>
            </select>
        </label>
        <button type="button" id="h_btnSearch">조회</button>
    </div>
    <div class="error" id="h_dtErrorMsg" style="margin:-6px 0 8px; min-height:14px;"></div>

    <div id="historyGrid"></div>
    <div class="grid-pagination" data-grid="historyGrid"></div>
</form>

<%-- 오류메시지 전체보기 오버레이 (SweetAlert2 미사용 - 상세 설명은 위 스타일 주석 참고) --%>
<div id="h_errDetailOverlay">
    <div class="err-detail-backdrop"></div>
    <div class="err-detail-box">
        <div class="err-detail-header">
            <span>오류 메시지 전체보기</span>
            <button type="button" class="err-detail-close" id="h_errDetailCloseX">&times;</button>
        </div>
        <div class="err-detail-body" id="h_errDetailBody"></div>
        <div class="err-detail-actions">
            <button type="button" id="h_errDetailCloseBtn">확인</button>
        </div>
    </div>
</div>

<script>
(function () {
    var fixedJobName = $('#h_jobName').val() || null;

    // 'yyyy-MM-ddTHH:mm:ss(.SSS)' 형태의 ISO 문자열을 'yyyy-MM-dd HH:mm:ss'로 표시
    function formatDateTime(value) {
        if (!value) return '';
        var s = String(value).replace('T', ' ');
        var dotIdx = s.indexOf('.');
        if (dotIdx > -1) s = s.substring(0, dotIdx); // 밀리초 이하 제거
        return s;
    }

    /**
     * 시작일/종료일 검색조건
     * - 날짜: datepicker-utils.js의 공통 jQuery UI datepicker(setDatepicker), readonly라 달력으로만 선택
     * - 시간: 날짜와 별도의 자유입력 텍스트(HH:mm:ss). 날짜만 readonly로 막아놨기 때문에 시간까지 검색
     *   조건으로 쓰려면 별도 입력란이 필요해서 분리했다.
     * - 미입력 시 기본값: 시작시간 00:00:00 / 종료시간 23:59:59 (날짜만 고르면 그 날 하루 전체를 검색)
     * - 날짜를 아예 선택하지 않으면 해당 조건(시작/종료) 자체를 검색에 안 씀 (시간만 입력해도 무시)
     * - [조회] 클릭 시에만 검증하고, 통과해야 hidden input(name=startDt/endDt)을 채워 실제 검색을 수행한다.
     */
    $('#h_startDt, #h_endDt').setDatepicker();

    var TIME_PATTERN = /^([01]\d|2[0-3]):([0-5]\d):([0-5]\d)$/; // HH:mm:ss (00~23 : 00~59 : 00~59)

    function showDtError(msg) {
        $('#h_dtErrorMsg').text(msg || '');
        return !msg;
    }

    /**
     * 날짜 + 시간을 검증하고 'yyyy-MM-dd HH:mm:ss' 문자열로 합쳐서 반환한다.
     * - 날짜가 비어있으면 null 반환 (해당 조건 미사용)
     * - 시간이 비어있으면 defaultTime 사용, 입력돼 있으면 형식(HH:mm:ss) 검증
     * - 검증 실패 시 예외(문자열 메시지)를 던진다.
     */
    function buildDt(dateVal, timeVal, defaultTime, label) {
        dateVal = (dateVal || '').trim();
        timeVal = (timeVal || '').trim();

        if (!dateVal) {
            if (timeVal) {
                throw label + '을(를) 먼저 선택해야 시간 조건을 사용할 수 있습니다.';
            }
            return null; // 날짜 미입력 -> 이 조건은 검색에서 제외
        }

        if (!timeVal) {
            timeVal = defaultTime;
        } else if (!TIME_PATTERN.test(timeVal)) {
            throw label + ' 시간 형식이 올바르지 않습니다. (예: 09:30:00, 00~23 / 00~59 / 00~59)';
        }

        return dateVal + ' ' + timeVal;
    }

    /** 문자열('yyyy-MM-dd HH:mm:ss')을 비교 가능한 Date로 변환 */
    function parseDt(dtStr) {
        var m = dtStr.match(/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/);
        if (!m) return null;
        return new Date(+m[1], +m[2] - 1, +m[3], +m[4], +m[5], +m[6]);
    }

    /** [조회] 실행 전 검증. 통과하면 hidden input을 채우고 true, 실패하면 에러 표시하고 false */
    function validateAndApplyDtSearch() {
        showDtError('');

        var startDt, endDt;
        try {
            startDt = buildDt($('#h_startDt').val(), $('#h_startTime').val(), '00:00:00', '시작일');
            endDt   = buildDt($('#h_endDt').val(),   $('#h_endTime').val(),   '23:59:59', '종료일');
        } catch (msg) {
            showDtError(msg);
            return false;
        }

        if (startDt && endDt) {
            var startDate = parseDt(startDt);
            var endDate   = parseDt(endDt);
            if (startDate && endDate && startDate.getTime() >= endDate.getTime()) {
                showDtError('시작일시는 종료일시보다 빨라야 합니다.');
                return false;
            }
        }

        $('#h_startDtHidden').val(startDt || '');
        $('#h_endDtHidden').val(endDt || '');
        return true;
    }

    /** 오류메시지 상세보기 오버레이 열기/닫기 (ComMsg.alert 대신 사용하는 이유는 위 CSS 주석 참고) */
    function showErrorDetail(msg) {
        $('#h_errDetailBody').text(msg);
        $('#h_errDetailOverlay').css('display', 'flex');
    }
    function hideErrorDetail() {
        $('#h_errDetailOverlay').hide();
    }
    $('#h_errDetailCloseX, #h_errDetailCloseBtn, #h_errDetailOverlay .err-detail-backdrop').on('click', hideErrorDetail);

    // ────────────────────────────────────────────────────────────────
    // (참고) formatter로도 동일한 걸 만들 수 있음. 값이 고정 포맷/코드값이라
    // 특수문자가 안 섞이는 경우엔 formatter + inline onclick이 더 간단해서 실무에서도 많이 씀.
    // 단, formatter는 리턴한 문자열이 그대로 innerHTML로 꽂히기 때문에, 값에 자유 텍스트
    // (여기서는 예외 메시지 원문처럼 따옴표/HTML 특수문자가 섞일 수 있는 값)가 들어올 수 있다면
    // 반드시 이스케이프 처리를 해야 함. 안 하면 마크업이 깨지거나 XSS로 이어질 수 있음.
    //
    // function escapeHtml(str) {
    //     return String(str)
    //         .replace(/&/g, '&amp;')
    //         .replace(/</g, '&lt;')
    //         .replace(/>/g, '&gt;')
    //         .replace(/"/g, '&quot;')
    //         .replace(/'/g, '&#39;');
    // }
    // {
    //     header: '오류메시지', name: 'errorMsg', width: 200,
    //     formatter: function (props) {
    //         var value = props.value || '';
    //         if (!value) return '';
    //         var escaped = escapeHtml(value);
    //         // onclick에 넘기는 값도 별도로 이스케이프 필요(작은따옴표 이스케이프 등).
    //         // window.showErrorDetail처럼 전역에 노출된 함수만 inline onclick에서 호출 가능.
    //         return '<span class="err-msg-cell" title="' + escaped + '" '
    //              + 'onclick="showErrorDetail(\'' + escaped.replace(/'/g, "\\'") + '\')">'
    //              + escaped + '</span>';
    //     }
    // }
    // ────────────────────────────────────────────────────────────────

    // TODO: 이 렌더러(말줄임 + 클릭 시 상세 오버레이 패턴)가 다른 그리드에서도 필요해지면
    // 공통 JS(toastGrid.js 등)로 빼서 재사용하는 걸 고려. 지금은 이 화면 전용(#h_errDetailOverlay
    // DOM 구조와 showErrorDetail 함수에 의존)이라 그대로 둠.
    /** 오류메시지 렌더러 - 말줄임 표시, 클릭 시 전체 내용을 오버레이로 확인 (hover 툴팁도 함께 제공) */
    class ErrorMsgRenderer {
        constructor(props) {
            var el = document.createElement('span');
            el.className = 'err-msg-cell';
            this.el = el;
            this.render(props);
        }
        render(props) {
            var value = props.value || '';
            this.el.textContent = value;
            this.el.title = value; // hover 시 브라우저 기본 툴팁으로 전체 내용 노출
            this.el.onclick = function () {
                if (!value) return;
                showErrorDetail(value);
            };
        }
        getElement() { return this.el; }
    }

    var options = {
        bodyHeight: 340,
        pageOptions: { perPage: 10 }, // 추후 사용자 설정화면에서 동적으로 바꿀 값 (testGridList.jsp와 동일한 커스텀 페이징 방식)
        isInfinite: false,
        columns: [
            { header: 'Job명', name: 'jobName', width: 140 },
            { header: 'Step명', name: 'stepName', width: 120 },
            { header: '트리거', name: 'triggerType', width: 90 },
            { header: '실행자', name: 'triggeredBy', width: 100 },
            { header: '상태', name: 'status', width: 90 },
            { header: '시작시각', name: 'startTime', width: 150, formatter: function (obj) { return formatDateTime(obj.value); } },
            { header: '종료시각', name: 'endTime', width: 150, formatter: function (obj) { return formatDateTime(obj.value); } },
            { header: 'InstanceId', name: 'instanceId', width: 120 },
            { header: 'ExecutionId', name: 'executionId', width: 100 },
            {
                header: '오류메시지', name: 'errorMsg', width: 200,
                renderer: { type: ErrorMsgRenderer }
            }
        ]
    };

    var dataUrl = fixedJobName
        ? (_CONTEXT_PATH + '/batch/api/jobs/' + encodeURIComponent(fixedJobName) + '/history')
        : (_CONTEXT_PATH + '/batch/api/history');

    ToastGrid.init('historyGrid', dataUrl, 'historyForm', options);

    function doSearch() {
        if (!validateAndApplyDtSearch()) return;
        ToastGrid.search('historyGrid', 1, false);
    }

    $('#h_btnSearch').on('click', doSearch);

    // Job명 입력 후 Enter → 조회 (전체 이력조회 팝업에서만 노출되는 필드라 존재할 때만 바인딩됨)
    $('#h_jobNameSearch').on('keydown', function (e) {
        if (e.key === 'Enter' || e.keyCode === 13) {
            e.preventDefault();
            doSearch();
        }
    });
})();
</script>
</body>
</html>
