package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.modules.objects.RPG;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RPG16 extends Minigame implements Listener {

    private static int rpg_cooldown = 3;
    private static int doublejump_cooldown = 3;
    private static ItemStack item = SpigotUtils.getItem(Material.IRON_BARDING, 1, ChatColor.YELLOW + "RPG-16");
    private static EntityType rpg_type = EntityType.PIG;

    private Map<UUID, Long> doubleJumpCooldown;
    private Map<UUID, Long> rpgCooldown;

    public RPG16(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.rpg_cooldown")) rpg_cooldown = config.getInt("parameters.rpg_cooldown");
        if(config.contains("parameters.doublejump_cooldown")) doublejump_cooldown = config.getInt("parameters.doublejump_cooldown");
        if(config.contains("parameters.item")) item = SpigotUtils.getItem(Material.valueOf(config.getString("parameters.item")), 1, ChatColor.YELLOW + "RPG-16");
        if(config.contains("parameters.rpg_type")) rpg_type = EntityType.valueOf(config.getString("parameters.rpg_type"));

        this.doubleJumpCooldown = new HashMap<>();
        this.rpgCooldown = new HashMap<>();

        try {
            super.validateVariables(
                    new IntValidation(rpg_cooldown, "[RPG-16] RPG Cooldown must be greater than/equals to 0", 0),
                    new IntValidation(doublejump_cooldown, "[RPG-16] Doublejump Cooldown must be greater than/equals to 0", 0));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            cp.getPlayer().getInventory().addItem(item);
            cp.getPlayer().setMaxHealth(6);
        }
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof RPG16)) return;

        e.setCancelled(true);

        if(!(e.getEntity() instanceof Player)) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        e.setCancelled(false);
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof RPG16)) return;

        if(!(e.getEntity() instanceof Player)) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onPigShoot(PlayerInteractEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof RPG16)) return;

        e.setCancelled(true);

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(!e.getPlayer().getItemInHand().equals(item)) return;

        CustomPlayer cp = super.getGame().getPlayers()[playerIndex];

        if(this.rpgCooldown.containsKey(cp.getPlayer().getUniqueId())) {
            if((System.currentTimeMillis() - this.rpgCooldown.get(cp.getPlayer().getUniqueId()))/1000 < rpg_cooldown) return;
        }

        new RPG(super.getGame(), cp.getPlayer(), cp.getPlayer().getLocation().add(0,0.5,0), rpg_type);
        this.rpgCooldown.put(cp.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onDropSpawn(EntitySpawnEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof RPG16)) return;

        if(e.getEntity().getType() == EntityType.DROPPED_ITEM)
            e.setCancelled(true);
    }

    @EventHandler
    public void onDoubleJump(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof RPG16)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        Player p = e.getPlayer();

        if(this.doubleJumpCooldown.containsKey(p.getUniqueId())) {
            if((System.currentTimeMillis() - this.doubleJumpCooldown.get(p.getUniqueId())) / 1000 < doublejump_cooldown) return;
        }

        if(p.getGameMode() != GameMode.CREATIVE && super.getGameState() == GameState.IN_GAME
                && p.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR && !p.isFlying()) {
            p.setAllowFlight(true);
            this.doubleJumpCooldown.put(p.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onFlight(PlayerToggleFlightEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof RPG16)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        Player p = e.getPlayer();

        if(p.getGameMode() == GameMode.CREATIVE) return;

        e.setCancelled(true);
        p.setAllowFlight(false);
        p.setFlying(false);
        p.setVelocity(p.getLocation().getDirection().multiply(1.5).setY(1));
        p.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1F, 1F);
        p.playSound(p.getLocation(), Sound.EXPLODE, 1F, 1F);
        p.playSound(p.getLocation(), Sound.BLAZE_BREATH, 0.5F, 1F);
        p.playSound(p.getLocation(), Sound.WITHER_SHOOT, 1F, 1F);
        p.playSound(p.getLocation(), Sound.BAT_IDLE, 0.5F, 1F);
    }
}