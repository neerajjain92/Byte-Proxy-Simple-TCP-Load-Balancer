package com.byteproxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleLoadBalancer {

    private final List<Server> servers;
    private final AtomicInteger currentIndex;
    private final HealthChecker healthChecker;

    public SimpleLoadBalancer() {
        this.servers = new ArrayList<>();
        this.currentIndex = new AtomicInteger(0);
        servers.addAll(List.of(new Server("Server-1", "localhost", 8081),
                new Server("Server-2", "localhost", 8082),
                new Server("Server-3", "localhost", 8083)));
        this.healthChecker = new HealthChecker(this);
    }

    //Round Robing getNextServer
    public Server getNextServer() {
        int totalAttempts = servers.size();
        while (totalAttempts > 0) {
            Server server = servers.get(currentIndex.getAndIncrement() % servers.size());
            if (server.isHealthy()) {
                return server;
            }
            totalAttempts--;
        }
        throw new RuntimeException("No healthy server available");
    }

    public List<Server> getAllServers() {
        return servers;
    }
}
