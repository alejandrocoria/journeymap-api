package net.techbrew.mcjm.ui;

import java.awt.Color;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.VersionCheck;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.task.MapRegionTask;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class MapOverlayOptions extends GuiScreen {

	final MapOverlay map;
	final String title;
	int lastWidth = 0;
	int lastHeight = 0;
	MapButton buttonCaves, buttonMonsters, buttonAnimals, buttonVillagers, buttonPets, buttonPlayers, buttonWaypoints, buttonGrid, buttonAutomap;
	MapButton buttonSave,buttonClose,buttonAlert,buttonBrowser;
	Color titleColor = new Color(0,0,100);
	
	public MapOverlayOptions(MapOverlay map) {
		this.map = map;
		title = Constants.getString("MapOverlay.options_title", JourneyMap.JM_VERSION);
	}

	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
        this.buttonList.clear();
        String on = Constants.getString("MapOverlay.on");
        String off = Constants.getString("MapOverlay.off");
        
   		buttonCaves = new MapButton(2,0,0,
   				Constants.getString("MapOverlay.show_caves", on),
   				Constants.getString("MapOverlay.show_caves", off),
   				map.showCaves); //$NON-NLS-1$ 
   		
		buttonSave = new MapButton(6,0,0,Constants.getString("MapOverlay.save_map")); //$NON-NLS-1$ 
		buttonClose = new MapButton(7,0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$ 
		String updateText = VersionCheck.getVersionIsChecked() ? Constants.getString("MapOverlay.update_available") : Constants.getString("MapOverlay.update_check"); //$NON-NLS-1$ //$NON-NLS-2$
		buttonAlert = new MapButton(8,0,0,updateText); //$NON-NLS-1$ 
		buttonAlert.drawButton = !VersionCheck.getVersionIsChecked() || !VersionCheck.getVersionIsCurrent();
		buttonBrowser = new MapButton(9,0,0,Constants.getString("MapOverlay.use_browser")); //$NON-NLS-1$ 
		
		buttonMonsters = new MapButton(10,0,0,
				Constants.getString("MapOverlay.show_monsters", on),
				Constants.getString("MapOverlay.show_monsters", off),
				map.showMonsters); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonAnimals = new MapButton(11,0,0,
				Constants.getString("MapOverlay.show_animals", on),
				Constants.getString("MapOverlay.show_animals", off),
				map.showAnimals); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonVillagers = new MapButton(12,0,0,
				Constants.getString("MapOverlay.show_villagers", on),
				Constants.getString("MapOverlay.show_villagers", off),
				map.showVillagers); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPets = new MapButton(13,0,0,
				Constants.getString("MapOverlay.show_pets", on),
				Constants.getString("MapOverlay.show_pets", off),
				map.showPets); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPlayers = new MapButton(14,0,0,
				Constants.getString("MapOverlay.show_players", on),
				Constants.getString("MapOverlay.show_players", off),
				map.showPlayers); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonWaypoints = new MapButton(15,0,0,
				Constants.getString("MapOverlay.show_waypoints", on),
				Constants.getString("MapOverlay.show_waypoints", off),
				map.showWaypoints); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWaypoints.enabled = WaypointHelper.waypointsEnabled();
		
		buttonGrid = new MapButton(16,0,0,
				Constants.getString("MapOverlay.show_grid", on),
				Constants.getString("MapOverlay.show_grid", off),
				PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_GRID)); //$NON-NLS-1$ //$NON-NLS-2$
		
		buttonAutomap = new MapButton(17,0,0,
				Constants.getString("MapOverlay.automap_title", on),
				Constants.getString("MapOverlay.automap_title", off),
				PropertyManager.getInstance().getBoolean(PropertyManager.Key.AUTOMAP_ENABLED)); //$NON-NLS-1$ //$NON-NLS-2$
		buttonAutomap.setHoverText(Constants.getString("MapOverlay.automap_text")); //$NON-NLS-1$
		buttonAutomap.enabled = Minecraft.getMinecraft().isSingleplayer();
		
		// Check for hardcore
		if(map.hardcore) {
			buttonCaves.setToggled(false);
			buttonCaves.enabled = false;
			buttonCaves.setHoverText(Constants.getString("MapOverlay.disabled_in_hardcore")); //$NON-NLS-1$
			
			buttonMonsters.setToggled(false);
			buttonMonsters.enabled = false;
			buttonMonsters.setHoverText(Constants.getString("MapOverlay.disabled_in_hardcore")); //$NON-NLS-1$
			
			buttonAnimals.setToggled(false);
			buttonAnimals.enabled = false;
			buttonAnimals.setHoverText(Constants.getString("MapOverlay.disabled_in_hardcore")); //$NON-NLS-1$
			
			buttonVillagers.setToggled(false);
			buttonVillagers.enabled = false;
			buttonVillagers.setHoverText(Constants.getString("MapOverlay.disabled_in_hardcore")); //$NON-NLS-1$
			
			buttonPets.setToggled(false);
			buttonPets.enabled = false;
			buttonPets.setHoverText(Constants.getString("MapOverlay.disabled_in_hardcore")); //$NON-NLS-1$
			
			buttonPlayers.setToggled(false);
			buttonPlayers.enabled = false;
			buttonPlayers.setHoverText(Constants.getString("MapOverlay.disabled_in_hardcore")); //$NON-NLS-1$
		}
		
		buttonList.add(buttonCaves);
		buttonList.add(buttonSave);
		buttonList.add(buttonClose);
		if(buttonAlert.drawButton) {
			buttonList.add(buttonAlert);
		}
		buttonList.add(buttonBrowser);
		buttonList.add(buttonMonsters);
		buttonList.add(buttonAnimals);
		buttonList.add(buttonVillagers);
		buttonList.add(buttonPets);
		buttonList.add(buttonPlayers);
		buttonList.add(buttonGrid);
		buttonList.add(buttonWaypoints);
		buttonList.add(buttonAutomap);
    }
    
    /**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		
		if(buttonList.isEmpty()) {
			initGui();
		}
		
		if(lastWidth!=width || lastHeight!=height) {
			
			lastWidth = width;
			lastHeight = height;
			
			int hgap = 160;
			int bx = this.width / 2 - hgap + 5;
			int by = (this.height / 4);
			int row = 0;
			
			layoutButton(buttonCaves, bx, by + (20*row));			
			layoutButton(buttonMonsters, bx + hgap, by + (20*row++));

			layoutButton(buttonAnimals, bx, by + (20*row));			
			layoutButton(buttonVillagers, bx + hgap, by + (20*row++));
			
			layoutButton(buttonPets, bx, by + (20*row));			
			layoutButton(buttonPlayers, bx + hgap, by + (20*row++));	
			
			layoutButton(buttonGrid, bx, by + (20*row));			
			layoutButton(buttonWaypoints, bx + hgap, by + (20*row++));	

			layoutButton(buttonAutomap, bx, by + (20*row));	
			layoutButton(buttonSave, bx + hgap, by + (20*row++));	
								
			if(buttonAlert.drawButton) {
				layoutButton(buttonAlert, bx + hgap/2, by + (20*row++));
			}
			
			layoutButton(buttonBrowser, bx + hgap/2, by + (20*row++));	
			layoutButton(buttonClose, bx + hgap/2, by + (20*row++));	
			
			
		}
		
	}
	
	private void layoutButton(GuiButton guibutton, int x, int y) {
		guibutton.xPosition = x;
		guibutton.yPosition = y;
	}


    @Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id) {
			case 2: { // caves
				MapOverlay.setShowCaves(!MapOverlay.showCaves);
				buttonCaves.setToggled(MapOverlay.showCaves);	
				boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
				if(underground) {
					map.refreshState();
				}
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_CAVES, MapOverlay.showCaves);
				break;
			}
			case 6: { // save
				map.save();
				break;
			}
			case 7: { // close
				close();
				break;
			}
			case 8: { // alert
				map.launchWebsite();
				break;
			}
			case 9: { // browser
				map.launchLocalhost();
				break;
			}
			case 10: { // monsters
				MapOverlay.showMonsters = !MapOverlay.showMonsters;
				buttonMonsters.setToggled(MapOverlay.showMonsters);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_MOBS, MapOverlay.showMonsters);
				break;
			}
			case 11: { // animals
				MapOverlay.showAnimals = !MapOverlay.showAnimals;
				buttonAnimals.setToggled(MapOverlay.showAnimals);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_ANIMALS, MapOverlay.showAnimals);
				break;
			}
			case 12: { // villagers
				MapOverlay.showVillagers = !MapOverlay.showVillagers;
				buttonVillagers.setToggled(MapOverlay.showVillagers);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_VILLAGERS, MapOverlay.showVillagers);
				break;
			}
			case 13: { // pets
				MapOverlay.showPets = !MapOverlay.showPets;
				buttonPets.setToggled(MapOverlay.showPets);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_PETS, MapOverlay.showPets);
				break;
			}
			case 14: { // players
				MapOverlay.showPlayers = !MapOverlay.showPlayers;
				buttonPlayers.setToggled(MapOverlay.showPlayers);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_PLAYERS, MapOverlay.showPlayers);
				break;
			}
			case 15: { // waypoints
				MapOverlay.showWaypoints = !MapOverlay.showWaypoints;
				buttonWaypoints.setToggled(MapOverlay.showWaypoints);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_WAYPOINTS, MapOverlay.showWaypoints);
				break;
			}
			case 16: { // grid
				boolean showGrid = !PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_GRID);
				buttonGrid.setToggled(showGrid);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_GRID, showGrid);
				map.refreshState();
				break;
			}
			case 17: { // automap
				boolean enable = !PropertyManager.getInstance().getBoolean(PropertyManager.Key.AUTOMAP_ENABLED);
				buttonAutomap.setToggled(enable);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.AUTOMAP_ENABLED, enable);
				JourneyMap.getInstance().toggleTask(MapRegionTask.Manager.class, enable);
				if(enable) {
					close();
					map.close();
				}
				break;
			}
		}
	}
    
    void close() {
		map.options = null;
		map.updateScreen();
	}
    
    @Override
	public void updateScreen() {
		super.updateScreen();
		//layoutButtons();
	}

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {        
        drawBackground(0);
        
        layoutButtons();
        
        super.drawScreen(par1, par2, par3);

        drawTitle();
        
    }
    
    @Override
	public void drawBackground(int layer)
	{    	
    	
	}
    
    private void drawTitle() {
    	int labelWidth = mc.fontRenderer.getStringWidth(title) + 10;
		int halfBg = width/2;
		
		int by = (this.height / 4);
		
		GL11.glEnable(GL11.GL_BLEND);
		if(map.coreRenderer!=null) {
			BaseOverlayRenderer.drawRectangle(halfBg - (labelWidth/2), by-20, labelWidth, 12, titleColor, 255);
		}
		map.drawCenteredString(this.fontRenderer, title , this.width / 2, by-18, 16777215);
    }
    
    @Override
	protected void keyTyped(char c, int i)
	{
		switch(i) {
		case Keyboard.KEY_ESCAPE : {
			close();
			break;
		}
		}
	}

}
