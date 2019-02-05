package org.cardioart.bluetoothdemo.ui.main;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import me.aflak.bluetooth.DeviceCallback;

public class BluetoothUICallback implements DeviceCallback {

    private Activity activity;
    private TextView console;
    private Switch uiSwitch;
    private String deviceName;
    private BluetoothMessageReaderInterface reader;
    private TimerTask task;
    private Timer timer;

    public BluetoothUICallback(TextView console, Switch uiSwitch, BluetoothMessageReaderInterface reader) {
        this.console = console;
        this.uiSwitch = uiSwitch;
        this.reader = reader;
        this.timer = new Timer();
    }

    public void setTimerTask(TimerTask task) {
        this.task = task;
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        this.deviceName = device.getName();
        console.append("Connected to " + device.getName() + " (" + device.getAddress() + ")\n");
        if (task != null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(task, 0, 1000);
        }
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device, String message) {
        if (timer != null) {
            timer.cancel();
        }
        console.append("Disconnected from " + device.getName() + " (" + device.getAddress() + ")\n");
        console.append(message + "\n");
        uiSwitch.setChecked(false);
    }

    @Override
    public void onMessage(String message) {
        if (reader != null) {
            reader.parse(message);
        }
    }

    @Override
    public void onError(String message) {
        console.append(deviceName + ":" + message + "\n");
    }

    @Override
    public void onConnectError(BluetoothDevice device, String message) {
        console.append("Connection error: " + device.getName() + "\n" + message + "\n");
        uiSwitch.setChecked(false);
    }
}
