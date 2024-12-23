package com.byteproxy;

import lombok.Data;

@Data
public class Server {

    private final String name;
    private final String host;
    private final int port;
    private boolean healthy = true;

    public Server(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }
}
