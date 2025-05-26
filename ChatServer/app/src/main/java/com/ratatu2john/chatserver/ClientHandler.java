package com.ratatu2john.chatserver;

import android.os.Handler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final PrintWriter clientOutputStream;
    private String username;
    private final LogKeeper logKeeper;
    private final EventHandler eventHandler;
    private final Handler updateLogHandler;
    private boolean isRunning;


    public ClientHandler(Socket clientSocket, Handler updateLogHandler,
                         EventHandler eventHandler, LogKeeper logKeeper) throws IOException {
        this.clientSocket = clientSocket;
        this.updateLogHandler = updateLogHandler;
        this.eventHandler = eventHandler;
        this.logKeeper = logKeeper;
        clientOutputStream = new PrintWriter(clientSocket.getOutputStream(), true);

    }


    @Override
    public void run() {
        try (BufferedReader clientInputStream = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {
            clientOutputStream.println("Enter your username:");
            username = clientInputStream.readLine();
            addLog(username + " connected!");
            ChatServer.broadcast(this, username + " connected!");

            isRunning = true;
            String inputLine;
            while (isRunning && (inputLine = clientInputStream.readLine()) != null) {
                addLog(username + ": " + inputLine);
                ChatServer.broadcast(this, username + ": " + inputLine);
            }
        } catch (IOException e) {
            addLog(username + " disconnected abruptly!");
        } finally {
            // user aborted connection
            if (isRunning) {
                stopClientSocket();
                ChatServer.removeClient(this);
                addLog(username + " left the chat.");
                ChatServer.broadcast(null, username + " left the chat.");
            }
        }
    }


    public void stopClientSocket() {
        isRunning = false;
        try {
            clientSocket.close();
        } catch (IOException e) {
            addLog("Error closing clientSocket: " + e.getMessage());
        }
    }


    public void sendMessage(String message) {
        clientOutputStream.println(message);
    }


    private void addLog(final String log) {
        updateLogHandler.post(() -> {
            logKeeper.addLog(log);
            eventHandler.update();
        });
    }
}