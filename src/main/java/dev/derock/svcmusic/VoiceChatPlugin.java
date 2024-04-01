package dev.derock.svcmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import org.jetbrains.annotations.Nullable;

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

        voicechatServerApi.registerVolumeCategory(musicVolumeCategory);
    }

}
