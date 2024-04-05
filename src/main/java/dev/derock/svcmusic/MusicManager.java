package dev.derock.svcmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import de.maxhenkel.voicechat.api.Group;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class MusicManager {
    private static MusicManager instance;
    public AudioPlayerManager playerManager;
    private HashMap<UUID, GroupManager> groups = new HashMap<>();

    public MusicManager() {
        SimpleVoiceChatMusic.LOGGER.info("Loading lavaplayer...");
        this.playerManager = new DefaultAudioPlayerManager();
        SimpleVoiceChatMusic.LOGGER.info("Registering lavaplayer...");

        YoutubeAudioSourceManager manager = null;

        try {
             manager = new YoutubeAudioSourceManager(true, null, null);
             SimpleVoiceChatMusic.LOGGER.info("Created manager!");
            playerManager.registerSourceManager(manager);
        } catch (Exception err) {
            err.printStackTrace();
            SimpleVoiceChatMusic.LOGGER.error("Failed to register: ", err);
        } finally {
            SimpleVoiceChatMusic.LOGGER.info("finally");
            SimpleVoiceChatMusic.LOGGER.info(manager == null ? "Manager is null" : manager.toString());
        }

        System.out.println("registered lavalink!");
    }

    public static MusicManager getInstance() {
        SimpleVoiceChatMusic.LOGGER.info("asdfghjk");
        if (instance == null) instance = new MusicManager();
        SimpleVoiceChatMusic.LOGGER.info("instance: " + instance.playerManager);
        return instance;
    }

    public GroupManager getGroup(Group group, MinecraftServer server) {
        if (groups.containsKey(group.getId())) {
            return groups.get(group.getId());
        } else {
            GroupManager gm = new GroupManager(group, playerManager.createPlayer(), server);
            groups.put(group.getId(), gm);
            return gm;
        }
    }
}
