package io.tavuc.skillsystem.api.events;

import io.tavuc.skillsystem.api.model.StatType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a player assigns stat points.
 */
public class PlayerAssignStatEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final StatType statType;
    private final int pointsAssigned;

    /**
     * Constructs the event.
     *
     * @param player         The player who assigned points.
     * @param statType       The stat type.
     * @param pointsAssigned The number of points assigned.
     */
    public PlayerAssignStatEvent(Player player, StatType statType, int pointsAssigned) {
        this.player = player;
        this.statType = statType;
        this.pointsAssigned = pointsAssigned;
    }

    /**
     * Returns the player who assigned the points.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the stat type that received points.
     *
     * @return The stat type.
     */
    public StatType getStatType() {
        return statType;
    }

    /**
     * Returns the amount of points assigned.
     *
     * @return The points assigned.
     */
    public int getPointsAssigned() {
        return pointsAssigned;
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
