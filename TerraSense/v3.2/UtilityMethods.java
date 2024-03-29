package me.hiro3.terrasense;

import java.util.ArrayList;

import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntityMagmaCube;

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
          Entity entityPlayer = ((CraftEntity) glowingEntity).getHandle();

          //DataWatcher toCloneDataWatcher = entityPlayer.getDataWatcher();
          DataWatcher toCloneDataWatcher = entityPlayer.ai();
          DataWatcher newDataWatcher = new DataWatcher(entityPlayer);
          
          try { //Spigot
        	  Class.forName("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap");
        	  Int2ObjectOpenHashMap<DataWatcher.Item<?>> currentMap = (Int2ObjectOpenHashMap) FieldUtils.readDeclaredField(toCloneDataWatcher, "f", true);
              Int2ObjectOpenHashMap<DataWatcher.Item<?>> newMap = new Int2ObjectOpenHashMap<DataWatcher.Item<?>>();
              for (Integer integer : currentMap.keySet()) {
                  newMap.put(integer, currentMap.get(integer).d()); 
              }

              DataWatcher.Item item = newMap.get(0);
              byte initialBitMask = (Byte) item.b(); 
              byte bitMaskIndex = (byte) 6; 
              if (glow) {
                  item.a((byte) (initialBitMask | 1 << bitMaskIndex));
              } else {
                  item.a((byte) (initialBitMask & ~(1 << bitMaskIndex))); 
              }

              FieldUtils.writeDeclaredField(newDataWatcher, "f", newMap, true);

              PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(glowingEntity.getEntityId(), newDataWatcher, true);
              ((CraftPlayer) sendPacketPlayer).getHandle().b.a(metadataPacket);
          } catch( ClassNotFoundException e ) { //Paper
        	  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<DataWatcher.Item<?>> currentMap = 
        			  (it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap) FieldUtils.readDeclaredField(toCloneDataWatcher, "f", true);
        	  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<DataWatcher.Item<?>> newMap = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<DataWatcher.Item<?>>();
              for (Integer integer : currentMap.keySet()) {
                  newMap.put(integer, currentMap.get(integer).d()); 
              }

              DataWatcher.Item item = newMap.get(0);
              byte initialBitMask = (Byte) item.b(); 
              byte bitMaskIndex = (byte) 6; 
              if (glow) {
                  item.a((byte) (initialBitMask | 1 << bitMaskIndex));
              } else {
                  item.a((byte) (initialBitMask & ~(1 << bitMaskIndex))); 
              }

              FieldUtils.writeDeclaredField(newDataWatcher, "f", newMap, true);

              PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(glowingEntity.getEntityId(), newDataWatcher, true);

              ((CraftPlayer) sendPacketPlayer).getHandle().b.a(metadataPacket);
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

        PlayerConnection connection = ((CraftPlayer) p).getHandle().b;
        EntityMagmaCube cube = new EntityMagmaCube(EntityTypes.X, ((CraftWorld) loc.getWorld()).getHandle());
        cube.a(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        cube.d(true);
        cube.m(true);
        cube.b(6, true); //Glow
        cube.b(5, true); //Invisibility

        PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(cube);
        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(cube.ae(),cube.ai(),false);
        connection.a(spawnPacket);
        connection.a(entityMetadata);

        Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, () -> {
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(cube.ae());
            connection.a(destroyPacket);
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
					if (TerraSense.senseOnlyEarthBlocks) {
						if (EarthAbility.isEarthbendable(player, center.getBlock()))
							sendGlowingBlock(player, center, 1);
					} else {
						if (GeneralMethods.isSolid(center.getBlock()))
							sendGlowingBlock(player, center, 1);
					}
					center.subtract(radius * Math.cos(angle), 0, radius * Math.sin(angle));
				}
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		
	}
	
	public static void sendPulseImproved(Player player, Location center, double maxRadius, int step, int angleIncrease) {
		Vector startingVector = new Vector(maxRadius/step, 0, 0);
		Location startingLocation = center;
		
		ArrayList<Location> l = new ArrayList<Location>();
		ArrayList<Vector> d = new ArrayList<Vector>();
		for (int i = 0; i < 360; i += angleIncrease) {
			l.add(startingLocation.clone());
			d.add(UtilityMethods.rotateVectorAroundY(startingVector, i));
		}
		
		new BukkitRunnable() {

			int tick = 0;
			
			@Override
			public void run() {
				if (tick >= step) {
					Bukkit.getScheduler().cancelTask(getTaskId());
					return;
				}
				
				if (TerraSense.senseOnlyEarthBlocks) {
					for (int i = 0; i < l.size(); i++) {
						if (EarthAbility.isEarthbendable(player, l.get(i).getBlock())) {
							UtilityMethods.sendGlowingBlock(player, l.get(i), 1);
						}
						if (EarthAbility.isEarthbendable(player, l.get(i).getBlock().getRelative(BlockFace.UP))) {
							l.get(i).add(0, 1, 0);
						} else {
							l.get(i).add(d.get(i));
							if (EarthAbility.isEarthbendable(player, l.get(i).getBlock().getRelative(BlockFace.UP))) {
								l.get(i).add(0, 1, 0);
							} else {
								l.set(i, UtilityMethods.getFloor(player, l.get(i)));
							}
						}
					}
				} else {
					for (int i = 0; i < l.size(); i++) {
						if (GeneralMethods.isSolid(l.get(i).getBlock())) {
							UtilityMethods.sendGlowingBlock(player, l.get(i), 1);
						}
						if (GeneralMethods.isSolid(l.get(i).getBlock().getRelative(BlockFace.UP))) {
							l.get(i).add(0, 1, 0);
						} else {
							l.get(i).add(d.get(i));
							if (GeneralMethods.isSolid(l.get(i).getBlock().getRelative(BlockFace.UP))) {
								l.get(i).add(0, 1, 0);
							} else {
								l.set(i, UtilityMethods.getFloor(player, l.get(i)));
							}
						}
					}
				}
				
				tick++;
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	public static Location getFloor(Player player, Location loc) {
		Location f = loc.clone();
		if (TerraSense.senseOnlyEarthBlocks) {
			while (!EarthAbility.isEarthbendable(player, loc.getBlock()) && loc.distance(f) < 30) {
				loc.add(0, -1, 0);
			}
		} else {
			while (!GeneralMethods.isSolid(loc.getBlock()) && loc.distance(f) < 30) {
				loc.add(0, -1, 0);
			}
		}
		return loc;
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
	
	public static String getVersion() {
		return "3.2";
	}
	
}