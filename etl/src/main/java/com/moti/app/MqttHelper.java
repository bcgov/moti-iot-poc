package com.moti.app;

import org.eclipse.kura.core.cloud.CloudPayloadProtoBufDecoderImpl;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.message.KuraPosition;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.*;

import com.moti.app.models.*;

class MqttHelper implements Runnable {

    private Config config;
    private Thread thread;
    private DbHelper dbHelper;

    MqttHelper() {}

    void start() {
        config = Config.getInstance();
        dbHelper = new DbHelper();
        subscribe();
    }

    private void subscribe() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttConnectOptions opts = new MqttConnectOptions();
            opts.setUserName(config.getBrokerUser());
            opts.setPassword(config.getBrokerPassword().toCharArray());

            MqttClient client = new MqttClient(config.getBrokerUrl(), "", persistence);
            client.connect(opts);
            System.out.println("Connection established!");
            client.subscribe(config.getBrokerTopic(), 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String s, MqttMessage message) throws Exception {
                    if(s.contains("EDC") || s.contains("sim-test")) {
                        System.out.println(s);
                        return;
                    }

                    String deviceName = s.split("/")[1];

                    try {
                        CloudPayloadProtoBufDecoderImpl decoder = new CloudPayloadProtoBufDecoderImpl(message.getPayload());
                        KuraPayload payload = decoder.buildFromByteArray();

                        KuraPosition pos = payload.getPosition();
                        int deviceId = dbHelper.getDeviceIdForName(deviceName, pos.getLatitude(), pos.getLongitude());

                        List<Event> events = new ArrayList<>();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                        String timestamp = sdf.format(payload.getTimestamp());

                        Iterator itr = payload.metrics().entrySet().iterator();

                        while(itr.hasNext()) {
                            Map.Entry pair = (Map.Entry) itr.next();

                            Event e = new Event();
                            e.setDeviceId(deviceId);
                            e.setSensor(pair.getKey().toString());
                            e.setVal(pair.getValue().toString());
                            e.setTs(timestamp);

                            events.add(e);
                        }

                        dbHelper.writeEvents(events);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override public void run() {
        while(thread != null) {}
    }
}
