package dev.derock.svcmusic.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.derock.svcmusic.core.SimpleVoiceChatMusic;
import dev.derock.svcmusic.core.audio.GroupManager;
import dev.derock.svcmusic.core.audio.MusicManager;
import dev.derock.svcmusic.core.util.ModUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.concurrent.BlockingQueue;

import static dev.derock.svcmusic.core.util.ModUtils.checkPlayerGroup;

public class QueueCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("music")
            .then(CommandManager.literal("queue")
                .executes(QueueCommand::execute)));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().getServer());

            MutableText text = Text.empty();
            AudioTrack currentTrack = gm.getPlayer().getPlayingTrack();
            BlockingQueue<AudioTrack> tracks = gm.getQueue();

            if (currentTrack != null) {
                text.append(Text.literal("Current: ").append(ModUtils.trackInfo(currentTrack.getInfo())).append("\n"));
            }

            AudioTrack[] tracksArr = tracks.toArray(AudioTrack[]::new);
            for (int i = 0; i < tracksArr.length; i++) {
                AudioTrack track = tracksArr[i];
                text.append(Text.literal(i + ". ").append(ModUtils.trackInfo(track.getInfo())).append(Text.literal("\n")));
            }

            if (text.getString().isBlank()) {
                text.append(Text.literal("No songs in the queue."));
            }

            result.source().sendFeedback(
                () -> text,
                false
            );
        });

        return 0;
    }

}
