package egovframework.filter;

import edms.com.util.HtmlEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public class XssRequestWrapper extends HttpServletRequestWrapper {
    public XssRequestWrapper(HttpServletRequest request) { super(request); }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = super.getParameterMap();
        Map<String, String[]> newMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String[] values = entry.getValue();
            String[] encodedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                encodedValues[i] = HtmlEscapeUtils.escape(values[i]);
            }
            newMap.put(entry.getKey(), encodedValues);
        }
        return newMap;
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) return null;
        String[] encoded = new String[values.length];
        for (int i = 0; i < values.length; i++) encoded[i] = HtmlEscapeUtils.escape(values[i]);
        return encoded;
    }

    @Override
    public String getParameter(String parameter) {
        return HtmlEscapeUtils.escape(super.getParameter(parameter));
    }
}