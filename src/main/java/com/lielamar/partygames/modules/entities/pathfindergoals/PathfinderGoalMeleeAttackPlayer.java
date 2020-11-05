package com.lielamar.partygames.modules.entities.pathfindergoals;

import com.lielamar.partygames.Main;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.PathfinderGoalMeleeAttack;

public class PathfinderGoalMeleeAttackPlayer extends PathfinderGoalMeleeAttack {

    public PathfinderGoalMeleeAttackPlayer(EntityCreature entity, Class<? extends Entity> oclass) {
        super(entity, oclass, 1.0D, true);
    }

    public boolean b() {
        float f = this.b.c(1.0F);
        if (f >= 0.5F && this.b.bc().nextInt(100) == 0) {
            return false;
        } else {
            /*
             * return true = Attack player and avoid calling other pathfinder goals.
             * return false = Move on to the next pathfinder goal.
             * We want the entity to have a chance to attack the player only if it's close to it (< 2 blocks)
             * If the entity is within 2 blocks of the attacker, we want to have a 1/3 chance it will attack.
            */
            if(super.b.getGoalTarget().h(super.b) > 3) return false;
            return Main.rnd.nextInt(3) == 0;
        }
    }

    protected double a(EntityLiving entityliving) {
        return (2.0F + entityliving.width);
    }
}
