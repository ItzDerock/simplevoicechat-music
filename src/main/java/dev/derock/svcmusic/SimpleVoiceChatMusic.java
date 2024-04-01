package dev.derock.svcmusic;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleVoiceChatMusic implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("simple-voice-chat-music");

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(
                        CommandManager.literal("dj")
                                .then(CommandManager.literal("search"))
                                .then(CommandManager.argument("query", StringArgumentType.greedyString()))
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    final string searchTerm = StringArgumentType.getString(context, "query");

                                    if (VoiceChatPlugin.voicechatServerApi == null) {
                                        source.sendFeedback(() -> Text.literal("VoiceChat API connection has not been established yet! Please try again later."), false);
                                        return 1;
                                    }

                                    ServerPlayerEntity player = source.getPlayer();

                                    if (player == null) {
                                        source.sendFeedback(() -> Text.literal("This command is player only!"), false);
                                        return 1;
                                    }

                                    VoicechatConnection connection = VoiceChatPlugin.voicechatServerApi.getConnectionOf(player.getUuid());

                                    if (connection == null) {
                                        source.sendFeedback(() -> Text.literal("You are not connected to voice chat!"), false);
                                        return 1;
                                    }

                                    Group group = connection.getGroup();

                                    if (group == null) {
                                        source.sendFeedback(() -> Text.literal("You're not in a group! Just use spotify smh.."), false);
                                        return 1;
                                    }

                                    GroupManager gm = MusicManager.getInstance().getGroup(group);

                                    MusicManager.playerManager.loadItem(query, new AudioLoadResultHandler() {
                                        @Override
                                        public void trackLoaded(AudioTrack track) {
                                            gm.
                                        }

                                        @Override
                                        public void playlistLoaded(AudioPlaylist playlist) {

                                        }

                                        @Override
                                        public void noMatches() {

                                        }

                                        @Override
                                        public void loadFailed(FriendlyException exception) {

                                        }
                                    });

                                    return 0;
                                })
                )
        );

        LOGGER.info("Loaded Simple Voice Chat Music!");
    }
}
