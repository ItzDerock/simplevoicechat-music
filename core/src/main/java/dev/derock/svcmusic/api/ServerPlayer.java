package dev.derock.svcmusic.api;

import java.util.UUID;

public interface ServerPlayer {
    /**
     * Get the player's unique id.
     *
     * @return the player's unique id.
     */
    UUID getUniqueId();
}
