(function($) {
    $.commonDateOptions = {
        dateFormat: 'yy-mm-dd'
        , monthNames: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월']
        , monthNamesShort: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월']
        , dayNamesMin: ['일','월','화','수','목','금','토']
        , showMonthAfterYear: true
        , yearSuffix: '년'
        , changeYear: true
        , changeMonth: true
        , showOn: "both" // 'focus'(입력창 클릭시), 'button'(버튼 클릭시), 'both'(둘다)
        , buttonImage: _CONTEXT_PATH + "/images/egovframework/cmmn/calendar.png" // 아이콘 이미지 경로
        , buttonImageOnly: true // 버튼 태그 대신 이미지만 표시
        , buttonText: "날짜 선택" // 웹 접근성을 위한 설명
        , showButtonPanel: false

    };

    $.fn.setDatepicker = function(customOptions) {
        return this.each(function() {
            var $el = $(this);
            if ($el.hasClass('hasDatepicker')) $el.datepicker('destroy');

            var finalOptions = $.extend({}, $.commonDateOptions, customOptions);
            $el.datepicker(finalOptions);
        });
    };
})(jQuery);