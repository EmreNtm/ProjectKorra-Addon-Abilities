package hiro3.disorient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ComboBar extends ChiAbility implements AddonAbility, PassiveAbility {

	private final double TICK = 20;
	
	private Listener CBL;
	
	private double defaultCapacity;
	private double defaultDecreaseRate;
	private double defaultIncreaseRate;
	
	private HashMap<UUID, BarInfo> barInfos;
	private ArrayList<UUID> tempKeys;
	
	private double max;
	private BarInfo willBeShown;
	private BarInfo lastShown;
	
	double markRadius;
	double markHeight;
	int markSign;
	int markPhase;
	double markAngle;
	double markRAngle;
	
	public ComboBar(Player player) {
		super(player);
		setField();
	}

	public void setField() {
		this.defaultCapacity = 100;
		this.defaultDecreaseRate = 0.5/TICK;
		this.defaultIncreaseRate = 5;
		
		this.barInfos = new HashMap<UUID, BarInfo>();
		this.tempKeys = new ArrayList<UUID>();
		
		this.lastShown = null;
	}
	
	@Override
	public void progress() {
		max = 0;
		willBeShown = null;
		for (BarInfo bi : barInfos.values()) {
			
			if (bi.getLevel() > max) {
				this.willBeShown = bi;
				max = bi.getLevel();
			}
			
			bi.updateLevel(-this.defaultDecreaseRate);
			if (bi.getLevel() <= 0 || bi.getTargetEntity().isDead()) {
				tempKeys.add(bi.getTargetEntity().getUniqueId());
			}
		}
		
		if (willBeShown != null) {
			if (lastShown == null || !lastShown.equals(willBeShown)) {
				setMarkField(willBeShown.getTargetEntity());
			}
			displayBarInfo(willBeShown);
			if (willBeShown.getLevel() > 10)
				displayMark(willBeShown.getTargetEntity());
			this.lastShown = willBeShown;
		}
		
		for (UUID key : tempKeys) {
			this.barInfos.remove(key);
		}
		tempKeys.clear();
		
	}
	
	public void setMarkField(LivingEntity e) {
		markRadius = 0.75;
		markHeight = Math.random() * 2;
		markSign = Math.random() < 0.5 ? 1 : -1;
		markPhase = Math.random() < 0.5 ? (int) (Math.random() * 5 - 10) : (int) (Math.random() * 5 + 5);
		markAngle = 0;
	}
	
	public void displayMark(LivingEntity e) {
		if (e.getPotionEffect(PotionEffectType.INVISIBILITY) != null)
			return;
		
		if (!e.getWorld().equals(player.getWorld()))
			return;
		
		markRAngle = Math.toRadians(markAngle);
		Location loc = e.getLocation().clone();
		loc.add(markRadius * Math.cos(markRAngle), markHeight, markRadius * Math.sin(markRAngle));
		player.spawnParticle(Particle.REDSTONE, loc, 0, -0.1, 0.9, 0);
		loc.subtract(markRadius * Math.cos(markRAngle), markHeight, markRadius * Math.sin(markRAngle));
		
		if (markHeight > 2) {
			markSign = -1;
		} else if (markHeight <= 0) {
			markSign = 1;
		}
		markHeight += (2.0 / 40) * markSign;
		markAngle += markPhase;
	}
	
	public void displayBarInfo(BarInfo bi) {
		if (bi.getTargetEntity().isDead()) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GOLD + "Dead (" + bi.getTargetEntity().getName() + ")"));
			return;
		}
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GOLD + "% " 
				+ String.format("%.2f", bi.getLevel()) + " (" + bi.getTargetEntity().getName() + ")"));
	}
	
	public void createBarInfo(LivingEntity target) {
		if (!hasBarInfo(target.getUniqueId())) {
			this.barInfos.put(target.getUniqueId(), new BarInfo(target, this.defaultCapacity));
		}
	}
	
	public void updateBarInfo(LivingEntity target, double amount) {
		this.barInfos.get(target.getUniqueId()).updateLevel(amount);
	}
	
	public void updateBarInfo(LivingEntity target) {
		this.barInfos.get(target.getUniqueId()).updateLevel(this.defaultIncreaseRate);
	}
	
	public void removeBarInfo(LivingEntity target) {
		this.barInfos.remove(target.getUniqueId());
	}
	
	public boolean hasBarInfo(UUID key) {
		return this.barInfos.containsKey(key);
	}
	
	public BarInfo getBarInfo(LivingEntity target) {
		if (hasBarInfo(target.getUniqueId())) {
			return this.barInfos.get(target.getUniqueId());
		}
		return null;
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
		return "ComboBar";
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
		return "Use your abilities to fill the bar. Use this bar to make unique combos.";
	}
	
	@Override
	public String getInstructions() {
		return "Stand on an earthbendable block. Sneak to sense nearby.";
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
		CBL = new ComboBarListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(CBL, ProjectKorra.plugin);
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(CBL);
		super.remove();
	}

}
