package dev.derock.svcmusic.core.api;

import dev.derock.svcmusic.core.translations.RichText;

public interface CommandSource {
    /**
     * Sends a message to the command source.
     */
    void sendMessage(RichText message);
}
