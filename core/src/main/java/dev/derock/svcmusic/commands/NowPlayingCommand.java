package dev.derock.svcmusic.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.audio.GroupManager;
import dev.derock.svcmusic.audio.GroupSettingsManager;
import dev.derock.svcmusic.audio.MusicManager;
import dev.derock.svcmusic.util.ModUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import static dev.derock.svcmusic.util.ModUtils.checkPlayerGroup;

public class NowPlayingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("music")
            .then(CommandManager.literal("now-playing")
                .executes(NowPlayingCommand::execute)));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().getServer());
            AudioTrack track = gm.getPlayer().getPlayingTrack();
            GroupSettingsManager settings = gm.getSettingsStore();

            if (track == null) {
                result.source().sendFeedback(() -> Text.literal("Nothing is playing."), false);
                return;
            }

            result.source().sendFeedback(
                () -> Text.literal("Currently Playing ")
                    .append(ModUtils.trackInfo(track.getInfo()))
                    .append(Text.literal("\n" + ModUtils.formatMMSS(track.getPosition()) + "/" + ModUtils.formatMMSS(track.getDuration())) )
                    .append(Text.literal(" • " + settings.volume + "% volume"))
                    .append(Text.literal(" • " + settings.bassboost + "% bassboost")),
                false
            );
        });

        return 0;
    }

}
