package me.hiro3.terrasense;

import java.util.List;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

public class MyPacketAdapter extends PacketAdapter {

	public MyPacketAdapter(Plugin plugin, Iterable<? extends PacketType> types) {
		super(plugin, types);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacket().getMetadata("TerraSense") != null) {
			return;
		}
		
		if (!TerraSense.senseMap.containsKey(event.getPlayer()))
			return;
		
		if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
			int flag = 0;
			List<WrappedWatchableObject> watchableObjectList = event.getPacket().getWatchableCollectionModifier().read(0);
			for (WrappedWatchableObject wwo : watchableObjectList) {
				if (wwo.getIndex() == 0) {
					flag = 1;
					break;
				}
			}
			if (flag == 0)
				return;
		} else {
			WrappedDataWatcher watcher = event.getPacket().getDataWatcherModifier().read(0);
            if (!watcher.hasIndex(0)) {
                return;
            }
		}
		
		UtilityMethods.refreshAllTerraSenses();
	}

}
