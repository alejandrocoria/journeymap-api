package net.techbrew.mcjm.ui.dialog;

import java.awt.Color;

import net.minecraft.src.GuiButton;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.ui.JmUI;
import net.techbrew.mcjm.ui.MapButton;
import net.techbrew.mcjm.ui.MapOverlay;
import net.techbrew.mcjm.ui.UIManager;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class MapOverlayOptions extends JmUI {
	
	final String title;
	int lastWidth = 0;
	int lastHeight = 0;
	
	private enum ButtonEnum {Caves,Monsters,Animals,Villagers,Pets,Players,Waypoints,Grid,Webserver,Close};	
	MapButton buttonCaves, buttonMonsters, buttonAnimals, buttonVillagers, buttonPets, buttonPlayers, buttonWaypoints, buttonGrid, buttonWebserver, buttonClose;
	Color titleColor = new Color(0,0,100);
	
	public MapOverlayOptions() {
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
        
   		buttonCaves = new MapButton(ButtonEnum.Caves.ordinal(),0,0,
   				Constants.getString("MapOverlay.show_caves", on),
   				Constants.getString("MapOverlay.show_caves", off),
   				MapOverlay.showCaves); //$NON-NLS-1$ 
   		
		buttonClose = new MapButton(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$ 
				
		buttonMonsters = new MapButton(ButtonEnum.Monsters.ordinal(),0,0,
				Constants.getString("MapOverlay.show_monsters", on),
				Constants.getString("MapOverlay.show_monsters", off),
				MapOverlay.showMonsters); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonAnimals = new MapButton(ButtonEnum.Animals.ordinal(),0,0,
				Constants.getString("MapOverlay.show_animals", on),
				Constants.getString("MapOverlay.show_animals", off),
				MapOverlay.showAnimals); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonVillagers = new MapButton(ButtonEnum.Villagers.ordinal(),0,0,
				Constants.getString("MapOverlay.show_villagers", on),
				Constants.getString("MapOverlay.show_villagers", off),
				MapOverlay.showVillagers); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPets = new MapButton(ButtonEnum.Pets.ordinal(),0,0,
				Constants.getString("MapOverlay.show_pets", on),
				Constants.getString("MapOverlay.show_pets", off),
				MapOverlay.showPets); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPlayers = new MapButton(ButtonEnum.Players.ordinal(),0,0,
				Constants.getString("MapOverlay.show_players", on),
				Constants.getString("MapOverlay.show_players", off),
				MapOverlay.showPlayers); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonWaypoints = new MapButton(ButtonEnum.Waypoints.ordinal(),0,0,
				Constants.getString("MapOverlay.show_waypoints", on),
				Constants.getString("MapOverlay.show_waypoints", off),
				MapOverlay.showWaypoints); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWaypoints.enabled = WaypointHelper.waypointsEnabled();
		
		boolean webserverOn = PropertyManager.getInstance().getBoolean(PropertyManager.Key.WEBSERVER_ENABLED);
		buttonWebserver = new MapButton(ButtonEnum.Webserver.ordinal(),0,0,
				Constants.getString("MapOverlay.enable_webserver", on),
				Constants.getString("MapOverlay.enable_webserver", off),
				webserverOn); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWebserver.setToggled(webserverOn);
		
		buttonGrid = new MapButton(ButtonEnum.Grid.ordinal(),0,0,
				Constants.getString("MapOverlay.show_grid", on),
				Constants.getString("MapOverlay.show_grid", off),
				PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_GRID)); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Check for hardcore
		if(MapOverlay.hardcore) {
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
		buttonList.add(buttonClose);		
		buttonList.add(buttonMonsters);
		buttonList.add(buttonAnimals);
		buttonList.add(buttonVillagers);
		buttonList.add(buttonPets);
		buttonList.add(buttonPlayers);
		buttonList.add(buttonGrid);
		buttonList.add(buttonWaypoints);
		buttonList.add(buttonWebserver);
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
			
			final int hgap = 4;
			final int vgap = 3;
			final int bx = (this.width / 2) - (buttonCaves.getWidth() - hgap/2);
			final int by = this.height / 4;
			
			buttonCaves.setPosition(bx, by);
			buttonMonsters.rightOf(buttonCaves, hgap).yPosition=by;
			
			buttonAnimals.below(buttonCaves, vgap).xPosition=bx;
			buttonVillagers.rightOf(buttonAnimals, hgap).below(buttonMonsters, vgap);

			buttonPets.below(buttonAnimals, vgap).xPosition=bx;
			buttonPlayers.rightOf(buttonPets, hgap).below(buttonVillagers, vgap);

			buttonGrid.below(buttonPets, vgap).xPosition=bx;
			buttonWaypoints.rightOf(buttonGrid, hgap).below(buttonPlayers, vgap);
			
			buttonWebserver.below(buttonGrid, vgap).centerHorizontalOn(this.width / 2);
			
			buttonClose.below(buttonWebserver, vgap*2).centerHorizontalOn(this.width / 2);

		}	
	}
	
    @Override
	protected void actionPerformed(GuiButton guibutton) {
    	
    	final ButtonEnum id = ButtonEnum.values()[guibutton.id];
    	switch(id) {
			case Caves: { // caves
				MapOverlay.toggleShowCaves();				
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_CAVES, MapOverlay.showCaves);
				buttonCaves.setToggled(MapOverlay.showCaves);
				break;
			}
			case Close: {
				UIManager.getInstance().openMap();
				break;
			}
			case Monsters: { 
				MapOverlay.showMonsters = !MapOverlay.showMonsters;
				buttonMonsters.setToggled(MapOverlay.showMonsters);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_MOBS, MapOverlay.showMonsters);
				break;
			}
			case Animals: { 
				MapOverlay.showAnimals = !MapOverlay.showAnimals;
				buttonAnimals.setToggled(MapOverlay.showAnimals);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_ANIMALS, MapOverlay.showAnimals);
				break;
			}
			case Villagers: { 
				MapOverlay.showVillagers = !MapOverlay.showVillagers;
				buttonVillagers.setToggled(MapOverlay.showVillagers);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_VILLAGERS, MapOverlay.showVillagers);
				break;
			}
			case Pets: {
				MapOverlay.showPets = !MapOverlay.showPets;
				buttonPets.setToggled(MapOverlay.showPets);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_PETS, MapOverlay.showPets);
				break;
			}
			case Players: { 
				MapOverlay.showPlayers = !MapOverlay.showPlayers;
				buttonPlayers.setToggled(MapOverlay.showPlayers);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_PLAYERS, MapOverlay.showPlayers);
				break;
			}
			case Waypoints: { 
				MapOverlay.showWaypoints = !MapOverlay.showWaypoints;
				buttonWaypoints.setToggled(MapOverlay.showWaypoints);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_WAYPOINTS, MapOverlay.showWaypoints);
				break;
			}
			case Grid: { 
				boolean showGrid = !PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_GRID);
				buttonGrid.setToggled(showGrid);
				PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_GRID, showGrid);
				MapOverlay.lastRefresh = 0;
				break;
			}
			case Webserver: { 
				boolean enableWebserver = !PropertyManager.getInstance().getBoolean(PropertyManager.Key.WEBSERVER_ENABLED);
				buttonWebserver.setToggled(enableWebserver);
				JourneyMap.getInstance().toggleWebserver(enableWebserver, true);
				break;
			}

		}
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
    	super.drawBackground(0);
    	MapOverlay.drawMapBackground(this);
    	super.drawDefaultBackground();
	}
    
    private void drawTitle() {
    	int labelWidth = mc.fontRenderer.getStringWidth(title) + 10;
		int halfBg = width/2;
		
		int by = (this.height / 4);
		
		GL11.glEnable(GL11.GL_BLEND);
		BaseOverlayRenderer.drawRectangle(halfBg - (labelWidth/2), by-20, labelWidth, 12, titleColor, 255);
		drawCenteredString(this.fontRenderer, title , this.width / 2, by-18, 16777215);
    }
    
    @Override
	protected void keyTyped(char c, int i)
	{
		switch(i) {
		case Keyboard.KEY_ESCAPE : {
			UIManager.getInstance().openMap();
			break;
		}
		}
	}
    
    @Override
	public void close() {	
	}

}
