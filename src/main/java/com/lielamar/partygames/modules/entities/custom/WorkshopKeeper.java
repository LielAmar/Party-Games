package com.lielamar.partygames.modules.entities.custom;

import com.lielamar.partygames.modules.entities.CustomEntity;
import com.lielamar.partygames.modules.entities.pathfindergoals.PathfinderGoalWrapper;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityVillager;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class WorkshopKeeper extends EntityVillager implements CustomEntity {

    private PathfinderGoalWrapper pathfinderGoalWrapper;
    private boolean isCustomEntity;

    public WorkshopKeeper(World world) {
        super(((CraftWorld)world).getHandle());
        this.isCustomEntity = true;

        this.pathfinderGoalWrapper = new PathfinderGoalWrapper(this);
        this.pathfinderGoalWrapper.setupPathfinderGoals(true, null, null);
    }

    @Override
    public void g(float sideMot, float forMot) {
        if(isCustomEntity)
            return;

        super.g(sideMot, forMot);
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
}