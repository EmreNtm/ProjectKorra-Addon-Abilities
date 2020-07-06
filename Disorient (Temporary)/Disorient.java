package hiro3.disorient;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;

import net.md_5.bungee.api.ChatColor;

public class Disorient extends ChiAbility implements AddonAbility, ComboAbility {

	private Listener DL;
	
//	private final int FRONT = 0;
//	private final int BACK = 1;
//	private final int RIGHT = 2;
//	private final int LEFT = 3;
	
	private ArrayList<Integer> frontArray;
	private ArrayList<Integer> rightArray;
	
	private boolean canTakeMoveRequest;
	
	private long cooldown;
	//private double range;
	//private double comboLevelRequirement;
	private long duration;
	
	private Vector targetDirection;
	
	private LivingEntity target;
	
	public Disorient(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (!player.isSneaking()) {
			return;
		}
		
		if (!hasAbility(player, ComboBar.class)) {
			return;
		}
		
		setField();
		//ComboBar cb = getAbility(player, ComboBar.class);
		
		//this.target = getTargetEntity(player, this.range);
		this.target = player;
		/*
		if (target != null && cb.getBarInfo((LivingEntity) target) != null) {
			if (cb.getBarInfo(target).getLevel() >= this.comboLevelRequirement) {
				cb.updateBarInfo(target, -this.comboLevelRequirement);
				
				player.sendMessage(ChatColor.GREEN + "Disorient activated on " + target.getName());
				start();
				return;
			} 
		}
		*/
		
		player.sendMessage(ChatColor.GREEN + "Disorient activated on " + target.getName());
		start();
		//player.sendMessage(ChatColor.GOLD + "Not enough combo percentage.");
	}

	public void setField() {
		//this.comboLevelRequirement = 10;
		//this.range = 2;
		this.duration = 10000;
		
		this.frontArray = new ArrayList<Integer>();
		this.rightArray = new ArrayList<Integer>();
		
		this.canTakeMoveRequest = true;
		this.targetDirection = null;
		
		shuffleMovement();
	}
	
	@Override
	public void progress() {
		
		if (System.currentTimeMillis() > getStartTime() + this.duration) {
			player.sendMessage(ChatColor.GREEN + "Disorient ended on " + target.getName());
			remove();
			return;
		}
		
	}
	
	public void takeMoveRequest(int front, int right) {
		if (!canTakeMoveRequest())
			return;
		
		if (front == 0 && right == 0)
			return;
		
		disableTakingMoveRequest();
		Vector vec = new Vector(0, 0, 0);
		vec.setY(target.getVelocity().getY());
		
		int flag = 0;
		
		if (front == 1) {
			vec.add(returnNewVector(this.frontArray.get(0)));
			if (this.frontArray.get(0) == 1)
				flag = 1;
		} else if (front == -1) {
			vec.add(returnNewVector(this.frontArray.get(1)));
			if (this.frontArray.get(1) == 0)
				flag = 1;
		}
		
		if (right == 1) {
			vec.add(returnNewVector(this.rightArray.get(0)));
			if (this.rightArray.get(0) == 3)
				flag = 1;
		} else if (right == -1) {
			vec.add(returnNewVector(this.rightArray.get(1)));
			if (this.rightArray.get(1) == 2)
				flag = 1;
		}
		
		vec.normalize().multiply(0.5);
		if (flag == 1)
			vec.multiply(2);
		this.targetDirection = vec.clone();
		player.setVelocity(vec);
	}
	
	public void shuffleMovement() {
		ArrayList<Integer> taken = new ArrayList<Integer>();
		int random;
		
		do {
			random = (int) (Math.random() * 4);
		} while (random == 0);
		taken.add(random);
		this.frontArray.add(random);
		
		do {
			random = (int) (Math.random() * 4);
		} while (taken.contains(random) || random == 1);
		taken.add(random);
		this.frontArray.add(random);
		
		do {
			random = (int) (Math.random() * 4);
		} while (taken.contains(random) || random == 2);
		taken.add(random);
		this.rightArray.add(random);
		
		do {
			random = (int) (Math.random() * 4);
		} while (taken.contains(random));
		taken.add(random);
		this.rightArray.add(random);
	}
	
	public Vector returnNewVector(int value) {
		if (value == 0) {
			return target.getLocation().getDirection().setY(0);
		} else if (value == 1) {
			return target.getLocation().getDirection().setY(0).multiply(-1);
		} else if (value == 2) {
			return new Vector(-target.getLocation().getDirection().getZ(), 0, +target.getLocation().getDirection().getX());
		} else {
			return new Vector(+target.getLocation().getDirection().getZ(), 0, -target.getLocation().getDirection().getX());
		}
	}
	
	public void disableTakingMoveRequest() {
		this.canTakeMoveRequest = false;
		
		new BukkitRunnable() {

			int tick = 0;
			
			@Override
			public void run() {
				if (tick == 8) {
					Bukkit.getScheduler().cancelTask(getTaskId());
					setTakeMoveRequest(true);
					return;
				}
				setTakeMoveRequest(false);
				tick++;
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	public Vector getTargetDirection() {
		return this.targetDirection;
	}
	
	public boolean canTakeMoveRequest() {
		return this.canTakeMoveRequest;
	}
	
	public void setTakeMoveRequest(boolean value) {
		this.canTakeMoveRequest = value;
	}
	
	public LivingEntity getTarget()  {
		return this.target;
	}
	
	public LivingEntity getTargetEntity(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.1);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
				if (e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId())) {
					return (LivingEntity) e;
				}
			}
		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()));
		
		return null;
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
		return "Disorient";
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
	public Object createNewComboInstance(Player player) {
		return new Disorient(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("RapidPunch", ClickType.LEFT_CLICK));

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
		DL = new DisorientListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(DL, ProjectKorra.plugin);
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(DL);
		super.remove();
	}

}
