package com.github.steveice10.mc.protocol.data.status;

import com.github.steveice10.mc.protocol.data.message.Message;

import java.awt.image.BufferedImage;

public class ServerStatusInfo {

    private final VersionInfo version;
    private final PlayerInfo players;
    private final Message description;
    private final BufferedImage icon;

    public ServerStatusInfo(VersionInfo version, PlayerInfo players, Message description, BufferedImage icon) {
        this.version = version;
        this.players = players;
        this.description = description;
        this.icon = icon;
    }

    public VersionInfo getVersionInfo() {
        return this.version;
    }

    public PlayerInfo getPlayerInfo() {
        return this.players;
    }

    public Message getDescription() {
        return this.description;
    }

    public BufferedImage getIcon() {
        return this.icon;
    }

}
