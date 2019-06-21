package Hiro3;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class EarthMoleListener implements Listener {

	@EventHandler
	public void onClick(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled() || bPlayer == null) {
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
		if (player.isSneaking())
			if (bPlayer.getBoundAbilityName().equalsIgnoreCase("EarthMole")) {
				if (CoreAbility.getAbility(player, EarthMole.class) == null)
					new EarthMole(player);
			}
		}
		}.runTaskLater(ProjectKorra.plugin, 1);
	}

}
