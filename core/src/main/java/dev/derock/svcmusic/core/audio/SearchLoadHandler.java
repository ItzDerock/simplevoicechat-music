package dev.derock.svcmusic.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.derock.svcmusic.core.SimpleVoiceChatMusic;
import dev.derock.svcmusic.core.api.CommandSource;
import dev.derock.svcmusic.core.translations.Translations;
import dev.derock.svcmusic.core.util.ModUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

public class SearchLoadHandler implements AudioLoadResultHandler {
    protected final CommandSource source;
    protected final GroupManager group;

    public SearchLoadHandler(CommandSource source, GroupManager group) {
        this.source = source;
        this.group = group;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        group.enqueueSong(track);

        if (source != null) {
            // this.group.broadcast(
            //     Text.literal("Enqueued ")
            //         .append(ModUtils.trackInfo(track.getInfo(), true))
            //         .append(" - ").append(Objects.requireNonNull(source.getPlayer()).getName())
            // );

            this.group.broadcast(Translations.load("song_enqueued", java.util.Map.of(
                "song", ModUtils.trackInfo(track.getInfo(), true)
            )));
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
