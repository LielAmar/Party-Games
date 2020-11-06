package com.lielamar.partygames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class PlayerEventsHandler implements Listener {

    private String[] permissions;
    public PlayerEventsHandler(String... permissions) {
        this.permissions = permissions;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDropItem(PlayerDropItemEvent event) {
        event.setCancelled(!hasPermissions(event.getPlayer()));
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPickupItem(PlayerDropItemEvent event) {
        event.setCancelled(!hasPermissions(event.getPlayer()));
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(!hasPermissions(event.getPlayer()));
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(!hasPermissions(event.getPlayer()));
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDropItem(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDropItem(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onHungerChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        if(event.getClickedInventory() == null && event.getInventory() == null && event.getCurrentItem() == null) {
            if(event.getClickedInventory() == player.getInventory()) {
                event.setCancelled(!hasPermissions(player));
            }
        }
    }

    /**
     * Whether a player has permissions to bypass event cancellation
     *
     * @param eventPlayer   Player to check
     * @return              Whether the player has permissions
     */
    private boolean hasPermissions(Player eventPlayer) {
        for(String permission : permissions) {
            if(eventPlayer.hasPermission(permission))
                return true;
        }
        return false;
    }
}