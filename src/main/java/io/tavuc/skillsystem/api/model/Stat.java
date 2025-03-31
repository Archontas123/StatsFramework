package io.tavuc.skillsystem.api.model;

/**
 * Represents a single stat instance for a player.
 */
public class Stat {

    private final StatType type;
    private int value;
    private int maxValue;

    /**
     * Constructs a stat.
     *
     * @param type      The type of the stat.
     * @param value     The initial value.
     * @param maxValue  The maximum allowed value.
     */
    public Stat(StatType type, int value, int maxValue) {
        this.type = type;
        this.value = Math.min(value, maxValue);
        this.maxValue = maxValue;
    }

    /**
     * Returns the stat type.
     *
     * @return The stat type.
     */
    public StatType getType() {
        return type;
    }

    /**
     * Returns the current stat value.
     *
     * @return The current value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the maximum stat value.
     *
     * @return The maximum value.
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the stat's value.
     *
     * @param value The new value.
     */
    public void setValue(int value) {
        this.value = Math.max(0, Math.min(value, maxValue));
    }

    /**
     * Sets the stat's maximum value.
     *
     * @param maxValue The new maximum value.
     */
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        if (value > maxValue) {
            value = maxValue;
        }
    }

    /**
     * Increases the stat value by a specified amount.
     *
     * @param amount The amount to add.
     */
    public void add(int amount) {
        setValue(this.value + amount);
    }

    /**
     * Decreases the stat value by a specified amount.
     *
     * @param amount The amount to remove.
     */
    public void remove(int amount) {
        setValue(this.value - amount);
    }
}
