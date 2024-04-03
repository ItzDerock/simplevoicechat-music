package dev.derock.svcmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.audiosender.AudioSender;
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
    private AudioSender sender;

    private ConcurrentHashMap<UUID, StaticAudioChannel> audioChannels;

    public GroupManager(Group group, AudioPlayer player, MinecraftServer server) {
        this.group = group;
        this.server = server;
        this.lavaplayer = player;

        // todo: max queue size
        this.queue = new LinkedBlockingQueue<>();

        // register events
        player.addListener(this);

        // schedule task
        ScheduledFuture<?> groupPlayerTask = SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (VoiceChatPlugin.voicechatServerApi == null) return;

            HashSet<UUID> uuids = new HashSet<>();

            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                VoicechatConnection playerConnection = VoiceChatPlugin.voicechatServerApi.getConnectionOf(serverPlayer.getUuid());

                if (playerConnection == null || !playerConnection.isConnected()) continue;
                Group playerGroup = playerConnection.getGroup();
                if (playerGroup == null || !playerGroup.equals(this.group)) continue;

                uuids.add(serverPlayer.getUuid());
                this.audioChannels.computeIfAbsent(serverPlayer.getUuid(), uuid -> {
                    StaticAudioChannel audioChannel = VoiceChatPlugin.voicechatServerApi.createStaticAudioChannel(
                        UUID.randomUUID(),
                        VoiceChatPlugin.voicechatServerApi.fromServerLevel(serverPlayer.)
                })
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
