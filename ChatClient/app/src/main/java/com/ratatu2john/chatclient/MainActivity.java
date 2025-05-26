package com.ratatu2john.chatclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class MainActivity extends Activity implements EventHandler {
    private ChatClient chatClient;
    private boolean clientConnected;
    private TextView logTextView;
    private EditText inputMessageField;
    private EditText inputAddressField;
    private EditText inputPortField;
    private Button sendButton;
    private Button connectButton;
    private LogKeeper logKeeper;
    private Handler updateLogHandler;
    private String message="";
    private final View.OnClickListener clickListener = v -> {
        int id = v.getId();
        if (id == R.id.sendButton) {
            synchronized (SharedLock.lock) {
                message = inputMessageField.getText().toString();
                inputMessageField.getText().clear();
                chatClient.setDataReadyTrue();
                SharedLock.lock.notifyAll();
            }
        } else if (id == R.id.connectButton) {
            connectServer();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.logTextView = findViewById(R.id.logTextView);
        this.inputMessageField = findViewById(R.id.inputMessageField);
        this.inputAddressField = findViewById(R.id.inputAddressField);
        this.inputPortField = findViewById(R.id.inputPortField);
        this.sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(clickListener);
        this.connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(clickListener);

        updateLogHandler = new Handler();
        logKeeper = new LogKeeper();
        clientConnected = false;
        initModel();
    }

    private void connectServer() {
        if (!clientConnected) {
            String serverAddress = inputAddressField.getText().toString();
            int port = Integer.parseInt(inputPortField.getText().toString());

            chatClient = new ChatClient(updateLogHandler, this, logKeeper, serverAddress, port);
            Thread clientThread = new Thread(chatClient);
            clientThread.start();

            clientConnected = true;
            initModel();
        }
    }


    private void stopClient() {
        if (clientConnected) {
            chatClient.stopClientSocket();
            clientConnected = false;
            initModel();
        }
    }


    public String getMessage() {
        return message;
    }


    public void update() {
        StringBuilder log = new StringBuilder();
        for (String logLine : logKeeper.getLogs()) {
            log.append(logLine).append("\n");
        }
        this.logTextView.setText(log.toString());

    }


    private void initModel() {
        if (clientConnected) {
            this.logTextView.setVisibility(View.VISIBLE);
            this.inputMessageField.setVisibility(View.VISIBLE);
            this.sendButton.setVisibility(View.VISIBLE);
            this.inputAddressField.setVisibility(View.GONE);
            this.inputPortField.setVisibility(View.GONE);
            this.connectButton.setVisibility(View.GONE);
        } else
        {
            this.logTextView.setVisibility(View.GONE);
            this.inputMessageField.setVisibility(View.GONE);
            this.sendButton.setVisibility(View.GONE);
            this.inputAddressField.setVisibility(View.VISIBLE);
            this.inputPortField.setVisibility(View.VISIBLE);
            this.connectButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        stopClient();
        super.onStop();
    }

}