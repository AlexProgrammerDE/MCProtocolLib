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

    @Override
    public boolean equals(Object o) {
        return o instanceof VersionInfo && this.name.equals(((VersionInfo) o).name) && this.protocol == ((VersionInfo) o).protocol;
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.protocol;
        return result;
    }
}
