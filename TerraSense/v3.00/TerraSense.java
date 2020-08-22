package me.hiro3.terrasense;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.jedk1.jedcore.ability.earthbending.EarthKick;
import com.jedk1.jedcore.ability.earthbending.EarthLine;
import com.jedk1.jedcore.ability.earthbending.EarthShard;
import com.jedk1.jedcore.ability.earthbending.MudSurge;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthDome;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;

public class TerraSense extends EarthAbility implements AddonAbility, PassiveAbility {

	public final static HashMap<Player, ArrayList<LivingEntity>> senseMap = new HashMap<Player, ArrayList<LivingEntity>>();
	
	private ArrayList<LivingEntity> glowingEntities;
	
	private enum AbilityState {
		SENSING,
	    NONSENSE,
	    CANTUSE;
	}
	
	private AbilityState state;
	
	private double range;
	private double blindMultiplier;
	private double isBlind;
	private double sandMultiplier;
	private double isOnSand;
	private boolean isActive;
	private boolean canSenseEarthbending;
	public static boolean canSenseWaves;
	private long senseStartTime;
	private long senseDelayTime;
	
	private int tick;
	private boolean canRemoveBlind;
	private int delayFlag;
	
	public TerraSense(Player player) {
		super(player);
		
		setField();
	}
	
	public void setField() {
		glowingEntities = new ArrayList<LivingEntity>();
		
		state = AbilityState.NONSENSE;
		
		range = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.Range");
		blindMultiplier = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.BlindMultiplier");
		isBlind = player.getPotionEffect(PotionEffectType.BLINDNESS) != null ? 1 : (1/blindMultiplier);
		sandMultiplier = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.SandMultiplier");
		isOnSand = EarthAbility.isSand(player.getLocation().clone().add(0, -0.2, 0).getBlock()) ? 1 : (1/sandMultiplier);
		isActive = true;
		tick = 0;
		canRemoveBlind = true;
		canSenseEarthbending = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Earth.TerraSense.CanSenseEarthbending");
		canSenseWaves = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Earth.TerraSense.CanSenseWaves");
		senseStartTime = Long.MAX_VALUE;
		senseDelayTime = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.TerraSense.SneakSenseDelay");
		delayFlag = 0;
	}

