package dev.derock.svcmusic.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.derock.svcmusic.core.SimpleVoiceChatMusic;
import dev.derock.svcmusic.core.audio.GroupManager;
import dev.derock.svcmusic.core.audio.MusicManager;
import dev.derock.svcmusic.core.util.ModUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import static dev.derock.svcmusic.core.util.ModUtils.checkPlayerGroup;

public class SkipCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("music")
            .then(CommandManager.literal("skip")
                .executes(SkipCommand::execute)));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().getServer());
            gm.broadcast(Text.literal("Song skipped by " + result.source().getName()));
            gm.nextTrack();
        });

        return 0;
    }

}
