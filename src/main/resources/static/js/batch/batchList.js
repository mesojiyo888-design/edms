/**
 * 배치관리 목록 화면 JS
 *
 * ============================================================
 * useYn(사용여부) 토글 모드 전환 플래그
 * ============================================================
 *   'instant' : 그리드의 사용여부 체크박스를 클릭하는 즉시 PUT toggle API를 호출해서
 *               서버에 바로 반영한다. (컬럼 renderer의 change 이벤트에서 ajax 호출)
 *   'batch'   : 체크박스를 클릭해도 그리드 데이터만 바뀌고 서버 호출은 하지 않는다.
 *               상단 [사용여부 저장] 버튼을 눌러야 변경된 행들을 모아
 *               PUT toggle-bulk API를 한 번에 호출한다.
 *
 * 실제 화면 정책에 맞게 아래 한 줄만 바꾸면 두 모드를 전환할 수 있다.
 */
var USE_YN_MODE = 'instant'; // 'instant' | 'batch'

var BATCH_GRID_ID = 'batchGrid';

/**
 * useYn 체크박스 렌더러
 * - instant 모드: change 즉시 PUT /batch/api/schedules/{jobName}/toggle 호출
 * - batch   모드: 그리드 값만 변경(row.useYn), _dirty 플래그만 세우고 서버 호출 안 함
 */
class UseYnCheckboxRenderer {
    constructor(props) {
        var el = document.createElement('input');
        el.type = 'checkbox';
        el.checked = props.value === 'Y';

        var rowData = props.grid.getRow(props.rowKey);

        el.addEventListener('change', function () {
            var checked = el.checked;
            var newValue = checked ? 'Y' : 'N';
            var jobName = rowData.jobName;

            if (USE_YN_MODE === 'instant') {
                el.disabled = true;
                $.ajax({
                    url: _CONTEXT_PATH + '/batch/api/schedules/' + encodeURIComponent(jobName) + '/toggle',
                    method: 'PUT',
                    success: function (res) {
                        el.disabled = false;
                        if (res && res.success) {
                            props.grid.setValue(props.rowKey, 'useYn', res.useYn || newValue);
                            ComMsg.toast('사용여부가 변경되었습니다.');
                        } else {
                            el.checked = !checked; // 롤백
                            ComMsg.error((res && res.message) || '사용여부 변경에 실패했습니다.');
                        }
                    },
                    error: function () {
                        el.disabled = false;
                        el.checked = !checked; // 롤백
                        ComMsg.error('사용여부 변경 중 오류가 발생했습니다.');
                    }
                });
            } else {
                // batch 모드: 그리드 데이터만 변경, 저장 버튼을 눌러야 서버 반영됨
                props.grid.setValue(props.rowKey, 'useYn', newValue);
                props.grid.setValue(props.rowKey, '_dirty', true);
            }
        });

        this.el = el;
    }
    getElement() {
        return this.el;
    }
    render(props) {
        this.el.checked = props.value === 'Y';
    }
}

