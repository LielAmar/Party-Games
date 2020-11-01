/**
 * This code is only working server-side and should not be used.
 * Original code: https://bukkit.org/threads/spawn-a-falling-block.93254/page-2
 * Designer: https://github.com/shinyquagsire23/SmoothElevator/blob/master/src/org/zzl/minegaming/SmoothElevator/Plugin.java#L434
 */

package some.legacy.code;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;

public class CustomFallingBlock extends EntityFallingBlock {

    boolean ignoreGravity = true;
    public int data;

    private IBlockData block;
    public int ticksLived;
    public boolean dropItem = true;

    private boolean e;
    private boolean hurtEntities;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 2.0F;

    public NBTTagCompound tileEntityData;

    public CustomFallingBlock(World world) {
        super(((CraftWorld)world).getHandle());
        this.dropItem = false;
    }

    public CustomFallingBlock(World world, double x, double y, double z, IBlockData blockData) {
        super(((CraftWorld)world).getHandle(), x, y, z, blockData);
    }

    public CustomFallingBlock(World world, double x, double y, double z, IBlockData iblockdata, int data) {
        super(((CraftWorld)world).getHandle());

        this.block = iblockdata;
        this.data = data;
        this.k = true;
        this.setSize(0.98F, 0.98F);
        this.setPosition(x, y, z);
        this.motX = 0.0D;
        this.motY = 0.0D;
        this.motZ = 0.0D;
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
    }

    protected void h() {}

    public boolean ad() {
        return !this.dead;
    }

    protected boolean s_() {
        return false;
    }

    public void t_() {
        Block block = this.block.getBlock();

        if (block.getMaterial() == Material.AIR) {
            this.die();
        } else {
            this.lastX = this.locX;
            this.lastY = this.locY;
            this.lastZ = this.locZ;
            BlockPosition blockposition;

            if (this.ticksLived++ == 0) {
                blockposition = new BlockPosition(this);
                if (this.world.getType(blockposition).getBlock() == block && !CraftEventFactory.callEntityChangeBlockEvent(this, blockposition.getX(), blockposition.getY(), blockposition.getZ(), Blocks.AIR, 0).isCancelled()) {
                    if (!this.ignoreGravity) {
                        this.world.setAir(blockposition);
                    }
                } else if (!this.world.isClientSide) {
                    if (!this.ignoreGravity) {
                        this.die();
                    }
                    return;
                }
            }
            if (!this.ignoreGravity) {
                this.motY -= 0.03999999910593033D;
            }
            this.move(this.motX, this.motY, this.motZ);
            this.motX *= 0.9800000190734863D;
            if (!this.ignoreGravity) {
                this.motY *= 0.9800000190734863D;
            }
            this.motZ *= 0.9800000190734863D;
            if (!this.world.isClientSide) {
                blockposition = new BlockPosition(this);
                if (this.onGround) {
                    this.motX *= 0.699999988079071D;
                    this.motZ *= 0.699999988079071D;
                    if (!this.ignoreGravity) {
                        this.motY *= -0.5D;
                    }
                    if (this.world.getType(blockposition).getBlock() != Blocks.PISTON_EXTENSION && (!this.ignoreGravity)) {
                        this.die();
                        if (!this.e) {
                            if (this.world.a(block, blockposition, true, EnumDirection.UP, (Entity) null, (ItemStack) null) && !BlockFalling.canFall(this.world, blockposition.down()) /* mimic the false conditions of setTypeIdAndData */ && blockposition.getX() >= -30000000 && blockposition.getZ() >= -30000000 && blockposition.getX() < 30000000 && blockposition.getZ() < 30000000 && blockposition.getY() >= 0 && blockposition.getY() < 256 && this.world.getType(blockposition) != this.block) {
                                if (CraftEventFactory.callEntityChangeBlockEvent(this, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this.block.getBlock(), this.block.getBlock().toLegacyData(this.block)).isCancelled()) {
                                    return;
                                }
                                this.world.setTypeAndData(blockposition, this.block, 3);
                                // CraftBukkit end
                                if (block instanceof BlockFalling) {
                                    ((BlockFalling) block).a_(this.world, blockposition);
                                }

                                if (this.tileEntityData != null && block instanceof IContainer) {
                                    TileEntity tileentity = this.world.getTileEntity(blockposition);

                                    if (tileentity != null) {
                                        NBTTagCompound nbttagcompound = new NBTTagCompound();

                                        tileentity.b(nbttagcompound);
                                        Iterator iterator = this.tileEntityData.c().iterator();

                                        while (iterator.hasNext()) {
                                            String s = (String) iterator.next();
                                            NBTBase nbtbase = this.tileEntityData.get(s);

                                            if (!s.equals("x") && !s.equals("y") && !s.equals("z")) {
                                                nbttagcompound.set(s, nbtbase.clone());
                                            }
                                        }

                                        tileentity.a(nbttagcompound);
                                        tileentity.update();
                                    }
                                }
                            } else if (this.dropItem && this.world.getGameRules().getBoolean("doEntityDrops")) {
                                this.a(new ItemStack(block, 1, block.getDropData(this.block)), 0.0F);
                            }
                        }
                    }
                } else if (((this.ticksLived > 100 && !this.world.isClientSide && (blockposition.getY() < 1 || blockposition.getY() > 256) || this.ticksLived > 600)
                        && (!this.ignoreGravity))){
                    if (this.dropItem && this.world.getGameRules().getBoolean("doEntityDrops")) {
                        this.a(new ItemStack(block, 1, block.getDropData(this.block)), 0.0F);
                    }

                    this.die();
                }
            }

        }
    }

