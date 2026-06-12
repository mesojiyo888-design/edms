const CommonValidatorManager = {
  isEmpty(value) {
    return value === null || value === undefined || String(value).trim() === '';
  },

  isLength(value, { min, max } = {}) {
    const len = String(value ?? '').length;
    if (min != null && len < min) return false;
    if (max != null && len > max) return false;
    return true;
  },

  isNumber(value) {
    return /^[0-9]+$/.test(String(value));
  },

  isNumberWithLength(value, { min, max } = {}) {
    if (!CommonValidatorManager.isNumber(value)) return false;
    return CommonValidatorManager.isLength(value, { min, max });
  },

  isDecimal(value) {
    return /^-?\d+(\.\d+)?$/.test(String(value));
  },

  isEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value));
  },

  isPhone(value) {
    return /^01[0-9]-?\d{3,4}-?\d{4}$/.test(String(value));
  },

  isAlphaNumeric(value) {
    return /^[A-Za-z0-9]+$/.test(String(value));
  },

  isKorean(value) {
    return /^[가-힣]+$/.test(String(value));
  },

  matches(value, regex) {
    return regex.test(String(value));
  }
};

// ===== type별 검증 규칙 =====
const RULES = {
  text: (value, opts) => {
    if (opts.required && CommonValidatorManager.isEmpty(value)) return '필수 입력 항목입니다.';
    if (!CommonValidatorManager.isLength(value, opts)) {
      return `${opts.min ?? 0}~${opts.max ?? '무제한'}자 사이로 입력하세요.`;
    }
    return null;
  },

  number: (value, opts) => {
    if (opts.required && CommonValidatorManager.isEmpty(value)) return '필수 입력 항목입니다.';
    if (!CommonValidatorManager.isEmpty(value) && !CommonValidatorManager.isNumberWithLength(value, opts)) {
      return `숫자만 입력 가능하며 ${opts.min ?? 0}~${opts.max ?? '무제한'}자리여야 합니다.`;
    }
    return null;
  },

  email: (value, opts) => {
    if (opts.required && CommonValidatorManager.isEmpty(value)) return '필수 입력 항목입니다.';
    if (!CommonValidatorManager.isEmpty(value) && !CommonValidatorManager.isEmail(value)) return '이메일 형식이 올바르지 않습니다.';
    return null;
  },

  tel: (value, opts) => {
    if (opts.required && CommonValidatorManager.isEmpty(value)) return '필수 입력 항목입니다.';
    if (!CommonValidatorManager.isEmpty(value) && !CommonValidatorManager.isPhone(value)) return '전화번호 형식이 올바르지 않습니다.';
    return null;
  }
};

// ===== 단일 필드 검증 =====
function validateField(type, value, opts = {}) {
  const rule = RULES[type];
  if (!rule) throw new Error(`알 수 없는 타입: ${type}`);
  const message = rule(value, opts);
  return { valid: message === null, message };
}

// ===== 폼 전체 검증 + 실패 시 첫 invalid 요소로 focus =====
// schema 예시:
// const schema = [
//   { id: 'name',  type: 'text',   opts: { required: true, min: 2, max: 10 }, msgId: 'nameMsg' },
//   { id: 'age',   type: 'number', opts: { required: true, min: 1, max: 3 }, msgId: 'ageMsg' },
//   { id: 'email', type: 'email',  opts: { required: true }, msgId: 'emailMsg' },
// ];
function validateForm(schema, { focusOnError = true } = {}) {
  const results = {};
  let isValid = true;
  let firstInvalidEl = null;

  schema.forEach(({ id, type, opts, msgId }) => {
    const el = document.getElementById(id);
    const result = validateField(type, el.value, opts);
    results[id] = result;

    // 에러 메시지 출력 (msgId가 있는 경우)
    if (msgId) {
      const msgEl = document.getElementById(msgId);
      if (msgEl) msgEl.textContent = result.message ?? '';
    }

    if (!result.valid) {
      isValid = false;
      if (!firstInvalidEl) firstInvalidEl = el; // 가장 먼저 발견된 invalid 요소만 기억
    }
  });

  // 검증 실패 시 첫 번째 invalid 요소로 focus 이동
  if (!isValid && focusOnError && firstInvalidEl) {
    firstInvalidEl.focus();
    // input이 select 가능한 타입이면 텍스트 전체 선택 (선택 사항)
    if (typeof firstInvalidEl.select === 'function') {
      firstInvalidEl.select();
    }
  }

  return { isValid, results };
}
