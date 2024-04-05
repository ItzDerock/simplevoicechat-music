package dev.derock.svcmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VoiceChatPlugin implements VoicechatPlugin {

    public static String MUSIC_CATEGORY = "streamed_music";

    public static VoicechatApi voicechatApi;
    @Nullable
    public static VoicechatServerApi voicechatServerApi;
    @Nullable
    public static VolumeCategory musicVolumeCategory;

    @Override
    public String getPluginId() {
        return "simplevoicecchat_music";
    }

    @Override
    public void initialize(VoicechatApi api) {
        SimpleVoiceChatMusic.LOGGER.info("Voicechat API initialized!");
        voicechatApi = api;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStart);
    }

    private void onServerStart(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();
        musicVolumeCategory = voicechatServerApi.volumeCategoryBuilder()
                .setId(MUSIC_CATEGORY)
                .setName("Music")
                .setDescription("The volume of streamed music.")
                .build();

        new YoutubeAudioSourceManager(true, null, null);
        voicechatServerApi.registerVolumeCategory(musicVolumeCategory);
    }

}
