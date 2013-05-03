package net.minecraft.src;


import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;

public class mod_JourneyMap extends BaseMod {
	
	JourneyMap instance;
	
	public mod_JourneyMap() {		
		super();
	}

	@Override
	public String getVersion() {
		return JourneyMap.JM_VERSION;
	}

	@Override
	public void load() 
	{				
		instance = new JourneyMap();
		
		ModLoader.setInGameHook(this, true, false);
		ModLoader.setInGUIHook(this, true, false);
		
		// Map GUI keycode
		int mapGuiKeyCode = PropertyManager.getInstance().getInteger(PropertyManager.Key.MAPGUI_KEYCODE);
		instance.enableMapGui = PropertyManager.getInstance().getBoolean(PropertyManager.Key.MAPGUI_ENABLED); 
		if(instance.enableMapGui) {
			instance.keybinding = new KeyBinding("JourneyMap", mapGuiKeyCode); //$NON-NLS-1$
			ModLoader.registerKey(this, instance.keybinding, false);
		}
	}
	
	@Override
	public boolean onTickInGUI(float f, Minecraft minecraft, GuiScreen guiscreen) {
		return instance.onTickInGUI(f, minecraft, guiscreen);
	}
	
	@Override
	public boolean onTickInGame(float f, final Minecraft minecraft) {
		return instance.onTickInGame(f, minecraft);
	}
	
	@Override
	public void keyboardEvent(KeyBinding keybinding)
	{
		instance.keyboardEvent(keybinding);
	}

}
