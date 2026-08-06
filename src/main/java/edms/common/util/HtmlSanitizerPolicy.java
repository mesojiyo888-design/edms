package edms.common.util;

import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.Arrays;
import java.util.regex.Pattern;



public class HtmlSanitizerPolicy {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "b", "strong", "i", "em", "u", "s", "del",
                    "ul", "ol", "li", "h1", "h2", "h3", "h4", "blockquote",
                    "table", "thead", "tbody", "tr", "td", "th",
                    "img", "a", "pre", "code", "hr", "span", "div")
            .allowAttributes("href").onElements("a")
            .allowAttributes("target").matching(Pattern.compile("^_blank$")).onElements("a")
            .allowAttributes("src", "alt", "width", "height").onElements("img")
            .allowAttributes("class").matching(Pattern.compile("^language-[a-zA-Z0-9]+$")).onElements("code")
            .allowAttributes("colspan", "rowspan").onElements("td", "th")
            .allowStyling(CssSchema.withProperties(
                    Arrays.asList("color", "background-color", "text-align")))
            .allowAttributes("style").onElements("span", "p", "td", "th")
            .allowUrlProtocols("http", "https")
            .requireRelNofollowOnLinks()
            .toFactory();

    public static String sanitize(String html) {
        if(html == null) {
            return null;
        }
        return POLICY.sanitize(html);
    }

}