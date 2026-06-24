/**
 * ToastGrid Wrapper for eGovFrame
 * - eGovFrame의 paginationInfo 구조에 맞춰 페이징 버튼 렌더링
 * - contextMenu 동적 설정 지원 (활성화/비활성화 및 메뉴 병합)
 * - 무한스크롤과 일반 페이징 모드 토글 지원
 * - 그리드별 인스턴스 관리로 다수의 그리드 동시 사용 가능
 *
 * 사용예시:
 * ① 기본예제
 * ToastGrid.init('myGrid', _CONTEXT_PATH + '/data/url', 'searchForm', {
 *     columns: [...],
 *     isInfinite: false,
 *     pageOptions: { perPage: 20 },
 *     contextMenuItems: [
 *         [{ name: 'copy', label: '복사', action: fn }],
 *         [{ name: 'delete', label: '삭제', action: fn }]
 *     ],
 *     refreshInterval: 5 // 5분마다 자동 새로고침
 * });
 * ② init 시부터 메뉴 지정
 * ToastGrid.init('myGrid', '/api/list', 'searchForm', {
 *     contextMenuItems: [
 *         [{ name: 'copy',   label: '복사',   action: function(ev) { ... } }],
 *         [{ name: 'delete', label: '삭제',   action: function(ev) { ... } }]
 *     ],
 *     columns: [ ... ]
 * });
 *
 * ③ contextMenu 동적 설정 (권한에 따른 메뉴 활성화/비활성화 등)
 * ToastGrid.setContextMenu('myGrid', [                 // 추가 활성화
 *     [{ name: 'copy',   label: '복사',   action: fn }],
 *     [{ name: 'delete', label: '삭제',   action: fn }]
 * ]);
 *
 * ④ contextMenu 비활성화 (권한에 따른 메뉴 설정 제거)
 * ToastGrid.setContextMenu('myGrid');                  // 비활성화
 * ToastGrid.setContextMenu('myGrid', null);            // 비활성화
 */

