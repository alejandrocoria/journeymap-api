package net.minecraft.src;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.src.ModLoader;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;

public class mod_JourneyMap extends BaseMod {
	
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
		JourneyMap instance = JourneyMap.getInstance();
		instance.initialize(Minecraft.getMinecraft());
		
		// Register hooks
		ModLoader.setInGameHook(this, true, false);
		ModLoader.setInGUIHook(this, true, false);
		
		// Register Map GUI keybinding
		if(instance.enableMapGui) {
			ModLoader.registerKey(this, instance.keybinding, false);
		}
	}
	
	@Override
	public void modsLoaded() {
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
		JourneyMap.getInstance().stopMapping();
	}
	
	@Override
	public boolean onTickInGUI(float f, Minecraft minecraft, GuiScreen guiscreen) {
		Minecraft mc = Minecraft.getMinecraft();	
		if(!(mc.entityRenderer instanceof EntityRendererProxy)) {
			JourneyMap.getLogger().warning("ModLoader didn't set EntityRendererProxy.  Doing so manually.");
			mc.entityRenderer = new EntityRendererProxy(mc);
		}
		return JourneyMap.getInstance().onTickInGUI(f, minecraft, guiscreen);
	}
	
	@Override
	public boolean onTickInGame(float f, final Minecraft minecraft) {
		return JourneyMap.getInstance().onTickInGame(f, minecraft);
	}
	
	@Override
	public void keyboardEvent(KeyBinding keybinding)
	{
		JourneyMap.getInstance().keyboardEvent(keybinding);
	}

}
