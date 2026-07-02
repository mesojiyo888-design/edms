/**
 * ComMsg.js - 공통 알림/확인 다이얼로그 유틸리티
 * 의존성: sweetalert2.min.js, jQuery (modalUrl에서 $.load 사용)
 *
 * ※ alert / success / error / confirm / modal / modalUrl 모두 바깥 영역 클릭, ESC 키로 닫히지 않음
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
 * opts 옵션 목록 (alert / success / error / confirm 공통)
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
 *                        false 시 X 버튼 숨김
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
 * ────────────────────────────────────────────────────────────
 *
 * [6] modal - 커스텀 HTML(폼 등)을 담는 범용 모달
 *
 *   ComMsg.modal(opts)
 *
 *   opts (전용):
 *     title              제목
 *     html               모달 안에 넣을 HTML 문자열 (필수)
 *     width              모달 너비 px (기본값: 500)
 *     showConfirmButton  확인 버튼 표시 여부 (기본값: true)
 *     showCancelButton   취소 버튼 표시 여부 (기본값: true)
 *                          ※ 버튼을 콘텐츠 내부에서 직접 제어하려면 둘 다 false로 두고
 *                            ComMsg.close(...)를 사용 (아래 [8] 참고)
 *     preConfirm         확인 버튼 클릭 시 실행. 입력값 검증/수집.
 *                          false 반환 시 모달 닫히지 않음 (Swal.showValidationMessage로 에러 표시)
 *                          반환값은 callback(value)의 인자로 전달됨
 *     didOpen            모달이 열린 직후 실행 (이벤트 바인딩 등)
 *     callback           확인(isConfirmed) 시 실행, preConfirm(또는 close) 반환값을 인자로 받음
 *     (그 외 confirmButtonText 등 [1]~[4] 공통 옵션도 동일하게 사용 가능)
 *
 *   예 1) SweetAlert2 자체 버튼(확인/취소) 사용 - 입력 폼 모달
 *   ComMsg.modal({
 *     title: '반려 사유 입력',
 *     html: '<textarea id="swal-reason" class="swal2-textarea" placeholder="반려 사유를 입력하세요"></textarea>',
 *     preConfirm: function () {
 *       var val = document.getElementById('swal-reason').value;
 *       if (!val.trim()) {
 *         Swal.showValidationMessage('사유를 입력해주세요.');
 *         return false;
 *       }
 *       return val;
 *     },
 *     callback: function (reason) {
 *       rejectDoc(reason);
 *     }
 *   });
 *
 *   예 2) 버튼 3개 이상 필요 - swal 기본 버튼 숨기고 콘텐츠 안 버튼에서 직접 제어
 *   ComMsg.modal({
 *     title: '문서 처리',
 *     width: 400,
 *     showConfirmButton: false,
 *     showCancelButton: false,
 *     html:
 *       '<div style="display:flex; gap:8px; justify-content:center;">' +
 *       '  <button type="button" class="swal2-confirm swal2-styled" id="btnApprove">승인</button>' +
 *       '  <button type="button" class="swal2-deny swal2-styled" id="btnReject">반려</button>' +
 *       '  <button type="button" class="swal2-cancel swal2-styled" id="btnHold">보류</button>' +
 *       '</div>',
 *     didOpen: function () {
 *       document.getElementById('btnApprove').onclick = function () {
 *         ComMsg.close(true, 'approve');   // 확인 처리, value='approve'
 *       };
 *       document.getElementById('btnReject').onclick = function () {
 *         ComMsg.close(true, 'reject');    // 확인 처리, value='reject'
 *       };
 *       document.getElementById('btnHold').onclick = function () {
 *         ComMsg.close(false);             // 취소 처리 (callback 실행 안 됨)
 *       };
 *     },
 *     callback: function (action) {
 *       if (action === 'approve') approveDoc();
 *       if (action === 'reject')  showRejectReasonModal();
 *     }
 *   });
 *
 * ────────────────────────────────────────────────────────────
 *
 * [7] modalUrl - 지정한 URL의 HTML을 AJAX로 불러와 모달 안에 표시
 *                (iframe 아님, jQuery $.load() 사용 → 같은 도메인 필수)
 *                로드된 화면과 부모가 같은 DOM/컨텍스트를 공유하므로,
 *                로드된 화면의 스크립트에서도 Swal.close(...) / ComMsg.close(...)를 그대로 호출 가능.
 *
 *   ComMsg.modalUrl(url, opts)
 *
 *   opts (전용):
 *     title              제목
 *     width              모달 너비 px (기본값: 700)
 *     showConfirmButton  확인 버튼 표시 여부 (기본값: false → 보통 로드된 화면 안 버튼으로 제어)
 *     showCancelButton   취소 버튼 표시 여부 (기본값: false)
 *     preConfirm         (showConfirmButton: true 로 쓸 때만) 확인 클릭 시 값 검증/수집
 *     onLoad             URL 로드 완료 후 실행 (로드된 화면 내부 이벤트 바인딩 등)
 *     callback           확인(isConfirmed) 시 실행, close(또는 preConfirm) 반환값을 인자로 받음
 *     (그 외 confirmButtonText 등 [1]~[4] 공통 옵션도 동일하게 사용 가능)
 *
 *   예 1) 로드되는 페이지(JSP) 안의 버튼에서 직접 닫기 - 부모 쪽 코드
 *   ComMsg.modalUrl('/approval/lineSelect.do', {
 *     title: '결재선 지정',
 *     width: 800,
 *     callback: function (approverId) {
 *       console.log('선택된 결재자:', approverId);
 *       setApproverToForm(approverId);
 *     }
 *   });
 *
 *   예 1-b) lineSelect.do 안에 있는 스크립트/마크업 (로드되는 쪽)
 *   //   <button type="button" onclick="onSaveClick()">저장</button>
 *   //   <button type="button" onclick="onCancelClick()">취소</button>
 *   //   <script>
 *   //   function onSaveClick() {
 *   //     var selected = $('#selectedApprover').val();
 *   //     if (!selected) { alert('결재자를 선택해주세요.'); return; }
 *   //     ComMsg.close(true, selected);   // 확인 처리하며 모달 닫기 → 부모의 callback(selected) 실행
 *   //   }
 *   //   function onCancelClick() {
 *   //     ComMsg.close(false);            // 그냥 닫기, callback 실행 안 됨
 *   //   }
 *   //   </script>
 *
 *   예 2) 부모 쪽에서 이벤트 위임으로 로드된 화면의 버튼을 바인딩하는 방식
 *   ComMsg.modalUrl('/approval/lineSelect.do', {
 *     title: '결재선 지정',
 *     width: 800,
 *     onLoad: function () {
 *       $('#swal-url-content').on('click', '#btnSave', function () {
 *         var selected = $('#swal-url-content #selectedApprover').val();
 *         if (!selected) { ComMsg.toast('결재자를 선택해주세요.', null, { icon: 'warning' }); return; }
 *         ComMsg.close(true, selected);
 *       });
 *       $('#swal-url-content').on('click', '#btnCancel', function () {
 *         ComMsg.close(false);
 *       });
 *       $('#swal-url-content').on('click', '#btnGoOtherScreen', function () {
 *         // 모달 유지한 채 내부 콘텐츠만 다른 화면으로 교체
 *         $('#swal-url-content').load('/approval/manualInput.do');
 *       });
 *     },
 *     callback: function (val) {
 *       console.log('선택된 결재자:', val);
 *     }
 *   });
 *
 *   ※ 주의
 *     - 같은 도메인(폐쇄망 내부)이어야 함. 외부 URL 불가.
 *     - 대상 페이지가 <html><body> 풀 페이지여도 $.load()가 body 내용만 자동 추출해서 넣어줌.
 *     - 대상 페이지의 <script>도 함께 실행되므로 전역 변수/함수명 충돌 주의.
 *     - $.load()로 콘텐츠를 다시 교체하면 그 안의 개별 이벤트 바인딩은 날아가므로,
 *       예 2처럼 상위 요소(#swal-url-content)에 이벤트 위임(on)으로 바인딩하면 재로드에도 안전함.
 *
 * ────────────────────────────────────────────────────────────
 *
 * [8] close - 열려 있는 modal/modalUrl을 코드에서 직접 닫는 헬퍼
 *             (SweetAlert2의 Swal.close()를 감싼 것. modal/modalUrl 내부 버튼에서 사용)
 *
 *   ComMsg.close()                     → 그냥 닫힘 (isConfirmed: false, callback 실행 안 됨)
 *   ComMsg.close(true)                 → 확인 처리 (isConfirmed: true, callback() 실행, 인자 없음)
 *   ComMsg.close(true, value)          → 확인 처리 + 값 전달 (callback(value) 실행)
 *   ComMsg.close(false)                → 취소 처리 (callback 실행 안 됨, close()와 동일)
 *
 *   ※ alert/success/error/confirm/toast 는 자체 버튼만 쓰므로 close()를 쓸 일이 거의 없고,
 *      주로 modal/modalUrl에서 버튼 2개(확인/취소) 이상을 콘텐츠 내부에서 직접 그릴 때 사용.
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

  function modal(opts) {
    var o = opts || {};
    return Swal.fire({
      title:              o.title || '',
      html:               o.html || '',
      width:              o.width || 500,
      showConfirmButton:  o.showConfirmButton !== undefined ? o.showConfirmButton : true,
      showCancelButton:   o.showCancelButton  !== undefined ? o.showCancelButton  : true,
      confirmButtonText:  o.confirmButtonText  || defaults.confirmButtonText,
      cancelButtonText:   o.cancelButtonText   || defaults.cancelButtonText,
      confirmButtonColor: o.confirmButtonColor || defaults.confirmButtonColor,
      cancelButtonColor:  o.cancelButtonColor  || defaults.cancelButtonColor,
      customClass:        o.customClass,
      buttonsStyling:     o.buttonsStyling !== undefined ? o.buttonsStyling : true,
      allowOutsideClick:  o.allowOutsideClick !== undefined ? o.allowOutsideClick : false,
      allowEscapeKey:     o.allowEscapeKey    !== undefined ? o.allowEscapeKey    : false,
      showCloseButton:    o.showCloseButton   !== undefined ? o.showCloseButton   : true,
      focusConfirm:       false,
      didOpen:            o.didOpen,
      preConfirm:         o.preConfirm
    }).then(function (result) {
      if (result.isConfirmed && typeof o.callback === 'function') {
        o.callback(result.value);
      }
    });
  }

  function modalUrl(url, opts) {
    var o = opts || {};
    return Swal.fire({
      title:              o.title || '',
      html:               '<div id="swal-url-content" style="min-height:100px;">로딩중...</div>',
      width:              o.width || 700,
      showConfirmButton:  o.showConfirmButton !== undefined ? o.showConfirmButton : false,
      showCancelButton:   o.showCancelButton  !== undefined ? o.showCancelButton  : false,
      confirmButtonText:  o.confirmButtonText  || defaults.confirmButtonText,
      cancelButtonText:   o.cancelButtonText   || defaults.cancelButtonText,
      confirmButtonColor: o.confirmButtonColor || defaults.confirmButtonColor,
      cancelButtonColor:  o.cancelButtonColor  || defaults.cancelButtonColor,
      customClass:        o.customClass,
      buttonsStyling:     o.buttonsStyling !== undefined ? o.buttonsStyling : true,
      allowOutsideClick:  o.allowOutsideClick !== undefined ? o.allowOutsideClick : false,
      allowEscapeKey:     o.allowEscapeKey    !== undefined ? o.allowEscapeKey    : false,
      showCloseButton:    o.showCloseButton   !== undefined ? o.showCloseButton   : true,
      focusConfirm:       false,
      didOpen: function () {
        $('#swal-url-content').load(url, function (response, status, xhr) {
          if (status === 'error') {
            $('#swal-url-content').html('<p class="text-danger">내용을 불러오지 못했습니다.</p>');
            return;
          }
          if (typeof o.onLoad === 'function') o.onLoad();
        });
      },
      preConfirm: o.preConfirm
    }).then(function (result) {
      if (result.isConfirmed && typeof o.callback === 'function') {
        o.callback(result.value);
      }
    });
  }

  /**
   * 열려 있는 modal/modalUrl을 직접 닫는 헬퍼.
   * modal/modalUrl의 html 콘텐츠 내부 버튼에서 호출하는 용도.
   *
   * @param {boolean} [isConfirmed=false] true면 확인 처리(callback 실행), false/생략이면 그냥 닫힘
   * @param {*} [value] isConfirmed=true일 때 callback에 전달할 값
   */
  function close(isConfirmed, value) {
    Swal.close({ isConfirmed: !!isConfirmed, value: value });
  }

  return {
    alert:    alert,
    success:  success,
    error:    error,
    confirm:  confirm,
    toast:    toast,
    modal:    modal,
    modalUrl: modalUrl,
    close:    close
  };

})();