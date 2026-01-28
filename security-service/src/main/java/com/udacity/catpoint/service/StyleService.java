package com.udacity.catpoint.service;

import java.awt.Font;

/**
 * Provides shared styling for UI components
 */
public class StyleService {

    // MUST be public for application package access
    public static final Font HEADING_FONT =
            new Font("SansSerif", Font.BOLD, 18);

    private StyleService() {
        // utility class – prevent instantiation
    }
}
