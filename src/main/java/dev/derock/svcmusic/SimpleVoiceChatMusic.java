package dev.derock.svcmusic;

import dev.derock.svcmusic.audio.MusicManager;
import dev.derock.svcmusic.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
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
        thread.setUncaughtExceptionHandler(
            (t, e) -> SimpleVoiceChatMusic.LOGGER.error("Uncaught exception in thread {}", t.getName(), e)
        );

        return thread;
    });

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(SearchCommand::register);
        CommandRegistrationCallback.EVENT.register(NowPlayingCommand::register);
        CommandRegistrationCallback.EVENT.register(SkipCommand::register);
        CommandRegistrationCallback.EVENT.register(PlayCommand::register);
        CommandRegistrationCallback.EVENT.register(QueueCommand::register);
        CommandRegistrationCallback.EVENT.register(PauseCommand::register);
        CommandRegistrationCallback.EVENT.register(ResumeCommand::register);
        CommandRegistrationCallback.EVENT.register(StopCommand::register);
        CommandRegistrationCallback.EVENT.register(KillCommand::register);
        CommandRegistrationCallback.EVENT.register(VolumeCommand::register);
        CommandRegistrationCallback.EVENT.register(BassboostCommand::register);

        ServerLifecycleEvents.SERVER_STOPPING.register((MinecraftServer server) -> {
            LOGGER.info("Cleaning up due to shutdown.");
            MusicManager.getInstance().cleanup();
        });

        LOGGER.info("Loaded Simple Voice Chat Music!");
    }
}
