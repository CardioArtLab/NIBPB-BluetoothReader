package org.cardioart.bluetoothdemo.thread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import me.aflak.bluetooth.DeviceCallback;
import me.aflak.bluetooth.ThreadHelper;

public class Bluetooth implements BluetoothInterface {
    private UUID uuid;
    private Activity activity;
    private Context context;
    private boolean connected;
    private boolean runOnUi;
    private String deviceAddress;
    private DeviceCallback deviceCallback;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private BufferedReader input;
    private OutputStream out;

    public Bluetooth(Context context) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        this.uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        this.deviceCallback = null;
        this.connected = false;
        this.runOnUi = false;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void beginConnect() {
        connectToAddress(deviceAddress);
    }

    public void setCallbackOnUI(Activity activity){
        this.activity = activity;
        this.runOnUi = true;
    }

    public void connectToAddress(String address, boolean insecureConnection) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        connectToDevice(device, insecureConnection);
    }

    public void connectToAddress(String address) {
        connectToAddress(address, false);
    }

    public void connectToName(String name, boolean insecureConnection) {
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            if (blueDevice.getName().equals(name)) {
                connectToDevice(blueDevice, insecureConnection);
                return;
            }
        }
    }

    public void connectToName(String name) {
        connectToName(name, false);
    }

    public void connectToDevice(BluetoothDevice device, boolean insecureConnection){
        new ConnectThread(device, insecureConnection).start();
    }

    public void connectToDevice(BluetoothDevice device){
        connectToDevice(device, false);
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (final IOException e) {
            if(deviceCallback !=null) {
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        deviceCallback.onError(e.getMessage());
                    }
                });
            }
        }
    }

    public boolean isConnected(){
        return connected;
    }

    public void send(String msg, String charset){
        try {
            if(!TextUtils.isEmpty(charset)) {
                out.write(msg.getBytes(charset));//Eg: "US-ASCII"
            }else {
                out.write(msg.getBytes());//Sending as UTF-8 as default
            }
        } catch (final IOException e) {
            connected=false;
            if(deviceCallback !=null){
                ThreadHelper.run(runOnUi, activity, new Runnable() {
                    @Override
                    public void run() {
                        deviceCallback.onDeviceDisconnected(device, e.getMessage());
                    }
                });
            }
        }
    }

    private class ReceiveThread extends Thread implements Runnable {
        public void run(){
            String msg;
            try {
                while((msg = input.readLine()) != null) {
                    if(deviceCallback != null){
                        final String msgCopy = msg;
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallback.onMessage(msgCopy);
                            }
                        });
                    }
                }
            } catch (final IOException e) {
                connected=false;
                if(deviceCallback != null){
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            deviceCallback.onDeviceDisconnected(device, e.getMessage());
                        }
                    });
                }
            }
        }
    }

    private class ConnectThread extends Thread {
        ConnectThread(BluetoothDevice d, boolean insecureConnection) {
            device=d;
            try {
                if(insecureConnection){
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                }
                else{
                    socket = device.createRfcommSocketToServiceRecord(uuid);
                }
            } catch (IOException e) {
                if(deviceCallback != null){
                    deviceCallback.onError(e.getMessage());
                }
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                out = socket.getOutputStream();
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected=true;

                new ReceiveThread().start();

                if(deviceCallback !=null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            deviceCallback.onDeviceConnected(device);
                        }
                    });
                }
            } catch (final IOException e) {
                if(deviceCallback !=null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            deviceCallback.onConnectError(device, e.getMessage());
                        }
                    });
                }

                try {
                    socket.close();
                } catch (final IOException closeException) {
                    if (deviceCallback != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallback.onError(closeException.getMessage());
                            }
                        });
                    }
                }
            }
        }
    }

    public void send(String msg){
        send(msg, null);
    }

    public void setDeviceCallback(DeviceCallback deviceCallback) {
        this.deviceCallback = deviceCallback;
    }
    public void removeCommunicationCallback(){
        this.deviceCallback = null;
    }
}
