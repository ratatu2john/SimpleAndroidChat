package com.ratatu2john.chatclient;

import android.os.Handler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient implements Runnable {
    private Socket clientSocket;
    private final String serverAddress;
    private final int port;
    private final LogKeeper logKeeper;
    private final EventHandler eventHandler;
    private final Handler updateLogHandler;
    private boolean isRunning;
    private boolean dataReady;


    public ChatClient(Handler updateLogHandler, EventHandler eventHandler, LogKeeper logKeeper,
                      String serverAddress, int port) {
        this.updateLogHandler = updateLogHandler;
        this.eventHandler = eventHandler;
        this.logKeeper = logKeeper;
        this.serverAddress = serverAddress;
        this.port = port;
    }


    public void run(){
        try  {
            clientSocket = new Socket(serverAddress, port);
            addLog("Connected to "+serverAddress+":"+port);
            isRunning = true;

            // Thread for messages reading
            new Thread(this::receiveMessages).start();

            sendMessages();

        } catch (IOException e) {
            addLog("Connection error: " + e.getMessage());
        }
    }


    private void receiveMessages() {
        try (BufferedReader clientInputStream = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {

            String serverMessage;
            while (isRunning && (serverMessage = clientInputStream.readLine()) != null) {
                addLog(serverMessage);
            }
        } catch (IOException e) {
            // Ignore errors after stop
            if (isRunning) {
                addLog("Error receiving message: " + e.getMessage());
            }
        }
    }


    private void sendMessages() {
        try (PrintWriter clientOutputStream = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String message;

            while (isRunning) {
                dataReady = false;

                synchronized (SharedLock.lock) {
                    while (!dataReady) {
                        SharedLock.lock.wait();
                    }
                    message = eventHandler.getMessage();
                }


                clientOutputStream.println(message);
                addLog("you: "+message);
            }
        } catch (IOException e) {
            addLog("Error sending message: " + e.getMessage());
        } catch (InterruptedException e) {
            addLog(e.getMessage());
        }
    }


    private void addLog(final String log) {
        updateLogHandler.post(() -> {
            logKeeper.addLog(log);
            eventHandler.update();
        });
    }


    public void setDataReadyTrue() {
        dataReady = true;
    }


    public void stopClientSocket() {
        isRunning = false;
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
        catch (IOException e) {
            addLog("Error closing socket: " + e.getMessage());
        }
    }
}