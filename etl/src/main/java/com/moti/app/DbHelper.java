package com.moti.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.moti.app.models.*;

public class DbHelper {
    private Config config;
    private Statement statement;
    public DbHelper() {
        config = Config.getInstance();
    }

    public void writeDevice(Device device) {
        List<Device> devices = new ArrayList<>();
        devices.add(device);
        writeDevices(devices);
    }

    public void writeDevices(List<Device> devices) {
        if(devices.size() == 0) return;
        String insertString = "insert into acs_device (lat, lng, name) values (";
        for (int i = 0; i < devices.size(); i++) {
            Device d = devices.get(i);
            insertString += String.format("%f, %f, '%s'", d.getLat(), d.getLng(), d.getName());
            if(i != devices.size() - 1) insertString += ", ";
        }
        insertString += ");";
        System.out.println(insertString);

        try {
            getStatement().execute(insertString);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void writeEvent(Event event) {
        List<Event> events = new ArrayList<>();
        events.add(event);
        writeEvents(events);
    }

    public void writeEvents(List<Event> events) {
        if(events.size() == 0) return;
        String insertString = "insert into acs_device_event (device_id, sensor, ts, val) values";
        for(int i = 0; i < events.size(); i++) {
            String eventString = " (%d, '%s', '%s', '%s')";

            if(i != events.size() - 1) eventString += ",";

            Event e = events.get(i);
            insertString += String.format(eventString, e.getDeviceId(), e.getSensor(), e.getTs(), e.getVal());
        }
        insertString += ";";
        System.out.println(insertString);
        try {
            getStatement().execute(insertString);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private int getDeviceIdForName(String name) {
        int deviceId = 0;
        try {
            ResultSet rs = getStatement().executeQuery(String.format("select id from acs_device where name='%s'", name.toLowerCase()));
            while(rs.next()) {
                deviceId = rs.getInt("id");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

    public int getDeviceIdForName(String name, double lat, double lng) {
        name = name.toLowerCase();
        int deviceId = getDeviceIdForName(name);
        try {
            if(deviceId > 0) {
                return deviceId;
            }

            Device d = new Device();
            d.setLat(lat);
            d.setLng(lng);
            d.setName(name);

            writeDevice(d);

            deviceId = getDeviceIdForName(name);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

    private Statement getStatement() {
        if(statement == null) {
            try {
                Class.forName(config.getPgDriver());
                Connection conn = DriverManager.getConnection(
                        config.getPgDatabase(),
                        config.getPgUser(),
                        config.getPgPassword()
                );
                statement = conn.createStatement();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return statement;
    }

}
