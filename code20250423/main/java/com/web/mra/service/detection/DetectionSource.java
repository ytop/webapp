// DetectionSource.java
package com.web.mra.service.detection;

/**
 * Constants defining different types of detection sources.
 */
public final class DetectionSource {
    public static final int DATA_MART = 1;
    public static final int EXCEL = 2;
    public static final int TEXT = 3;

    private DetectionSource() {
    } // Prevent instantiation

    /**
     * Get the string representation of a detection source type.
     *
     * @param value The source type value
     * @return String representation of the source type
     * @throws IllegalArgumentException if the value is invalid
     */
    public static String getSourceName(int value) {
        switch (value) {
            case DATA_MART:
                return "DATA_MART";
            case EXCEL:
                return "EXCEL";
            case TEXT:
                return "TEXT";
            default:
                throw new IllegalArgumentException("Invalid detection source: " + value);
        }
    }

    /**
     * Validate if a source type value is valid.
     *
     * @param value The source type value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSource(int value) {
        return value >= DATA_MART && value <= TEXT;
    }
}

