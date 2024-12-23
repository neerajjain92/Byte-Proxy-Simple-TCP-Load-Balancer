package com.byteproxy;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Sample LoadBalancer Application!");
        LoadBalancerServer loadBalancerServer = new LoadBalancerServer(9090);
        loadBalancerServer.start();
    }
}
