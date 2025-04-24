// MatchRule.java
package com.web.mra.service.detection;

/**
 * Constants defining different types of match rules for detection comparison.
 */
public final class MatchRule {
    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int GREATER_THAN = 3;
    public static final int LESS_THAN = 4;
    public static final int GREATER_THAN_EQUAL = 5;
    public static final int LESS_THAN_EQUAL = 6;

    private MatchRule() {
    } // Prevent instantiation

    /**
     * Get the string representation of a match rule type.
     *
     * @param value The match rule value
     * @return String representation of the match rule
     * @throws IllegalArgumentException if the value is invalid
     */
    public static String getRuleName(int value) {
        switch (value) {
            case EQUAL:
                return "EQUAL";
            case NOT_EQUAL:
                return "NOT_EQUAL";
            case GREATER_THAN:
                return "GREATER_THAN";
            case LESS_THAN:
                return "LESS_THAN";
            case GREATER_THAN_EQUAL:
                return "GREATER_THAN_EQUAL";
            case LESS_THAN_EQUAL:
                return "LESS_THAN_EQUAL";
            default:
                throw new IllegalArgumentException("Invalid match rule: " + value);
        }
    }

    /**
     * Validate if a match rule value is valid.
     *
     * @param value The match rule value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRule(int value) {
        return value >= EQUAL && value <= LESS_THAN_EQUAL;
    }
}
