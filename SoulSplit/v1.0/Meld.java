package Hiro3;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;

import me.xnuminousx.spirits.ability.api.SpiritAbility;

public class Meld extends SpiritAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	
	public Meld(Player player) {
		super(player);
		
		if (!this.bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		setField();
		bPlayer.addCooldown(this);
		start();
	}

	public void setField() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.Meld.Cooldown");
	}
	
	@Override
	public void progress() {
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
		return "Meld";
	}

	@Override
	public String getDescription() {
		return "Make your soul pieces invisible for other players.";
	}

	@Override
	public String getInstructions() {
		return "SoulSplit (Tap Sneak) -> SoulSplit (Tap Sneak) -> Phase (Left Click)";
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
	  	if (CoreAbility.hasAbility(player, SoulSplit.class) && !CoreAbility.hasAbility(player, Meld.class)) {
	  		if (CoreAbility.getAbility(player, SoulSplit.class).getSoulNo() != 0) {
	  			for (Soul s : CoreAbility.getAbility(player, SoulSplit.class).getSouls()) {
	  				s.setIsMelding(true);
	  			}
	  			return new Meld(player);
	  		}
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
		combination.add(new AbilityInformation("Vanish", ClickType.LEFT_CLICK));

		return combination;
	}

	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void load() {
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.Meld.Cooldown", 2000);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
