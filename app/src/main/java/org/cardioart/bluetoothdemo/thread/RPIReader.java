package org.cardioart.bluetoothdemo.thread;

import android.util.Log;

import org.cardioart.bluetoothdemo.hub.Device;
import org.cardioart.bluetoothdemo.ui.main.BluetoothMessageReaderInterface;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class RPIReader implements BluetoothMessageReaderInterface {

    public static final int RPIREADER_NORMAL = 4;
    public static final int RPIREADER_LIST = 1;
    public static final int RPIREADER_ERR = 2;
    public static final int RPIREADER_LEN = 3;
    public static final int RPIREADER_SUCCESS = 0;

    private int state = 0;
    private String msg;
    private ArrayList<Device> deviceArrayList = new ArrayList<>();
    private ArrayList<String> errorArrayList = new ArrayList<>();

    @Override
    public synchronized void parse(String msg) {
        StringTokenizer tokens;
        switch (this.state) {
            case RPIREADER_LIST:
                tokens = new StringTokenizer(msg, "\u001f");
                if (tokens.countTokens() == 2) {
                    int count = Integer.valueOf(tokens.nextToken());
                    deviceArrayList.clear();
                    String s = tokens.nextToken();
                    tokens = new StringTokenizer(s, ",");
                    for (int i=0; i<tokens.countTokens(); i++) {
                        try {
                            s = tokens.nextToken();
                            Device dev = new Device(s);
                            deviceArrayList.add(dev);
                        } catch (Exception e) {
                        }
                    }
                }
                state = RPIREADER_SUCCESS;
                break;
            case RPIREADER_ERR:
                tokens = new StringTokenizer(msg, "\u001f");
                if (tokens.countTokens() == 2) {
                    int count = Integer.valueOf(tokens.nextToken());
                    errorArrayList.clear();
                    String s = tokens.nextToken();
                    tokens = new StringTokenizer(s, ",");
                    for (int i=0; i<tokens.countTokens(); i++) {
                        errorArrayList.add(tokens.nextToken());
                    }
                }
                state = RPIREADER_SUCCESS;
                break;
            case RPIREADER_LEN:
                state = RPIREADER_SUCCESS;
                break;
            case RPIREADER_NORMAL:
                break;
        }
    }

    public synchronized void setState(int state) {
        this.state = state;
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized String getMessage() {
        return msg;
    }

    public synchronized ArrayList<Device> getDevices() {
        return deviceArrayList;
    }

    public synchronized  ArrayList<String> getErrors() {
        return errorArrayList;
    }
}