/** 실행/이력/수정/삭제 액션 버튼 렌더러 */
class BatchActionRenderer {
    constructor(props) {
        var wrap = document.createElement('span');
        wrap.style.display = 'inline-flex';
        wrap.style.gap = '4px';

        var rowData = props.grid.getRow(props.rowKey);
        var jobName = rowData.jobName;
        var isNative = !rowData.taskletType || rowData.taskletType === 'NATIVE';

        var btnRun = document.createElement('button');
        btnRun.type = 'button';
        btnRun.innerText = '실행';
        btnRun.onclick = function () {
            openRunServerPopup(jobName);
        };

        var btnHistory = document.createElement('button');
        btnHistory.type = 'button';
        btnHistory.innerText = '이력';
        btnHistory.onclick = function () {
            openHistoryPopup(jobName);
        };

        var btnEdit = document.createElement('button');
        btnEdit.type = 'button';
        btnEdit.innerText = '수정';
        btnEdit.onclick = function () {
            openFormPopup(jobName);
        };

        var btnDelete = document.createElement('button');
        btnDelete.type = 'button';
        btnDelete.innerText = '삭제';
        // NATIVE 삭제 제한은 임시로 해제(배치서버도 동일하게 해제됨).
        // NATIVE는 실제 자바 Job 클래스(@Bean)가 이미 배포된 상태라, 삭제해도 코드가 없어지는 게
        // 아니라 스케줄(크론 등록)만 사라진다는 점을 사용자가 인지할 수 있도록 확인 문구에 표시한다.
        btnDelete.onclick = function () {
            var confirmMsg = isNative
                ? '[' + jobName + ']은(는) NATIVE(하드코딩) Job입니다. 삭제해도 배포된 자바 Job 코드는 그대로 남고, 스케줄(크론 등록)만 제거됩니다. 삭제하시겠습니까?'
                : '[' + jobName + '] 스케줄을 삭제하시겠습니까?';

            ComMsg.confirm(confirmMsg, '삭제', function () {
                $.ajax({
                    url: _CONTEXT_PATH + '/batch/api/schedules/' + encodeURIComponent(jobName),
                    method: 'DELETE',
                    success: function (res) {
                        if (res && res.success) {
                            ComMsg.success('삭제되었습니다.', '완료', function () {
                                ToastGrid.search(BATCH_GRID_ID, 1, false);
                            });
                        } else {
                            ComMsg.error((res && res.message) || '삭제에 실패했습니다.');
                        }
                    },
                    error: function () {
                        ComMsg.error('삭제 요청 중 오류가 발생했습니다.');
                    }
                });
            }, { confirmButtonText: '삭제', cancelButtonText: '아니오' });
        };

        wrap.appendChild(btnRun);
        wrap.appendChild(btnHistory);
        wrap.appendChild(btnEdit);
        wrap.appendChild(btnDelete);
        this.el = wrap;
    }
    getElement() {
        return this.el;
    }
    render() {}
}

/**
 * 수동 실행 - 배치서버 선택 팝업
 *
 * 우선순위: 텍스트 직접입력 값이 있으면 그 값 사용, select2에서 선택하면 텍스트 입력값은 지우고
 * select 값을 사용한다. 서버 목록(batch.api.servers)이 비어있으면 텍스트 입력란에 현재(기본)
 * 배치서버 URL이 기본값으로 채워진다.
 */
function openRunServerPopup(jobName) {
    ComMsg.modal({
        title: '[' + jobName + '] 배치 실행',
        width: 460,
        html:
            '<div class="run-server-form">' +
            '  <label for="rs_serverSelect">배치서버 선택</label>' +
            '  <select id="rs_serverSelect" style="width:100%;"><option value="">(목록에서 선택)</option></select>' +
            '  <label for="rs_serverInput">서버 주소 직접입력</label>' +
            '  <input type="text" id="rs_serverInput" placeholder="예: http://10.0.0.11:8081 또는 10.0.0.11" />' +
            '  <div class="hint">직접입력 값이 있으면 그 값이 우선 사용됩니다. select2에서 선택하면 직접입력 값은 자동으로 지워집니다.</div>' +
            '</div>',
        confirmButtonText: '실행',
        showCancelButton: true,
        didOpen: function () {
            $('#rs_serverSelect').select2({
                dropdownParent: $('.swal2-popup:visible'),
                width: '100%'
            });

            $.ajax({
                url: _CONTEXT_PATH + '/batch/api/server-options',
                method: 'GET',
                success: function (res) {
                    var servers = (res && res.servers) || [];
                    var defaultUrl = (res && res.defaultUrl) || '';

                    if (servers.length > 0) {
                        servers.forEach(function (s) {
                            $('#rs_serverSelect').append(
                                $('<option>').val(s.url).text((s.name || s.url) + ' (' + s.url + ')')
                            );
                        });
                        $('#rs_serverSelect').trigger('change');
                    } else {
                        // 등록된 서버 목록이 없으면 현재(기본) 배치서버 주소를 직접입력란 기본값으로 채운다
                        $('#rs_serverInput').val(defaultUrl);
                        $('#rs_serverSelect').prop('disabled', true).trigger('change');
                    }
                },
                error: function () {
                    ComMsg.error('배치서버 목록 조회에 실패했습니다.');
                }
            });

            // select2에서 값을 고르면 직접입력값은 지워서 select 값이 우선되도록
            $('#rs_serverSelect').on('change', function () {
                var selected = $(this).val();
                if (selected) {
                    $('#rs_serverInput').val('');
                }
            });

            // 직접입력란에 값을 타이핑하면 select 선택을 해제해 직접입력이 우선임을 명확히 함
            $('#rs_serverInput').on('input', function () {
                if ($(this).val().trim()) {
                    $('#rs_serverSelect').val('').trigger('change');
                }
            });
        },
        preConfirm: function () {
            var inputVal  = ($('#rs_serverInput').val() || '').trim();
            var selectVal = $('#rs_serverSelect').val() || '';
            // 우선순위: 직접입력 > select
            return inputVal || selectVal || '';
        },
        callback: function (serverUrl) {
            $.ajax({
                url: _CONTEXT_PATH + '/batch/api/jobs/' + encodeURIComponent(jobName) + '/run',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ serverUrl: serverUrl }),
                success: function (res) {
                    if (res && res.success) {
                        ComMsg.success('실행되었습니다. (executionId: ' + res.executionId + ')');
                    } else {
                        ComMsg.error((res && res.message) || '실행에 실패했습니다.');
                    }
                },
                error: function () {
                    ComMsg.error('실행 요청 중 오류가 발생했습니다.');
                }
            });
        }
    });
}

