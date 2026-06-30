/**
 * ComMsg.js - 공통 알림/확인 다이얼로그 유틸리티
 * 의존성: sweetalert2.min.js
 *
 * ※ alert / success / error / confirm 모두 바깥 영역 클릭, ESC 키로 닫히지 않음
 *    (allowOutsideClick: false, allowEscapeKey: false)
 *    → 반드시 버튼 클릭으로만 닫혀서 메시지 확인 및 콜백 실행이 보장됨
 *    ※ toast는 자동 닫힘 알림이라 해당 없음
 *
 * ============================================================
 * 사용법
 * ============================================================
 *
 * [1] alert - 단순 알림
 *
 *   ComMsg.alert(msg)
 *   ComMsg.alert(msg, title)
 *   ComMsg.alert(msg, title, callback)
 *   ComMsg.alert(msg, title, callback, opts)
 *
 *   예)
 *   ComMsg.alert('저장되었습니다.');
 *   ComMsg.alert('저장되었습니다.', '알림');
 *   ComMsg.alert('저장되었습니다.', '알림', function() { location.reload(); });
 *   ComMsg.alert('저장되었습니다.', '알림', null, { icon: 'warning' });
 *
 * ────────────────────────────────────────────────────────────
 *
 * [2] success - 성공 알림
 *
 *   ComMsg.success(msg)
 *   ComMsg.success(msg, title)
 *   ComMsg.success(msg, title, callback)
 *   ComMsg.success(msg, title, callback, opts)
 *
 *   예)
 *   ComMsg.success('승인이 완료되었습니다.');
 *   ComMsg.success('저장되었습니다.', '완료', function() { goList(); });
 *
 * ────────────────────────────────────────────────────────────
 *
 * [3] error - 오류 알림
 *
 *   ComMsg.error(msg)
 *   ComMsg.error(msg, title)
 *   ComMsg.error(msg, title, opts)
 *
 *   예)
 *   ComMsg.error('처리 중 오류가 발생했습니다.');
 *   ComMsg.error('권한이 없습니다.', '오류');
 *
 * ────────────────────────────────────────────────────────────
 *
 * [4] confirm - 확인/취소 다이얼로그
 *
 *   ComMsg.confirm(msg, title, callback)
 *   ComMsg.confirm(msg, title, callback, opts)
 *
 *   예)
 *   ComMsg.confirm('상신하시겠습니까?', '상신', function() { submitApproval(); });
 *   ComMsg.confirm('삭제하시겠습니까?', '삭제', function() { deleteDoc(); }, { icon: 'warning' });
 *
 * ============================================================
 * opts 옵션 목록
 * ============================================================
 *
 *   icon               아이콘 종류 (기본값은 각 메서드마다 상이)
 *                        'info'     - 파란 느낌표       (alert 기본값)
 *                        'success'  - 초록 체크         (success 기본값)
 *                        'error'    - 빨간 X            (error 기본값)
 *                        'warning'  - 노란 느낌표
 *                        'question' - 물음표            (confirm 기본값)
 *                        null       - 아이콘 없음
 *
 *   confirmButtonText  확인 버튼 텍스트  (기본값: '확인')
 *   cancelButtonText   취소 버튼 텍스트  (기본값: '취소')
 *   confirmButtonColor 확인 버튼 색상    (기본값: '#1670B8')
 *   cancelButtonColor  취소 버튼 색상    (기본값: '#6c757d')
 *
 *   buttonsStyling     SweetAlert2 기본 버튼 스타일 사용 여부 (기본값: true)
 *                        false 시 customClass로 직접 CSS 적용 가능
 *
 *   showCloseButton    우상단 X(닫기) 버튼 표시 여부 (기본값: true)
 *                        false 시 X 버튼 숨김 (alert/confirm 모두 적용)
 *
 *   customClass        버튼에 커스텀 CSS 클래스 지정 (buttonsStyling: false 와 함께 사용)
 *                        예) { confirmButton: 'btn-ok', cancelButton: 'btn-cancel' }
 *
 * ============================================================
 * opts 사용 예시
 * ============================================================
 *
 *   // 버튼 텍스트 변경
 *   ComMsg.confirm('삭제하시겠습니까?', '삭제', function() { deleteDoc(); }, {
 *     confirmButtonText: '삭제',
 *     cancelButtonText: '아니오',
 *     icon: 'warning'
 *   });
 *
 *   // 아이콘 제거
 *   ComMsg.alert('처리되었습니다.', '알림', null, { icon: null });
 *
 *   // X 버튼 숨김
 *   ComMsg.confirm('상신하시겠습니까?', '상신', function() { submitApproval(); }, {
 *     showCloseButton: false
 *   });
 *
 *   // 이미지 버튼 (CSS 직접 제어)
 *   ComMsg.confirm('상신하시겠습니까?', '상신', function() { submitApproval(); }, {
 *     buttonsStyling: false,
 *     customClass: { confirmButton: 'btn-custom-ok', cancelButton: 'btn-custom-cancel' }
 *   });
 *
 * ────────────────────────────────────────────────────────────
 *
 * [5] toast - 토스트 메시지 (자동 닫힘, 버튼 없음)
 *
 *   ComMsg.toast(msg)
 *   ComMsg.toast(msg, callback)
 *   ComMsg.toast(msg, callback, opts)
 *
 *   예)
 *   ComMsg.toast('저장되었습니다.');
 *   ComMsg.toast('저장되었습니다.', function() { resetForm(); });
 *   ComMsg.toast('권한이 없습니다.', null, { icon: 'error' });
 *   ComMsg.toast('삭제되었습니다.', function() { grid.refresh(); }, { position: 'bottom-end', timer: 5000 });
 *
 * ============================================================
 * toast opts 옵션 목록
 * ============================================================
 *
 *   icon               아이콘 종류 (기본값: 'success')
 *                        'success'  - 초록 체크
 *                        'error'    - 빨간 X
 *                        'warning'  - 노란 느낌표
 *                        'info'     - 파란 느낌표
 *                        null       - 아이콘 없음
 *
 *   position           표시 위치 (기본값: 'top-end')
 *                        'top-end'     - 우상단
 *                        'top-start'   - 좌상단
 *                        'top'         - 상단 중앙
 *                        'bottom-end'  - 우하단
 *                        'bottom-start'- 좌하단
 *                        'bottom'      - 하단 중앙
 *
 *   timer              자동 닫힘 시간 ms (기본값: 3000)
 *
 * ============================================================
 */
