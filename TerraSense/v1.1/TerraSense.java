package me.Hiro.MyAbilities.TerraSense;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.glow.GlowAPI;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class TerraSense extends EarthAbility implements AddonAbility, PassiveAbility {

	private double r;
	private LivingEntity[] entities;

	public TerraSense(Player player) {
		super(player);
		setFields();
	}

	public void setFields() {
		entities = new LivingEntity[50];
	}

	@Override
	public void progress() {
		r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.Radius");
		int j, i = 0;
		int flag = 0;
		Location loc = player.getLocation();
		for (PotionEffect p : player.getActivePotionEffects()) {
			if (p.getType().getName() == "BLINDNESS") {
				flag = 1;
				r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.BlindRadius");
			}
		}
		if (player.isSneaking() || flag == 1) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, r)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					j = 0;
					while (j < 50 && entity != entities[j]) {
						j++;
					}
					if (j == 50) {
						Block eBlock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
						Block pBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
						if (GeneralMethods.isSolid(pBlock) && isEarthbendable(pBlock) && GeneralMethods.isSolid(eBlock)
								&& isEarthbendable(eBlock)) {
							GlowAPI.setGlowing(entity, GlowAPI.Color.GRAY, player);
							entities[i] = (LivingEntity) entity;
							i++;
						}
					}
					if (i == 50) {
						i = 0;
					}
				}
			}
		}

		check();

	}

	public void check() {
		int j;
		int flag = 0;
		for (PotionEffect p : player.getActivePotionEffects()) {
			if (p.getType().getName() == "BLINDNESS") {
				flag = 1;
			}
		}
		for (j = 0; j < 50; j++) {
			if (entities[j] != null && entities[j].getLocation().distance(player.getLocation()) > r) {
				GlowAPI.setGlowing(entities[j], null, player);
				entities[j] = null;
			} else if (entities[j] != null) {
				Block eBlock = entities[j].getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
				Block pBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
				if (!(GeneralMethods.isSolid(pBlock) && isEarthbendable(pBlock) && GeneralMethods.isSolid(eBlock)
						&& isEarthbendable(eBlock) && (player.isSneaking() || flag == 1))) {
					GlowAPI.setGlowing(entities[j], null, player);
					entities[j] = null;
				}
			}
		}
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
		return false;
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
		return "Feel the tremor around the earth and locate living entities!";
	}
	
	@Override
	public String getInstructions() {
		return "Stand on an earthbendable block and sneak/close your eyes.";
	}
	
	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "1.1";
	}

	@Override
	public void load() {
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.Radius", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.BlindRadius", 45);

		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
