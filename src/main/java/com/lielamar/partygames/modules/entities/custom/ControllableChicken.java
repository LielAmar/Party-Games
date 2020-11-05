package com.lielamar.partygames.modules.entities.custom;

import com.lielamar.partygames.modules.entities.ControllableEntityHandler;
import com.lielamar.partygames.modules.entities.CustomEntity;
import com.lielamar.partygames.modules.entities.pathfindergoals.PathfinderGoalWrapper;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.plugin.Plugin;

public class ControllableChicken extends EntityChicken implements CustomEntity {

    private ControllableEntityHandler controllableEntityHandler;
    private PathfinderGoalWrapper pathfinderGoalWrapper;

    private boolean isCustomEntity;

    public ControllableChicken(Plugin plugin, World world) {
        super(((CraftWorld)world).getHandle());
        this.isCustomEntity = true;

        this.controllableEntityHandler = new ControllableEntityHandler(plugin, this);
        this.pathfinderGoalWrapper = new PathfinderGoalWrapper(this);

        this.pathfinderGoalWrapper.setupPathfinderGoals(true, null, null);
    }

    @Override
    public void g(float sideMot, float forMot) {
        /*
         * Entity move
         *
         * @param sideMot   Side Motion
         * @param forMot    Forward Motion
         */
        if(!isCustomEntity) {
            super.g(sideMot, forMot);
            return;
        }

        if(this.passenger == null || !(this.passenger instanceof EntityHuman)) return;
        if(!this.controllableEntityHandler.isCanMove())                        return;

        EntityHuman human = (EntityHuman) this.passenger;
        this.lastYaw = this.yaw = human.yaw;
        this.pitch = human.pitch * 0.5F;

        this.setYawPitch(this.yaw, this.pitch);

        if(Math.abs(motY) != 0.2) this.motY = 0; // Allow changes of Y level only when we send it through {@link ControllableEntitiesPacketReader [readChickenPacket] }
        super.g(sideMot, forMot);
    }

    @Override
    public EntityItem a(Item item, int amount) {
        /*
         * Entity drop item (egg)
         *
         * @param item     Item to drop
         * @param amount   Amount to drop
         * @return         Dropped Item
         */
        return null;
    }

    @Override
    public void collide(Entity entity) {
        if(!isCustomEntity)
            super.collide(entity);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if(!isCustomEntity)
            return super.damageEntity(damagesource, f);
        return false;
    }

    public ControllableEntityHandler getControllableEntityHandler() {
        return this.controllableEntityHandler;
    }
}