package io.tavuc.skillsystem.api.model;

import java.util.*;

/**
 * Represents a single stat instance for a player with support for modifiers.
 */
public class Stat {

    private final StatType type;
    private int baseValue;
    private int maxValue;
    private final Map<UUID, StatModifier> modifiers = new HashMap<>();

    /**
     * Constructs a stat.
     *
     * @param type      The type of the stat.
     * @param baseValue The initial base value.
     * @param maxValue  The maximum allowed value.
     */
    public Stat(StatType type, int baseValue, int maxValue) {
        this.type = type;
        this.baseValue = Math.min(baseValue, maxValue);
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
     * Returns the base stat value without modifiers.
     *
     * @return The base value.
     */
    public int getBaseValue() {
        return baseValue;
    }
    
    /**
     * Returns the effective stat value after applying all modifiers.
     *
     * @return The effective value with modifiers applied.
     */
    public int getValue() {
        float total = baseValue;
        
        // Apply additive modifiers first
        for (StatModifier modifier : modifiers.values()) {
            if (modifier.getType() == ModifierType.ADDITIVE) {
                total += modifier.getValue();
            }
        }
        
        // Then apply multiplicative modifiers
        for (StatModifier modifier : modifiers.values()) {
            if (modifier.getType() == ModifierType.MULTIPLICATIVE) {
                total *= (1 + modifier.getValue() / 100.0f);
            }
        }
        
        return Math.min(Math.round(total), maxValue);
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
     * Sets the stat's base value.
     *
     * @param value The new value.
     */
    public void setBaseValue(int value) {
        this.baseValue = Math.max(0, Math.min(value, maxValue));
    }

    /**
     * Sets the stat's maximum value.
     *
     * @param maxValue The new maximum value.
     */
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        if (baseValue > maxValue) {
            baseValue = maxValue;
        }
    }

    /**
     * Increases the base stat value by a specified amount.
     *
     * @param amount The amount to add.
     */
    public void add(int amount) {
        setBaseValue(this.baseValue + amount);
    }

    /**
     * Decreases the base stat value by a specified amount.
     *
     * @param amount The amount to remove.
     */
    public void remove(int amount) {
        setBaseValue(this.baseValue - amount);
    }
    
    /**
     * Add a temporary modifier to this stat.
     *
     * @param value     The value of the modifier
     * @param type      The type of modifier (additive or multiplicative)
     * @param duration  Duration in ticks or -1 for permanent until removed
     * @param source    The source of this modifier (potion, equipment, etc.)
     * @return The UUID of the created modifier
     */
    public UUID addModifier(float value, ModifierType type, int duration, String source) {
        UUID id = UUID.randomUUID();
        StatModifier modifier = new StatModifier(id, value, type, duration, source);
        modifiers.put(id, modifier);
        return id;
    }
    
    /**
     * Add a temporary modifier with a specific ID.
     *
     * @param id        The UUID to use for this modifier
     * @param value     The value of the modifier
     * @param type      The type of modifier (additive or multiplicative)
     * @param duration  Duration in ticks or -1 for permanent until removed
     * @param source    The source of this modifier (potion, equipment, etc.)
     * @return The created modifier
     */
    public StatModifier addModifier(UUID id, float value, ModifierType type, int duration, String source) {
        StatModifier modifier = new StatModifier(id, value, type, duration, source);
        modifiers.put(id, modifier);
        return modifier;
    }
    
    /**
     * Remove a specific modifier by its ID.
     *
     * @param id The UUID of the modifier to remove
     * @return true if found and removed, false otherwise
     */
    public boolean removeModifier(UUID id) {
        return modifiers.remove(id) != null;
    }
    
    /**
     * Remove all modifiers from a specific source.
     *
     * @param source The source to remove modifiers for
     * @return The number of modifiers removed
     */
    public int removeModifiersFromSource(String source) {
        int count = 0;
        Iterator<StatModifier> it = modifiers.values().iterator();
        
        while (it.hasNext()) {
            StatModifier modifier = it.next();
            if (modifier.getSource().equals(source)) {
                it.remove();
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Get all current modifiers.
     *
     * @return Map of modifier ID to StatModifier
     */
    public Map<UUID, StatModifier> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }
    
    /**
     * Update all modifier durations.
     * Should be called every tick for active stats.
     */
    public void updateModifiers() {
        Iterator<StatModifier> it = modifiers.values().iterator();
        
        while (it.hasNext()) {
            StatModifier modifier = it.next();
            if (!modifier.tick()) {
                it.remove();
            }
        }
    }
}