package com.byteproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadBalancerServer {
    private final int port;
    private final SimpleLoadBalancer loadBalancer;
    private final ExecutorService executor;

    public LoadBalancerServer(int port) {
        this.port = port;
        this.loadBalancer = new SimpleLoadBalancer();
        this.executor = Executors.newFixedThreadPool(100);
    }

    public void start() {
        // Opening the loadBalancer port for others to access
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(true); // Blocking mode to wait for connections
            System.out.println("Server is listening on port " + port);

            while (true) {
                SocketChannel clientSocket = serverSocketChannel.accept();
                System.out.println("New connection accepted!");
                executor.execute(() -> {
                    try {
                        handleRequest(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(SocketChannel clientChannel) throws IOException {
        Server server = loadBalancer.getNextServer();

        // Set up the client SocketChannel
        // Get existing channel or create from socket
        clientChannel.configureBlocking(false);

        // Set up the server Socket channel
        try (clientChannel; SocketChannel serverChannel = SocketChannel.open()) {
            serverChannel.configureBlocking(false);
            serverChannel.connect(new InetSocketAddress(server.getHost(), server.getPort()));

            while (!serverChannel.finishConnect()) {
                // Wait for connection to complete
            }


            // Selector to monitor multiple channel
            Selector selector = Selector.open();

            // Register both channels with selector for Read operations
            // When data is available to read, the selector will be notified
            clientChannel.register(selector, SelectionKey.OP_READ);
            serverChannel.register(selector, SelectionKey.OP_READ);

            // Create a buffer for data transfer
            // 4KB is common buffer size
            ByteBuffer buffer = ByteBuffer.allocate(4096);

            // Main event loop
            while (true) {
                // Block until at least one channel is ready
                selector.select();

                // Process each ready channel
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isReadable()) {
                        SocketChannel sourceChannel = (SocketChannel) key.channel();
                        SocketChannel targetChannel = sourceChannel == clientChannel ?
                                serverChannel : clientChannel;

                        buffer.clear();
                        int bytesRead = sourceChannel.read(buffer);
                        if (bytesRead == -1) {
                            return;
                        }
                        buffer.flip();
                        targetChannel.write(buffer);
                    }
                    iterator.remove();
                }
            }
        }
    }
}
