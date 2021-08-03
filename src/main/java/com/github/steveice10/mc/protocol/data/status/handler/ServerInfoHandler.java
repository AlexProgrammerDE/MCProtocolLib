package com.github.steveice10.mc.protocol.data.status.handler;

import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;


public interface ServerInfoHandler {

    void handle(ServerStatusInfo info);

}