var ToastGrid = (function() {
    //  마스크 포맷 유틸
    // pattern: '9' = 숫자, 'A' = 영문, '*' = 숫자+영문 , 나머지는 구분자로 처리
    var _applyMask = function(value, pattern) {
        if (!value) return '';
        var str = String(value).replace(/[^a-zA-Z0-9]/g, ''); // 숫자+영문만 추출
        var result = '';
        var si = 0; // str 인덱스

        for (var pi = 0; pi < pattern.length; pi++) {
            if (si >= str.length) break;
            var pc = pattern[pi];

            if (pc === '9') {
                if (/[0-9]/.test(str[si])) result += str[si++];
                else break;
            } else if (pc === 'A') {
                if (/[a-zA-Z]/.test(str[si])) result += str[si++];
                else break;
            } else if (pc === '*') {
                result += str[si++];
            } else {
                // 구분자 자동 삽입
                result += pc;
            }
        }
        return result;
    };

    var instances = {};

    return {
        init: function(gridId, dataUrl, formId, options) {
            var el = document.getElementById(gridId);
            var perPage = options.pageOptions ? (options.pageOptions.perPage || 10) : 10;

            // 내장 페이징 완전 제거
            delete options.pageOptions;
            delete options.dataSource;

            // contextMenu 기본 비활성화
            // 활성화하려면 options.contextMenuItems 배열 전달
            // 미전달 시 항상 null (우클릭 메뉴 없음)
            options.contextMenu = options.contextMenuItems ? options.contextMenuItems : null;
            delete options.contextMenuItems;

            options.el = el;
            options.perPage = perPage;

            var grid = new tui.Grid(options);
            instances[gridId] = {
                grid: grid,
                dataUrl: dataUrl,
                formId: formId,
                options: options,
                currentPage: 1
            };

            $(document).on('mousedown.tuiFinish_' + gridId, function(e) {
                if (!$('.tui-grid-layer-editing').length) return; // 편집 중인 셀 없으면 무시
                if ($(e.target).closest('#' + gridId).length) return; // 그리드 내부 클릭 무시
                if ($(e.target).closest('.tui-datepicker').length) return; //  body에 붙은 TUI 달력 영역 클릭도 무시

                $('.tui-grid-layer-editing').find('input, textarea, select').blur();
                grid.finishEditing();
            });

            // 내장 select 에디터 선택 즉시 값 반영
            grid.on('editingStart', function(ev) {
                var col = (options.columns || []).find(function(c) {
                    return c.name === ev.columnName;
                });
                if (!col || !col.editor) return;

                var editorType = typeof col.editor === 'string'
                    ? col.editor
                    : (col.editor.type || '');

                setTimeout(function() {
                    // 편집 중인 셀 레이어 안의 요소 탐색
                    var $layer = $('.tui-grid-layer-editing');
                    console.log('editorType:', editorType, 'layer:', $layer);
                    switch (editorType) {

                        // select: 선택 즉시 finishEditing
                        case 'select':
                            $layer.find('select')
                                .off('change.tuiInstant')
                                .on('change.tuiInstant', function() {
                                    grid.finishEditing(ev.rowKey, ev.columnName, $(this).val());
                                });
                            break;

                        // radio: 선택 즉시 finishEditing
                        case 'radio':
                            $layer.find('input[type="radio"]')
                                .off('change.tuiInstant')
                                .on('change.tuiInstant', function() {
                                    grid.finishEditing(ev.rowKey, ev.columnName, $(this).val());
                                });
                            break;

                        // checkbox: 체크 즉시 finishEditing
                        // TUI Grid 체크박스 에디터는 값이 배열(,구분 문자열)
                        case 'checkbox':
                            $layer.find('input[type="checkbox"]')
                                .off('change.tuiInstant')
                                .on('change.tuiInstant', function() {
                                    var checked = [];
                                    $layer.find('input[type="checkbox"]:checked').each(function() {
                                        checked.push($(this).val());
                                    });
                                    grid.finishEditing(ev.rowKey, ev.columnName, checked.join(','));
                                });
                            break;

                        // text: 입력 즉시 그리드 값 반영
                        case 'text':
                            var $input = $layer.find('input[type="text"]');
                            $input
                                .off('input.tuiInstant')
                                .on('input.tuiInstant', function() {
                                    var val = $(this).val();
                                    // 현재 input DOM 값을 그리드에 즉시 반영
                                    grid.finishEditing(ev.rowKey, ev.columnName, val);
                                    // 편집모드 재진입 (커서 유지)
                                    grid.startEditing(ev.rowKey, ev.columnName);
                                    // 재진입 후 커서를 끝으로
                                    setTimeout(function() {
                                        var $newInput = $('.tui-grid-layer-editing input[type="text"]');
                                        var len = $newInput.val().length;
                                        $newInput[0] && $newInput[0].setSelectionRange(len, len);
                                    }, 0);
                                });
                            break;

                        // textarea
                        case 'textarea':
                            $layer.find('textarea')
                                .off('input.tuiInstant')
                                .on('input.tuiInstant', function() {
                                    var val = $(this).val();
                                    grid.finishEditing(ev.rowKey, ev.columnName, val);
                                    grid.startEditing(ev.rowKey, ev.columnName);
                                    setTimeout(function() {
                                        var $ta = $('.tui-grid-layer-editing textarea');
                                        var len = $ta.val().length;
                                        $ta[0] && $ta[0].setSelectionRange(len, len);
                                    }, 0);
                                });
                            break;
                    }
                }, 0);
            });

            if (options.isInfinite) {
                grid.on('scrollEnd', function() {
                    var item = instances[gridId];
                    item.currentPage++;
                    ToastGrid.search(gridId, item.currentPage, true);
                });
            }

            this.search(gridId, 1, false);
        },

        getData: function(gridId) { return instances[gridId].grid.getData(); },
        getSelectedData: function(gridId) { return instances[gridId].grid.getCheckedRows(); },

        // contextMenu 동적 설정
        // - menuItems 미전달 or null → 비활성화
        // - menuItems 전달 → 기존 메뉴에 병합(append) 후 활성화
        //
        // 사용예시:
        // ToastGrid.setContextMenu('myGrid');                  // 비활성화
        // ToastGrid.setContextMenu('myGrid', null);            // 비활성화
        // ToastGrid.setContextMenu('myGrid', [                 // 추가 활성화
        //     [{ name: 'copy',   label: '복사',   action: fn }],
        //     [{ name: 'delete', label: '삭제',   action: fn }]
        // ]);
        setContextMenu: function(gridId, menuItems) {
            var item = instances[gridId];
            if (!item) return;

            if (!menuItems || !menuItems.length) {
                item.grid.setOptions({ contextMenu: null });
                return;
            }

            // 기존 메뉴가 있으면 병합, 없으면 새로 세팅
            var currentMenu = item.grid.getOptions
                ? (item.grid.getOptions().contextMenu || [])
                : [];

            var merged = currentMenu.concat(menuItems);
            item.grid.setOptions({ contextMenu: merged });
        },

        // 마스크 formatter 팩토리
        // formatter: ToastGrid.maskFormatter('9999-99-99')        날짜
        // formatter: ToastGrid.maskFormatter('999-9999-9999')     전화번호
        // formatter: ToastGrid.maskFormatter('999-99-99999')      사업자번호
        // formatter: ToastGrid.maskFormatter('AAAA-9999')         영문+숫자 혼합
        maskFormatter: function(pattern) {
            return function(props) {
                return _applyMask(props.value, pattern);
            };
        },

        search: function(gridId, page, isAppend) {
            var item = instances[gridId];
            if (!item) return;
            var formData = $('#' + item.formId).serialize();

            this.resetTimer(gridId);
            $.ajax({
                url: item.dataUrl,
                data: formData + '&pageIndex=' + (page || 1) + '&pageSize=' + item.options.perPage,
                success: function(res) {
                    if (isAppend) {
                        item.grid.appendRows(res.dataList);
                    } else {
                        item.grid.resetData(res.dataList);
                    }

                    if (item.options.isInfinite) {
                        // ✅ 무한스크롤 모드: 페이징 영역 숨김/비움
                        $('.grid-pagination[data-grid="' + gridId + '"]').empty();
                    } else if (res.paginationInfo) {
                        ToastGrid.renderPagination(gridId, res.paginationInfo);
                    }
                }
            });
        },

        // ✅ eGovFrame paginationInfo로 페이징 버튼 렌더링
        renderPagination: function(gridId, paginationInfo) {
            var item = instances[gridId];
            var $pagination = $('.grid-pagination[data-grid="' + gridId + '"]');
            if (!$pagination.length) return;

            var currentPage = paginationInfo.currentPageNo;
            var totalPages = paginationInfo.lastPageNo;
            var startPage = paginationInfo.firstPageNoOnPageList;
            var endPage = paginationInfo.lastPageNoOnPageList;

            var html = '<ul class="pagination">';

            // 처음/이전 - 항상 출력, 비활성 시 disabled 클래스
            if (currentPage > 1) {
                html += '<li><a href="#" data-page="1">&laquo;</a></li>';
                html += '<li><a href="#" data-page="' + (currentPage - 1) + '">&lsaquo;</a></li>';
            } else {
                html += '<li class="disabled"><a href="#">&laquo;</a></li>';
                html += '<li class="disabled"><a href="#">&lsaquo;</a></li>';
            }

            for (var i = startPage; i <= endPage; i++) {
                if (i === currentPage) {
                    html += '<li class="active"><a href="#" data-page="' + i + '">' + i + '</a></li>';
                } else {
                    html += '<li><a href="#" data-page="' + i + '">' + i + '</a></li>';
                }
            }

            // 다음/끝 - 항상 출력, 비활성 시 disabled 클래스
            if (currentPage < totalPages) {
                html += '<li><a href="#" data-page="' + (currentPage + 1) + '">&rsaquo;</a></li>';
                html += '<li><a href="#" data-page="' + totalPages + '">&raquo;</a></li>';
            } else {
                html += '<li class="disabled"><a href="#">&rsaquo;</a></li>';
                html += '<li class="disabled"><a href="#">&raquo;</a></li>';
            }

            html += '</ul>';
            $pagination.html(html);

            $pagination.off('click').on('click', 'a', function(e) {
                e.preventDefault();
                var $li = $(this).closest('li');
                if ($li.hasClass('disabled')) return; // 비활성 클릭 무시
                var page = $(this).data('page');
                ToastGrid.search(gridId, page, false);
            });
        },

        bindColumnData: function(gridId, columnName, dataList, textKey, valueKey) {
            var item = instances[gridId];
            var grid = item.grid;
            var vKey = valueKey || 'value';
            var tKey = textKey || 'text';

            var currentColumns = grid.getColumns();
            var newColumns = currentColumns.map(function(col) {
                if (col.name === columnName) {
                    col.editor.options.listItems = dataList.map(function(d) {
                        return { text: d[tKey], value: d[vKey] };
                    });
                    col.formatter = function(props) {
                        var found = dataList.find(function(c) { return c[vKey] == props.value; });
                        return found ? found[tKey] : props.value;
                    };
                }
                return col;
            });
            grid.setColumns(newColumns);
        },

        toggleMode: function(gridId, isInfinite, perPage) {
            var item = instances[gridId];
            item.options.isInfinite = isInfinite;
            if (perPage) item.options.perPage = perPage;
            item.grid.off('scrollEnd');
            if (isInfinite) {
                item.grid.on('scrollEnd', function() {
                    item.currentPage++;
                    ToastGrid.search(gridId, item.currentPage, true);
                });
            }
            this.search(gridId, 1, false);
        },

        resetTimer: function(gridId) {
            var item = instances[gridId];
            if (item.timer) clearInterval(item.timer);
            if (item.options.refreshInterval) {
                item.timer = setInterval(function() {
                    ToastGrid.search(gridId, 1, false);
                }, item.options.refreshInterval * 60 * 1000);
            }
        },

        destroy: function(gridId) {
            var item = instances[gridId];
            if (item) {
                clearInterval(item.timer);
                item.grid.destroy();
                $(document).off('mousedown.tuiFinish_' + gridId);
                delete instances[gridId];
            }
        }
    };
})();

