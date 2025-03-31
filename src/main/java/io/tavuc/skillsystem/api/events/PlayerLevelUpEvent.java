package io.tavuc.skillsystem.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a player levels up.
 */
public class PlayerLevelUpEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final int newLevel;

    /**
     * Constructs the event.
     *
     * @param player   The player who leveled up.
     * @param newLevel The player's new level.
     */
    public PlayerLevelUpEvent(Player player, int newLevel) {
        this.player = player;
        this.newLevel = newLevel;
    }

    /**
     * Returns the player who leveled up.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the new level reached.
     *
     * @return The new level.
     */
    public int getNewLevel() {
        return newLevel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the handler list.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
