package com.lielamar.partygames.modules.entities.custom;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.EntityFallingBlock;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomFallingBlock extends EntityFallingBlock {

    private int blockId;
    private int blockData;

    public CustomFallingBlock(Location location, int blockId, int blockData) {
        super(((CraftWorld)location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(),
                getIBlockData(blockId, blockData));

        ((CraftWorld)location.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        this.ticksLived = 1;

        this.blockId = blockId;
        this.blockData = blockData;
    }

    /**
     * Calls the #t_ method from the super class (EntityFallingBlock) in order to update the falling block's movement.
     * However, also sets the ticksLived value to 1 to keep it alive (falling blocks are being removed above 600 ticks).
     */
    public void t_() {
        super.t_();
        this.ticksLived = 1;
    }

    /**
     * Calculates the sum of blockId and the binary value of blockData after shiting 12 digits to the left.
     * Creates an IBlockData object with this sum.
     *
     * @param blockId     Id of the block
     * @param blockData   Data of the block (aka color)
     * @return            IBlockData object created
     */
    private static IBlockData getIBlockData(int blockId, int blockData) {
        int combined = blockId + (blockData << 12);

        return Block.getByCombinedId(combined);
    }


    public int getBukkitBlockId() {
        return this.blockId;
    }
    public int getBukkitBlockData() {
        return this.blockData;
    }
}
