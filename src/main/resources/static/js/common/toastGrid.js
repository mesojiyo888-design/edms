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

        this.calendarDiv = document.createElement('div');

        this.el.appendChild(this.input);
        this.el.appendChild(this.btn);
        this.el.appendChild(this.calendarDiv);

        var format = (props.columnInfo.editor.options && props.columnInfo.editor.options.format) || 'yyyy-MM-dd';
        var lang = (props.columnInfo.editor.options && props.columnInfo.editor.options.language) || 'ko';

        this.dp = new tui.DatePicker(this.calendarDiv, {
            date: parseInitialDate(props.value, format), // ✅ 원본 값 기준
            input: {
                element: this.input,
                format: format
            },
            language: lang
        });

        var self = this;
        this.btn.addEventListener('click', function() {
            self.dp.isOpened() ? self.dp.close() : self.dp.open();
        });

        // ✅ 캘린더 z-index (그리드 안이라 더 중요)
        this.calendarDiv.style.zIndex = 9999;
        this.calendarDiv.style.position = 'absolute';
    }

    getElement() {
        return this.el;
    }

    getValue() {
        return this.input.value;
    }

    mounted() {
        var self = this;
        this.input.focus();
        this.input.select();

        // ✅ 그리드의 더블클릭 이벤트 처리가 끝난 후 열기
        setTimeout(function() {
            self.dp.open();
        }, 50);
    }

    beforeDestroy() {
        this.dp.destroy();
    }
}