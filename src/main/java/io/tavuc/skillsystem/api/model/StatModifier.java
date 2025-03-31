package io.tavuc.skillsystem.api.model;

import java.util.UUID;

/**
 * Represents a temporary modifier applied to a stat.
 */
public class StatModifier {
    private final UUID id;
    private final float value;
    private final ModifierType type;
    private int duration; // in ticks, -1 for permanent
    private final String source;
    
    /**
     * Creates a new stat modifier.
     *
     * @param id       The unique identifier for this modifier
     * @param value    The value of the modifier
     * @param type     The type of modifier (additive or multiplicative)
     * @param duration Duration in ticks or -1 for permanent until removed
     * @param source   The source of this modifier (potion, equipment, etc.)
     */
    public StatModifier(UUID id, float value, ModifierType type, int duration, String source) {
        this.id = id;
        this.value = value;
        this.type = type;
        this.duration = duration;
        this.source = source;
    }
    
    /**
     * Get the unique identifier of this modifier.
     *
     * @return The UUID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Get the value of this modifier.
     *
     * @return The value
     */
    public float getValue() {
        return value;
    }
    
    /**
     * Get the type of this modifier.
     *
     * @return The modifier type
     */
    public ModifierType getType() {
        return type;
    }
    
    /**
     * Get the remaining duration of this modifier.
     *
     * @return The duration in ticks or -1 for permanent
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Set the remaining duration of this modifier.
     *
     * @param duration The new duration in ticks
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    /**
     * Get the source of this modifier.
     *
     * @return The source description
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Decrement the duration by one tick.
     * 
     * @return True if the modifier is still active, false if expired
     */
    public boolean tick() {
        if (duration <= 0) {
            return true; // Permanent modifier
        }
        
        duration--;
        return duration > 0;
    }
    
    /**
     * Format the duration into a human-readable string.
     *
     * @return A string representation of the duration
     */
    public String getFormattedDuration() {
        if (duration == -1) {
            return "Permanent";
        }
        
        int seconds = duration / 20;
        if (seconds < 60) {
            return seconds + "s";
        }
        
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes + "m " + seconds + "s";
    }
}