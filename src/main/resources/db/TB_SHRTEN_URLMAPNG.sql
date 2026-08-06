-- =====================================================================
-- 단축URL_매핑 (TB_SHRTEN_URLMAPNG)
-- 행정표준 용어사전 기준 (eXERD 자동 생성 용어 기반, 추후 검토/수정 가능)
-- 기존 테이블: SHORT_URL_MAPPING  →  표준 테이블: TB_SHRTEN_URLMAPNG
-- =====================================================================

CREATE TABLE TB_SHRTEN_URLMAPNG (
    SN                 NUMBER          NOT NULL,           -- 일련번호
    PROGRM_ID          VARCHAR2(100)   NOT NULL,           -- 프로그램_ID
    ORGINL_URL         VARCHAR2(500)   NOT NULL,           -- 원본_URL
    KUTT_ID            VARCHAR2(100)   NOT NULL,           -- KUTT 고유 아이디
    SHRTEN_URL         VARCHAR2(200)   NOT NULL,           -- 단축URL
    SHRTEN_URL_ADRES   VARCHAR2(100),                      -- 단축URL_주소
    SHRTEN_URL_DC      VARCHAR2(500),                      -- 단축URL_설명
    EXPIRT_PD          VARCHAR2(100),                      -- 만료_기간 ("7 days" 형식)
    USE_YN             CHAR(1)  DEFAULT 'Y' NOT NULL,      -- 사용_여부
    REGIST_DT          DATE     DEFAULT SYSDATE,           -- 등록_일시
    UPDT_DT            DATE,                               -- 수정_일시
    CONSTRAINT PK_SHRTEN_URLMAPNG PRIMARY KEY (SN),
    CONSTRAINT UQ_SHRTEN_URLMAPNG_PROGRM UNIQUE (PROGRM_ID)
);

CREATE SEQUENCE SEQ_SHRTEN_URLMAPNG START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE  TB_SHRTEN_URLMAPNG                    IS '단축URL_매핑';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.SN                IS '일련번호';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.PROGRM_ID         IS '프로그램_ID';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.ORGINL_URL        IS '원본_URL';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.KUTT_ID           IS 'KUTT 고유 아이디';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.SHRTEN_URL        IS '단축URL';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.SHRTEN_URL_ADRES  IS '단축URL_주소';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.SHRTEN_URL_DC     IS '단축URL_설명';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.EXPIRT_PD         IS '만료_기간';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.USE_YN            IS '사용_여부';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.REGIST_DT         IS '등록_일시';
COMMENT ON COLUMN TB_SHRTEN_URLMAPNG.UPDT_DT           IS '수정_일시';
