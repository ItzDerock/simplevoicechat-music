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
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.UUID;

import static dev.derock.svcmusic.core.util.ModUtils.checkPlayerGroup;

public class KillCommand {
    private static HashSet<UUID> warned = new HashSet<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("music")
            .then(CommandManager.literal("kill")
                .executes(KillCommand::execute)));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        if (warned.add(result.player().getUuid())) {
            result.source().sendFeedback(
                () -> Text.literal("Are you sure you want to do this? This command should be used when everything is broken and you need to alt-f4 the plugin. Group members may hear a bit of earrape as the opus packets abruptly end.")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)).append(Text.literal("\n\nIf you understand this, run the command again.")),
                false
            );

            return 0;
        }

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().getServer());
            gm.broadcast(Text.literal("Playback forcibly killed by " + result.source().getName() + "."));
            gm.cleanup();
        });

        return 0;
    }

}
