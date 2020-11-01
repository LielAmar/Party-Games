package com.lielamar.partygames.models.games;

import org.bukkit.Location;
import org.bukkit.Material;

public class Canvas {

    private Material[][] canvas;
    private Location middle;

    public Canvas(Location middle) {
        this.canvas = new Material[3][3];
        this.middle = middle;
    }

    /**
     * Calculates the I relationship (I axis) with the given location & middle location
     *
     * @param location   Location to calculate
     * @return           I Relationship (-1,0,1)
     */
    public int getIRelationship(Location location) {
        if(location.getX() == this.getMiddle().getX()) {
            if(Math.abs(location.getZ() - this.getMiddle().getZ()) > 1) return -1;
            if(location.getZ() == this.getMiddle().getZ()) return 1;
            return(location.getZ() > this.getMiddle().getZ()) ? 0 : 2;
        }

        if(location.getZ() == this.getMiddle().getZ()) {
            if(Math.abs(location.getX() - this.getMiddle().getX()) > 1) return -1;
            if(location.getX() == this.getMiddle().getX()) return 1;
            return (location.getX() > this.getMiddle().getX()) ? 0 : 2;
        }

        return -1;
    }

    /**
     * Calculates the J relationship (J axis) with the given location & middle location
     *
     * @param location   Location to calculate
     * @return           J Relationship (-1,0,1)
     */
    public int getJRelationship(Location location) {
        if(location.getY() == this.getMiddle().getY()) return 1;

        if(Math.abs(location.getY() - this.getMiddle().getY()) > 1) return -1;

        return (location.getY() > this.getMiddle().getY()) ? 0 : 2;
    }

    /**
     * Compares the given canvas the this canvas
     *
     * @param canvas   Canvas to compare with
     * @return         Whether or not the two canvases are similar
     */
    public boolean isSimilar(Material[][] canvas) {
        if(this.canvas.length != canvas.length) return false;

        for(int i = 0; i < this.canvas.length; i++) {
            for(int j = 0; j < this.canvas.length; j++) {
                if(this.canvas[i][j] != canvas[i][j]) return false;
            }
        }
        return true;
    }


    public Material[][] getCanvas() { return this.canvas; }
    public Location getMiddle() { return this.middle; }
}