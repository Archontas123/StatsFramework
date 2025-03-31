package io.tavuc.skillsystem.api.model;

/**
 * Default stat types for the Skill Framework.
 */
public enum StatType {

    /**
     * Increases melee and projectile damage.
     */
    STRENGTH,

    /**
     * Reduces damage taken.
     */
    DEFENSE,

    /**
     * Increases maximum health.
     */
    VITALITY,

    /**
     * Grants a chance to deal a critical hit for bonus damage.
     */
    FEROCITY,

    /**
     * Grants a chance to hit multiple times in a single attack.
     */
    AGILITY,

    /**
     * Increases experience gained.
     */
    INTELLIGENCE
}
