package com.ratatu2john.chatclient;

import java.util.ArrayList;
import java.util.List;


public class LogKeeper {

    private final List<String> entries;

    public LogKeeper()
    {
        this.entries = new ArrayList<>();
    }

    public List<String> getLogs()
    {
        return entries;
    }

    public void addLog(String log)
    {
        entries.add(log);
    }

}