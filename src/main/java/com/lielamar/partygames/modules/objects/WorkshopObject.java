package com.lielamar.partygames.modules.objects;

import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.partygames.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WorkshopObject {

    public static Material[] materials = { Material.STONE, Material.WEB, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.REDSTONE_ORE, Material.MELON_BLOCK, Material.LOG };

    private Location recipeStart, recipeEnd, recipeProduct;
    private ItemFrame[][] frames;
    private ItemFrame product;
    private Location npcLocation;
    private List<Pair<Location, Location>> materialLocations;

    private List<Integer> materialYs;

    public WorkshopObject(Location recipeStart, Location recipeEnd, Location recipeProduct, Location npcLocation, List<Pair<Location, Location>> materialLocations) {
        this.recipeStart = recipeStart;
        this.recipeEnd = recipeEnd;
        this.recipeProduct = recipeProduct;
        this.frames = new ItemFrame[3][3];

        this.npcLocation = npcLocation;

        this.materialLocations = materialLocations;

        this.materialYs = new ArrayList<>();
        for(Pair<Location, Location> pair : this.materialLocations) {
            materialYs.add(pair.getKey().getBlockY());
            materialYs.add(pair.getValue().getBlockY());
        }
    }

    /**
     * Loads the workshop
     *
     * @param recipe   Recipe to load
     */
    @SuppressWarnings("deprecation")
    public void loadWorkshop(Recipe recipe) {
        char axis = 'x';
        if(recipeStart.getBlockX() == recipeEnd.getBlockX()) axis = 'z';

        if(axis == 'x') {
            for(int x = 0; Math.abs(x) <= Math.abs(recipeStart.getX()-recipeEnd.getX()); x+= (recipeStart.getX() > recipeEnd.getX()) ? -1 : 1) {
                for(int y = 0; Math.abs(y) <= Math.abs(recipeStart.getY()-recipeEnd.getY()); y+= (recipeStart.getY() > recipeEnd.getY()) ? -1 : 1) {
                    if(this.frames[Math.abs(x)][Math.abs(y)] != null) {
                        if(recipe.getRecipeType().getIngredients()[Math.abs(x)][Math.abs(y)] != null)
                            this.frames[Math.abs(x)][Math.abs(y)].setItem(new ItemStack(recipe.getRecipeType().getIngredients()[Math.abs(x)][Math.abs(y)]));
                        else
                            this.frames[Math.abs(x)][Math.abs(y)].setItem(new ItemStack(Material.AIR));
                    } else {
                        Block b = recipeStart.clone().add(x, y, 0).getBlock();
                        ItemFrame itemframe = (ItemFrame) b.getLocation().getWorld().spawnEntity(b.getLocation(), EntityType.ITEM_FRAME);

                        if(recipe.getRecipeType().getIngredients()[Math.abs(x)][Math.abs(y)] != null)
                            itemframe.setItem(new ItemStack(recipe.getRecipeType().getIngredients()[Math.abs(x)][Math.abs(y)]));

                        if(this.frames[Math.abs(x)][Math.abs(y)] == null)
                            this.frames[Math.abs(x)][Math.abs(y)] = itemframe;
                    }
                }
            }
        } else {
            for(int z = 0; Math.abs(z) <= Math.abs(recipeStart.getZ()-recipeEnd.getZ()); z+= (recipeStart.getZ() > recipeEnd.getZ()) ? -1 : 1) {
                for(int y = 0; Math.abs(y) <= Math.abs(recipeStart.getY()-recipeEnd.getY()); y+= (recipeStart.getY() > recipeEnd.getY()) ? -1 : 1) {
                    if(this.frames[Math.abs(z)][Math.abs(y)] != null) {
                        if(recipe.getRecipeType().getIngredients()[Math.abs(z)][Math.abs(y)] != null)
                            this.frames[Math.abs(z)][Math.abs(y)].setItem(new ItemStack(recipe.getRecipeType().getIngredients()[Math.abs(z)][Math.abs(y)]));
                        else
                            this.frames[Math.abs(z)][Math.abs(y)].setItem(new ItemStack(Material.AIR));
                    } else {
                        Block b = recipeStart.clone().add(0, y, z).getBlock();
                        ItemFrame itemframe = (ItemFrame) b.getLocation().getWorld().spawnEntity(b.getLocation(), EntityType.ITEM_FRAME);

                        if(recipe.getRecipeType().getIngredients()[Math.abs(z)][Math.abs(y)] != null)
                            itemframe.setItem(new ItemStack(recipe.getRecipeType().getIngredients()[Math.abs(z)][Math.abs(y)]));

                        if(this.frames[Math.abs(z)][Math.abs(y)] == null)
                            this.frames[Math.abs(z)][Math.abs(y)] = itemframe;
                    }
                }
            }
        }

        if(this.product != null)
            product.setItem(new ItemStack(recipe.getRecipeType().getProduct()));
        else {
            Block b = recipeProduct.getBlock();
            ItemFrame itemframe = (ItemFrame) b.getWorld().spawnEntity(b.getLocation(), EntityType.ITEM_FRAME);
            itemframe.setItem(new ItemStack(recipe.getRecipeType().getProduct()));
            this.product = itemframe;
        }

        for(Pair<Location, Location> materialLocation : this.materialLocations) {
            for(int x = 0; Math.abs(x) <= Math.abs(materialLocation.getKey().getX()-materialLocation.getValue().getX()); x+= (materialLocation.getKey().getX() > materialLocation.getValue().getX()) ? -1 : 1) {
                for(int y = 0; Math.abs(y) <= Math.abs(materialLocation.getKey().getY()-materialLocation.getValue().getY()); y+= (materialLocation.getKey().getY() > materialLocation.getValue().getY()) ? -1 : 1) {
                    for(int z = 0; Math.abs(z) <= Math.abs(materialLocation.getKey().getZ()-materialLocation.getValue().getZ()); z+= (materialLocation.getKey().getZ() > materialLocation.getValue().getZ()) ? -1 : 1) {
                        Material randomMat = getRandomMaterial();

                        materialLocation.getKey().clone().add(x, y, z).getBlock().setType(randomMat);
                        if(randomMat == Material.LOG)
                            materialLocation.getKey().clone().add(x, y, z).getBlock().setData((byte)1);
                    }
                }
            }
        }
    }

    /**
     * Returns a random material from the material array
     *
     * @return   The random material picked
     */
    public Material getRandomMaterial() {
        return materials[Main.rnd.nextInt(materials.length)];
    }

    /**
     * Whether or not a material is in the array of materials
     *
     * @param block   Block to check
     * @return        If the material is indeed in the array
     */
    @SuppressWarnings("deprecation")
    public boolean containsMaterial(Block block) {
        if(!materialYs.contains(block.getY())) return false;

        for(Material material : materials) {
            if(material == block.getType()) {
                if(block.getType() == Material.LOG)
                    if(block.getData() != (byte) 1) continue;
                return true;
            }
        }
        return false;
    }

    /**
     * Destroys the workshop object
     */
    public void destroy() {
        this.product.remove();
        for(ItemFrame[] frame : this.frames) {
            for(int j = 0; j < this.frames.length; j++)
                frame[j].remove();
        }
    }

    /**
     * Returns the matching material of mat
     *
     * @param mat   Material to get matching material of
     * @return      The matching material
     */
    public static ItemStack getMatchingMaterial(Material mat) {
        if(mat == Material.DIAMOND_ORE) return new ItemStack(Material.DIAMOND);
        if(mat == Material.REDSTONE_ORE) return new ItemStack(Material.REDSTONE);
        if(mat == Material.LOG) return new ItemStack(Material.LOG, 1, (byte)1);
        if(mat == Material.WEB) return new ItemStack(Material.STRING);
        return new ItemStack(mat);
    }


    public Location getStart() {
        return this.recipeStart;
    }

    public Location getEnd() {
        return this.recipeEnd;
    }

    public Location getNPCLocation() { return this.npcLocation; }
}