package com.lielamar.partygames.models.entities.pathfindergoals;

import com.lielamar.lielsutils.modules.Pair;
import com.packetmanager.lielamar.PacketManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;

public class PathfinderGoalWrapper {

    private EntityInsentient entity;
    private PathfinderGoalSelector goalSelector;
    private PathfinderGoalSelector targetSelector;

    public PathfinderGoalWrapper(EntityInsentient entity) {
        this.entity = entity;
        this.goalSelector = entity.goalSelector;
        this.targetSelector = entity.targetSelector;
    }

    public void setupPathfinderGoals(boolean removeExistingPathfinderGoals, EntityLiving target, List<Pair<PathfinderGoal, Integer>> pathfinderGoalPriorities) {
        if(removeExistingPathfinderGoals) {
            this.removePathfinderGoals();
        }

        if(target != null) {
            this.entity.setGoalTarget(target, EntityTargetEvent.TargetReason.CUSTOM, true);
        }

        if(pathfinderGoalPriorities != null) {
            for(Pair<PathfinderGoal, Integer> pathfinderGoal : pathfinderGoalPriorities) {
                this.goalSelector.a(pathfinderGoal.getValue(), pathfinderGoal.getKey());
            }
        }
    }

    private void removePathfinderGoals() {
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, targetSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, targetSelector)).clear();
    }
}
