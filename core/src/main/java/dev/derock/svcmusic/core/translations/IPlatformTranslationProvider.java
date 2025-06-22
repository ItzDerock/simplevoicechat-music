package dev.derock.svcmusic.core.translations;

import java.util.Map;

public interface IPlatformTranslationProvider {
    /**
     * Formats and expands all variables in the given translation string.
     *
     * @param raw          The raw translation string from the config
     * @param placeholders Placeholders to replace
     */
    RichText formatTranslation(String raw, @Nullable Map<String, Object> placeholders);

    /**
     * Returns the raw translation string from the config
     *
     * @param key
     */
    String getTranslationRaw(String key);
}
