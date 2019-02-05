package org.cardioart.bluetoothdemo.ui.main;

public interface BluetoothEventListenerInterface {
    void onConnected(String msg);
    void onDisconnected(String msg);
    void onError(String msg);
}
