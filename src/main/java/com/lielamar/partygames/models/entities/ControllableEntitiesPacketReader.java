package com.lielamar.partygames.models.entities;

import com.lielamar.partygames.game.games.ChickenRings;
import com.lielamar.partygames.game.games.LawnMoower;
import com.lielamar.partygames.game.games.PigJousting;
import com.lielamar.partygames.game.games.SuperSheep;
import com.packetmanager.lielamar.PacketManager;
import com.packetmanager.lielamar.ParticleEffect;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.PacketPlayInSteerVehicle;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ControllableEntitiesPacketReader {

    private Channel channel;

    public static Map<UUID, Channel> channels = new HashMap<>();

    /**
     * Injects a player to the packet reader
     *
     * @param player   Player to inject
     */
    public void inject(Player player) {
        CraftPlayer cp = (CraftPlayer) player;
        this.channel = cp.getHandle().playerConnection.networkManager.channel;
        channels.put(player.getUniqueId(), this.channel);

        if(channel.pipeline().get("PacketInjector") != null)
            return;

        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<PacketPlayInSteerVehicle>() {

            @Override
            public void decode(ChannelHandlerContext channel, PacketPlayInSteerVehicle packet, List<Object> args) {
                if(ChickenRings.chickens.containsKey(player.getUniqueId()) && ChickenRings.chickens.get(player.getUniqueId()) != null)
                    readChickenPacket(packet, args, ChickenRings.chickens.get(player.getUniqueId()));
                else if(LawnMoower.cows.containsKey(player.getUniqueId()) && LawnMoower.cows.get(player.getUniqueId()) != null)
                    readCowPacket(packet, args, LawnMoower.cows.get(player.getUniqueId()));
                else if(PigJousting.pigs.containsKey(player.getUniqueId()) && PigJousting.pigs.get(player.getUniqueId()) != null)
                    readPigPacket(packet, args, PigJousting.pigs.get(player.getUniqueId()));
                else if(SuperSheep.sheeps.containsKey(player.getUniqueId()) && SuperSheep.sheeps.get(player.getUniqueId()) != null)
                    readSheepPacket(packet, args, SuperSheep.sheeps.get(player.getUniqueId()), player);
                else
                    args.add(packet);
            }
        });
    }

    /**
     * Ejects a player from the packet reader
     *
     * @param player   Player to eject
     */
    public void eject(Player player) {
        if(player == null) return;

        channel = channels.get(player.getUniqueId());
        if(channel != null && channel.pipeline().get("PacketInjector") != null)
            channel.pipeline().remove("PacketInjector");
    }

    /**
     * Reads the PacketPlayInSteerVehicle packet, specifically for the chicken entity {@link com.lielamar.partygames.models.entities.ControllableChicken}
     *
     * @param packet    Packet sent
     * @param args      Packet arguments
     * @param chicken   Rideable chicken
     */
    public void readChickenPacket(PacketPlayInSteerVehicle packet, List<Object> args, ControllableChicken chicken) {
        if(!packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInSteerVehicle")) return;

        if(!packet.d()) args.add(packet); // If we're not pressing shift, process the packet because we don't need to worry about getting out
        else chicken.motY=-0.2D;          // If we are pressing shift, set the Y motion to -0.2D

        if(packet.c()) chicken.motY=0.2D; // If we are pressing space, set the Y motion to +0.2D

        float forMot = packet.b();
        if(forMot > 0.98) forMot = 0.98F;
        if(forMot < 0) forMot = 0;

        float sideMot = packet.a();
        sideMot*=0.85F;

        chicken.g(sideMot, forMot);

        if(forMot != 0) {
            chicken.motX *= Math.sqrt(chicken.getSpeed());
            chicken.motZ *= Math.sqrt(chicken.getSpeed());
        }
    }

    /**
     * Reads the PacketPlayInSteerVehicle packet, specifically for the cow entity {@link com.lielamar.partygames.models.entities.ControllableCow}
     *
     * @param packet   Packet sent
     * @param args     Packet arguments
     * @param cow      Rideable cow
     */
    public void readCowPacket(PacketPlayInSteerVehicle packet, List<Object> args, ControllableCow cow) {
        if(!packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInSteerVehicle")) return;

        if(!packet.d()) args.add(packet); // If we're not pressing shift, process the packet because we don't need to worry about getting out

        float forMot = packet.b();
        if(forMot > 0.98) forMot = 0.98F;
        if(forMot < 0)
            forMot = forMot*0.25F;

        float sideMot = packet.a();
        sideMot*=0.35F;

        cow.g(sideMot, forMot);

        if(forMot != 0) {
            cow.motX *= Math.sqrt(cow.getSpeed());
            cow.motZ *= Math.sqrt(cow.getSpeed());
        }
    }

    /**
     * Reads the PacketPlayInSteerVehicle packet, specifically for the pig entity {@link com.lielamar.partygames.models.entities.ControllablePig}
     *
     * @param packet   Packet sent
     * @param args     Packet arguments
     * @param pig      Rideable pig
     */
    public void readPigPacket(PacketPlayInSteerVehicle packet, List<Object> args, ControllablePig pig) {
        if(!packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInSteerVehicle")) return;

        if(!packet.d()) args.add(packet); // If we're not pressing shift, process the packet because we don't need to worry about getting out

        float forMot = packet.b();
        if(forMot > 0.98) forMot = 0.98F;
        if(forMot < 0)
            forMot = forMot*0.25F;

        float sideMot = packet.a();
        sideMot*=0.35F;

        pig.g(sideMot, forMot);

        if(forMot != 0) {
            pig.motX *= Math.sqrt(pig.getSpeed());
            pig.motZ *= Math.sqrt(pig.getSpeed());
        }
    }

    /**
     * Reads the PacketPlayInSteerVehicle packet, specifically for the sheep entity {@link com.lielamar.partygames.models.entities.ControllableSheep}
     *
     * @param packet   Packet sent
     * @param args     Packet arguments
     * @param sheep    Rideable sheep
     * @param player   Player riding the sheep
     */
    public void readSheepPacket(PacketPlayInSteerVehicle packet, List<Object> args, ControllableSheep sheep, Player player) {
        if(!packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInSteerVehicle")) return;

        if(packet.b() <= 0) return;

        if(!packet.d()) args.add(packet); // If we're not pressing shift, process the packet because we don't need to worry about getting out

        float forMot = packet.b();
        if(forMot > 0.98) forMot = 0.98F;
        if(forMot < 0)
            forMot = forMot*0.25F;

        float sideMot = packet.a();
        sideMot*=0.35F;

        if(player.getExp() >= 0.05F) {
            PacketManager.sendParticle(player,
                    ParticleEffect.REDSTONE, sheep.getBukkitEntity().getLocation().clone().add(0, 1, 0), 255, 0, 0, 1, 0);
            PacketManager.sendParticle(player,
                    ParticleEffect.REDSTONE, sheep.getBukkitEntity().getLocation().clone().add(0, 1, 0), 0, 255, 0, 1, 0);
            PacketManager.sendParticle(player,
                    ParticleEffect.REDSTONE, sheep.getBukkitEntity().getLocation().clone().add(0, 1, 0), 0, 0, 255, 1, 0);

            player.setExp(player.getExp() - 0.05F);

            sheep.g(sideMot, forMot);

            if(forMot != 0) {
                sheep.motX *= Math.sqrt(sheep.getSpeed()+0.1);
                sheep.motZ *= Math.sqrt(sheep.getSpeed()+0.1);
            }
        }
    }
}
