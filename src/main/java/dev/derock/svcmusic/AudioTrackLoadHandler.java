package dev.derock.svcmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class AudioTrackLoadHandler implements AudioLoadResultHandler {

    private final ServerCommandSource source;
    private final GroupManager group;

    AudioTrackLoadHandler(ServerCommandSource source, GroupManager group) {
        this.source = source;
        this.group = group;

    }

    @Override
    public void trackLoaded(AudioTrack track) {
        group.enqueueSong(track);

        if (source != null) {
            source.sendFeedback(
                () -> Text.literal("Enqueued " + track.getInfo().title), false
            );
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        playlist.getTracks().forEach(gm::enqueueSong);

        if (source != null) {
            source.sendFeedback(
                () -> Text.literal("Enqueued " + playlist.getTracks().size() + " songs from playlist."),
                false
            );
        }
    }

    @Override
    public void noMatches() {
        if (source != null) {
            source.sendFeedback(
                () -> Text.literal("No matches found!"),
                false
            );
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (!exception.severity.equals(FriendlyException.Severity.COMMON)) {
            LOGGER.warn("Failed to load track from query: " + query, exception);
        }

        if (source != null) {
            source.sendFeedback(
                () -> Text.literal(
                    exception.severity == FriendlyException.Severity.COMMON
                        ? "Failed to load track: " + exception.getMessage()
                        : "Track failed to load! Check server logs for more information"
                ), false
            );
        }
    }
}
