package me.hiro3.terrasense;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.google.common.collect.Maps;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;

import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityMagmaCube;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_12_R1.PlayerConnection;

public class UtilityMethods {

	public static void addGlowAll(TerraSense ts) {
		for (LivingEntity le : ts.getGlowingEntities()) {
			setGlowing(le, ts.getPlayer(), true);
		}
	}
	
	public static void removeGlowAll(TerraSense ts) {
		for (LivingEntity le : ts.getGlowingEntities()) {
			setGlowing(le, ts.getPlayer(), false);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public static void setGlowing(LivingEntity glowingEntity, Player sendPacketPlayer, boolean glow) {
		try {
			
			PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
			
			Entity entityPlayer = ((CraftEntity) glowingEntity).getHandle();
			
			DataWatcher toCloneDataWatcher = entityPlayer.getDataWatcher();
			
			Map<Integer, DataWatcher.Item<?>> currentMap = (Map<Integer, DataWatcher.Item<?>>) FieldUtils.readDeclaredField(toCloneDataWatcher, "d", true);
			Map<Integer, DataWatcher.Item<?>> newMap = Maps.newHashMap();
			
			for (Integer integer : currentMap.keySet()) {
	            newMap.put(integer, currentMap.get(integer).d()); // Puts a copy of the DataWatcher.Item in newMap
	        }
			
			DataWatcher.Item item = newMap.get(0);
			byte initialBitMask = (Byte) item.b();
			
			   
		    packet.getIntegers().write(0, glowingEntity.getEntityId()); //Set packet's entity id
		    WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
		    Serializer serializer = Registry.get(Byte.class); //Found this through google, needed for some stupid reason
		    watcher.setEntity(glowingEntity); //Set the new data watcher's target
		    if (glow)
		    	watcher.setObject(0, serializer, initialBitMask |= 0b01000000); //Set status to glowing, found on protocol page
		    else
		    	watcher.setObject(0, serializer, initialBitMask &= 0b10111111); //Set status to glowing, found on protocol page
		    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); //Make the packet's datawatcher the one we created
		    packet.addMetadata("TerraSense", "dummy");
		    try {
		        ProtocolLibrary.getProtocolManager().sendServerPacket(sendPacketPlayer, packet);
		    } catch (InvocationTargetException e) {
		        e.printStackTrace();
		    }
			
		} catch (IllegalAccessException e) {
            e.printStackTrace();
        }
	}
	
	public static void sendGlowingBlock(Player p, Location loc, long lifetime){
		if (CoreAbility.hasAbility(p, TerraSense.class)
				&& !CoreAbility.getAbility(p, TerraSense.class).canSense()) {
			return;
		}
		
        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;

        EntityMagmaCube cube = new EntityMagmaCube(((CraftWorld) loc.getWorld()).getHandle());
        cube.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        cube.setSilent(true);
        cube.setInvulnerable(true);
        cube.setFlag(6, true); //Glow
        cube.setFlag(5, true); //Invisibility

        PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(cube);
        connection.sendPacket(spawnPacket);

        Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, () -> {
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(cube.getId());
            connection.sendPacket(destroyPacket);
        },  lifetime);
    }
	
	public static void sendPulse(Player player, Location center, double maxRadius, int step, int angleIncrease) {
		
		new BukkitRunnable() {

			double angle;
			double radius = 0;
			double speed = maxRadius/step;
			
			@Override
			public void run() {
				
				if (radius >= maxRadius) {
					Bukkit.getScheduler().cancelTask(getTaskId());
					return;
				}
				
				radius += speed;
				for(int i = 0; i <= 360; i+=angleIncrease) {
					angle = Math.toRadians(i);
					center.add(radius * Math.cos(angle), 0, radius * Math.sin(angle));
					if (EarthAbility.isEarthbendable(player, center.getBlock()))
						sendGlowingBlock(player, center, 1);
					center.subtract(radius * Math.cos(angle), 0, radius * Math.sin(angle));
				}
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		
	}
	
	public static void refreshAllTerraSenses() {
		for (TerraSense ts : CoreAbility.getAbilities(TerraSense.class)) {
			addGlowAll(ts);
		}
	}
	
	public static Vector rotateVectorAroundY(Vector vector, double degrees) {
        double rad = Math.toRadians(degrees);
       
        double currentX = vector.getX();
        double currentZ = vector.getZ();
       
        double cosine = Math.cos(rad);
        double sine = Math.sin(rad);
       
        return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
    }
	
}
