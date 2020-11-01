package com.lielamar.partygames.models.entities.pathfindergoals;

import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.PathfinderGoal;

public class PathfinderGoalTargetPlayer extends PathfinderGoal {

    private EntityCreature entity;
    private EntityLiving target;

    private double speed;
    private float distance;

    private double x, y, z;

    public PathfinderGoalTargetPlayer(EntityCreature entity, double speed, float distance) {
        this.entity = entity;
        this.speed = speed;
        this.distance = distance;
        this.a(1);
    }

    public boolean a() {
        this.target = this.entity.getGoalTarget();
        if (this.target == null || !this.target.isAlive())
            return false;

        if(this.target.h(this.entity) > (double) this.distance * this.distance) {
            this.entity.setPosition(this.target.locX, this.target.locY, this.target.locZ);
            return true;
        }

        this.x = this.target.locX;
        this.y = this.target.locY;
        this.z = this.target.locZ;
        return true;
    }

    public void c() {
        this.entity.getNavigation().a(this.x, this.y, this.z, this.speed);
    }

    public boolean b() {
        return !this.entity.getNavigation().m();
    }
}