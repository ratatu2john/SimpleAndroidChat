package com.ratatu2john.chatserver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements EventHandler {
    private ChatServer chatServer;
    private boolean serverStarted;
    private TextView logTextView;
    private Button startServerButton;
    private Button stopServerButton;
    private LogKeeper logKeeper;
    private Handler updateLogHandler;
    private final View.OnClickListener clickListener = v -> {
        int id = v.getId();
        if (id == R.id.startServerButton) {
            startServer();
        } else if (id == R.id.stopServerButton) {
            stopServer();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Restore activity state
        super.onCreate(savedInstanceState);
        // Loads and displays the layout defined in activity_main.xml
        setContentView(R.layout.activity_main);
        // Binds UI to variables and sets state
        this.logTextView = findViewById(R.id.logTextView);
        this.logTextView.setVisibility(View.VISIBLE);
        this.startServerButton = findViewById(R.id.startServerButton);
        startServerButton.setOnClickListener(clickListener);
        this.stopServerButton = findViewById(R.id.stopServerButton);
        stopServerButton.setOnClickListener(clickListener);

        updateLogHandler = new Handler();
        logKeeper = new LogKeeper();
        serverStarted = false;
        initModel();
    }


    private void startServer() {
        if (!serverStarted) {
            chatServer = new ChatServer(updateLogHandler, this, logKeeper);
            Thread serverThread = new Thread(chatServer);
            serverThread.start();
            serverStarted = true;
            initModel();
        }
    }


    private void stopServer() {
        if (serverStarted) {
            chatServer.stopServerSocket();
            serverStarted = false;
            initModel();
        }
    }


    public void update() {
        StringBuilder log = new StringBuilder();
        for (String logLine : logKeeper.getLogs()) {
            log.append(logLine).append("\n");
        }
        this.logTextView.setText(log.toString());
    }


    private void initModel() {
        if (serverStarted) {
            this.startServerButton.setVisibility(View.GONE);
            this.stopServerButton.setVisibility(View.VISIBLE);
        } else {
            this.startServerButton.setVisibility(View.VISIBLE);
            this.stopServerButton.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onStop() {
        stopServer();
        super.onStop();
    }

}