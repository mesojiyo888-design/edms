var ToastDatepicker = (function() {
    var instances = {};
    return {
        initAll: function(selector) {
            selector = selector || '.datepicker';
            $(selector).each(function() {
                var $el = $(this);
                var id = $el.attr('id');
                if (!id) return;
                if (instances[id]) return;

                var format = $el.data('format') || 'yyyy-MM-dd';
                var lang = $el.data('lang') || 'ko';
                var wrapperId = id + '_wrapper';
                var calendarId = id + '_calendar';

                if (!$('#' + wrapperId).length) {
                    $el.wrap('<span id="' + wrapperId + '" class="datepicker-wrapper" style="position:relative; display:inline-block;"></span>');

                    //var $btn = $('<button type="button" class="datepicker-btn" data-target="' + id + '">📅</button>');
                    var $btn = $('<button type="button" class="datepicker-btn" data-target="' + id + '"><img src="' + _CONTEXT_PATH + '/images/egovframework/cmmn/calendar.png" /> </button>');
                    $el.after($btn);

                    // ✅ 캘린더 전용 컨테이너 (input/button과 분리)
                    $('#' + wrapperId).append('<div id="' + calendarId + '"></div>');
                }

                var dp = new tui.DatePicker('#' + calendarId, {
                    date: new Date(),
                    input: {
                        element: '#' + id,
                        format: format
                    },
                    language: lang,
                    usageStatistics: false
                });
                instances[id] = dp;

                $('.datepicker-btn[data-target="' + id + '"]').off('click').on('click', function() {
                    dp.isOpened() ? dp.close() : dp.open();
                });
            });
        },
        getInstance: function(id) { return instances[id]; },
        getDate: function(id) { var dp = instances[id]; return dp ? dp.getDate() : null; },
        setDate: function(id, date) { var dp = instances[id]; if (dp) dp.setDate(date); },
        destroy: function(id) {
            var dp = instances[id];
            if (dp) { dp.destroy(); delete instances[id]; }
        }
    };
})();