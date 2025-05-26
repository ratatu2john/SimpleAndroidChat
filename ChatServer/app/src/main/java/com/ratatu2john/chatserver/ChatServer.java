package com.ratatu2john.chatserver;

import android.os.Handler;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ChatServer implements Runnable {
    private static final int PORT = 7000;
    // CopyOnWriteArrayList is thread-safe
    private static List<ClientHandler> clients;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private static LogKeeper logKeeper;
    private static EventHandler eventHandler;
    private static Handler updateLogHandler;


    public ChatServer(Handler updateLogHandler, EventHandler eventHandler, LogKeeper logKeeper) {
        ChatServer.updateLogHandler = updateLogHandler;
        ChatServer.eventHandler = eventHandler;
        ChatServer.logKeeper = logKeeper;
        clients = new CopyOnWriteArrayList<>();
        isRunning = false;
    }


    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            addLog("Server started on port " + PORT);
            isRunning = true;

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    addLog("New connection: " +clientSocket.getLocalAddress() +
                            ":" + clientSocket.getPort());

                    ClientHandler clientHandler = new ClientHandler(clientSocket, updateLogHandler,
                                                                    eventHandler, logKeeper);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    addLog("Error accepting client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            addLog("Server error: " + e.getMessage());
        } finally {
            broadcast(null, "Server stopped.");
            for (ClientHandler client : clients) {
                client.stopClientSocket();
            }
        }
    }


    private static void addLog(final String log) {
        updateLogHandler.post(() -> {
            logKeeper.addLog(log);
            eventHandler.update();
        });
    }


    public void stopServerSocket() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            addLog("Server error: " + e.getMessage());
        }
    }


    public static void broadcast(ClientHandler sender, String message) {
        for (ClientHandler client : clients) {
            if (sender != client) {
                client.sendMessage(message);
            }
        }
    }


    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}

