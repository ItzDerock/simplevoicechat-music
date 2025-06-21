package dev.derock.svcmusic.audio;

import de.maxhenkel.voicechat.api.Group;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GroupSettingsManager {
    private static final ConcurrentHashMap<UUID, GroupSettingsManager> settings = new ConcurrentHashMap<>();

    public float bassboost = 0;
    public int volume = 100;

    public static GroupSettingsManager getGroup(Group group) {
        return GroupSettingsManager.settings.computeIfAbsent(group.getId(), (u) -> new GroupSettingsManager());
    }
}
