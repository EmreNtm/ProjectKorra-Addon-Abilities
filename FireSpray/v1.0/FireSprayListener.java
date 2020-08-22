package me.hiro3.firespray;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.firebending.combo.JetBlast;
import com.projectkorra.projectkorra.firebending.combo.JetBlaze;

public class FireSprayListener implements Listener {
	
	@EventHandler
	public void onClick(PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (CoreAbility.hasAbility(player, FireJet.class)
				|| CoreAbility.hasAbility(player, JetBlast.class)
				|| CoreAbility.hasAbility(player, JetBlaze.class))
			return;
		
		if (event.isCancelled() || bPlayer == null) {
			return;
		}	else if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		} else if (bPlayer.getBoundAbilityName().equalsIgnoreCase("FireSpray")) {
			if (CoreAbility.hasAbility(player, FireSpray.class) && player.isSneaking()) {
				FireSpray fs = CoreAbility.getAbility(player, FireSpray.class);
				fs.setTick(fs.getMaximumTick());
				return;
			}
			new FireSpray(player);
		}
	}

	@EventHandler
	public void onAbilityStart(AbilityStartEvent event) {
		Player player = event.getAbility().getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled() || bPlayer == null)
			return;
		
		if (CoreAbility.hasAbility(player, FireSpray.class)) {
			FireSpray fs = CoreAbility.getAbility(player, FireSpray.class);
			if (fs.getTick() < fs.getMaximumTick())
				event.setCancelled(true);
		}
	}
	
	@SuppressWarnings("static-access")
	@EventHandler
	public void onBlueFireRequest(AsyncPlayerChatEvent  event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (event.isCancelled() || bPlayer == null || !bPlayer.hasElement(Element.FIRE))
			return;
		
		if (event.getMessage().equalsIgnoreCase("@BlueSpray on")) {
			((FireSpray) CoreAbility.getAbility(FireSpray.class)).blueFirePlayers.add(player.getName());
			player.sendMessage(ChatColor.BLUE + "Your FireSpray is blue.");
			event.setCancelled(true);
		} else if (event.getMessage().equalsIgnoreCase("@BlueSpray off")) {
			((FireSpray) CoreAbility.getAbility(FireSpray.class)).blueFirePlayers.remove(player.getName());
			player.sendMessage(ChatColor.BLUE + "Your FireSpray is no longer blue.");
			event.setCancelled(true);
		}
	}
	
}
