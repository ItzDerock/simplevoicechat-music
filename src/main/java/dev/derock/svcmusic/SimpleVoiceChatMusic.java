package dev.derock.svcmusic;

import com.mojang.brigadier.Command;
import dev.derock.svcmusic.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
        CommandRegistrationCallback.EVENT.register(SearchCommand::register);
        CommandRegistrationCallback.EVENT.register(NowPlayingCommand::register);
        CommandRegistrationCallback.EVENT.register(SkipCommand::register);
        CommandRegistrationCallback.EVENT.register(PlayCommand::register);
        CommandRegistrationCallback.EVENT.register(QueueCommand::register);

        LOGGER.info("Loaded Simple Voice Chat Music!");
    }
}