class JQueryDatepickerEditor {
    constructor(props) {
        this.el = document.createElement('div');
        this.el.style.display = 'flex';
        this.el.style.alignItems = 'center';
        this.el.style.width = '100%';

        this.input = document.createElement('input');
        this.input.type = 'text';
        this.input.value = props.value || '';
        this.input.style.width = '100%';
        this.input.style.boxSizing = 'border-box';

        this.el.appendChild(this.input);

        var customOptions = (props.columnInfo.editor.options) || {};

        // 날짜 선택 즉시 편집 종료
        var originalOnSelect = customOptions.onSelect;
        customOptions.onSelect = function(dateText, inst) {
            if (originalOnSelect) originalOnSelect(dateText, inst);
            // 선택한 값을 input에 반영 후 포커스 이동으로 finishEditing 유도
            setTimeout(function() {
                $(inst.input).trigger('change');
                inst.input.blur();
            }, 0);
        };

        $(this.input).setDatepicker(customOptions);
    }

    getElement() {
        return this.el;
    }

    getValue() {
        return this.input.value;
    }

    mounted() {
        var $input = $(this.input);
        $input.focus();
        $input.select();

        // ✅ 그리드 내부 포커스 처리 완료 후 show
        setTimeout(function() {
            $input.datepicker('show');
        }, 50);
    }

