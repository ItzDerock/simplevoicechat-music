package dev.derock.svcmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import de.maxhenkel.voicechat.api.Group;

import java.util.HashMap;
import java.util.UUID;

public class MusicManager {
    private static MusicManager instance = new MusicManager();
    public static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private HashMap<UUID, GroupManager> groups = new HashMap<>();

    public MusicManager() {
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public static MusicManager getInstance() {
        return instance;
    }

    public GroupManager getGroup(Group group) {
        if (groups.containsKey(group.getId())) {
            return groups.get(group.getId());
        } else {
            GroupManager gm = new GroupManager(group);
            groups.put(group.getId(), gm);
            return gm;
        }
    }
}