    @Override
    public void move(double d0, double d1, double d2) {
        super.move(d0, d1, d2);
    }

    public void e(float f, float f1) {
        Block block = this.block.getBlock();

        if (this.hurtEntities) {
            int i = MathHelper.f(f - 1.0F);

            if (i > 0) {
                ArrayList arraylist = Lists.newArrayList(this.world.getEntities(this, this.getBoundingBox()));
                boolean flag = block == Blocks.ANVIL;
                DamageSource damagesource = flag ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;
                Iterator iterator = arraylist.iterator();

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();

                    CraftEventFactory.entityDamage = this; // CraftBukkit
                    entity.damageEntity(damagesource, (float) Math.min(MathHelper.d((float) i * this.fallHurtAmount), this.fallHurtMax));
                    CraftEventFactory.entityDamage = null; // CraftBukkit
                }

                if (flag && (double) this.random.nextFloat() < 0.05000000074505806D + (double) i * 0.05D) {
                    int j = ((Integer) this.block.get(BlockAnvil.DAMAGE)).intValue();

                    ++j;
                    if (j > 2) {
                        this.e = true;
                    } else {
                        this.block = this.block.set(BlockAnvil.DAMAGE, Integer.valueOf(j));
                    }
                }
            }
        }
    }

    protected void b(NBTTagCompound nbttagcompound) {
        Block block = this.block != null ? this.block.getBlock() : Blocks.AIR;
        MinecraftKey minecraftkey = (MinecraftKey) Block.REGISTRY.c(block);

        nbttagcompound.setString("Block", minecraftkey == null ? "" : minecraftkey.toString());
        nbttagcompound.setByte("Data", (byte) block.toLegacyData(this.block));
        nbttagcompound.setByte("Time", (byte) this.ticksLived);
        nbttagcompound.setBoolean("DropItem", this.dropItem);
        nbttagcompound.setBoolean("HurtEntities", this.hurtEntities);
        nbttagcompound.setFloat("FallHurtAmount", this.fallHurtAmount);
        nbttagcompound.setInt("FallHurtMax", this.fallHurtMax);
        if (this.tileEntityData != null) {
            nbttagcompound.set("TileEntityData", this.tileEntityData);
        }

    }

    protected void a(NBTTagCompound nbttagcompound) {
        int i = nbttagcompound.getByte("Data") & 255;

        if (nbttagcompound.hasKeyOfType("Block", 8)) {
            this.block = Block.getByName(nbttagcompound.getString("Block")).fromLegacyData(i);
        } else if (nbttagcompound.hasKeyOfType("TileID", 99)) {
            this.block = Block.getById(nbttagcompound.getInt("TileID")).fromLegacyData(i);
        } else {
            this.block = Block.getById(nbttagcompound.getByte("Tile") & 255).fromLegacyData(i);
        }

        this.ticksLived = nbttagcompound.getByte("Time") & 255;
        Block block = this.block.getBlock();

        if (nbttagcompound.hasKeyOfType("HurtEntities", 99)) {
            this.hurtEntities = nbttagcompound.getBoolean("HurtEntities");
            this.fallHurtAmount = nbttagcompound.getFloat("FallHurtAmount");
            this.fallHurtMax = nbttagcompound.getInt("FallHurtMax");
        } else if (block == Blocks.ANVIL) {
            this.hurtEntities = true;
        }

        if (nbttagcompound.hasKeyOfType("DropItem", 99)) {
            this.dropItem = nbttagcompound.getBoolean("DropItem");
        }

        if (nbttagcompound.hasKeyOfType("TileEntityData", 10)) {
            this.tileEntityData = nbttagcompound.getCompound("TileEntityData");
        }

        if (block == null || block.getMaterial() == Material.AIR) {
            this.block = Blocks.SAND.getBlockData();
        }

    }

    public void a(boolean flag) {
        this.hurtEntities = flag;
    }

    public void appendEntityCrashDetails(CrashReportSystemDetails crashreportsystemdetails) {
        super.appendEntityCrashDetails(crashreportsystemdetails);
        if (this.block != null) {
            Block block = this.block.getBlock();

            crashreportsystemdetails.a("Immitating block ID", (Object) Integer.valueOf(Block.getId(block)));
            crashreportsystemdetails.a("Immitating block data", (Object) Integer.valueOf(block.toLegacyData(this.block)));
        }
    }

    public IBlockData getBlock() {
        return this.block;
    }

    public Vector getVelocity() {
        return new Vector(this.motX, this.motY, this.motZ);
    }

    public void setVelocity(Vector vel) {
        this.motX = vel.getX();
        this.motY = vel.getY();
        this.motZ = vel.getZ();
        this.velocityChanged = true;
    }

