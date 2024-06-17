package dev.derock.svcmusic.util;

import com.mojang.brigadier.context.CommandContext;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import dev.derock.svcmusic.VoiceChatPlugin;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ModUtils {

    public static MutableText hyperlink(String string, String url) {
        return Text.literal(string)
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
    }

    public static MutableText trackInfo(AudioTrackInfo track) {
        return trackInfo(track, false);
    }

    public static MutableText trackInfo(AudioTrackInfo track, boolean longFormat ) {
       MutableText text = Text.literal(track.title)
           .setStyle(
               Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.AQUA))
                   .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, track.uri))
           )
           .append(Text.literal(" by ").setStyle(Style.EMPTY))
           .append(Text.literal(track.author).setStyle(
               Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.AQUA)))
           );

       // if long format, add more track data
       if (longFormat) {
           text.append(Text.literal(" [" + formatMMSS(track.length) + "]").setStyle(Style.EMPTY));
       }

       return text;
    }

    public static String formatMMSS(long millis) {

        return String.format("%d:%d",
            TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public static String parseTrackId(String userInput) {
        if (userInput.startsWith("ytsearch:") || userInput.startsWith("ytmsearch:") || userInput.startsWith("scsearch:")) {
            return userInput;
        }

        // if starts with id:, parse ourselves
        if (userInput.startsWith("id:")) {
            return userInput.substring(3);
        }

        // try and parse as URL
        try {
            new URL(userInput);
        } catch (MalformedURLException e) {
            return "ytsearch:" + userInput;
        }

        return userInput;
    }

    public static @Nullable CheckPlayerGroup checkPlayerGroup(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (VoiceChatPlugin.voicechatServerApi == null) {
            source.sendFeedback(
                () -> Text.literal("VoiceChat API connection has not been established yet! Please try again later."),
                false
            );
            return null;
        }

        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendFeedback(
                () -> Text.literal("This command is player only!"),
                false
            );
            return null;
        }

        VoicechatConnection connection = VoiceChatPlugin.voicechatServerApi.getConnectionOf(player.getUuid());

        if (connection == null) {
            source.sendFeedback(
                () -> Text.literal("You are not connected to voice chat!"),
                false
            );
            return null;
        }

        Group group = connection.getGroup();

        if (group == null) {
            source.sendFeedback(
                () -> Text.literal("You're not in a group! Just use spotify smh.."),
                false
            );
            return null;
        }
        CheckPlayerGroup result = new CheckPlayerGroup(source, player, group);
        return result;
    }

    public record CheckPlayerGroup(ServerCommandSource source, ServerPlayerEntity player, Group group) {
    }
}
