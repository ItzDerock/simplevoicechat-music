package dev.derock.svcmusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.util.ModUtils;
import net.minecraft.text.Text;

public class TrackScheduler extends AudioEventAdapter {
    private final GroupManager group;

    TrackScheduler(GroupManager groupManager) {
        super();
        this.group = groupManager;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.group.broadcast(Text.literal("Now playing: ").append(
            ModUtils.hyperlink(track.getInfo().title + " by " + track.getInfo().author, track.getInfo().uri)));
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // only start next if applicable
        if (endReason.mayStartNext) {
            this.group.nextTrack();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        SimpleVoiceChatMusic.LOGGER.error(exception.getMessage());

        if (exception.severity == FriendlyException.Severity.COMMON) {
            this.group.broadcast(Text.literal("Failed to play song: " + exception.getMessage()));
        } else {
            this.group.broadcast(Text.literal("Failed to play song due to an internal error."));
        }

        this.group.nextTrack();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        this.group.broadcast(Text.literal("Track stuck -- skipping!"));
        this.group.nextTrack();
    }
}
