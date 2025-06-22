package dev.derock.svcmusic.core.api;

import java.util.List;

/**
 * Represents the Minecraft server.
 */
public interface MinecraftServer {
  /**
   * Returns a list of online server players.
   */
  List<ServerPlayer> getPlayers();

  /**
   * Executes the given lambda on the server thread.
   * @param func the function to execute
   */
  void execute(Runnable func);
}
