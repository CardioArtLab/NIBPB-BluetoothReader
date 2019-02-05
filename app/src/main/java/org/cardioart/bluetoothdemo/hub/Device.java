package org.cardioart.bluetoothdemo.hub;

import java.util.Objects;

public class Device {

    public static final int INVALID_PRODUCT_ID = 0;
    public static final String INVALID_PRODUCT_NAME = "Invalid";
    public static final int SPO2_PRODUCT_ID = 1;
    public static final String SPO2_PRODUCT_NAME = "Pulse Oximeter";
    public static final int SPO2_SAMPLING_RATE_HZ = 200;
    public static final int ECG_PRODUCT_ID = 2;
    public static final String ECG_PRODUCT_NAME = "ECG Monitor";
    public static final int ECG_SAMPLING_RATE_HZ = 500;
    public static final int NIBP_PRODUCT_ID = 3;
    public static final String NIBP_PRODUCT_NAME = "NIBP";

    public int bus;
    public int address;
    public int productId;

    public Device(int bus, int address, int productId) {
        this.bus = bus;
        this.address = address;
        this.productId = productId;
    }

    public Device(String str) throws Exception {
        String[] tokens = str.split(":");
        if (tokens.length == 3) {
            this.bus = Integer.valueOf(tokens[0]);
            this.address = Integer.valueOf(tokens[1]);
            this.productId = Integer.valueOf(tokens[2]);
        } else {
            throw new Exception("Device requires string format %d:%d:%d");
        }
    }

    public String getProductName() {
        switch (productId) {
            case SPO2_PRODUCT_ID:
                return SPO2_PRODUCT_NAME;
            case ECG_PRODUCT_ID:
                return ECG_PRODUCT_NAME;
            case NIBP_PRODUCT_ID:
                return NIBP_PRODUCT_NAME;
        }
        return INVALID_PRODUCT_NAME;
    }

    @Override
    public String toString() {
        return "Device{" +
                "bus=" + bus +
                ", address=" + address +
                ", product=" + getProductName()+
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return bus == device.bus &&
                address == device.address &&
                productId == device.productId;
    }
}
