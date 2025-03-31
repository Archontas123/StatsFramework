package io.tavuc.skillsystem.api.model;

import java.util.*;

/**
 * Represents the stats and progression of a player with support for modifiers.
 */
public class PlayerStats {

    private final Map<StatType, Stat> stats = new EnumMap<>(StatType.class);
    private int level;
    private int experience;
    private int unspentPoints;
    private final Map<String, Set<UUID>> activeEffects = new HashMap<>();

    /**
     * Constructs a PlayerStats object with default values.
     */
    public PlayerStats() {
        for (StatType type : StatType.values()) {
            stats.put(type, new Stat(type, 0, 100));
        }
        this.level = 1;
        this.experience = 0;
        this.unspentPoints = 0;
    }

    /**
     * Returns the stat object for a given type.
     *
     * @param type The stat type.
     * @return The Stat object.
     */
    public Stat getStat(StatType type) {
        return stats.get(type);
    }

    /**
     * Returns all the player's stats.
     *
     * @return Map of StatType to Stat.
     */
    public Map<StatType, Stat> getAllStats() {
        return Collections.unmodifiableMap(stats);
    }

    /**
     * Returns the player's level.
     *
     * @return The level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the player's level.
     *
     * @param level The new level.
     */
    public void setLevel(int level) {
        this.level = level;
        updateStatCaps();
    }
    
    /**
     * Updates the maximum values for all stats based on the current level.
     */
    private void updateStatCaps() {
        int maxValue = getMaxStatValueForLevel(level);
        for (Stat stat : stats.values()) {
            stat.setMaxValue(maxValue);
        }
    }
    
    /**
     * Calculate the maximum stat value allowed for a given level.
     *
     * @param level The player level.
     * @return The maximum stat value.
     */
    public int getMaxStatValueForLevel(int level) {
        // Base cap + tier increases
        if (level < 10) {
            return 50; // Tier 1: Levels 1-9
        } else if (level < 25) {
            return 100; // Tier 2: Levels 10-24
        } else if (level < 50) {
            return 200; // Tier 3: Levels 25-49
        } else if (level < 75) {
            return 350; // Tier 4: Levels 50-74
        } else {
            return 500; // Tier 5: Levels 75+
        }
    }

    /**
     * Returns the player's experience.
     *
     * @return The experience.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Sets the player's experience.
     *
     * @param experience The new experience.
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * Returns the player's unspent stat points.
     *
     * @return The unspent points.
     */
    public int getUnspentPoints() {
        return unspentPoints;
    }

    /**
     * Sets the player's unspent stat points.
     *
     * @param unspentPoints The new amount of unspent points.
     */
    public void setUnspentPoints(int unspentPoints) {
        this.unspentPoints = unspentPoints;
    }

    /**
     * Increases unspent stat points.
     *
     * @param amount The amount to add.
     */
    public void addUnspentPoints(int amount) {
        this.unspentPoints += amount;
    }

    /**
     * Decreases unspent stat points.
     *
     * @param amount The amount to remove.
     */
    public void removeUnspentPoints(int amount) {
        this.unspentPoints = Math.max(0, this.unspentPoints - amount);
    }
    
    /**
     * Add a temporary effect to a stat.
     *
     * @param type      The stat type to affect
     * @param value     The effect value
     * @param modType   The modifier type (additive or multiplicative)
     * @param duration  Duration in ticks or -1 for permanent until removed
     * @param source    Source identifier for the effect
     * @return The UUID of the created effect
     */
    public UUID addStatEffect(StatType type, float value, ModifierType modType, int duration, String source) {
        UUID effectId = stats.get(type).addModifier(value, modType, duration, source);
        
        // Track the effect by source
        activeEffects.computeIfAbsent(source, k -> new HashSet<>()).add(effectId);
        
        return effectId;
    }
    
    /**
     * Add a temporary effect to multiple stats at once.
     *
     * @param types     The stat types to affect
     * @param value     The effect value
     * @param modType   The modifier type (additive or multiplicative)
     * @param duration  Duration in ticks or -1 for permanent until removed
     * @param source    Source identifier for the effect
     * @return Map of StatType to the UUID of the created effect
     */
    public Map<StatType, UUID> addStatEffects(Collection<StatType> types, float value, ModifierType modType, 
                                             int duration, String source) {
        Map<StatType, UUID> addedEffects = new EnumMap<>(StatType.class);
        UUID sharedId = UUID.randomUUID();
        
        for (StatType type : types) {
            Stat stat = stats.get(type);
            stat.addModifier(sharedId, value, modType, duration, source);
            addedEffects.put(type, sharedId);
            
            // Track the effect by source
            activeEffects.computeIfAbsent(source, k -> new HashSet<>()).add(sharedId);
        }
        
        return addedEffects;
    }
    
    /**
     * Remove a specific effect by its UUID.
     *
     * @param effectId The UUID of the effect to remove
     * @return true if found and removed, false otherwise
     */
    public boolean removeEffect(UUID effectId) {
        boolean removed = false;
        
        // Remove from stats
        for (Stat stat : stats.values()) {
            if (stat.removeModifier(effectId)) {
                removed = true;
            }
        }
        
        // Remove from tracking
        if (removed) {
            for (Set<UUID> effects : activeEffects.values()) {
                effects.remove(effectId);
            }
            
            // Clean up empty sources
            activeEffects.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
        
        return removed;
    }
    
    /**
     * Remove all effects from a specific source.
     *
     * @param source The source to remove effects for
     * @return The number of effects removed
     */
    public int removeEffectsFromSource(String source) {
        Set<UUID> sourceEffects = activeEffects.get(source);
        if (sourceEffects == null) {
            return 0;
        }
        
        int count = 0;
        for (Stat stat : stats.values()) {
            count += stat.removeModifiersFromSource(source);
        }
        
        activeEffects.remove(source);
        
        return count;
    }
    
    /**
     * Update all active effect durations.
     * Should be called every tick.
     */
    public void updateEffects() {
        for (Stat stat : stats.values()) {
            stat.updateModifiers();
        }
        
        // Clean up expired effects from tracking
        for (Map.Entry<String, Set<UUID>> entry : activeEffects.entrySet()) {
            String source = entry.getKey();
            Set<UUID> effects = entry.getValue();
            
            effects.removeIf(uuid -> {
                boolean expired = true;
                for (Stat stat : stats.values()) {
                    if (stat.getModifiers().containsKey(uuid)) {
                        expired = false;
                        break;
                    }
                }
                return expired;
            });
        }
        
        // Clean up empty sources
        activeEffects.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Get all active effects grouped by source.
     *
     * @return Map of source to set of effect IDs
     */
    public Map<String, Set<UUID>> getActiveEffectsBySources() {
        return Collections.unmodifiableMap(activeEffects);
    }
}