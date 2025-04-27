package com.mdev.chatcord.server;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UDPServerListener {

    private final int SERVER_PORT;
    private DatagramSocket socket;
    private final Set<SocketAddress> clientAddresses = ConcurrentHashMap.newKeySet();

    public UDPServerListener(@Value("${spring.application.udp.server.port}") int serverPort) {
        SERVER_PORT = serverPort;
    }

    @PostConstruct
    public void startServer() {
        new Thread(() -> {
            try {
                socket = new DatagramSocket(SERVER_PORT);
                byte[] buffer = new byte[1024];
                System.out.println("UDP server listening on port " + SERVER_PORT);

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                    // Store sender's address
                    clientAddresses.add(packet.getSocketAddress());

                    // Broadcast to all known clients
                    broadcastToAllClients(message, packet.getSocketAddress());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void broadcastToAllClients(String message, SocketAddress senderAddress) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        for (SocketAddress clientAddress : clientAddresses) {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length,
                        ((InetSocketAddress) clientAddress).getAddress(),
                        ((InetSocketAddress) clientAddress).getPort());

                socket.send(packet);
            } catch (IOException e) {
                System.err.println("Failed to send to: " + clientAddress);
                e.printStackTrace();
            }
        }
    }
}