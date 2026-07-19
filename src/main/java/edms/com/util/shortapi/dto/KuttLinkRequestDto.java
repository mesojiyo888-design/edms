package edms.com.util.shortapi.dto;

/**
 * Kutt API 링크 생성/수정 요청 DTO
 *
 * [Kutt API 필드 설명]
 * - target      : (필수) 단축할 원본 URL
 * - description : (선택) 링크 설명
 * - expire_in   : (선택) 만료 기간. 예) "1 days", "24 hours", "3 months"
 *                  형식: "{숫자} {단위}" — minutes/hours/days/weeks/months
 * - address     : (선택) 커스텀 단축 주소 (예: "my-page" → kutt.to/my-page)
 * - password    : (선택) 접근 비밀번호
 * - reuse       : (선택) 동일 target이 존재하면 기존 URL 재사용 여부 (POST 전용)
 */
public class KuttLinkRequestDto {

    /** 단축할 원본 URL (필수) */
    private String target;

    /** 링크 설명 (선택) */
    private String description;

    /**
     * 만료 기간 (선택) — null이면 만료 없음
     * 형식: "1 days" / "24 hours" / "3 months" 등
     */
    private String expire_in;

    /** 커스텀 단축 주소 (선택) */
    private String address;

    /** 접근 비밀번호 (선택) */
    private String password;

    /**
     * 동일 target URL 재사용 여부 (POST 전용, 선택)
     * true: 동일 target이 이미 있으면 기존 단축 URL 반환
     */
    private Boolean reuse;

    // ===================== 생성자 =====================

    public KuttLinkRequestDto() {}

    /** 가장 기본적인 생성 (target만 필수) */
    public KuttLinkRequestDto(String target) {
        this.target = target;
    }

    /** expire_in 포함 생성자 */
    public KuttLinkRequestDto(String target, String description, String expire_in) {
        this.target = target;
        this.description = description;
        this.expire_in = expire_in;
    }

    // ===================== Getter / Setter =====================

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExpire_in() { return expire_in; }
    public void setExpire_in(String expire_in) { this.expire_in = expire_in; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getReuse() { return reuse; }
    public void setReuse(Boolean reuse) { this.reuse = reuse; }

    @Override
    public String toString() {
        return "KuttLinkRequestDto{" +
                "target='" + target + '\'' +
                ", description='" + description + '\'' +
                ", expire_in='" + expire_in + '\'' +
                ", address='" + address + '\'' +
                ", reuse=" + reuse +
                '}';
    }
}
