package io.tavuc.skillsystem.test.manager;

import io.tavuc.skillsystem.api.model.PlayerStats;
import io.tavuc.skillsystem.api.model.StatType;
import io.tavuc.skillsystem.config.ConfigManager;
import io.tavuc.skillsystem.config.FormulaConfig;
import io.tavuc.skillsystem.config.FormulaType;
import io.tavuc.skillsystem.manager.StatEffectCalculator;
import io.tavuc.skillsystem.manager.StatManager;
import io.tavuc.skillsystem.test.UnitTest;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StatEffectCalculatorTest extends UnitTest {
    
    @Mock
    private Plugin plugin;
    
    @Mock
    private StatManager statManager;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private Player player;
    
    @Mock
    private LivingEntity target;
    
    @Mock
    private Server server;
    
    @Mock
    private PluginManager pluginManager;
    
    @Mock
    private BukkitScheduler scheduler;
    
    @Mock
    private PlayerStats playerStats;
    
    @Mock
    private AttributeInstance maxHealthAttribute;
    
    @Mock
    private AttributeInstance movementSpeedAttribute;
    
    private StatEffectCalculator effectCalculator;
    private UUID playerId = UUID.randomUUID();
    
    @BeforeEach
    public void setUp() {
        when(plugin.getLogger()).thenReturn(Logger.getLogger("Test"));
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getScheduler()).thenReturn(scheduler);
        
        when(player.getUniqueId()).thenReturn(playerId);
        when(statManager.getPlayerStats(playerId)).thenReturn(playerStats);
        
        // Setup attribute mocks
        when(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).thenReturn(maxHealthAttribute);
        when(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).thenReturn(movementSpeedAttribute);
        
        // Setup config mocks
        when(configManager.getBoolean("debug.stats", false)).thenReturn(false);
        when(configManager.getBoolean("messages.show-crit-hit", true)).thenReturn(false);
        when(configManager.getBoolean("messages.show-multi-hit", true)).thenReturn(false);
        
        effectCalculator = new StatEffectCalculator(plugin, statManager, configManager);
    }
    
    @Test
    public void testUpdatePlayerAttributes() {
        // Setup vitality formula
        FormulaConfig healthFormula = new FormulaConfig("vitality_health", FormulaType.LINEAR, 20.0, 0.5, 0.0);
        when(configManager.getFormulaConfig("vitality_health")).thenReturn(healthFormula);
        
        // Setup agility formula
        FormulaConfig speedFormula = new FormulaConfig("agility_speed", FormulaType.LINEAR, 1.0, 0.01, 0.0);
        when(configManager.getFormulaConfig("agility_speed")).thenReturn(speedFormula);
        
        // Mock stat values
        mockStatValue(StatType.VITALITY, 20);
        mockStatValue(StatType.AGILITY, 10);
        
        // Test the method that updates attributes
        effectCalculator.updatePlayerAttributes(player, playerStats);
        
        // Verify max health was set
        verify(maxHealthAttribute).setBaseValue(30.0); // 20 base + (20 vitality * 0.5)
        
        // Verify movement speed was set
        verify(movementSpeedAttribute).setBaseValue(0.21); // 0.2 default * (1 + 10 agility * 0.01)
    }
    
    @Test
    public void testStrengthIncreaseDamage() {
        // Setup damage formula
        FormulaConfig damageFormula = new FormulaConfig("strength_damage", FormulaType.LINEAR, 1.0, 0.01, 0.0);
        when(configManager.getFormulaConfig("strength_damage")).thenReturn(damageFormula);
        
        // Mock stat values
        mockStatValue(StatType.STRENGTH, 50);
        
        // Create and process a damage event
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(player);
        when(event.getEntity()).thenReturn(target);
        when(event.getDamage()).thenReturn(10.0);
        
        effectCalculator.onEntityDamage(event);
        
        // Verify damage was increased
        verify(event).setDamage(15.0); // 10 base * (1 + 50 strength * 0.01)
    }
    
    @Test
    public void testDefenseReducesDamage() {
        // Setup defense formula
        FormulaConfig defenseFormula = new FormulaConfig("defense_reduction", FormulaType.LINEAR, 0.0, 0.5, 0.0);
        when(configManager.getFormulaConfig("defense_reduction")).thenReturn(defenseFormula);
        
        // Mock stat values
        mockStatValue(StatType.DEFENSE, 40);
        
        // Create and process a damage event
        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getDamage()).thenReturn(100.0);
        
        effectCalculator.onEntityDamageTaken(event);
        
        // Verify damage was reduced
        verify(event).setDamage(80.0); // 100 base * (1 - 40 defense * 0.5 / 100)
    }
    
    @Test
    public void testVitalityIncreasesHealing() {
        // Setup healing formula
        FormulaConfig regenFormula = new FormulaConfig("vitality_regen", FormulaType.LINEAR, 1.0, 0.01, 0.0);
        when(configManager.getFormulaConfig("vitality_regen")).thenReturn(regenFormula);
        
        // Mock stat values
        mockStatValue(StatType.VITALITY, 50);
        
        // Create and process a healing event
        EntityRegainHealthEvent event = mock(EntityRegainHealthEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getAmount()).thenReturn(2.0);
        when(event.getRegainReason()).thenReturn(EntityRegainHealthEvent.RegainReason.SATIATED);
        
        effectCalculator.onEntityHeal(event);
        
        // Verify healing was increased
        verify(event).setAmount(3.0); // 2 base * (1 + 50 vitality * 0.01)
    }
    
    @Test
    public void testCleanup() {
        effectCalculator.cleanup();
        
        // Expect that tasks were cancelled
        verify(scheduler).cancelTask(anyInt());
    }
    
    private void mockStatValue(StatType type, int value) {
        io.tavuc.skillsystem.api.model.Stat stat = mock(io.tavuc.skillsystem.api.model.Stat.class);
        when(stat.getValue()).thenReturn(value);
        when(playerStats.getStat(type)).thenReturn(stat);
    }
}