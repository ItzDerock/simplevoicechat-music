package dev.derock.svcmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.audiosender.AudioSender;

public class GroupManager {
    private AudioSender sender;
    final private Group group;

    public GroupManager(Group group) {
       this.group = group;
    }

    public void enqueueSong() {

    }
}
