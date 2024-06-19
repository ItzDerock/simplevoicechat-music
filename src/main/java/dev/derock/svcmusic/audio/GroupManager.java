package dev.derock.svcmusic.audio;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.VoiceChatPlugin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.*;

import static dev.derock.svcmusic.util.Constants.BASS_BOOST;

public class GroupManager {
    private final Group group;
    private final AudioPlayer lavaplayer;
    private final MinecraftServer server;
    private final BlockingQueue<AudioTrack> queue;

    private final ConcurrentHashMap<UUID, StaticAudioChannel> connections = new ConcurrentHashMap<>();
    private final MutableAudioFrame currentFrame;
    private final EqualizerFactory equalizer = new EqualizerFactory();

    private @Nullable ScheduledFuture<?> audioFrameSendingTask = null;
    private @Nullable ScheduledFuture<?> playerTrackingTask = null;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "SVCGroupMusicExecutor");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(
            (t, e) -> SimpleVoiceChatMusic.LOGGER.error("Uncaught exception in thread {}", t.getName(), e)
        );

        return thread;
    });

    public GroupManager(Group group, AudioPlayer player, MinecraftServer server) {
        this.group = group;
        this.server = server;
        this.lavaplayer = player;
        this.currentFrame = new MutableAudioFrame();

        // apply EQ
        this.lavaplayer.setFilterFactory(this.equalizer);
        this.lavaplayer.setFrameBufferDuration(500);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        currentFrame.setBuffer(buffer);

        // todo: max queue size
        this.queue = new LinkedBlockingQueue<>();

        // register events
        player.addListener(new TrackScheduler(this));

        // schedule task
        startGroupTracking();
        startAudioFrameSending();
    }

    private void startAudioFrameSending() {
        if (this.audioFrameSendingTask != null && !this.audioFrameSendingTask.isDone()) {
            // already started, so leave it.
            SimpleVoiceChatMusic.LOGGER.info("Not starting new audio frame sending task.");
            return;
        }

        if (this.audioFrameSendingTask != null && this.audioFrameSendingTask.isDone()) {
            // stop and restart
            SimpleVoiceChatMusic.LOGGER.info("Frame task in stuck state, attempting to revive");
            this.audioFrameSendingTask.cancel(true);
        }

        SimpleVoiceChatMusic.LOGGER.info("Starting new audio frame sending task.");
        this.audioFrameSendingTask = this.executorService.scheduleAtFixedRate(() -> {
            if (VoiceChatPlugin.voicechatServerApi == null) {
                return;
            }

            // check if playback is paused
            if (this.lavaplayer == null || this.lavaplayer.isPaused() || this.lavaplayer.getPlayingTrack() == null) {
                return;
            }

            if (lavaplayer.provide(this.currentFrame)) {
                for (StaticAudioChannel channel : connections.values()) {
                    channel.send(this.currentFrame.getData());
                }
            }
        }, 1000L, 20L, TimeUnit.MILLISECONDS);
    }

    private void startGroupTracking() {
        this.playerTrackingTask = executorService.scheduleAtFixedRate(() -> {
            if (VoiceChatPlugin.voicechatServerApi == null) return;

            HashSet<UUID> uuids = new HashSet<>();

            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                VoicechatConnection playerConnection = VoiceChatPlugin.voicechatServerApi.getConnectionOf(serverPlayer.getUuid());

                if (playerConnection == null || !playerConnection.isConnected()) continue;
                Group playerGroup = playerConnection.getGroup();
                if (playerGroup == null || playerGroup.getId() != this.group.getId()) continue;

                uuids.add(serverPlayer.getUuid());

                connections.computeIfAbsent(
                    serverPlayer.getUuid(),
                    (uuid) -> {
                        StaticAudioChannel channel = VoiceChatPlugin.voicechatServerApi.createStaticAudioChannel(
                            UUID.randomUUID(),
                            VoiceChatPlugin.voicechatServerApi.fromServerLevel(serverPlayer.getWorld()),
                            playerConnection
                        );

                        if (channel == null) return null;
                        channel.setCategory(VoiceChatPlugin.MUSIC_CATEGORY);

                        return channel;
                    }
                );
            }

            // now remove all that aren't here anymore
            for (UUID uuid : connections.keySet()) {
                if (uuids.contains(uuid)) continue;
                connections.remove(uuid);
            }

            // clean up if no players
            if (this.connections.isEmpty()) {
                SimpleVoiceChatMusic.LOGGER.info("Group {} is now empty. Cleaning up...", this.group.getName());
                this.cleanup();
            }

            // stop if no songs queued
            // if (this.lavaplayer.getPlayingTrack() == null && this.queue.isEmpty() && this.audioFrameSendingTask != null) {
            //     SimpleVoiceChatMusic.LOGGER.info("Pausing playback in {} due to empty queue", this.group.getName());
            //     this.audioFrameSendingTask.cancel(false);
            //     this.audioFrameSendingTask = null;
            // }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
    }

    public boolean enqueueSong(AudioTrack track) {
        // noInterrupt true => false return if smth already playing
        //                     true return if nothing playing
        if (!lavaplayer.startTrack(track, true)) {
            return this.queue.offer(track);
        }

        return true;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void nextTrack() {
        // ensure this happens in the correct thread
        this.executorService.execute(() -> {
            // poll returns track or null
            // if null, lavaplayer stops
            AudioTrack track = queue.poll();
            lavaplayer.startTrack(track, false);

            // revive task if needed
            if (track != null) {
                this.startAudioFrameSending();
            } else {
                // no more songs to play, so quit
                this.cleanup();
            }
        });
    }

    public AudioPlayer getPlayer() {
        return this.lavaplayer;
    }

    public void broadcast(MutableText text) {
        // execute on main thread
        server.execute(() -> {
            ServerPlayerEntity[] players = server.getPlayerManager().getPlayerList().stream().filter(
                (player) -> this.connections.containsKey(player.getUuid())
            ).toArray(ServerPlayerEntity[]::new);

            for (ServerPlayerEntity player : players) {
                player.sendMessage(text);
            }
        });
    }

    public void cleanup() {
        this.broadcast(Text.literal("No more songs to play."));
        if (this.audioFrameSendingTask != null) this.audioFrameSendingTask.cancel(true);
        this.lavaplayer.destroy();
        MusicManager.getInstance().deleteGroup(this.group);
        if (this.playerTrackingTask != null) this.playerTrackingTask.cancel(false);
        this.executorService.shutdown();
    }

    public void setBassBoost(float percentage) {
        final float multiplier = percentage / 100.00f;

        for (int i = 0; i < BASS_BOOST.length; i++) {
            this.equalizer.setGain(i, BASS_BOOST[i] * multiplier);
        }
    }
}
