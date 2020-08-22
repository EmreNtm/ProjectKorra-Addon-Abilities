package me.hiro3.terrasense;

import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;

import net.minecraft.server.v1_16_R1.DataWatcher;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityMagmaCube;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_16_R1.PlayerConnection;

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

          DataWatcher toCloneDataWatcher = entityPlayer.getDataWatcher();
          DataWatcher newDataWatcher = new DataWatcher(entityPlayer);
          
          try { //Spigot
        	  Class.forName("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap");
        	  Int2ObjectOpenHashMap<DataWatcher.Item<?>> currentMap = (Int2ObjectOpenHashMap) FieldUtils.readDeclaredField(toCloneDataWatcher, "entries", true);
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

              FieldUtils.writeDeclaredField(newDataWatcher, "entries", newMap, true);

              PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(glowingEntity.getEntityId(), newDataWatcher, true);

              ((CraftPlayer) sendPacketPlayer).getHandle().playerConnection.sendPacket(metadataPacket);
          } catch( ClassNotFoundException e ) { //Paper
        	  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<DataWatcher.Item<?>> currentMap = 
        			  (it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap) FieldUtils.readDeclaredField(toCloneDataWatcher, "entries", true);
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

              FieldUtils.writeDeclaredField(newDataWatcher, "entries", newMap, true);

              PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(glowingEntity.getEntityId(), newDataWatcher, true);

              ((CraftPlayer) sendPacketPlayer).getHandle().playerConnection.sendPacket(metadataPacket);
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
        
        EntityMagmaCube cube = new EntityMagmaCube(EntityTypes.MAGMA_CUBE, ((CraftWorld) loc.getWorld()).getHandle());
        cube.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        cube.setSilent(true);
        cube.setInvulnerable(true);
        cube.setFlag(6, true); //Glow
        cube.setFlag(5, true); //Invisibility

        PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(cube);
        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(cube.getId(),cube.getDataWatcher(),false);
        connection.sendPacket(spawnPacket);
        connection.sendPacket(entityMetadata);

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