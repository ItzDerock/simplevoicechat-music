package dev.derock.svcmusic;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SimpleVoiceChatMusic implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("simple-voice-chat-music");

    public static ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "SVCMusicExecutor");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) -> {
            SimpleVoiceChatMusic.LOGGER.error("Uncaught exception in thread {}", t.getName(), e);
        });

        return thread;
    });

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("music")
                    .then(CommandManager.literal("search")
                    .then(CommandManager.argument("query", StringArgumentType.string())
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        final String query = StringArgumentType.getString(context, "query");
                        LOGGER.debug("Running with " + query);

                        if (VoiceChatPlugin.voicechatServerApi == null) {
                            source.sendFeedback(
                                () -> Text.literal("VoiceChat API connection has not been established yet! Please try again later."),
                                false
                            );
                            return 1;
                        }

                        ServerPlayerEntity player = source.getPlayer();

                        if (player == null) {
                            source.sendFeedback(
                                () -> Text.literal("This command is player only!"),
                                false
                            );
                            return 1;
                        }

                        VoicechatConnection connection = VoiceChatPlugin.voicechatServerApi.getConnectionOf(player.getUuid());

                        if (connection == null) {
                            source.sendFeedback(
                                () -> Text.literal("You are not connected to voice chat!"),
                                false
                            );
                            return 1;
                        }

                        Group group = connection.getGroup();

                        if (group == null) {
                            source.sendFeedback(
                                () -> Text.literal("You're not in a group! Just use spotify smh.."),
                                false
                            );
                            return 1;
                        }

                        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
                            GroupManager gm = MusicManager.getInstance().getGroup(group, player.getServer());
                            LOGGER.debug("Loading song...");
                            MusicManager.getInstance().playerManager.loadItem(query, new AudioTrackLoadHandler(source, gm));
                        });


                        source.sendFeedback(
                            () -> Text.literal("Ok"),
                            false
                        );

                        return 0;
                    })
            )))
        );



        LOGGER.info("Loaded Simple Voice Chat Music!");
    }
}
