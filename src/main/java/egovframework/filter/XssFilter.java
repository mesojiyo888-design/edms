package egovframework.filter;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public class XssFilter implements Filter
{
    private List<String> excludeParams;

    @Override
    public void init(FilterConfig filterConfig)
    {
        WebApplicationContext ctx =
                WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());

        if(ctx != null)
        {
            XssProperties xssProperties = ctx.getBean(XssProperties.class);
            this.excludeParams = xssProperties.getExcludeParams();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestUri = httpRequest.getRequestURI();

        if(requestUri.startsWith(httpRequest.getContextPath() + "/raonkHandler.do"))
        {
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request, excludeParams), response);

    }

    @Override
    public void destroy() {}
}