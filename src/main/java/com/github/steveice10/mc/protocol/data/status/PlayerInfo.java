package com.github.steveice10.mc.protocol.data.status;

import ch.spacebase.mc.auth.GameProfile;

public class PlayerInfo {

    private final int max;
    private final int online;
    private final GameProfile[] players;

    public PlayerInfo(int max, int online, GameProfile[] players) {
        this.max = max;
        this.online = online;
        this.players = players;
    }

    public int getMaxPlayers() {
        return this.max;
    }

    public int getOnlinePlayers() {
        return this.online;
    }

    public GameProfile[] getPlayers() {
        return this.players;
    }

}
