package chargeTemplate;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;

public class ChargeTemplate extends AirAbility implements AddonAbility {

	private long chargeTime;
	private int abilityState;
	
	public ChargeTemplate(Player player) {
		super(player);
		
		setField();
		//DON'T PUT bPlayer.addCooldown(this); HERE.
		//ADD THAT LINE BEFORE remove() LINES YOU WILL WROTE.
		//(EXCEPT THE remove() I MENTIONED IN STATE 0)
		start();
	}

	public void setField() {
		chargeTime = 2000;
		abilityState = 0;
	}
	
	@Override
	public void progress() {
		
		/* WRITE YOUR CODE HERE */
		//Controls that will remove the move goes here.
		//(Region protection check, ability range check, ability duration check etc.)
		/* WRITE YOUR CODE HERE */
		
		if (abilityState == 0) { //State 0 means ability has started but it is not charged yet.
			if (!player.isSneaking()) {
				remove(); //Don't add cooldown to the ability if it is removed from here.
				return; 
			} else if (System.currentTimeMillis() > getStartTime() + chargeTime) {
				abilityState++;
			} else {
				/* WRITE YOUR CODE HERE */
				//Not charged particle codes goes here.
				/* WRITE YOUR CODE HERE */
			}
		} else if (abilityState == 1) { //State 1 means ability is charged but not released yet.
			if (!player.isSneaking()) {
				abilityState++;
			} else {
				/* WRITE YOUR CODE HERE */
				//Charged particle codes goes here.
				/* WRITE YOUR CODE HERE */
			}
		} else if (abilityState == 2) { //State 2 means ability is launched.
			/* WRITE YOUR CODE HERE */
			//All other codes goes here. (Logic of the launch part.)
			/* WRITE YOUR CODE HERE */
		}
	}
	
	@Override
	public long getCooldown() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHarmlessAbility() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
