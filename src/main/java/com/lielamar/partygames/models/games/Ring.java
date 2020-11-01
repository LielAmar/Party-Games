package com.lielamar.partygames.models.games;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class Ring {

    private Location middle;
    private Block[] blocks;

    public Ring(Location middle, Character axis) {
        this.middle = middle;
        this.blocks = new Block[16];

        if(axis == 'x') {
            this.blocks[0] = this.middle.clone().add(0, 3, 1).getBlock();
            this.blocks[1] = this.middle.clone().add(0, 3, 0).getBlock();
            this.blocks[2] = this.middle.clone().add(0, 3, -1).getBlock();

            this.blocks[3] = this.middle.clone().add(0, 1, -3).getBlock();
            this.blocks[4] = this.middle.clone().add(0, 0, -3).getBlock();
            this.blocks[5] = this.middle.clone().add(0, -1, -3).getBlock();

            this.blocks[6] = this.middle.clone().add(0, -3, 1).getBlock();
            this.blocks[7] = this.middle.clone().add(0, -3, 0).getBlock();
            this.blocks[8] = this.middle.clone().add(0, -3, -1).getBlock();

            this.blocks[9] = this.middle.clone().add(0, 1, 3).getBlock();
            this.blocks[10] = this.middle.clone().add(0, 0, 3).getBlock();
            this.blocks[11] = this.middle.clone().add(0, -1, 3).getBlock();

            this.blocks[12] = this.middle.clone().add(0, 2, 2).getBlock();
            this.blocks[13] = this.middle.clone().add(0, 2, -2).getBlock();
            this.blocks[14] = this.middle.clone().add(0, -2, 2).getBlock();
            this.blocks[15] = this.middle.clone().add(0, -2, -2).getBlock();
        } else if(axis == 'z') {
            this.blocks[0] = this.middle.clone().add(1, 3, 0).getBlock();
            this.blocks[1] = this.middle.clone().add(0, 3, 0).getBlock();
            this.blocks[2] = this.middle.clone().add(-1, 3, 0).getBlock();

            this.blocks[3] = this.middle.clone().add(-3, 1, 0).getBlock();
            this.blocks[4] = this.middle.clone().add(-3, 0, 0).getBlock();
            this.blocks[5] = this.middle.clone().add(-3, -1, 0).getBlock();

            this.blocks[6] = this.middle.clone().add(1, -3, 0).getBlock();
            this.blocks[7] = this.middle.clone().add(0, -3, 0).getBlock();
            this.blocks[8] = this.middle.clone().add(-1, -3, 0).getBlock();

            this.blocks[9] = this.middle.clone().add(3, 1, 0).getBlock();
            this.blocks[10] = this.middle.clone().add(3, 0, 0).getBlock();
            this.blocks[11] = this.middle.clone().add(3, -1, 0).getBlock();

            this.blocks[12] = this.middle.clone().add(2, 2, 0).getBlock();
            this.blocks[13] = this.middle.clone().add(-2, 2, 0).getBlock();
            this.blocks[14] = this.middle.clone().add(2, -2, 0).getBlock();
            this.blocks[15] = this.middle.clone().add(-2, -2, 0).getBlock();
        }
    }

    public Location getMiddle() {
        return this.middle;
    }

    public Block[] getBlocks() {
        return this.blocks;
    }
}
