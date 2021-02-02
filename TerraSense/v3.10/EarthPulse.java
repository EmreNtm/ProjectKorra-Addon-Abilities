package me.hiro3.terrasense;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;

import net.md_5.bungee.api.ChatColor;

public class EarthPulse extends EarthAbility implements AddonAbility {

	private Listener listener;
	
	private long cooldown;
	
	public EarthPulse(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this))
			return;
		
		setField();
		start();
	}

	public void setField() {
		cooldown = 1000;
	}
	
	@Override
	public void progress() {
		if (!CoreAbility.hasAbility(player, TerraSense.class)) {
			remove();
			return;
		}
		TerraSense ts = CoreAbility.getAbility(player, TerraSense.class);
		if (!player.isSneaking() && (player.getPotionEffect(PotionEffectType.BLINDNESS) == null)) {
			if (ts.isActive()) {
				ts.setActive(false);
				player.sendMessage(ChatColor.GREEN + "TerraSense is disabled!");
			} else {
				ts.setActive(true);
				player.sendMessage(ChatColor.GREEN + "TerraSense is enabled!");
			}
			bPlayer.addCooldown(this);
		} else if (player.getPotionEffect(PotionEffectType.BLINDNESS) == null) {
			ts.setActive(true);
			ts.setCanRemoveBlind(true);
			PotionEffect pe = new PotionEffect(PotionEffectType.BLINDNESS, 100000*20, 1);
			player.addPotionEffect(pe);
			player.sendMessage(ChatColor.DARK_GREEN + "You closed your eyes to sharpen your senses!");
			if (TerraSense.canSenseWaves) {
				if (TerraSense.improvedWavesOnFallAndMove)
					UtilityMethods.sendPulseImproved(player, player.getLocation().add(0, -1, 0), ts.getBlindRadius(), 40, 5);
				else
					UtilityMethods.sendPulse(player, player.getLocation().add(0, -1, 0), ts.getBlindRadius(), 40, 5);
			}
			bPlayer.addCooldown(this);
		} else if (player.isSneaking() && ts.getCanRemoveBlind()) {
			ts.setActive(true);
			ts.setCanRemoveBlind(false);
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.sendMessage(ChatColor.DARK_GREEN + "You opened your eyes!");
			bPlayer.addCooldown(this);
		}
		remove();
	}
	
	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "EarthPulse";
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
	public String getDescription() {
		return "Enable EarthPulse to sharpen your seismic sense! (Enables TerraSense passive.)";
	}
	
	@Override
	public String getInstructions() {
		return "Left Click: Enable/Disable the TerraSense passive.\n"
				+ "Sneak + Left Click: Close your eyes to sense the earth better. Use it again to"
				+ " open your eyes.";
	}
	
	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return UtilityMethods.getVersion();
	}

	@Override
	public void load() {
		listener = new MainListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(listener);
		super.remove();
	}

}