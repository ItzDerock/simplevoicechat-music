package dev.derock.svcmusic.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.derock.svcmusic.audio.GroupManager;
import dev.derock.svcmusic.audio.MusicManager;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.audio.PlayLoadHandler;
import dev.derock.svcmusic.util.ModUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static dev.derock.svcmusic.util.ModUtils.checkPlayerGroup;

import java.net.MalformedURLException;
import java.net.URL;

public class PlayCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("music")
            .then(CommandManager.literal("play")
                .then(CommandManager.argument("query", StringArgumentType.string())
                    .executes(PlayCommand::execute))));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final String query = ModUtils.parseTrackId(StringArgumentType.getString(context, "query"));
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().getServer());

            result.source().sendFeedback(() -> Text.literal("Loading songs..."), false);
            MusicManager.getInstance().playerManager.loadItemOrdered(
                gm.getPlayer(),
                query,
                new PlayLoadHandler(result.source(), gm)
            );
        });

        return 0;
    }

}
