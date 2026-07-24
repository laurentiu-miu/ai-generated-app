package com.example.fileimporter;

import com.example.fileimporter.controller.LocaleController;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LocaleControllerTest {
    @Test
    void persistsRomanianAndRejectsExternalRedirects() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("file-importer-locale");
        resolver.setDefaultLocale(Locale.ENGLISH);
        LocaleController controller = new LocaleController(resolver);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String redirect = controller.changeLanguage("ro", "https://example.com", request, response);

        assertThat(redirect).isEqualTo("redirect:/parents");
        assertThat(response.getCookies()).extracting(Cookie::getValue).contains("ro");
    }
}
