package com.medverify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Arrays;
import java.util.Locale;

/**
 * Configuration i18n pour MedVerify
 * 
 * Support des langues :
 * - Français (FR)
 * - Portugais (PT)
 * - Créole Bissau-Guinéen (CR)
 * 
 * La langue est détectée automatiquement depuis le header Accept-Language
 * ou peut être changée avec le paramètre ?lang=pt
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * Résolveur de locale basé sur le header Accept-Language
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.FRENCH);
        localeResolver.setSupportedLocales(Arrays.asList(
                Locale.FRENCH, // fr
                new Locale("pt"), // pt
                new Locale("cr") // cr (créole)
        ));
        return localeResolver;
    }

    /**
     * Interceptor pour changer la langue via paramètre ?lang=xx
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Source des messages i18n
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Enregistrer l'interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}





