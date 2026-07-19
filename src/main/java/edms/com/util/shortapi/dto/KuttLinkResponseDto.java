package edms.com.util.shortapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Kutt API 링크 단건 응답 DTO
 *
 * [Kutt API 응답 필드]
 * {
 *   "id": "accefebc-f139-4f08-8b6a-e45b89ce0873",
 *   "address": "NFJ",
 *   "description": null,
 *   "banned": false,
 *   "password": false,
 *   "expire_in": null,
 *   "target": "https://example.com",
 *   "visit_count": 0,
 *   "created_at": "2020-11-06T17:26:22.533Z",
 *   "updated_at": "2020-11-06T17:26:22.533Z",
 *   "link": "https://kutt.to/NFJ"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KuttLinkResponseDto {

    /** Kutt 내부 고유 ID (UUID) — 수정/삭제 시 사용 */
    private String id;

    /** 단축 주소 (path 부분만, 예: "NFJ") */
    private String address;

    /** 링크 설명 */
    private String description;

    /** 차단 여부 */
    private boolean banned;

    /** 비밀번호 설정 여부 */
    private boolean password;

    /** 만료 일시 (ISO 8601, null이면 만료 없음) */
    private String expire_in;

    /** 원본(대상) URL */
    private String target;

    /** 방문 횟수 */
    private int visit_count;

    /** 생성 일시 (ISO 8601) */
    private String created_at;

    /** 수정 일시 (ISO 8601) */
    private String updated_at;

    /** 완성된 단축 URL (예: https://kutt.to/NFJ) */
    private String link;

    // ===================== Getter / Setter =====================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }

    public boolean isPassword() { return password; }
    public void setPassword(boolean password) { this.password = password; }

    public String getExpire_in() { return expire_in; }
    public void setExpire_in(String expire_in) { this.expire_in = expire_in; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public int getVisit_count() { return visit_count; }
    public void setVisit_count(int visit_count) { this.visit_count = visit_count; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    @Override
    public String toString() {
        return "KuttLinkResponseDto{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", target='" + target + '\'' +
                ", link='" + link + '\'' +
                ", expire_in='" + expire_in + '\'' +
                ", visit_count=" + visit_count +
                '}';
    }
}
