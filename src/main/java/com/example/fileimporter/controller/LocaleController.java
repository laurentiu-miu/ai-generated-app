package com.example.fileimporter.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Map;

@Controller
public class LocaleController {
    private static final Map<String, Locale> SUPPORTED = Map.of(
            "en", Locale.ENGLISH,
            "ro", Locale.forLanguageTag("ro")
    );

    private final LocaleResolver localeResolver;

    public LocaleController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @PostMapping("/language")
    public String changeLanguage(@RequestParam String language, @RequestParam(defaultValue = "/parents") String redirect,
                                 HttpServletRequest request, HttpServletResponse response) {
        Locale locale = SUPPORTED.getOrDefault(language, Locale.ENGLISH);
        localeResolver.setLocale(request, response, locale);
        return "redirect:" + safeRedirect(redirect);
    }

    private String safeRedirect(String redirect) {
        if (!redirect.startsWith("/") || redirect.startsWith("//") || redirect.contains("\\")) {
            return "/parents";
        }
        return redirect;
    }
}
