package dev.derock.svcmusic.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import net.minecraft.server.command.ServerCommandSource;

public class PlayLoadHandler extends SearchLoadHandler{
    public PlayLoadHandler(ServerCommandSource source, GroupManager group) {
        super(source, group);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        super.trackLoaded(playlist.getTracks().get(0));
    }
}
