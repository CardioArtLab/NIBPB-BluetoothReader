package org.cardioart.bluetoothdemo.thread;

import org.cardioart.bluetoothdemo.ui.main.BluetoothMessageReaderInterface;

public class NIBPReader implements BluetoothMessageReaderInterface {

    private long count = 0;
    private long systolicPressure = 0;
    private long diastolicPressure = 0;
    private long pulseRate = 0;
    private long cuffPressure = 0;
    private int state;

    public static int UNKNOWN = -1;
    public static int BEGIN = 0;
    public static int CUFF_H = 1;
    public static int CUFF_L = 2;
    public static int SYS_H = 3;
    public static int SYS_L = 4;
    public static int DIA_H = 5;
    public static int DIA_L = 6;
    public static int PUL_H = 7;
    public static int PUL_L = 8;
    public static int END = 9;

    public NIBPReader() {
        this.state = UNKNOWN;
    }

    @Override
    public void parse(String msg) {
        int tmp = 0;
        for (char x : msg.toCharArray()) {
            if (x == '\2') {
                state = BEGIN;
            } else if (state == BEGIN) {
                tmp = x;
                state = CUFF_H;
            } else if (state == CUFF_H) {
                cuffPressure = (tmp << 8) + x;
                tmp = 0;
                state = CUFF_L;
            } else if (state == CUFF_L) {
                tmp = x;
                state = SYS_H;
            } else if (state == SYS_H) {
                systolicPressure = (tmp << 8) + x;
                tmp = 0;
                state = SYS_L;
            } else if (state == SYS_L) {
                tmp = x;
                state = DIA_H;
            } else if (state == DIA_H) {
                diastolicPressure = (tmp << 8) + x;
                tmp = 0;
                state = DIA_L;
            } else if (state == DIA_L) {
                tmp = x;
                state = PUL_H;
            } else if (state == PUL_H) {
                pulseRate = (tmp << 8) + x;
                tmp = 0;
                state = PUL_L;
            } else if (state == PUL_L && x == '\3') {
                state = END;
            } else if (x == '\r' && state == END) {
                state = UNKNOWN;
            }
        }
    }

    public long getCount() {
        return count;
    }

    public long getSystolicPressure() {
        return systolicPressure;
    }

    public long getDiastolicPressure() {
        return diastolicPressure;
    }

    public long getPulseRate() {
        return pulseRate;
    }

    public long getCuffPressure() {
        return cuffPressure;
    }
}