//    public CustomFallingBlock(Location location, int color) {
//        super(((CraftWorld)location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(),
//                (new BlockCloth(Material.CLOTH).fromLegacyData(color)));
//    }
//
//    public CustomFallingBlock(net.minecraft.server.v1_8_R3.World world) {
//        super(world);
//    }

    /**
     * Spawns a falling block at location
     *
     * @param location        Location to teleport the falling block to
     * @return                Created falling block
     */
    public static Entity spawnCustomEntity(Location location) {
        double x = location.getBlockX() + 0.5;
        double y = location.getBlockY() + 1.5;
        double z = location.getBlockZ() + 0.5;

//        IBlockData tempBlock = net.minecraft.server.v1_8_R2.Block.getByCombinedId(material.getId());
//        tempBlock = tempBlock.getBlock().fromLegacyData(data);

        CustomFallingBlock entity = new CustomFallingBlock(location.getWorld(), x, y, z, Block.getByCombinedId(35), 5);
        entity.ticksLived = 1; // ticksLived

        ((CraftWorld)location.getWorld()).getHandle().addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);

//        net.minecraft.server.v1_8_R3.Entity nmsEntity = (entity.getBukkitEntity()).getHandle();
//        NBTTagCompound tag = nmsEntity.getNBTTag();
//        if(tag == null) {
//            tag = new NBTTagCompound();
//        }
//        nmsEntity.c(tag);
//        tag.setInt("NoAI", 1);
//        nmsEntity.f(tag);

        return entity;
//        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
//
//        ((CraftWorld)location.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
//        return this;
    }
}