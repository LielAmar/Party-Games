package com.lielamar.partygames.models.entities;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ControllableEntityHandler {

    private Plugin plugin;
    private EntityInsentient entity;

    private BukkitTask constantMovementTask;
    private boolean canMove;
    private boolean constantMovement;
    private double speed;

    public ControllableEntityHandler(Plugin plugin, EntityInsentient entity) {
        this.plugin = plugin;
        this.entity = entity;

        this.constantMovementTask = null;
        this.canMove = false;
        this.constantMovement = false;
        this.speed = 1.16;
    }

    public void setCanMove(boolean canMove) { this.canMove = canMove; }
    public boolean isCanMove() {
        return this.canMove;
    }

    public void setConstantMovement(boolean constantMovement) {
        this.constantMovement = constantMovement;
        if(constantMovement)
            updateConstantMovement();
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
    public double getSpeed() {
        return this.speed;
    }


    /**
     * Attempts to start a timer that makes the entity constantly move (depending on {@param constantMovement}
     */
    public void updateConstantMovement() {
        if(constantMovementTask != null)
            constantMovementTask.cancel();

        if(constantMovement) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(!constantMovement) {
                        this.cancel();
                        return;
                    }
                    entity.g(0, 0.98f);
                }
            }.runTaskTimer(plugin, 0, 1L);
        }
    }

    /**
     * Destroy the controllable entity handler
     */
    public void destroy() {
        if(constantMovementTask != null)
            constantMovementTask.cancel();
    }
}
