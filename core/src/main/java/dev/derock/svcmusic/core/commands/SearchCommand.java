package dev.derock.svcmusic.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.derock.svcmusic.core.audio.SearchLoadHandler;
import dev.derock.svcmusic.core.audio.GroupManager;
import dev.derock.svcmusic.core.audio.MusicManager;
import dev.derock.svcmusic.core.SimpleVoiceChatMusic;
import dev.derock.svcmusic.core.util.ModUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static dev.derock.svcmusic.core.util.ModUtils.checkPlayerGroup;
import static dev.derock.svcmusic.core.util.ModUtils.parseTrackId;

public class SearchCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("music")
            .then(CommandManager.literal("search")
                .then(CommandManager.argument("query", StringArgumentType.string())
                    .executes(SearchCommand::execute))));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final String query = parseTrackId(StringArgumentType.getString(context, "query"));

        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        SimpleVoiceChatMusic.LOGGER.debug("Searching for " + query);

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().getServer());
            result.source().sendFeedback(() -> Text.literal("Loading songs..."), false);
            MusicManager.getInstance().playerManager.loadItemOrdered(
                gm.getPlayer(),
                query,
                new SearchLoadHandler(result.source(), gm)
            );
        });

        return 0;
    }

}
