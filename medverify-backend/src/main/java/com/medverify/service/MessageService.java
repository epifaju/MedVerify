package com.medverify.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service pour faciliter l'accès aux messages i18n
 * 
 * Utilisation :
 * - messageService.get("auth.login.success")
 * - messageService.get("bdpm.import.progress", 1000)
 */
@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Obtient un message traduit dans la langue courante
     * 
     * @param key Clé du message (ex: "auth.login.success")
     * @return Message traduit
     */
    public String get(String key) {
        return get(key, null);
    }

    /**
     * Obtient un message traduit avec des paramètres
     * 
     * @param key  Clé du message
     * @param args Arguments pour les placeholders {0}, {1}, etc.
     * @return Message traduit avec paramètres
     */
    public String get(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }

    /**
     * Obtient un message dans une langue spécifique
     * 
     * @param key    Clé du message
     * @param locale Locale spécifique
     * @param args   Arguments pour les placeholders
     * @return Message traduit
     */
    public String get(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, key, locale);
    }

    /**
     * Obtient la locale courante
     * 
     * @return Locale courante
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Obtient le code de langue courant (fr, pt, cr)
     * 
     * @return Code de langue
     */
    public String getCurrentLanguage() {
        return LocaleContextHolder.getLocale().getLanguage();
    }
}
