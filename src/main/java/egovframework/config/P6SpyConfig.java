package egovframework.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import com.p6spy.engine.spy.P6SpyOptions;
import java.util.Locale;

@Configuration
public class P6SpyConfig implements MessageFormattingStrategy {

    @PostConstruct
    public void setLogMessageFormat() {
        // P6Spy에 이 커스텀 포맷터 클래스를 등록합니다.
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6SpyConfig.class.getName());
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        // 1. 실제 실행되는 SQL문(STATEMENT) 가공 및 눈금선 적용
        if (Category.STATEMENT.getName().equals(category)) {
            sql = formatSql(category, sql);
            if (sql.trim().isEmpty()) {
                return "";
            }

            return String.format("\n======================================================================\n" +
                    "[SQL 실행 시간: %d ms]\n" +
                    "%s" +
                    "\n======================================================================", elapsed, sql);
        }

        // 2. [추가] 데이터 결과값(RESULTSET) 정보 가공 및 눈금선 적용
        if (Category.RESULTSET.getName().equals(category)) {
            return String.format("\n======================================================================\n" +
                    "[DATA RESULTSET - 결과 데이터 정보]\n" +
                    "%s" +
                    "\n======================================================================", sql);
        }

        return "";
    }

    private String formatSql(String category, String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        if (Category.STATEMENT.getName().equals(category)) {
            String tmpsql = sql.trim().toLowerCase(Locale.ROOT);

            // 기존 조건에 select, insert, update, delete 추가하여 포맷팅이 먹히도록 개선
            if (tmpsql.startsWith("create") || tmpsql.startsWith("alter") || tmpsql.startsWith("comment")) {
                return FormatStyle.DDL.getFormatter().format(sql);
            } else {
                return FormatStyle.BASIC.getFormatter().format(sql);
            }
        }
        return sql;
    }
}
