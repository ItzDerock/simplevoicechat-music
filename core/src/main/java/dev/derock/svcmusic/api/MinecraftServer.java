package dev.derock.svcmusic.api;

import java.util.List;

/**
 * Represents the Minecraft server.
 */
public interface MinecraftServer {
  /**
   * Returns a list of online server players.
   */
  List<ServerPlayer> getPlayers();
}
