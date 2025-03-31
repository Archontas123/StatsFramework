package io.tavuc.skillsystem.config;

/**
 * Types of formulas available for stat calculations.
 */
public enum FormulaType {
    /**
     * Linear scaling: baseValue + (statValue * scale)
     */
    LINEAR,
    
    /**
     * Diminishing returns curve: baseValue + (statValue * scale * (1 - e^(-factor * statValue)))
     */
    DIMINISHING,
    
    /**
     * Stepped scaling (stair-like): baseValue + (floor(statValue / step) * scale)
     */
    STEPPED,
    
    /**
     * Percentage chance: min(baseValue + (statValue * scale), 100)
     * Used for proc-based effects like critical hits
     */
    CHANCE
}