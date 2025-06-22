package dev.derock.svcmusic.core.api;

import dev.derock.svcmusic.core.translations.RichText;

import java.util.UUID;

public interface ServerPlayer {
    /**
     * Get the player's unique id.
     *
     * @return the player's unique id.
     */
    UUID getUniqueId();

    /**
     * Send a message to the player.
     * @param message The message to send.
     */
    void sendMessage(RichText message);

    /**
     * Get the platform-dependent player level (i.e. world-the-end, world-nether, etc.).
     * @return The platform-dependent player level.
     */
    Object getWorld();
}
