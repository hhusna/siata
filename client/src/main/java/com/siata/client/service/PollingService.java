package com.siata.client.service;

import com.siata.client.api.DataVersionApi;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

/**
 * Background polling service to detect data changes on server.
 * Polls the server every 60 seconds and sets hasNewData flag if version changed.
 * UI can bind to hasNewData to show refresh notification.
 */
public class PollingService {
    
    private static final PollingService instance = new PollingService();
    
    private final DataVersionApi dataVersionApi = new DataVersionApi();
    private Timeline pollingTimeline;
    private long lastKnownVersion = 0;
    
    // Observable property for UI binding
    private final BooleanProperty hasNewData = new SimpleBooleanProperty(false);
    
    // Polling interval in seconds (10 for testing, change to 60 for production)
    private static final int POLLING_INTERVAL_SECONDS = 10;
    
    private PollingService() {
        // Private constructor for singleton
    }
    
    public static PollingService getInstance() {
        return instance;
    }
    
    /**
     * Start background polling.
     * Call this after user login.
     */
    public void startPolling() {
        // Fetch initial version
        long initialVersion = dataVersionApi.getDataVersion();
        if (initialVersion > 0) {
            lastKnownVersion = initialVersion;
        }
        
        // Stop any existing polling
        stopPolling();
        
        // Create new polling timeline
        pollingTimeline = new Timeline(
            new KeyFrame(Duration.seconds(POLLING_INTERVAL_SECONDS), event -> {
                checkForUpdates();
            })
        );
        pollingTimeline.setCycleCount(Animation.INDEFINITE);
        pollingTimeline.play();
        
        System.out.println("Polling started. Initial version: " + lastKnownVersion);
    }
    
    /**
     * Stop background polling.
     * Call this on logout.
     */
    public void stopPolling() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
            pollingTimeline = null;
        }
        hasNewData.set(false);
        System.out.println("Polling stopped.");
    }
    
    /**
     * Check for updates from server.
     * Called automatically by the polling timeline.
     */
    private void checkForUpdates() {
        System.out.println("[POLLING] Checking for updates...");
        
        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            long serverVersion = dataVersionApi.getDataVersion();
            
            System.out.println("[POLLING] Server version: " + serverVersion + ", Last known: " + lastKnownVersion);
            
            if (serverVersion > 0 && serverVersion != lastKnownVersion) {
                System.out.println("[POLLING] *** New data detected! Showing red dot ***");
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    hasNewData.set(true);
                });
            } else if (serverVersion == -1) {
                System.out.println("[POLLING] ERROR: Failed to fetch version from server!");
            } else {
                System.out.println("[POLLING] No changes detected.");
            }
        }).start();
    }
    
    /**
     * Acknowledge that user has seen the update (clicked refresh).
     * Updates lastKnownVersion and hides notification.
     */
    public void acknowledgeUpdate() {
        long currentVersion = dataVersionApi.getDataVersion();
        if (currentVersion > 0) {
            lastKnownVersion = currentVersion;
        }
        hasNewData.set(false);
        System.out.println("Update acknowledged. Version: " + lastKnownVersion);
    }
    
    /**
     * Force update the lastKnownVersion after user makes a change.
     * Call this after any local data modification.
     */
    public void updateLocalVersion() {
        long currentVersion = dataVersionApi.getDataVersion();
        if (currentVersion > 0) {
            lastKnownVersion = currentVersion;
        }
        hasNewData.set(false);
    }
    
    /**
     * Observable property for UI to bind to.
     * True if there's new data on server that user hasn't seen.
     */
    public BooleanProperty hasNewDataProperty() {
        return hasNewData;
    }
    
    public boolean hasNewData() {
        return hasNewData.get();
    }
}
