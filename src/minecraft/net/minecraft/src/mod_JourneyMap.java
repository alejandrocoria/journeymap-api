package net.minecraft.src;


import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.src.Minecraft;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.KeyBinding;
import net.techbrew.mcjm.JourneyMap;

public class mod_JourneyMap extends BaseMod {
	
	boolean loadSuccess = false;
	
	public mod_JourneyMap() {		
		super();
	}

	@Override
	public String getName()
    { 
        return "JourneyMap"; //$NON-NLS-1$
    }
	
	@Override
	public String getVersion() {
		return JourneyMap.JM_VERSION;
	}

	@Override
	public void load() 
	{				
		String version = "?";
		try {
			version = JourneyMap.JM_VERSION + " for Minecraft " + JourneyMap.MC_VERSION;
			JourneyMap instance = JourneyMap.getInstance();
			instance.initialize(Minecraft.getMinecraft());
			
			// Register hooks
			ModLoader.setInGameHook(this, true, false);
			ModLoader.setInGUIHook(this, true, false);
			
			// Register Map GUI keybinding
			if(instance.enableMapGui) {
				ModLoader.registerKey(this, instance.keybinding, false);
			}
			
			loadSuccess = true;
		} catch(Throwable t) {
			throw new RuntimeException("Could not load JourneyMap " + version + ". Ensure compatible Minecraft/Modloader/Forge versions.", t);
		}
	}
	
	@Override
	public void modsLoaded() {
		if(!loadSuccess) return;
		StringBuffer sb = new StringBuffer("ModLoader mods loaded: ");
		Iterator<BaseMod> iter = new ArrayList<BaseMod>(ModLoader.getLoadedMods()).iterator();
		while(iter.hasNext()) {
			sb.append(iter.next().toString());
			if(iter.hasNext()){
				sb.append(", ");
			}
		}
		
		JourneyMap.getLogger().info(sb.toString());		
	}
	
	@Override
	public void clientConnect(NetClientHandler clientHandler) {
		
	}

	@Override
    public void clientDisconnect(NetClientHandler clientHandler) {
		if(!loadSuccess) return;
		JourneyMap.getInstance().stopMapping();
	}
	
	@Override
	public boolean onTickInGUI(float tick, Minecraft game, GuiScreen gui) {
		if(!loadSuccess) return false;
//		if(!(game.entityRenderer instanceof EntityRendererProxy)) {
//			JourneyMap.getLogger().warning("ModLoader didn't set EntityRendererProxy.  Doing so manually.");
//			game.entityRenderer = new EntityRendererProxy(game);
//		}
		return JourneyMap.getInstance().onTickInGUI(tick, game, gui);
	}
	
	@Override
	public boolean onTickInGame(float f, final Minecraft minecraft) {
		if(!loadSuccess) return false;
		return JourneyMap.getInstance().onTickInGame(f, minecraft);
	}
	
	@Override
	public void keyboardEvent(KeyBinding keybinding)
	{
		JourneyMap.getInstance().keyboardEvent(keybinding);
	}
	
}


