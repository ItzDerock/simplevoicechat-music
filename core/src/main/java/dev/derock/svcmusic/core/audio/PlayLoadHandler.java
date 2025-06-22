package dev.derock.svcmusic.core.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import dev.derock.svcmusic.core.api.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

public class PlayLoadHandler extends SearchLoadHandler {
    public PlayLoadHandler(CommandSource source, GroupManager group) {
        super(source, group);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        super.trackLoaded(playlist.getTracks().get(0));
    }
}
