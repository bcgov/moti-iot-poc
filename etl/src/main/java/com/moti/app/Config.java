package com.moti.app;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private String h2Driver = "org.h2.Driver";
    private String h2Database = "";
    private String h2User = "";
    private String h2Password = "";

    private String pgDriver = "org.postgresql.Driver";
    private String pgDatabase = "";
    private String pgUser = "";
    private String pgPassword = "";

    private String brokerUrl = "";
    private String brokerTopic = "";

    private String brokerUser = "";
    private String brokerPassword = "";

    private static Config instance;

    public Config() {}

    public static Config getInstance() {
        if(instance == null) {
            instance = new Config();

            instance.setH2Database(System.getenv("H2_DATABASE"));
            instance.setH2User(System.getenv("H2_USER"));
            instance.setH2Password(System.getenv("H2_PASSWORD"));

            instance.setPgDatabase(System.getenv("PG_DATABASE"));
            instance.setPgUser(System.getenv("PG_USER"));
            instance.setPgPassword(System.getenv("PG_PASSWORD"));

            instance.setBrokerUrl(System.getenv("BROKER_URL"));
            instance.setBrokerTopic(System.getenv("BROKER_TOPIC"));
            instance.setBrokerUser(System.getenv("BROKER_USER"));
            instance.setBrokerPassword(System.getenv("BROKER_PASSWORD"));
        }
        return instance;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getBrokerTopic() {
        return brokerTopic;
    }

    public void setBrokerTopic(String brokerTopic) {
        this.brokerTopic = brokerTopic;
    }

    public String getBrokerUser() {
        return brokerUser;
    }

    public void setBrokerUser(String brokerUser) {
        this.brokerUser = brokerUser;
    }

    public String getBrokerPassword() {
        return brokerPassword;
    }

    public void setBrokerPassword(String brokerPassword) {
        this.brokerPassword = brokerPassword;
    }

    public String getPgPassword() {
        return pgPassword;
    }

    public void setPgPassword(String pgPassword) {
        this.pgPassword = pgPassword;
    }

    public String getH2Driver() {
        return h2Driver;
    }

    public String getH2Database() {
        return h2Database;
    }

    public void setH2Database(String h2Database) {
        this.h2Database = h2Database;
    }

    public String getH2User() {
        return h2User;
    }

    public void setH2User(String h2User) {
        this.h2User = h2User;
    }

    public String getH2Password() {
        return h2Password;
    }

    public void setH2Password(String h2Password) {
        this.h2Password = h2Password;
    }

    public String getPgDriver() {
        return pgDriver;
    }

    public String getPgDatabase() {
        return pgDatabase;
    }

    public void setPgDatabase(String pgDatabase) {
        this.pgDatabase = pgDatabase;
    }

    public String getPgUser() {
        return pgUser;
    }

    public void setPgUser(String pgUser) {
        this.pgUser = pgUser;
    }
}
