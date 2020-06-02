package Hiro3;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;

import me.xnuminousx.spirits.ability.api.SpiritAbility;

public class SoulSwap extends SpiritAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private Soul soul;
	
	public SoulSwap(Player player, Soul soul) {
		super(player);
		
		 if (!this.bPlayer.canBendIgnoreBinds(this)) {
		      return;
		 }
		
		setField();
		this.soul = soul;
		bPlayer.addCooldown(this);
		start();
	}

	public void setField() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSwap.Cooldown");
	}
	
	@Override
	public void progress() {
		Location tmp = soul.getLoc();
		soul.setLoc(player.getLocation());
		soul.setDir(player.getLocation().getDirection());
		player.teleport(tmp);
		player.getWorld().playSound(soul.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
		remove();
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "SoulSwap";
	}

	@Override
	public String getDescription() {
		return "Change your location with the nearest soul.";
	}

	@Override
	public String getInstructions() {
		return "SoulSplit (Tap Sneak) -> SoulSplit (Tap Sneak) -> SoulSplit (Left Click)";
	}
	
	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		double min;
		Soul soul;
		if (hasAbility(player, SoulSplit.class) && getAbility(player, SoulSplit.class).getSoulNo() != 0) {
			min = getAbility(player, SoulSplit.class).getSouls().get(0).getLoc().distance(player.getLocation());
			soul = getAbility(player, SoulSplit.class).getSouls().get(0);
			for (Soul s : getAbility(player, SoulSplit.class).getSouls()) {
				if (s.getLoc().distance(player.getLocation()) < min) {
					min = s.getLoc().distance(player.getLocation());
					soul = s;
				}
			}
			return new SoulSwap(player, soul);
		}
		player.sendMessage(ChatColor.AQUA + "You don't have any soul!");
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("SoulSplit", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("SoulSplit", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("SoulSplit", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("SoulSplit", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("SoulSplit", ClickType.LEFT_CLICK));

		return combination;
	}

	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "2.0";
	}

	@Override
	public void load() {
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSwap.Cooldown", 2000);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
