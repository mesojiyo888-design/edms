package egovframework.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edms.common.util.HtmlEscapeUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XssRequestWrapper extends HttpServletRequestWrapper
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final List<String> excludeParams;
    private byte[] sanitizedBody;

    public XssRequestWrapper(HttpServletRequest request, List<String> excludeParams)
    {
        super(request);
        this.excludeParams = excludeParams;

        String contentType = request.getContentType();

        if(contentType != null && contentType.toLowerCase().contains("application/json"))
        {
            try
            {
                this.sanitizedBody = sanitizeJsonBody(request);
            }
            catch(IOException e)
            {
                this.sanitizedBody = null;   // 파싱 실패 시 원본 바디 그대로 흘려보냄
            }
        }
    }

    private boolean isExcluded(String parameterName)
    {
        return excludeParams != null && excludeParams.contains(parameterName);
    }

    @Override
    public Map<String, String[]> getParameterMap()
    {
        Map<String, String[]> map    = super.getParameterMap();
        Map<String, String[]> newMap = new HashMap<>();

        for(Map.Entry<String, String[]> entry : map.entrySet())
        {
            String   paramName = entry.getKey();
            String[] values    = entry.getValue();

            if(isExcluded(paramName))
            {
                newMap.put(paramName, values);   // 이스케이프 안 하고 원본 그대로
                continue;
            }

            String[] encodedValues = new String[values.length];

            for(int i = 0; i < values.length; i++)
            {
                encodedValues[i] = HtmlEscapeUtils.escape(values[i]);
            }

            newMap.put(paramName, encodedValues);
        }

        return newMap;
    }

    @Override
    public String[] getParameterValues(String parameter)
    {
        String[] values = super.getParameterValues(parameter);

        if(values == null)
        {
            return null;
        }

        if(isExcluded(parameter))
        {
            return values;
        }

        String[] encoded = new String[values.length];

        for(int i = 0; i < values.length; i++)
        {
            encoded[i] = HtmlEscapeUtils.escape(values[i]);
        }

        return encoded;
    }

    @Override
    public String getParameter(String parameter)
    {
        String value = super.getParameter(parameter);

        if(isExcluded(parameter))
        {
            return value;
        }

        return HtmlEscapeUtils.escape(value);
    }

    private byte[] sanitizeJsonBody(HttpServletRequest request) throws IOException
    {
        byte[] rawBody = readRawBody(request);

        if(rawBody.length == 0)
        {
            return rawBody;
        }

        JsonNode rootNode = OBJECT_MAPPER.readTree(rawBody);
        sanitizeNode(rootNode);

        return OBJECT_MAPPER.writeValueAsBytes(rootNode);
    }

    private byte[] readRawBody(HttpServletRequest request) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try(InputStream is = request.getInputStream())
        {
            byte[] buffer = new byte[1024];
            int    len;

            while((len = is.read(buffer)) != -1)
            {
                baos.write(buffer, 0, len);
            }
        }

        return baos.toByteArray();
    }

    private void sanitizeNode(JsonNode node)
    {
        if(node.isObject())
        {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

            while(fields.hasNext())
            {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();

                if(isExcluded(fieldName))
                {
                    continue;
                }

                JsonNode child = entry.getValue();

                if(child.isTextual())
                {
                    objectNode.put(fieldName, HtmlEscapeUtils.escape(child.asText()));
                }
                else if(child.isObject() || child.isArray())
                {
                    sanitizeNode(child);
                }
            }
        }
        else if(node.isArray())
        {
            ArrayNode arrayNode = (ArrayNode) node;

            for(int i = 0; i < arrayNode.size(); i++)
            {
                JsonNode child = arrayNode.get(i);

                if(child.isTextual())
                {
                    arrayNode.set(i, OBJECT_MAPPER.getNodeFactory().textNode(HtmlEscapeUtils.escape(child.asText())));
                }
                else if(child.isObject() || child.isArray())
                {
                    sanitizeNode(child);
                }
            }
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        if(sanitizedBody == null)
        {
            return super.getInputStream();
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(sanitizedBody);

        return new ServletInputStream()
        {
            @Override
            public boolean isFinished()
            {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {}

            @Override
            public int read() throws IOException
            {
                return bais.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        if(sanitizedBody == null)
        {
            return super.getReader();
        }

        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
}