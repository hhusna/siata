package com.siata.client;

/**
 * Launcher class for creating executable JAR.
 * JavaFX applications need a non-JavaFX class as entry point when packaged as a fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        // Fix DPI scaling issues on Windows with high-DPI displays
        // Allow JavaFX to use system DPI scaling properly
        System.setProperty("prism.allowhidpi", "true");
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("glass.win.forceIntegerRenderScale", "false");
        
        MainApplication.main(args);
    }
}

