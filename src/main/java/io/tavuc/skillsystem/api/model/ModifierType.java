package io.tavuc.skillsystem.api.model;

/**
 * Types of stat modifiers.
 */
public enum ModifierType {
    /**
     * Adds a flat value to the base stat.
     * Applied before multiplicative modifiers.
     */
    ADDITIVE,
    
    /**
     * Multiplies the stat by a percentage.
     * Applied after additive modifiers.
     * Values are percentage-based (e.g., 10 = 10% increase)
     */
    MULTIPLICATIVE
}