package edms.com.util.shortapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Kutt API 링크 목록 조회 응답 DTO
 *
 * GET /api/v2/links 응답 형식:
 * {
 *   "total": 1,
 *   "limit": 10,
 *   "skip": 0,
 *   "data": [ { ...KuttLinkResponseDto... } ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KuttLinkListResponseDto {

    /** 전체 링크 수 */
    private int total;

    /** 요청 시 limit 값 */
    private int limit;

    /** 요청 시 skip(offset) 값 */
    private int skip;

    /** 링크 목록 */
    private List<KuttLinkResponseDto> data;

    // ===================== Getter / Setter =====================

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public int getSkip() { return skip; }
    public void setSkip(int skip) { this.skip = skip; }

    public List<KuttLinkResponseDto> getData() { return data; }
    public void setData(List<KuttLinkResponseDto> data) { this.data = data; }
}
