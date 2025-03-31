package io.tavuc.skillsystem.config;

/**
 * Represents a formula configuration for stat calculations.
 */
public class FormulaConfig {
    private final String key;
    private final FormulaType type;
    private final double baseValue;
    private final double scaleValue;
    private final double factor;
    private final double step;
    
    /**
     * Creates a new formula configuration.
     *
     * @param key        The formula key
     * @param type       The formula type
     * @param baseValue  The base value
     * @param scaleValue The scale value
     * @param factor     The factor (for diminishing returns)
     */
    public FormulaConfig(String key, FormulaType type, double baseValue, double scaleValue, double factor) {
        this(key, type, baseValue, scaleValue, factor, 1.0);
    }
    
    /**
     * Creates a new formula configuration.
     *
     * @param key        The formula key
     * @param type       The formula type
     * @param baseValue  The base value
     * @param scaleValue The scale value
     * @param factor     The factor (for diminishing returns)
     * @param step       The step value (for stepped formula)
     */
    public FormulaConfig(String key, FormulaType type, double baseValue, double scaleValue, double factor, double step) {
        this.key = key;
        this.type = type;
        this.baseValue = baseValue;
        this.scaleValue = scaleValue;
        this.factor = factor;
        this.step = step;
    }
    
    /**
     * Get the formula key.
     *
     * @return The key
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Get the formula type.
     *
     * @return The type
     */
    public FormulaType getType() {
        return type;
    }
    
    /**
     * Get the base value.
     *
     * @return The base value
     */
    public double getBaseValue() {
        return baseValue;
    }
    
    /**
     * Get the scale value.
     *
     * @return The scale value
     */
    public double getScaleValue() {
        return scaleValue;
    }
    
    /**
     * Get the factor value.
     *
     * @return The factor value
     */
    public double getFactor() {
        return factor;
    }
    
    /**
     * Get the step value.
     *
     * @return The step value
     */
    public double getStep() {
        return step;
    }
    
    /**
     * Calculate a result based on this formula.
     *
     * @param statValue The stat value to use
     * @return The calculated result
     */
    public double calculate(double statValue) {
        switch (type) {
            case LINEAR:
                return baseValue + (statValue * scaleValue);
            
            case DIMINISHING:
                return baseValue + (statValue * scaleValue * (1 - Math.exp(-factor * statValue)));
            
            case STEPPED:
                return baseValue + (Math.floor(statValue / step) * scaleValue);
            
            case CHANCE:
                return Math.min(baseValue + (statValue * scaleValue), 100.0);
            
            default:
                return statValue;
        }
    }
}