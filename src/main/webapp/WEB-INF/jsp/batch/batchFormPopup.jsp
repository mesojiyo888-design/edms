<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <style>
        #batchFormTable { width: 100%; border-collapse: collapse; text-align: left; }
        #batchFormTable th { width: 130px; padding: 6px 8px; background: #f5f5f5; }
        #batchFormTable td { padding: 6px 8px; }
        #batchFormTable input[type="text"], #batchFormTable select, #batchFormTable textarea {
            width: 100%; box-sizing: border-box; padding: 4px 6px;
        }
        .batch-form-actions { margin-top: 14px; text-align: right; }
        .batch-form-actions button { padding: 6px 16px; margin-left: 6px; cursor: pointer; }
        /* /test/commonValidator 예시와 동일한 에러 메시지 스타일 */
        .error { color: red; font-size: 12px; margin-top: 2px; min-height: 14px; }
        .cron-guide {
            margin-top: 8px; padding: 8px 10px; background: #f5f8ff; border: 1px solid #dbe4ff;
            border-radius: 4px; font-size: 12px; color: #333;
        }
        .cron-guide table { width: 100%; border-collapse: collapse; margin-top: 4px; }
        .cron-guide th, .cron-guide td { padding: 3px 6px; text-align: left; border-bottom: 1px solid #e5e9f5; }
        .cron-guide th { color: #555; font-weight: normal; width: 45%; }
    </style>
</head>

<body>
<%-- jobName이 있으면 수정 모드(readonly), 없으면 신규 등록 --%>
<c:set var="isEdit" value="${not empty jobName}" />

<div id="batchFormWrap">
    <input type="hidden" id="bf_isEdit" value="${isEdit}" />
    <table id="batchFormTable">
        <tr>
            <th>Job명 <span style="color:red;">*</span></th>
            <td>
                <input type="text" id="bf_jobName" value="${jobName}" ${isEdit ? 'readonly' : ''} placeholder="영문/숫자 Job 이름" />
                <div class="error" id="bf_jobNameMsg"></div>
            </td>
        </tr>
        <tr>
            <th>Cron 표현식 <span style="color:red;">*</span></th>
            <td>
                <input type="text" id="bf_cronExpr" placeholder="예: 0 0 3 * * *" />
                <div class="error" id="bf_cronExprMsg"></div>
                <div class="cron-guide">
                    초 분 시 일 월 요일 (6필드, 공백으로 구분)
                    <table>
                        <tr><th>0 0 2 * * *</th><td>매일 02:00</td></tr>
                        <tr><th>0 30 1 * * *</th><td>매일 01:30</td></tr>
                        <tr><th>0 0 6 * * MON</th><td>매주 월요일 06:00</td></tr>
                        <tr><th>0 0/30 * * * *</th><td>30분마다</td></tr>
                        <tr><th>0 0 9-18 * * MON-FRI</th><td>평일 9시~18시 매시</td></tr>
                    </table>
                </div>
            </td>
        </tr>
        <tr>
            <th>타입</th>
            <td>
                <select id="bf_taskletType">
                    <option value="NATIVE" ${isEdit ? '' : 'selected'}>NATIVE (자바 Job 클래스)</option>
                    <option value="SQL">SQL</option>
                    <option value="PROCEDURE">PROCEDURE</option>
                </select>
                <div class="cron-guide" id="bf_typeGuide"></div>
            </td>
        </tr>
        <tr>
            <th>설정(SQL/Proc) <span style="color:red;" id="bf_taskletConfigRequired">*</span></th>
            <td>
                <textarea id="bf_taskletConfig" rows="4" placeholder=""></textarea>
                <div class="error" id="bf_taskletConfigMsg"></div>
            </td>
        </tr>
        <tr>
            <th>설명</th>
            <td><input type="text" id="bf_description" placeholder="목적, 담당자, 주의사항 등" /></td>
        </tr>
        <tr>
            <th>사용여부</th>
            <td>
                <label><input type="radio" name="bf_useYn" value="Y" checked /> 사용</label>
                <label style="margin-left:12px;"><input type="radio" name="bf_useYn" value="N" /> 미사용</label>
            </td>
        </tr>
    </table>

    <div class="batch-form-actions">
        <button type="button" id="bf_btnSave">저장</button>
        <button type="button" id="bf_btnCancel">취소</button>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/common/commonValidator.js"></script>
<script>
(function () {
    var isEdit = $('#bf_isEdit').val() === 'true';
    var jobName = $('#bf_jobName').val();

    // /test/commonValidator 예시와 동일한 방식의 클라이언트단 필수값 검증
    // (taskletConfig는 타입(SQL/PROCEDURE/NATIVE)에 따라 필수 여부가 달라져서
    //  고정 스키마가 아니라 저장 시점에 buildValidatorSchema()로 동적으로 구성한다)
    function buildValidatorSchema() {
        var type = $('#bf_taskletType').val();
        var configRequired = (type === 'SQL' || type === 'PROCEDURE');
        return [
            { id: 'bf_jobName',       type: 'alphanumeric', opts: { required: true, min: 1, max: 100 }, msgId: 'bf_jobNameMsg' },
            { id: 'bf_cronExpr',      type: 'cron',          opts: { required: true }, msgId: 'bf_cronExprMsg' },
            { id: 'bf_taskletConfig', type: 'text',          opts: { required: configRequired, min: 1 }, msgId: 'bf_taskletConfigMsg' }
        ];
    }

    var TYPE_GUIDE = {
        SQL:       'DB에 별도로 만들어둘 필요 없이, 실행할 SQL문 원문을 그대로 입력합니다. INSERT/UPDATE/DELETE 등 실행형 SQL만 가능하고 SELECT문은 지원하지 않습니다. (설정값 필수)',
        PROCEDURE: 'DB에 미리 생성해 둔 프로시저의 이름을 입력합니다. 패키지 소속이면 "패키지명.프로시저명" 형식으로 입력하세요. ⚠ 파라미터(IN/OUT 인자)가 없는 프로시저만 지원합니다. (설정값 필수)',
        NATIVE:    '이미 배포된 자바 Job 클래스가 있어야 합니다. @Bean 이름을 Job명과 동일하게 맞춰서 배포한 뒤 등록하세요. 설정값은 사용하지 않습니다.'
    };

    var TYPE_PLACEHOLDER = {
        SQL:       "예: UPDATE DOC_TB SET STATUS='CLOSED' WHERE STATUS='PENDING'",
        PROCEDURE: '예: PROC_SYNC_DOC  (패키지 소속이면 예: PKG_BATCH.PROC_SYNC_DOC)',
        NATIVE:    ''
    };

    function applyTaskletTypeUI() {
        var type = $('#bf_taskletType').val();
        $('#bf_typeGuide').text(TYPE_GUIDE[type] || '');
        $('#bf_taskletConfig').attr('placeholder', TYPE_PLACEHOLDER[type] || '');

        var configRequired = (type === 'SQL' || type === 'PROCEDURE');
        $('#bf_taskletConfigRequired').toggle(configRequired);
        $('#bf_taskletConfig').prop('disabled', type === 'NATIVE');
        if (type === 'NATIVE') {
            $('#bf_taskletConfig').val('');
            $('#bf_taskletConfigMsg').text('');
        }
    }

    $('#bf_taskletType').on('change', applyTaskletTypeUI);

    // 수정모드면 기존 데이터 조회해서 채워넣기 (목록 전체 조회에서 해당 jobName 필터)
    if (isEdit && jobName) {
        $.ajax({
            url: _CONTEXT_PATH + '/batch/api/schedules',
            method: 'GET',
            success: function (res) {
                var list = (res && res.dataList) || [];
                var found = list.filter(function (row) { return row.jobName === jobName; })[0];
                if (found) {
                    $('#bf_cronExpr').val(found.cronExpr || '');
                    $('#bf_taskletType').val(found.taskletType || 'NATIVE');
                    $('#bf_taskletConfig').val(found.taskletConfig || '');
                    $('#bf_description').val(found.description || '');
                    $('input[name="bf_useYn"][value="' + (found.useYn || 'Y') + '"]').prop('checked', true);
                }
                applyTaskletTypeUI();
            }
        });
    }

    applyTaskletTypeUI();

    $('#bf_btnCancel').on('click', function () {
        ComMsg.close(false);
    });

    $('#bf_btnSave').on('click', function () {
        var vo = {
            jobName: $('#bf_jobName').val() ? $('#bf_jobName').val().trim() : '',
            cronExpr: $('#bf_cronExpr').val() ? $('#bf_cronExpr').val().trim() : '',
            taskletType: $('#bf_taskletType').val(),
            taskletConfig: $('#bf_taskletConfig').val(),
            description: $('#bf_description').val(),
            useYn: $('input[name="bf_useYn"]:checked').val()
        };

        // 클라이언트단 필수값 검증 (/test/commonValidator 예시와 동일한 validateForm 사용)
        var validation = validateForm(buildValidatorSchema());
        if (!validation.isValid) {
            return;
        }

        var url = _CONTEXT_PATH + '/batch/api/schedules';
        var method = 'POST';
        if (isEdit) {
            url = _CONTEXT_PATH + '/batch/api/schedules/' + encodeURIComponent(jobName);
            method = 'PUT';
        }

        $.ajax({
            url: url,
            method: method,
            contentType: 'application/json',
            data: JSON.stringify(vo),
            success: function (res) {
                if (res && res.success) {
                    // 팝업(swal) 안에서 다시 toast/alert를 띄우면 중첩되므로 바로 닫고
                    // 목록 화면(batchList.js)의 modalUrl callback에서 그리드만 재조회한다.
                    ComMsg.close(true);
                } else {
                    ComMsg.error((res && res.message) || '저장에 실패했습니다.');
                }
            },
            error: function () {
                ComMsg.error('저장 중 오류가 발생했습니다.');
            }
        });
    });
})();
</script>
</body>
</html>
