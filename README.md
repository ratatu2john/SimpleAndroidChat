# Android Chat Application (Client-Server)




https://github.com/user-attachments/assets/a99b8900-ce1e-4260-8730-92db9bce2c99




A simple Android chat application demonstrating client-server architecture using Java multithreading. The project consists of two parts: a **server** (handles connections and messaging) and a **client** (user interface for chatting).

## Key Features

### Server
- **Active application** (not a background service)
- **Real-time logging** of all activities
- **Read-only mode** (cannot send messages to clients)
- **Handles multiple clients simultaneously** using multithreading
- **Graceful shutdown** (notifies all clients before stopping)
- **Connection events** broadcasts when clients join/leave

### Client
- **Real-time messaging**
- **User presence notifications** (join/leave alerts)
- **Duplicate message handling** (consecutive identical messages allowed)
- **Server disconnect handling** (receives termination notice)
- **Handles `exit` command**

## Testing Tip
The demo video shows apps running on 3 Android emulators with port forwarding.

To repeat, do 
`adb -s emulator-555* forward tcp:7000 tcp:7000` 
for the emulator with the server and 
`adb -s emulator-555* reverse tcp:7000 tcp:7000` 
for emulators with the client.

