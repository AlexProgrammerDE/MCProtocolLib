package com.github.steveice10.mc.protocol.data.status;

public class VersionInfo {

    private final String name;
    private final int protocol;

    public VersionInfo(String name, int protocol) {
        this.name = name;
        this.protocol = protocol;
    }

    public String getVersionName() {
        return this.name;
    }

    public int getProtocolVersion() {
        return this.protocol;
    }

}
