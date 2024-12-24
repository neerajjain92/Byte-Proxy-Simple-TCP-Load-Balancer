package com.byteproxy;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HealthChecker {
    private final int HEALTH_CHECK_INTERVAL = 5000; // 5 seconds
    private final SimpleLoadBalancer simpleLoadBalancer;
    private final ScheduledExecutorService scheduler;

    public HealthChecker(SimpleLoadBalancer simpleLoadBalancer) {
        this.simpleLoadBalancer = simpleLoadBalancer;
        this.scheduler = Executors.newScheduledThreadPool(1);
        startHealthCheck();
    }

    private void startHealthCheck() {
        scheduler.scheduleAtFixedRate(this::checkAllServers, 0, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void checkAllServers() {
        simpleLoadBalancer.getAllServers().forEach(this::checkServerHealth);
    }

    private void checkServerHealth(Server server) {
        try {
            String url = String.format("http://%s:%d/health", server.getHost(), server.getPort());
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(2000);
            server.setHealthy(connection.getResponseCode() == 200);
        } catch (Exception e) {
            server.setHealthy(false);
        }
    }
}
