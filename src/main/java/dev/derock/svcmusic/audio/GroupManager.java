package dev.derock.svcmusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.VoiceChatPlugin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.*;

public class GroupManager {
    private final Group group;
    private final AudioPlayer lavaplayer;
    private final MinecraftServer server;
    private final BlockingQueue<AudioTrack> queue;

    private final ConcurrentHashMap<UUID, StaticAudioChannel> connections = new ConcurrentHashMap<>();
    private final MutableAudioFrame currentFrame;
    private @Nullable ScheduledFuture<?> audioFrameSendingTask = null;
    private @Nullable ScheduledFuture<?> playerTrackingTask = null;

    public GroupManager(Group group, AudioPlayer player, MinecraftServer server) {
        this.group = group;
        this.server = server;
        this.lavaplayer = player;
        this.currentFrame = new MutableAudioFrame();
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
        if (this.audioFrameSendingTask != null && this.audioFrameSendingTask.isDone()) {
            // stop and restart
            this.audioFrameSendingTask.cancel(true);
        }

        this.audioFrameSendingTask = SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (VoiceChatPlugin.voicechatServerApi == null) {
                return;
            }

            try {
                if (lavaplayer.provide(this.currentFrame, 20, TimeUnit.MILLISECONDS)) {
                    for (StaticAudioChannel channel : connections.values()) {
                        channel.send(this.currentFrame.getData());
                    }
                }
            } catch (TimeoutException | InterruptedException ignored) {}
        }, 1000L, 20L, TimeUnit.MILLISECONDS);
    }

    private void startGroupTracking() {
        this.playerTrackingTask = SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
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
                if (this.audioFrameSendingTask != null) this.audioFrameSendingTask.cancel(true);
                this.lavaplayer.destroy();
                MusicManager.getInstance().deleteGroup(this.group);

                if (this.playerTrackingTask != null) this.playerTrackingTask.cancel(false);
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
        // poll returns track or null
        // if null, lavaplayer stops
        lavaplayer.startTrack(queue.poll(), false);

        // revive task if needed
        this.startAudioFrameSending();
    }

    public AudioPlayer getPlayer() {
        return this.lavaplayer;
    }

    public void broadcast(MutableText text) {
        ServerPlayerEntity[] players = server.getPlayerManager().getPlayerList().stream().filter(
            (player) -> this.connections.containsKey(player.getUuid())
        ).toArray(ServerPlayerEntity[]::new);

       for (ServerPlayerEntity player : players) {
           player.sendMessage(text);
       }
    }
}
