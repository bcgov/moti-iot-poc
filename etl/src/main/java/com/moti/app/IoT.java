package com.moti.app;

public class IoT {
    public static void main(String []args) {
        MqttHelper reader = new MqttHelper();
        reader.start();
    }
}
