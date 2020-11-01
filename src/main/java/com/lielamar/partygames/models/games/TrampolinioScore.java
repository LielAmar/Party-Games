package com.lielamar.partygames.models.games;

import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.models.entities.CustomFallingBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class TrampolinioScore {

    private CustomFallingBlock cfb;
    private ArmorStand holder;
    private ArmorStand text;

    @SuppressWarnings("deprecation")
    public TrampolinioScore(Location location, ItemStack itemstack) {
        if(itemstack.getData().getData() == 14)
            location = location.clone().add(0, 10, 0);

        this.cfb = new CustomFallingBlock(location, itemstack.getType().getId(), itemstack.getData().getData());
        this.holder = (ArmorStand)location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        this.holder.setPassenger(cfb.getBukkitEntity());
        this.holder.setGravity(false);
        this.holder.setVisible(false);

        this.text = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, -1.2, 0), EntityType.ARMOR_STAND);
        this.text.setGravity(false);
        this.text.setVisible(false);
        this.text.setCustomNameVisible(true);

        if(itemstack.getType() == Material.WEB)
            this.text.setCustomName(ChatColor.GRAY + "" + ChatColor.BOLD + "BOOST");
        else if(itemstack.getType() == Material.WOOL) {
            switch(itemstack.getData().getData()) {
                case 5:
                    this.text.setCustomName(ChatColor.GREEN + "+1 Score");
                    break;
                case 4:
                    this.text.setCustomName(ChatColor.YELLOW + "+3 Score");
                    break;
                case 14:
                    this.text.setCustomName(ChatColor.RED + "+10 Score");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Checks if a player hit the score
     */
    public boolean run(CustomPlayer cp) {
        if(cp == null) return false;
        if(this.cfb == null) return false;

        if(getLocation().distance(cp.getPlayer().getLocation()) > 0.5) return false;

        destroy();

        if(cfb.getBukkitBlockId() == 30) {
            cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.FIREWORK_BLAST, 1F, 1F);
            cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.EXPLODE, 1F, 1F);
            cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.BLAZE_BREATH, 0.5F, 1F);
            cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.WITHER_SHOOT, 1F, 1F);
            cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.BAT_IDLE, 0.5F, 1F);

            cp.getPlayer().setVelocity(cp.getPlayer().getVelocity().multiply(1.5).setY(2));

        } else if(cfb.getBukkitBlockId() == 35) {
            cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.SUCCESSFUL_HIT, 1F, 1F);
            switch(cfb.getBukkitBlockData()) {
                case 5:
                    cp.addMinigameScore(1);
                    break;
                case 4:
                    cp.addMinigameScore(3);
                    break;
                case 14:
                    cp.addMinigameScore(10);
                    break;
                default:
                    return false;
            }
        }

        return true;
    }

    /**
     * Destroys the score
     */
    public void destroy() {
        this.cfb.getBukkitEntity().remove();
        this.holder.remove();
        this.text.remove();
    }


    private Location getLocation() {
        return this.cfb.getBukkitEntity().getLocation();
    }
}
