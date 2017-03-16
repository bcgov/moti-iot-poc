/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.example.publisher;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.message.KuraPosition;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;
import java.util.Set;

public class ExamplePublisher implements ConfigurableComponent, CloudClientListener {

    private static final Logger s_logger = LoggerFactory.getLogger(ExamplePublisher.class);

    // Cloud Application identifier
    private static final String APP_ID = "WEATHER-CSV-PUBLISHER";

    // Publishing Property Names
    private static final String PUBLISH_RATE_PROP_NAME = "publish.rate";
    private static final String PUBLISH_TOPIC_PROP_NAME = "publish.appTopic";
    private static final String PUBLISH_QOS_PROP_NAME = "publish.qos";
    private static final String PUBLISH_RETAIN_PROP_NAME = "publish.retain";
    private static final String[] DEV_PROP_NAMES = {"dev.lat", "dev.long", "dev.csvPath"};


    private CloudService m_cloudService;
    private CloudClient m_cloudClient;

    private final ScheduledExecutorService m_worker;
    private ScheduledFuture<?> m_handle;

    private Map<String, Object> m_properties;

    private Map<String, Vector<Float>> m_weatherData = null;
    private int m_rowIndex = 0;
    private int m_totalRows;
    private Set<String> m_dataCols;


    // ----------------------------------------------------------------
    //
    // Dependencies
    //
    // ----------------------------------------------------------------

    public ExamplePublisher() {
        super();
        this.m_worker = Executors.newSingleThreadScheduledExecutor();
    }

    public void setCloudService(CloudService cloudService) {
        this.m_cloudService = cloudService;
    }

    public void unsetCloudService(CloudService cloudService) {
        this.m_cloudService = null;
    }

    // ----------------------------------------------------------------
    //
    // Activation APIs
    //
    // ----------------------------------------------------------------

    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        s_logger.info("Activating ExamplePublisher...");

        this.m_properties = properties;
        for (String s : properties.keySet()) {
            s_logger.info("Activate - " + s + ": " + properties.get(s));
        }
        
        //load the csv data
        String fpath = (String) this.m_properties.get("dev.csvPath");
        parse(fpath);


