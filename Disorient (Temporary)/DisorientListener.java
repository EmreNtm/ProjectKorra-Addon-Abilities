package hiro3.disorient;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.CoreAbility;

public class DisorientListener implements Listener {

	private int front;
	private int right;
	
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (!event.getPlayer().isOnGround())
			return;
		
		int flag = 0;
		Disorient disorient = null;
		for (Disorient d : CoreAbility.getAbilities(Disorient.class)) {
			if (d.getTarget().getUniqueId().equals(event.getPlayer().getUniqueId())) {
				flag = 1;
				disorient = d;
			}
		}
		
		if (flag == 0) {
			return;
		}

		if (!disorient.canTakeMoveRequest()) {
			if (disorient.getTargetDirection() != null) {
				Location from = event.getFrom();
				Location to = event.getTo();
				Vector moveDirection = to.clone().toVector().subtract(from.clone().toVector()).normalize();
				moveDirection.setY(0);
				double frontAngle = Math.acos( disorient.getTargetDirection().dot(moveDirection)  /  (disorient.getTargetDirection().length() * moveDirection.length()) );
				if (!Double.isNaN(frontAngle) && Math.abs(frontAngle) >= 0.5) {
					disorient.disableTakingMoveRequest();
					//event.setCancelled(true);
					event.getPlayer().setVelocity(disorient.getTargetDirection().clone().multiply(0.75));
				}
			}
			return;
		}
		
		Player player = event.getPlayer();
		Vector playerDirection = player.getLocation().getDirection();
		playerDirection.setY(0);
		
		Vector playerRightDirection = new Vector(-playerDirection.getZ(), 0, +playerDirection.getX());
		
		Location from = event.getFrom();
		Location to = event.getTo();
		
		Vector moveDirection = to.clone().toVector().subtract(from.clone().toVector()).normalize();
		moveDirection.setY(0);
		
        double frontAngle = Math.acos( playerDirection.dot(moveDirection)  /  (playerDirection.length() * moveDirection.length()) );
        double rightAngle = Math.acos( playerRightDirection.dot(moveDirection)  /  (playerRightDirection.length() * moveDirection.length()) );
        //Will be near 0 while moving forward.
        frontAngle = Math.toDegrees(frontAngle);
        //Will be near 0 while moving right.
        rightAngle = Math.toDegrees(rightAngle);
        
        if (!Double.isNaN(frontAngle) && !Double.isNaN(rightAngle)) {
			player.teleport(from);
		}
        
        setMovingDirections(frontAngle, rightAngle);
        
        //event.setCancelled(true);
        disorient.takeMoveRequest(front, right);
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (event.getPlayer().isSneaking())
			return;
		
		int flag = 0;
		for (Disorient d : CoreAbility.getAbilities(Disorient.class)) {
			if (d.getTarget().getUniqueId().equals(event.getPlayer().getUniqueId())) {
				flag = 1;
			}
		}
		
		if (flag == 0) {
			return;
		}
		
		if (!event.getPlayer().isOnGround())
			return;
		event.getPlayer().setVelocity(event.getPlayer().getVelocity().setY(0.5));
	}
	
	public void setMovingDirections(double frontAngle, double rightAngle) {
		if (Double.isNaN(frontAngle) || Double.isNaN(rightAngle)) {
			this.front = 0;
			this.right = 0;
			return;
		}
		
		if (Math.abs(frontAngle) < 60)
			this.front = 1;
		else if (Math.abs(frontAngle) > 120)
			this.front = -1;
		else
			this.front = 0;
		
		if (Math.abs(rightAngle) < 60)
			this.right = 1;
		else if (Math.abs(rightAngle) > 120)
			this.right = -1;
		else
			this.right = 0;
	}
	
}
