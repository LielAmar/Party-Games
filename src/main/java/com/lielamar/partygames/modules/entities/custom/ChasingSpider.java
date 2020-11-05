package com.lielamar.partygames.modules.entities.custom;

import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.partygames.modules.entities.CustomEntity;
import com.lielamar.partygames.modules.entities.pathfindergoals.PathfinderGoalMeleeAttackPlayer;
import com.lielamar.partygames.modules.entities.pathfindergoals.PathfinderGoalTargetPlayer;
import com.lielamar.partygames.modules.entities.pathfindergoals.PathfinderGoalWrapper;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntitySpider;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChasingSpider extends EntitySpider implements CustomEntity {

    private Player target;
    private PathfinderGoalWrapper pathfinderGoalWrapper;

    public ChasingSpider(World world, Player target) {
        super(((CraftWorld)world).getHandle());

        this.target = target;

        this.pathfinderGoalWrapper = new PathfinderGoalWrapper(this);
        List<Pair<PathfinderGoal, Integer>> pathfinderGoals = new ArrayList<>();
        pathfinderGoals.add(new Pair<>(new PathfinderGoalFloat(this), 1));
        pathfinderGoals.add(new Pair<>(new PathfinderGoalMeleeAttackPlayer(this, EntityHuman.class), 2));
        pathfinderGoals.add(new Pair<>(new PathfinderGoalTargetPlayer(this, 5,  10), 2));
        this.pathfinderGoalWrapper.setupPathfinderGoals(true, ((CraftPlayer)this.target).getHandle(), pathfinderGoals);
    }
}