package dev.derock.svcmusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.util.ModUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class SearchLoadHandler implements AudioLoadResultHandler {

    protected final ServerCommandSource source;
    protected final GroupManager group;

    public SearchLoadHandler(ServerCommandSource source, GroupManager group) {
        this.source = source;
        this.group = group;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        group.enqueueSong(track);

        if (source != null) {
            this.group.broadcast(
                Text.literal("Enqueued ")
                    .append(ModUtils.trackInfo(track.getInfo(), true))
                    .append(" - ").append(Objects.requireNonNull(source.getPlayer()).getName())
            );
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        // if over 10, trim
        List<AudioTrack> loaded = playlist.getTracks().subList(0, 5);

        if (source != null) {
            // get all titles and create one large string
            MutableText text = Text.literal("Found " + loaded.size() + " results: \n");

            for (AudioTrack track : loaded) {
                text.append(Text.literal("  - "))
                    .append(ModUtils.trackInfo(track.getInfo(), true))
                    .append(Text.literal("\n"))
                    .append(Text.literal("    "))
                    .append(Text.literal("[Click to add to queue]").setStyle(
                        Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/music play \"" + track.getIdentifier() + "\""))
                    ))
                    .append(Text.literal("\n\n"));
            }

            source.sendFeedback(() -> text, false);
        }
    }

    @Override
    public void noMatches() {
        if (source != null) {
            source.sendFeedback(() -> Text.literal("No matches found!"), false);
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (!exception.severity.equals(FriendlyException.Severity.COMMON)) {
            SimpleVoiceChatMusic.LOGGER.warn("Failed to load track from query", exception);
        }

        if (source != null) {
            source.sendFeedback(() -> Text.literal(exception.severity == FriendlyException.Severity.COMMON ? "Failed to load track: " + exception.getMessage() : "Track failed to load! Check server logs for more information"), false);
        }
    }
}