var ComMsg = (function () {

  var defaults = {
    confirmButtonText:  '확인',
    cancelButtonText:   '취소',
    confirmButtonColor: '#1670B8',
    cancelButtonColor:  '#6c757d'
  };

  function _mergeOpts(opts) {
    if (!opts) return { icon: null, showCloseButton: true };
    return {
      icon:               opts.icon !== undefined ? opts.icon : null,
      confirmButtonText:  opts.confirmButtonText  || defaults.confirmButtonText,
      cancelButtonText:   opts.cancelButtonText   || defaults.cancelButtonText,
      confirmButtonColor: opts.confirmButtonColor || defaults.confirmButtonColor,
      cancelButtonColor:  opts.cancelButtonColor  || defaults.cancelButtonColor,
      customClass:        opts.customClass         || null,
      buttonsStyling:     opts.buttonsStyling !== undefined ? opts.buttonsStyling : true,
      showCloseButton:    opts.showCloseButton !== undefined ? opts.showCloseButton : true
    };
  }

  function alert(msg, title, callback, opts) {
    var o = _mergeOpts(opts);
    return Swal.fire({
      title:              title || '알림',
      text:               msg,
      icon:               o.icon !== null ? o.icon : 'info',
      confirmButtonText:  o.confirmButtonText  || defaults.confirmButtonText,
      confirmButtonColor: o.confirmButtonColor || defaults.confirmButtonColor,
      customClass:        o.customClass,
      buttonsStyling:     o.buttonsStyling,
      allowOutsideClick:  false,
      allowEscapeKey:     false,
      showCloseButton:    o.showCloseButton
    }).then(function () {
      if (typeof callback === 'function') callback();
    });
  }

  function success(msg, title, callback, opts) {
    var o = _mergeOpts(opts);
    return Swal.fire({
      title:              title || '완료',
      text:               msg,
      icon:               o.icon !== null ? o.icon : 'success',
      confirmButtonText:  o.confirmButtonText  || defaults.confirmButtonText,
      confirmButtonColor: o.confirmButtonColor || defaults.confirmButtonColor,
      customClass:        o.customClass,
      buttonsStyling:     o.buttonsStyling,
      allowOutsideClick:  false,
      allowEscapeKey:     false,
      showCloseButton:    o.showCloseButton
    }).then(function () {
      if (typeof callback === 'function') callback();
    });
  }

  function error(msg, title, opts) {
    var o = _mergeOpts(opts);
    return Swal.fire({
      title:              title || '오류',
      text:               msg,
      icon:               o.icon !== null ? o.icon : 'error',
      confirmButtonText:  o.confirmButtonText  || defaults.confirmButtonText,
      confirmButtonColor: o.confirmButtonColor || defaults.confirmButtonColor,
      customClass:        o.customClass,
      buttonsStyling:     o.buttonsStyling,
      allowOutsideClick:  false,
      allowEscapeKey:     false,
      showCloseButton:    o.showCloseButton
    });
  }

  function confirm(msg, title, callback, opts) {
    var o = _mergeOpts(opts);
    return Swal.fire({
      title:              title || '확인',
      text:               msg,
      icon:               o.icon !== null ? o.icon : 'question',
      showCancelButton:   true,
      confirmButtonText:  o.confirmButtonText  || defaults.confirmButtonText,
      cancelButtonText:   o.cancelButtonText   || defaults.cancelButtonText,
      confirmButtonColor: o.confirmButtonColor || defaults.confirmButtonColor,
      cancelButtonColor:  o.cancelButtonColor  || defaults.cancelButtonColor,
      customClass:        o.customClass,
      buttonsStyling:     o.buttonsStyling,
      allowOutsideClick:  false,
      allowEscapeKey:     false,
      showCloseButton:    o.showCloseButton
    }).then(function (result) {
      if (result.isConfirmed && typeof callback === 'function') callback();
    });
  }

  function toast(msg, callback, opts) {
    var o = opts || {};
    return Swal.fire({
      text:             msg,
      icon:             o.icon !== undefined ? o.icon : 'success',
      toast:            true,
      position:         o.position || 'top-end',
      showConfirmButton: false,
      timer:            o.timer || 3000,
      timerProgressBar: true
    }).then(function () {
      if (typeof callback === 'function') callback();
    });
  }

  return { alert: alert, success: success, error: error, confirm: confirm, toast: toast };

})();