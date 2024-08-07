package dev.derock.svcmusic.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.audio.GroupManager;
import dev.derock.svcmusic.audio.MusicManager;
import dev.derock.svcmusic.util.ModUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static dev.derock.svcmusic.util.ModUtils.checkPlayerGroup;

public class VolumeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("music")
            .then(CommandManager.literal("volume")
                .then(CommandManager.argument("volume_percent", IntegerArgumentType.integer(0, 100))
                    .executes(VolumeCommand::execute))));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int volume = IntegerArgumentType.getInteger(context, "volume_percent");
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().getServer());
            gm.broadcast(Text.literal("Volume set to " + volume + "% by " + result.source().getName()));
            gm.setVolume(volume);
        });

        return 0;
    }

}
