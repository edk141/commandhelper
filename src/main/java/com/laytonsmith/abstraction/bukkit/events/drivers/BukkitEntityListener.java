

package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 *
 * @author Layton
 */
public class BukkitEntityListener implements Listener{

	@EventHandler(priority=EventPriority.LOWEST)
	public void onSpawn(CreatureSpawnEvent event) {
		EventUtils.TriggerListener(Driver.CREATURE_SPAWN, "creature_spawn", new BukkitEntityEvents.BukkitMCCreatureSpawnEvent(event));
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onClickEnt(PlayerInteractEntityEvent event) {
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT_ENTITY, "player_interact_entity", new BukkitEntityEvents.BukkitMCPlayerInteractEntityEvent(event));
	}
	
    @EventHandler(priority=EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        EventUtils.TriggerListener(Driver.ITEM_DROP, "item_drop", new BukkitEntityEvents.BukkitMCPlayerDropItemEvent(event));
    }
    
	@EventHandler(priority=EventPriority.LOWEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
		EventUtils.TriggerListener(Driver.ITEM_PICKUP, "item_pickup", new BukkitEntityEvents.BukkitMCPlayerPickupItemEvent(event));
	}
	
    @EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player){
            EventUtils.TriggerListener(Driver.PLAYER_DEATH, "player_death", new BukkitPlayerEvents.BukkitMCPlayerDeathEvent(event));
        }
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onTargetLiving(EntityTargetEvent event) {
        EventUtils.TriggerListener(Driver.TARGET_ENTITY, "target_player", new BukkitEntityEvents.BukkitMCTargetEvent(event));
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
    	if (event instanceof EntityDamageByEntityEvent) {
    		EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", new BukkitEntityEvents.BukkitMCEntityDamageByEntityEvent((EntityDamageByEntityEvent) event));
    	} else {
    		EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", new BukkitEntityEvents.BukkitMCEntityDamageEvent(event));
    	}
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDamagePlayer(EntityDamageByEntityEvent event) {
        EventUtils.TriggerListener(Driver.ENTITY_DAMAGE_PLAYER, "entity_damage_player", new BukkitEntityEvents.BukkitMCEntityDamageByEntityEvent(event));
    }
}
