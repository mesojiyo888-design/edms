// ComAjax.js
//===============================================================
// 1. 개요
/* ===============================================================
 * - jQuery ajax 호출 시 공통 에러 처리 기능을 제공한다.
 * - 화면에서 개별적으로 error 콜백을 지정한 경우, 공통 에러 처리 후 이어서 실행된다.
 * - 화면에서 개별적으로 error 콜백을 지정하지 않은 경우, 공통 에러 처리만 실행된다.
 * - 화면에서 개별적으로 error 콜백을 지정하면서 공통 에러 처리를 스킵하고 싶은 경우, ajax 옵션에 skipGlobalError: true 를 추가하면 된다.
 * - error 콜백 없는 기존 호출 → 공통 처리만 실행됨
 * 사용 예:
 *  $.ajax({
 *     url: "/edms/file/list",
 *     type: "GET",
 *     success: function (data) { ... }
 * });
 *
 * // error 콜백 있는 호출 → 공통 처리 + 화면 전용 처리 순서대로 실행됨
 * $.ajax({
 *     url: "/edms/doc/save",
 *     type: "POST",
 *     success: function (data) { ... },
 *     error: function (jqXHR, textStatus, errorThrown) {
 *         $("#saveBtn").prop("disabled", false); // 버튼 재활성화 같은 화면별 후처리
 *     }
 * });
 *
 * // 전역 처리 자체를 원치 않는 화면 → 완전히 자체 제어
 * $.ajax({
 *     url: "/edms/special",
 *     type: "POST",
 *     skipGlobalError: true,
 *     error: function (jqXHR) {
 *         // 자체 커스텀 에러 UI
 *     }
 * });
=============================================================== */


(function () {

    /* ========================
     * jQuery $.ajax 공통 에러 처리
     * ======================== */
    $.ajaxPrefilter(function (options, originalOptions, jqXHR) {

        // 개별 호출에서 명시적으로 스킵한 경우, 원래 error 콜백만 그대로 둠
        if (options.skipGlobalError) {
            return;
        }

        var originalError = originalOptions.error; // 화면에서 넘긴 error 콜백 (없을 수도 있음)

        options.error = function (jqXHR, textStatus, errorThrown) {
            // 1. 공통 에러 처리 먼저 실행
            handleGlobalError(jqXHR, textStatus, errorThrown);

            // 2. 화면별 error 콜백이 있으면 이어서 실행
            if (typeof originalError === "function") {
                originalError.call(this, jqXHR, textStatus, errorThrown);
            }
        };
    });

    function handleGlobalError(jqXHR, textStatus, errorThrown) {
        var status = jqXHR.status;
        var res = jqXHR.responseJSON; // {success, data, message, messageCode}
        var messageCode = res && res.messageCode;
        var message = (res && res.message) || errorThrown;

        switch (status) {
            case 401:
                ComMsg.confirm("세션이 만료되었습니다. 다시 로그인하시겠습니까?", function () {
                    location.href = "/login";
                });
                return;

            case 403:
                ComMsg.alert("접근 권한이 없습니다.");
                return;

            case 404:
                ComMsg.alert("요청하신 페이지를 찾을 수 없습니다.");
                return;

            case 405:
                ComMsg.alert("허용되지 않은 요청입니다.");
                return;

            case 500:
                handleServerError(messageCode, message);
                return;

            default:
                ComMsg.alert("알 수 없는 오류가 발생했습니다.");
        }
    }

    function handleServerError(messageCode, message) {
        switch (messageCode) {
            case "ERR_500_DB":
                ComMsg.alert("데이터 처리 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.");
                break;
            default:
                ComMsg.alert(message || "시스템 오류가 발생했습니다.");
        }
    }

})();