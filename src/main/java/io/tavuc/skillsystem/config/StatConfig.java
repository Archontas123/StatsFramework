package io.tavuc.skillsystem.config;

import io.tavuc.skillsystem.api.model.StatType;
import org.bukkit.Material;

/**
 * Configuration for a specific stat type.
 */
public class StatConfig {
    private final StatType type;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final double baseScale;
    private final boolean hidden;
    
    /**
     * Creates a new stat configuration with default values.
     *
     * @param type The stat type
     */
    public StatConfig(StatType type) {
        this.type = type;
        this.displayName = type.name();
        this.description = "";
        this.icon = Material.BOOK;
        this.baseScale = 1.0;
        this.hidden = false;
    }
    
    /**
     * Creates a new stat configuration.
     *
     * @param type        The stat type
     * @param displayName The display name
     * @param description The description
     * @param icon        The icon material
     * @param baseScale   The base scale
     * @param hidden      Whether the stat is hidden
     */
    public StatConfig(StatType type, String displayName, String description, 
                     Material icon, double baseScale, boolean hidden) {
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.baseScale = baseScale;
        this.hidden = hidden;
    }
    
    /**
     * Get the stat type.
     *
     * @return The stat type
     */
    public StatType getType() {
        return type;
    }
    
    /**
     * Get the display name.
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the description.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the icon material.
     *
     * @return The icon material
     */
    public Material getIcon() {
        return icon;
    }
    
    /**
     * Get the base scale.
     *
     * @return The base scale
     */
    public double getBaseScale() {
        return baseScale;
    }
    
    /**
     * Check if the stat is hidden.
     *
     * @return true if hidden, false otherwise
     */
    public boolean isHidden() {
        return hidden;
    }
}