function openFormPopup(jobName) {
    var url = _CONTEXT_PATH + '/batch/form' + (jobName ? ('?jobName=' + encodeURIComponent(jobName)) : '');
    ComMsg.modalUrl(url, {
        title: jobName ? '배치 스케줄 수정' : '배치 스케줄 등록',
        width: 640,
        onLoad: function () {
            // batchFormPopup.jsp 내부에서 저장 성공 시 ComMsg.close(true)를 호출해 이 콜백까지 도달함
        },
        callback: function () {
            ToastGrid.search(BATCH_GRID_ID, 1, false);
        }
    });
}

function openHistoryPopup(jobName) {
    var url = _CONTEXT_PATH + '/batch/history' + (jobName ? ('?jobName=' + encodeURIComponent(jobName)) : '');
    ComMsg.modalUrl(url, {
        title: jobName ? ('[' + jobName + '] 실행 이력') : '전체 실행 이력',
        width: 900
    });
}

$(document).ready(function () {
    var options = {
        bodyHeight: 500,
        pageOptions: { perPage: 20 },
        isInfinite: false,
        rowHeaders: ['checkbox'],
        columns: [
            { header: 'Job명', name: 'jobName', width: 160 },
            { header: 'Cron', name: 'cronExpr', width: 130 },
            {
                header: '사용여부', name: 'useYn', width: 90, align: 'center',
                renderer: { type: UseYnCheckboxRenderer }
            },
            { header: '타입', name: 'taskletType', width: 100 },
            { header: '설정(SQL/Proc)', name: 'taskletConfig', width: 220 },
            { header: '설명', name: 'description', width: 220 },
            { header: '수정자', name: 'modifiedBy', width: 100 },
            {
                header: '관리', name: 'actions', width: 220, align: 'center',
                renderer: { type: BatchActionRenderer }
            },
            // batch(일괄저장) 모드에서 변경된 행을 표시하기 위한 내부 플래그 컬럼 (화면에는 숨김)
            { header: '_dirty', name: '_dirty', hidden: true }
        ]
    };

    ToastGrid.init(BATCH_GRID_ID, _CONTEXT_PATH + '/batch/api/schedules', 'batchForm1', options);

    // batch(일괄저장) 모드일 때만 저장 버튼 노출
    if (USE_YN_MODE === 'batch') {
        $('#btnSaveUseYn').show();
    }

    $('#btnNew').on('click', function () {
        openFormPopup(null);
    });

    $('#btnAllHistory').on('click', function () {
        openHistoryPopup(null);
    });

    $('#btnSaveUseYn').on('click', function () {
        var allData = ToastGrid.getData(BATCH_GRID_ID);
        var dirtyRows = (allData || []).filter(function (row) { return row._dirty; });

        if (!dirtyRows.length) {
            ComMsg.alert('변경된 사용여부가 없습니다.');
            return;
        }

        var items = dirtyRows.map(function (row) {
            return { jobName: row.jobName, useYn: row.useYn };
        });

        ComMsg.confirm(items.length + '건의 사용여부 변경을 저장하시겠습니까?', '저장', function () {
            $.ajax({
                url: _CONTEXT_PATH + '/batch/api/schedules/toggle-bulk',
                method: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(items),
                success: function (res) {
                    if (res && res.success) {
                        ComMsg.success('저장되었습니다.', '완료', function () {
                            ToastGrid.search(BATCH_GRID_ID, 1, false);
                        });
                    } else {
                        ComMsg.error((res && res.message) || '저장에 실패했습니다.');
                    }
                },
                error: function () {
                    ComMsg.error('저장 중 오류가 발생했습니다.');
                }
            });
        });
    });
});