	@Override
	public void progress() {
		
		if (!isActive || !this.bPlayer.canUsePassive(this) || !this.bPlayer.canBendPassive(this)) {
			if (this.state != AbilityState.CANTUSE) {
				state = AbilityState.CANTUSE;
				UtilityMethods.removeGlowAll(this);
				glowingEntities.clear();
				senseMap.remove(player);
			}
			return;
		}
		
		if (canSense()) {
			if (state != AbilityState.SENSING) {
				senseMap.put(player, glowingEntities);
				state = AbilityState.SENSING;
			}
			
			//Artýk geçersiz olanlarý gönder
			if (!glowingEntities.isEmpty()) {
				for (int i = glowingEntities.size()-1; i >= 0; i--) {
					if (!isSenseable(glowingEntities.get(i))) {
						UtilityMethods.setGlowing(glowingEntities.get(i), player, false);
						glowingEntities.remove(glowingEntities.get(i));
					}
				}
			}
			
			//Yeni gelenleri al
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), getCurrentRange())) {
				if (e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()) 
						&& !glowingEntities.contains(e) && isSenseable((LivingEntity) e)) {
					glowingEntities.add((LivingEntity) e);
					UtilityMethods.setGlowing((LivingEntity) e, player, true);
				}
			}
			
			if (this.canSenseEarthbending)
				senseEarthbending();
			
			if (tick % 1 == 0) {
				UtilityMethods.addGlowAll(this);
			}
			
		} else if (state == AbilityState.SENSING) {
			state = AbilityState.NONSENSE;
			UtilityMethods.removeGlowAll(this);
			glowingEntities.clear();
			senseMap.remove(player);
		}
		
		tick++;
		tick %= 20;
	}
	
	public void senseEarthbending() {
		if (this.isBlind != 1)
			return;
		
		for (EarthBlast eb : CoreAbility.getAbilities(EarthBlast.class)) {
			if (eb.getLocation() != null
					//&& !eb.getBendingPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())
					&& eb.getLocation().getWorld().equals(player.getWorld())
					&& eb.getLocation().distance(player.getLocation()) <= getCurrentRange()) {
				UtilityMethods.sendGlowingBlock(player, eb.getLocation().clone().add(0.5, 0, 0.5), 1);
			}
		}
		
		for (EarthSmash eb : CoreAbility.getAbilities(EarthSmash.class)) {
			if (eb.getLocations() != null && !eb.getLocations().isEmpty()
					//&& !eb.getBendingPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())
					&& eb.getLocations().get(0).getWorld().equals(player.getWorld())
					&& eb.getLocations().get(0).distance(player.getLocation()) <= getCurrentRange()) {
				for (Location l : eb.getLocations())
					if (l != null)
						UtilityMethods.sendGlowingBlock(player, l.clone().add(0.5, 0, 0.5), 1);
			}
		}
		
		for (RaiseEarth eb : CoreAbility.getAbilities(RaiseEarth.class)) {
			if (eb.getLocations() != null && !eb.getLocations().isEmpty()
					//&& !eb.getBendingPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())
					&& eb.getLocations().get(0).getWorld().equals(player.getWorld())
					&& eb.getLocations().get(0).distance(player.getLocation()) <= getCurrentRange()) {
				for (Location l : eb.getLocations())
					if (l != null)
						UtilityMethods.sendGlowingBlock(player, l.clone().add(0.5, 0, 0.5), 1);
			}
		}
		
		for (EarthDome eb : CoreAbility.getAbilities(EarthDome.class)) {
			if (eb.getLocations() != null && !eb.getLocations().isEmpty()
					//&& !eb.getBendingPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())
					&& eb.getLocations().get(0).getWorld().equals(player.getWorld())
					&& eb.getLocations().get(0).distance(player.getLocation()) <= getCurrentRange()) {
				for (Location l : eb.getLocations())
					if (l != null)
						UtilityMethods.sendGlowingBlock(player, l.clone().add(0.5, 0, 0.5), 1);
			}
		}
		
		if(!Bukkit.getPluginManager().isPluginEnabled("JedCore")) 
			return;
		
		for (MudSurge eb : CoreAbility.getAbilities(MudSurge.class)) {
			if (eb.getLocations() != null && !eb.getLocations().isEmpty()
					//&& !eb.getBendingPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())
					&& eb.getLocations().get(0).getWorld().equals(player.getWorld())
					&& eb.getLocations().get(0).distance(player.getLocation()) <= getCurrentRange()) {
				for (Location l : eb.getLocations())
					if (l != null)
						UtilityMethods.sendGlowingBlock(player, l.clone().add(0.5, 0, 0.5), 1);
			}
		}
		
		for (EarthLine eb : CoreAbility.getAbilities(EarthLine.class)) {
			if (eb.getLocations() != null && !eb.getLocations().isEmpty()
					//&& !eb.getBendingPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())
					&& eb.getLocations().get(0).getWorld().equals(player.getWorld())
					&& eb.getLocations().get(0).distance(player.getLocation()) <= getCurrentRange()) {
				for (Location l : eb.getLocations())
					if (l != null)
						UtilityMethods.sendGlowingBlock(player, l.clone().add(0.5, 0, 0.5), 1);
			}
		}
		
		for (EarthShard eb : CoreAbility.getAbilities(EarthShard.class)) {
			if (eb.getLocations() != null && !eb.getLocations().isEmpty()
					//&& !eb.getBendingPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())
					&& eb.getLocations().get(0).getWorld().equals(player.getWorld())
					&& eb.getLocations().get(0).distance(player.getLocation()) <= getCurrentRange()) {
				for (Location l : eb.getLocations())
					if (l != null)
						UtilityMethods.sendGlowingBlock(player, l.clone().add(0.5, 0, 0.5), 1);
			}
		}
		
		for (EarthKick eb : CoreAbility.getAbilities(EarthKick.class)) {
			if (eb.getLocations() != null && !eb.getLocations().isEmpty()
					//&& !eb.getBendingPlayer().getPlayer().getUniqueId().equals(player.getUniqueId())
					&& eb.getLocations().get(0).getWorld().equals(player.getWorld())
					&& eb.getLocations().get(0).distance(player.getLocation()) <= getCurrentRange()) {
				for (Location l : eb.getLocations())
					if (l != null)
						UtilityMethods.sendGlowingBlock(player, l.clone().add(0.5, 0, 0.5), 1);
			}
		}
		
	}
	
	public boolean isSenseable(LivingEntity le) {
		if (le.isOnGround() && isEarthbendable(le.getLocation().clone().add(0, -0.2, 0).getBlock())
				&& player.getLocation().distance(le.getLocation()) <= getCurrentRange())
			return true;
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean canSense() {
		if (EarthAbility.isSand(player.getLocation().clone().add(0, -0.2, 0).getBlock())) {
			this.isOnSand = 1;
		} else {
			this.isOnSand = 1/this.sandMultiplier;
		}

		if (player.isOnGround() && isEarthbendable(player.getLocation().clone().add(0, -0.2, 0).getBlock()))
			if (player.isSneaking() && player.getPotionEffect(PotionEffectType.BLINDNESS) == null) {
				isBlind = 1/blindMultiplier;
				if (state == AbilityState.NONSENSE && delayFlag == 0) {
					delayFlag = 1;
					senseStartTime = System.currentTimeMillis();
				}
				if (System.currentTimeMillis() < senseStartTime + senseDelayTime)
					return false;
				return true;
			} else if (player.getPotionEffect(PotionEffectType.BLINDNESS) != null) { 
				isBlind = 1;
				return true;
			} else {
				delayFlag = 0;
			}
		return false;
	}
	
	public double getCurrentRange() {
		return this.range * this.blindMultiplier * this.isBlind * this.sandMultiplier * this.isOnSand;
	}
	
	public ArrayList<LivingEntity> getGlowingEntities() {
		return this.glowingEntities;
	}
	
	public boolean isActive() {
		return this.isActive;
	}
	
	public void setActive(boolean isActive) {
		if (!isActive && state == AbilityState.SENSING) {
			state = AbilityState.NONSENSE;
			UtilityMethods.removeGlowAll(this);
			glowingEntities.clear();
			senseMap.remove(player);
		}
		
		this.isActive = isActive;
	}
	
	public double getBlindRadius() {
		return this.range * this.blindMultiplier;
	}
	
	public boolean getCanRemoveBlind() {
		return this.canRemoveBlind;
	}
	
	public void setCanRemoveBlind(boolean value) {
		this.canRemoveBlind = value;
	}
	
	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "TerraSense";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}

	@Override
	public String getDescription() {
		return "As an earthbender, you are able to sense the tremor to locate living creatures around you.";
	}
	
	@Override
	public String getInstructions() {
		return "Stand on an earthbendable block and sneak.";
	}
	
	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "3.00 (REDONE)";
	}
	
	@Override
	public void load() {
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.Range", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.BlindMultiplier", 3);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.SandMultiplier", 0.4);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.CanSenseEarthbending", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.CanSenseWaves", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.SneakSenseDelay", 0);
		ConfigManager.defaultConfig.save();
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}