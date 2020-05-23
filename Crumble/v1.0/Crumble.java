package Hiro3;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

public class Crumble extends EarthAbility implements ComboAbility, AddonAbility {

	private Location startLocation;
	
	private long cooldown;
	private double detectRange;
	private int maxBfsLevel;
	private int maxBlockCount;
	private int currentBlockCount;
	
	private boolean isRegenOn;
	private long latency;
	
	private Block head;
	
	private HashMap<Location, Material> firstBlockMaterials;
	private HashMap<Location, Byte> firstBlockDatas;
	private ArrayList<FallingBlock> fallingBlocks;
	
	private int state;
	
	public Crumble(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		setField();
		start();
	}

	public void setField() {
		this.startLocation = player.getLocation();
		
		this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.Crumble.Cooldown");
		this.detectRange = 5;
		this.maxBfsLevel = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.Crumble.MaximumDepth");
		this.maxBlockCount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.Crumble.MaximumBlockAmount");
		this.currentBlockCount = 0;
		
		this.isRegenOn = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Earth.Crumble.Revert.Enable");
		this.latency = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.Crumble.Revert.Latency");
		
		this.firstBlockMaterials = new HashMap<Location, Material>(0);
		this.firstBlockDatas = new HashMap<Location, Byte>(0);
		this.fallingBlocks = new ArrayList<FallingBlock>(0);
		
		this.state = 0;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		
		if (state == 0) {
			if (!player.isSneaking()) {
				remove();
				return;
			}
			//ArrayList<Block> detectBlocks = getTargetBlocks(player, detectRange);
			//for (Block b : detectBlocks) {
					checkCollision();
					Location tmpLoc = getTargetLocation(player, detectRange);
					Block b = tmpLoc.getBlock();
					this.startLocation = b.getLocation();
				if (isEarthbendable(b)) {
					this.head = b;
					state++;
					//break;
				} else {
					player.spawnParticle(Particle.SMOKE_NORMAL, tmpLoc, 0);
				}
			//}
		} else if (state == 1) {
			bPlayer.addCooldown(this);
			ArrayList<Location> affectedBlockLocations = new ArrayList<Location>(0);
			affectedBlockLocations.add(head.getLocation());
			this.firstBlockMaterials.put(head.getLocation(), head.getType());
			this.firstBlockDatas.put(head.getLocation(), head.getData());
			this.currentBlockCount++;
			breakBlocks(affectedBlockLocations);
			breadthFirstSearch(affectedBlockLocations, head.getLocation(), 0);
			state++;
		} else {
			for (FallingBlock fb : fallingBlocks) {
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 2)) {
					if (e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId())) {
						DamageHandler.damageEntity(e, 3, this);
					}
				}
			}
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public void breadthFirstSearch(ArrayList<Location> oldAffectedBlockLocations, Location headLocation, int level) {
		
		//player.sendMessage("level = " + level);
		
		if (level > this.maxBfsLevel) {
			remove();
			return;
		}
		
		if (this.currentBlockCount >= this.maxBlockCount) {
			remove();
			return;
		}
		
		ArrayList<Location> affectedBlockLocations = new ArrayList<Location>(0);
		for (Location loc : oldAffectedBlockLocations) {
			ArrayList<Block> tmpList = (ArrayList<Block>) GeneralMethods.getBlocksAroundPoint(loc, 2);
			for (Block b : tmpList) {
				if (!b.isEmpty() && !affectedBlockLocations.contains(b.getLocation())) {
					if (!firstBlockMaterials.containsKey(b.getLocation())) {
						this.firstBlockMaterials.put(b.getLocation(), b.getType());
						this.firstBlockDatas.put(b.getLocation(), b.getData());
					}
					if (isEarthbendable(b) && ((int) b.getLocation().distance(headLocation)) == level+1) {
						if (this.currentBlockCount < this.maxBlockCount) {
							affectedBlockLocations.add(b.getLocation());
							this.currentBlockCount++;
							//player.sendMessage("blocks = " + this.currentBlockCount);
						}
					}
				}
			}
		}
		
		breakBlocks(affectedBlockLocations);
		
		new BukkitRunnable() {

			@Override
			public void run() {
				breadthFirstSearch(affectedBlockLocations, headLocation, level+1);
			}
				
		}.runTaskLater(ProjectKorra.plugin, 2);
	}
	
	@SuppressWarnings("deprecation")
	public void breakBlocks(ArrayList<Location> affectedBlockLocations) {
		Vector pDirection = player.getLocation().getDirection().clone();
		int flag = 0;
		if (pDirection.getY() <= -0.35) {
			pDirection.setY(pDirection.getY() * -1);
			flag = 2;
		}
		for (Location loc : affectedBlockLocations) {
			FallingBlock temp = player.getWorld().spawnFallingBlock(loc, loc.getBlock().getType(), loc.getBlock().getData());
			temp.setVelocity(rotateVectorAroundXZ(pDirection, Math.random()*60 - 20).multiply(0.5));
			temp.setVelocity(temp.getVelocity().add(rotateVectorAroundY(temp.getVelocity(), Math.random()*90*(1 + flag) - 45*(1 + flag))).multiply(0.5));
			//temp.setVelocity(temp.getVelocity().add(temp.getVelocity().clone().multiply(player.getVelocity().length())));
			if (flag != 0) {
				temp.setVelocity(temp.getVelocity().multiply(2));
			}
			temp.setDropItem(false);
			this.fallingBlocks.add(temp);
			
			loc.getBlock().setType(Material.AIR);
		}
		if (affectedBlockLocations.size() != 0)
			playEarthbendingSound(affectedBlockLocations.get(0));
	}
	
	public ArrayList<Block> getTargetBlocks(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.01);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()));
		
		return (ArrayList<Block>) GeneralMethods.getBlocksAroundPoint(loc, 2);
	}
	
	public Location getTargetLocation(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.01);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()));
		
		return loc;
	}
	
	public static Vector rotateVectorAroundY(Vector vector, double degrees) {
	    double rad = Math.toRadians(degrees);
	   
	    double currentX = vector.getX();
	    double currentZ = vector.getZ();
	   
	    double cosine = Math.cos(rad);
	    double sine = Math.sin(rad);
	   
	    return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
	}
	
	public static Vector rotateVectorAroundXZ(Vector vector, double degrees) {
		Vector rightTmpVec = new Vector(-vector.getZ(), 0, +vector.getX());
		
		double rad = Math.toRadians(degrees);
		Vector v1 = vector.clone().multiply(Math.cos(rad));
		Vector v2 = vector.clone().crossProduct(rightTmpVec);
		v2.multiply(Math.sin(rad));
		return v1.add(v2);
	}
	
	public void checkCollision() {
		for ( EarthSmash es : getAbilities(EarthSmash.class) ) {
			Location esLoc = es.getLocation();
			if (!esLoc.getWorld().equals(player.getWorld()))
				return;
			if (es.getLocation().distance(this.getLocation()) <= this.getCollisionRadius()) {
				handleEarthSmash(es);
			}
		}
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
//		if (head == null) {
//			return getTargetBlocks(player, detectRange).get(0).getLocation();
//		}
//		return head.getLocation();
		//return getTargetLocation(player, detectRange);
		return startLocation;
		//return player.getLocation();
	}
	
	@Override
	public double getCollisionRadius() {
		return 1;
	}
	
	public void handleEarthSmash(EarthSmash smash) {
		ArrayList<Block> blocks = (ArrayList<Block>) smash.getBlocks();
		ArrayList<Material> materials = new ArrayList<Material>();
		for (TempBlock tb : smash.getAffectedBlocks()) {
			materials.add(tb.getBlock().getType());
		}
		smash.remove();
		
		int i = 0;
		for (Block b : blocks) {
			if (materials.size() > i)
				b.setType(materials.get(i));
			i++;
		}
	}
	
	@Override
	public String getName() {
		return "Crumble";
	}

	@Override
	public String getDescription() {
		return "Crumble the obstacles you face! With this ability you can destroy earth blocks, EarthSmashes and EarthBlasts.";
	}

	@Override
	public String getInstructions() {
		return "Shockwave (Sneak) -> Shockwave (Left Click)";
	}
	
	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "v1.0";
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new Crumble(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Shockwave", ClickType.LEFT_CLICK));

		return combination;
	}
	
	@Override
	public void load() {
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Crumble.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Crumble.MaximumBlockAmount", 50);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Crumble.MaximumDepth", 10);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Crumble.Revert.Enable", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Crumble.Revert.Latency", 120);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

	@Override
	public void remove() {
		super.remove();
		
		if (this.isRegenOn) {
			for (FallingBlock fb : fallingBlocks) {
				new BukkitRunnable() {
	
					@Override
					public void run() {
						if (!fb.isDead())
							fb.remove();
						else if (!firstBlockMaterials.containsKey(fb.getLocation())) {
							fb.getLocation().getBlock().setType(Material.AIR);
						}
					}
					
				}.runTaskLater(ProjectKorra.plugin, latency * 20);
			}
			
			new BukkitRunnable() {
	
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					for (Location loc : firstBlockMaterials.keySet()) {
						loc.getBlock().setType(firstBlockMaterials.get(loc));
						loc.getBlock().setData(firstBlockDatas.get(loc));
					}
				}
				
			}.runTaskLater(ProjectKorra.plugin, latency * 20); 
		}
		
	}
	
}
