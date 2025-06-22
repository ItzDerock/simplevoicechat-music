package dev.derock.svcmusic.core.translations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translations {
    private static IPlatformTranslationProvider provider;
    private static final Logger LOGGER = LoggerFactory.getLogger("simple-voice-chat-music");
    private static final Pattern MACRO_PATTERN = Pattern.compile("\\{\\{(macro..+?)}}");

    /**
     * Initializes the translation provider. Must be called before any translations are used.
     *
     * @param provider The translation provider to use
     */
    public static void init(IPlatformTranslationProvider provider) {
        if (Translations.provider != null) {
            LOGGER.warn("Translation provider already initialized, ignoring new provider.");
            return;
        }

        Translations.provider = provider;
    }

    /**
     * Loads, formats, and prepares a translatable text component.
     *
     * @param key          The translation key from your messages.yml.
     * @param replacements List of replacements for the placeholders in the translation.
     * @return A RichText object.
     */
    public static RichText load(String key, @Nullable Map<String, Object> replacements) {
        if (provider == null) {
            throw new IllegalStateException("Translations have not been initialized! The platform must call Translations.init() at startup.");
        }

        String raw = provider.getTranslationRaw(key);
        if (raw == null) {
            LOGGER.warn("Translation key '{}' not found in translations.", key);
            raw = "Missing translation: " + key;
        }

        String formatted = replaceMacros(raw);
        return provider.formatTranslation(formatted, replacements);
    }

    /**
     * Loads, formats, and prepares a translatable text component.
     *
     * @param key The translation key from your messages.yml.
     * @return A RichText object.
     */
    public static RichText load(String key) {
        return load(key, null);
    }

    /**
     * Expands macros in a string.
     *
     * @param text The text to expand macros in. Macros are in the format {{macro.<key>}}.
     * @return The expanded text. Macros are replaced with their corresponding values from the translation provider.
     */
    private static String replaceMacros(String text) {
        Matcher matcher = MACRO_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        // if no matches, return the original string
        if (!matcher.find()) {
            return text;
        }

        // loop through all matches and replace with corresponding
        do {
            // macro should be macros.<key>
            String macro = matcher.group(1);
            String replacement = provider.getTranslationRaw(macro);

            // if macro contains more macros, raise a recursive warning
            if (MACRO_PATTERN.matcher(replacement).find()) {
                LOGGER.warn("Recursive macro detected in translation with key '{}'.", macro);
                LOGGER.warn("Macros in macros are not supported as they can cause infinite recursion.");

                replacement = matcher.group();
            }

            if (replacement == null) {
                LOGGER.warn("Macro '{}' not found in translations.", macro);
                replacement = matcher.group();
            }

            matcher.appendReplacement(sb, replacement);
        } while (matcher.find());

        return sb.toString();
    }
}
