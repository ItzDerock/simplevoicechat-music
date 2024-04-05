package dev.derock.svcmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.audiosender.AudioSender;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.packets.ConvertablePacket;
import de.maxhenkel.voicechat.api.packets.Packet;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import net.minecraft.client.sound.StaticSound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class GroupManager extends AudioEventAdapter {
    private final Group group;
    private final AudioPlayer lavaplayer;
    private final MinecraftServer server;
    private final BlockingQueue<AudioTrack> queue;

    private ConcurrentHashMap<UUID, StaticAudioChannel> connections;
    private MutableAudioFrame currentFrame;

    public GroupManager(Group group, AudioPlayer player, MinecraftServer server) {
        this.group = group;
        this.server = server;
        this.lavaplayer = player;
        this.currentFrame = new MutableAudioFrame();

        // todo: max queue size
        this.queue = new LinkedBlockingQueue<>();

        // register events
        player.addListener(this);

        // schedule task
        startGroupTracking();
        startAudioFrameSending();
    }

    private void startAudioFrameSending() {
        ScheduledFuture<?> sendAudioFrames = SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (VoiceChatPlugin.voicechatServerApi == null) return;
            if (lavaplayer.provide(currentFrame)) {
                for (StaticAudioChannel channel : connections.values()) {
                    channel.send(currentFrame.getData());
                }
            }
        }, 0L, 20L, TimeUnit.MILLISECONDS);
    }

    private void startGroupTracking() {
        ScheduledFuture<?> groupPlayerTask = SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (VoiceChatPlugin.voicechatServerApi == null) return;

            HashSet<UUID> uuids = new HashSet<>();

            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                VoicechatConnection playerConnection = VoiceChatPlugin.voicechatServerApi.getConnectionOf(serverPlayer.getUuid());

                if (playerConnection == null || !playerConnection.isConnected()) continue;
                Group playerGroup = playerConnection.getGroup();
                if (playerGroup == null || playerGroup.getId() != this.group.getId()) continue;

                uuids.add(serverPlayer.getUuid());
                SimpleVoiceChatMusic.LOGGER.info("Found " + serverPlayer.getName() + " (" + serverPlayer.getUuid() + ") in group.");

                connections.computeIfAbsent(serverPlayer.getUuid(), (uuid) -> {
                    return VoiceChatPlugin.voicechatServerApi.createStaticAudioChannel(
                        UUID.randomUUID(),
                        VoiceChatPlugin.voicechatServerApi.fromServerLevel(serverPlayer.getWorld()),
                        playerConnection
                    );
                });
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
    }

    public boolean enqueueSong(AudioTrack track) {
        // noInterrupt true = false return if smth already playing
        if (!lavaplayer.startTrack(track, true)) {
            return this.queue.offer(track);
        }

        return true;
    }

    public void nextTrack() {
        // poll returns track or null
        // if null, lavaplayer stops
        lavaplayer.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // only start next if applicable
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }
}
