var GridManager = (function() {
    var instances = {};

    return {
        init: function(gridId, dataUrl, formId, options) {
            var el = document.getElementById(gridId);
            var perPage = options.pageOptions ? (options.pageOptions.perPage || 10) : 10;

            // 내장 페이징 완전 제거
            delete options.pageOptions;
            delete options.dataSource;

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
                    GridManager.search(gridId, item.currentPage, true);
                });
            }

            this.search(gridId, 1, false);
        },

        getData: function(gridId) { return instances[gridId].grid.getData(); },
        getSelectedData: function(gridId) { return instances[gridId].grid.getCheckedRows(); },

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
                        GridManager.renderPagination(gridId, res.paginationInfo);
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
                GridManager.search(gridId, page, false);
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
                    GridManager.search(gridId, item.currentPage, true);
                });
            }
            this.search(gridId, 1, false);
        },

        resetTimer: function(gridId) {
            var item = instances[gridId];
            if (item.timer) clearInterval(item.timer);
            if (item.options.refreshInterval) {
                item.timer = setInterval(function() {
                    GridManager.search(gridId, 1, false);
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