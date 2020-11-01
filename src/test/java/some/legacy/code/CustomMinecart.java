package some.legacy.code;

import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityMinecartRideable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomMinecart extends EntityMinecartRideable {

    public CustomMinecart(World world) {
        super(((CraftWorld)world).getHandle());
//        j(org.bukkit.Material.WOOL.getId());

//        setDisplayBlock(new BlockCloth(Material.CLOTH).fromLegacyData(color));
    }

    public CustomMinecart(net.minecraft.server.v1_8_R3.World world) {
        super(world);
    }

    @Override
    public void move(double d0, double d1, double d2) {
        return;
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float v) {
        return false;
    }

    @Override
    public void makeSound(String s, float f, float f1) {
        return;
    }

    /**
     * Spawns a custom minecart at location
     *
     * @param location        Location to teleport the minecart to
     * @return                Created minecart
     */
    public CustomMinecart spawnCustomEntity(Location location) {
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        setInvisible(true);

        ((CraftWorld)location.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return this;
    }
}
