package hiro3.disorient;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;

public class ComboBarListener implements Listener {
	
	@EventHandler
	public void onAbilityDamage(AbilityDamageEntityEvent e) {
		
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		
		LivingEntity target = (LivingEntity) e.getEntity();
		
		if (!e.getAbility().getElement().equals(Element.CHI)) {
			return;
		}
		
		Player player = e.getSource();
		
		if (!CoreAbility.hasAbility(player, ComboBar.class)) {
			return;
		}
		
		ComboBar cb = CoreAbility.getAbility(player, ComboBar.class);
		if (!cb.hasBarInfo(target.getUniqueId())) {
			cb.createBarInfo(target);
		}
		cb.updateBarInfo(target);
		
	}

}