        // get the mqtt client for this application
        try {

            // Acquire a Cloud Application Client for this Application
            s_logger.info("Getting CloudApplicationClient for {}...", APP_ID);
            this.m_cloudClient = this.m_cloudService.newCloudClient(APP_ID);
            this.m_cloudClient.addCloudClientListener(this);

            // Don't subscribe because these are handled by the default
            // subscriptions and we don't want to get messages twice
            doUpdate();
        } catch (Exception e) {
            s_logger.error("Error during component activation", e);
            throw new ComponentException(e);
        }
        s_logger.info("Activating ExamplePublisher... Done.");
    }

    protected void deactivate(ComponentContext componentContext) {
        s_logger.debug("Deactivating ExamplePublisher...");

        // shutting down the worker and cleaning up the properties
        this.m_worker.shutdown();

        // Releasing the CloudApplicationClient
        s_logger.info("Releasing CloudApplicationClient for {}...", APP_ID);
        this.m_cloudClient.release();

        s_logger.debug("Deactivating ExamplePublisher... Done.");
    }

    public void updated(Map<String, Object> properties) {
        s_logger.info("Updated ExamplePublisher...");

        // store the properties received
        this.m_properties = properties;
        for (String s : properties.keySet()) {
            s_logger.info("Update - " + s + ": " + properties.get(s));
        }

        // try to kick off a new job
        doUpdate();
        s_logger.info("Updated ExamplePublisher... Done.");
    }

    // ----------------------------------------------------------------
    //
    // Cloud Application Callback Methods
    //
    // ----------------------------------------------------------------

    @Override
    public void onConnectionEstablished() {
        s_logger.info("Connection established");

        try {
            // Getting the lists of unpublished messages
            s_logger.info("Number of unpublished messages: {}", this.m_cloudClient.getUnpublishedMessageIds().size());
        } catch (KuraException e) {
            s_logger.error("Cannot get the list of unpublished messages");
        }

        try {
            // Getting the lists of in-flight messages
            s_logger.info("Number of in-flight messages: {}", this.m_cloudClient.getInFlightMessageIds().size());
        } catch (KuraException e) {
            s_logger.error("Cannot get the list of in-flight messages");
        }

        try {
            // Getting the lists of dropped in-flight messages
            s_logger.info("Number of dropped in-flight messages: {}",
                    this.m_cloudClient.getDroppedInFlightMessageIds().size());
        } catch (KuraException e) {
            s_logger.error("Cannot get the list of dropped in-flight messages");
        }
    }

    @Override
    public void onConnectionLost() {
        s_logger.warn("Connection lost!");
    }

    @Override
    public void onControlMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
        s_logger.info("Control message arrived on assetId: {} and semantic topic: {}", deviceId, appTopic);
    }

    @Override
    public void onMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
        s_logger.info("Message arrived on assetId: {} and semantic topic: {}", deviceId, appTopic);
    }

    @Override
    public void onMessagePublished(int messageId, String appTopic) {
        s_logger.info("Published message with ID: {} on application topic: {}", messageId, appTopic);
    }

    @Override
    public void onMessageConfirmed(int messageId, String appTopic) {
        s_logger.info("Confirmed message with ID: {} on application topic: {}", messageId, appTopic);
    }

    // ----------------------------------------------------------------
    //
    // Private Methods
    //
    // ----------------------------------------------------------------

    /**
     * Called after a new set of properties has been configured on the service
     */
    private void doUpdate() {
        // cancel a current worker handle if one if active
        if (this.m_handle != null) {
            this.m_handle.cancel(true);
        }

        if (!this.m_properties.containsKey(PUBLISH_RATE_PROP_NAME)) {        	
            s_logger.info(
                    "Update ExamplePublisher - Ignore as properties do not contain TEMP_INITIAL_PROP_NAME and PUBLISH_RATE_PROP_NAME.");
            return;
        }

        // schedule a new worker based on the properties of the service
        int pubrate = (Integer) this.m_properties.get(PUBLISH_RATE_PROP_NAME);
        this.m_handle = this.m_worker.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                doPublish();
            }
        }, 0, pubrate, TimeUnit.MILLISECONDS);
    }

    /***
     * Written by Jeff!
     * 
     * @param path: path to the weather data csv file on disk.
     */
    private void parse(String path) {
    	Map<String, Vector<Float>> map = new HashMap<String, Vector<Float>>();
    	String[] columnNames = new String[10];
    	int rows = 0;
    	try {
    		s_logger.info("Attempting to read: " + path);
    		BufferedReader reader = new BufferedReader(new FileReader(path));
    		String line;
    		boolean firstLine = true;
    		while ((line = reader.readLine()) != null) {
    			if(firstLine) {
    				columnNames = line.split(",");
    				for(int i = 0; i < columnNames.length; i++) {
    					map.put(columnNames[i], new Vector<Float>());
    				}
    				firstLine = false;
    			}
    			else {
    				rows++;
    				String[] columnVals = line.split(",");
    				for(int i = 0; i < columnVals.length; i++) {
    					map.get(columnNames[i]).add(Float.parseFloat(columnVals[i]));
    				}
    			}
    		}
    		reader.close();

    		this.m_weatherData = map;
    		
    		// remove date-time columns. What remains is expected to be data cols (metrics to be sent).
    		this.m_dataCols = map.keySet();
   			this.m_dataCols.remove("year");
   			this.m_dataCols.remove("day");
   			this.m_dataCols.remove("hour_min");
    		
    		this.m_totalRows = rows;
    		
    		s_logger.info(String.format("Processed %d rows of data.", this.m_totalRows));
    	}
    	catch (Exception e) {
    		this.m_weatherData = null;
    		System.err.format("Exception occurred trying to read '%s'.", path);
    		e.printStackTrace();
    	}
    }
    
    /**
     * Called at the configured rate to publish the next temperature measurement.
     */
    private void doPublish() {
        // fetch the publishing configuration from the publishing properties
        String topic = (String) this.m_properties.get(PUBLISH_TOPIC_PROP_NAME);
        Integer qos = (Integer) this.m_properties.get(PUBLISH_QOS_PROP_NAME);
        Boolean retain = (Boolean) this.m_properties.get(PUBLISH_RETAIN_PROP_NAME);

        
        float lat = (Float) this.m_properties.get("dev.lat");
        float lng = (Float) this.m_properties.get("dev.long");
        
        // Allocate a new payload
        KuraPayload payload = new KuraPayload();
        
        // Timestamp the message
        payload.setTimestamp(new Date());

        KuraPosition pos = new KuraPosition();
        pos.setLatitude(lat);
        pos.setLongitude(lng);
        
        payload.setPosition(pos);
 
        if (this.m_weatherData != null) {
        	String debug_str = "Hello from kura emulator!";
        	
        	for (String k: this.m_dataCols) {
        		float v = this.m_weatherData.get(k).get(this.m_rowIndex);
            	payload.addMetric(k, v);
            	debug_str += String.format("%s: %f,", k, v);
        	}

        	payload.addMetric("debug_msg", debug_str);
        	
        	this.m_rowIndex = (this.m_rowIndex + 1) % this.m_totalRows;
        }
     
        
        // Publish the message
        try {
            int messageId = this.m_cloudClient.publish(topic, payload, qos, retain);
            s_logger.info("Published to {} message: {} with ID: {}", new Object[] { topic, payload, messageId });
        } catch (Exception e) {
            s_logger.error("Cannot publish topic: " + topic, e);
        }
    }
}