    beforeDestroy() {
        $(this.input).datepicker('destroy');
    }
}

function parseInitialDate(value, format) {
    if (!value) return new Date();
    var digits = String(value).replace(/[^0-9]/g, '');
    if (digits.length === 8) {
        return new Date(digits.substr(0,4), digits.substr(4,2) - 1, digits.substr(6,2));
    }
    return new Date();
}

class ToastDatepickerEditor {
    constructor(props) {
        this.el = document.createElement('span');
        this.el.style.position = 'relative';
        this.el.style.display = 'inline-block';
        this.el.style.width = '100%';

        this.input = document.createElement('input');
        this.input.type = 'text';
        this.input.value = props.value || '';
        this.input.style.width = '100%';
        this.input.style.boxSizing = 'border-box';

        this.btn = document.createElement('button');
        this.btn.type = 'button';
        //this.btn.innerText = '📅';
        var img = document.createElement('img');
        img.src = _CONTEXT_PATH + "/images/egovframework/cmmn/calendar.png";
        img.alt = '달력';
        img.style.width = '16px';
        img.style.height = '16px';
        img.style.verticalAlign = 'middle';
        this.btn.appendChild(img);

        this.el.appendChild(this.input);
        this.el.appendChild(this.btn);

        //  calendarDiv를 body에 직접 붙임 (overflow 탈출)
        this.calendarDiv = document.createElement('div');
        this.calendarDiv.style.cssText = 'position:fixed; z-index:99999; display:none;';
        document.body.appendChild(this.calendarDiv);

        var format = (props.columnInfo.editor.options && props.columnInfo.editor.options.format) || 'yyyy-MM-dd';
        var lang   = (props.columnInfo.editor.options && props.columnInfo.editor.options.language) || 'ko';

        this.dp = new tui.DatePicker(this.calendarDiv, {
            date: parseInitialDate(props.value, format),
            input: { element: this.input, format: format },
            language: lang,
            usageStatistics: false
        });

        var self = this;

        //  버튼 클릭 시 위치 계산 후 open
        this.btn.addEventListener('click', function() {
            if (self.dp.isOpened()) {
                self.dp.close();
            } else {
                self._positionCalendar();
                self.dp.open();
            }
        });

        this.dp.on('change', function() {
            self.dp.close();
            setTimeout(function() { self.input.blur(); }, 0);
        });
    }

    //  input 기준으로 달력 위치 계산
    _positionCalendar() {
        var rect = this.input.getBoundingClientRect();
        this.calendarDiv.style.top  = rect.bottom + 'px';
        this.calendarDiv.style.left = rect.left + 'px';
        this.calendarDiv.style.display = '';
    }

    getElement() { return this.el; }
    getValue()    { return this.input.value; }

    mounted() {
        var self = this;
        this.input.focus();
        this.input.select();
        setTimeout(function() {
            self._positionCalendar();
            self.dp.open();
        }, 50);
    }

    beforeDestroy() {
        this.dp.destroy();
        //  body에 붙인 div 반드시 제거
        if (this.calendarDiv && this.calendarDiv.parentNode) {
            this.calendarDiv.parentNode.removeChild(this.calendarDiv);
        }
    }
}
