package ch.aaap.prototype.platform.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.util.HtmlUtils;

/**
 * The one layout of the Spring-rendered entry pages, /gate and /login: a single form in a card,
 * styled with the 3ap design tokens. The CSS (classpath:platform/, generated entry-tokens.css plus
 * entry-page.css) is inlined, because nothing but the entry page itself is served without a
 * session; no script.
 */
public final class EntryPage {

  private static final String STYLE =
      read("platform/entry-tokens.css") + read("platform/entry-page.css");

  private EntryPage() {}

  /**
   * @param fieldsHtml the form's labels and inputs, trusted markup from the caller
   * @param error shown above the form, or null; escaped here
   */
  public static String html(
      String title,
      String action,
      CsrfToken csrf,
      String fieldsHtml,
      String submitLabel,
      String error) {
    String errorHtml =
        error == null ? "" : "<p role=\"alert\">" + HtmlUtils.htmlEscape(error) + "</p>\n";
    return """
    <!doctype html>
    <html lang="en">
    <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="robots" content="noindex"><title>%1$s</title>
    <style>
    %2$s</style>
    </head>
    <body>
    <main>
    <h1>%1$s</h1>
    %3$s<form method="post" action="%4$s">
    %5$s
    <input type="hidden" name="%6$s" value="%7$s">
    <button type="submit">%8$s</button>
    </form>
    </main>
    </body>
    </html>
    """
        .formatted(
            HtmlUtils.htmlEscape(title),
            STYLE,
            errorHtml,
            HtmlUtils.htmlEscape(action),
            fieldsHtml,
            HtmlUtils.htmlEscape(csrf.getParameterName()),
            HtmlUtils.htmlEscape(csrf.getToken()),
            HtmlUtils.htmlEscape(submitLabel));
  }

  private static String read(String path) {
    try {
      return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Missing " + path + "; run make generate", e);
    }
  }
}
