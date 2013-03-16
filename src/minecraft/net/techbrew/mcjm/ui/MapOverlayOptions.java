package net.techbrew.mcjm.ui;

import net.minecraft.src.GuiAchievements;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiGameOver;
import net.minecraft.src.GuiIngameMenu;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiOptions;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiShareToLan;
import net.minecraft.src.GuiStats;
import net.minecraft.src.StatCollector;
import net.minecraft.src.StatList;
import net.minecraft.src.WorldClient;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.VersionCheck;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.FileHandler;

public class MapOverlayOptions extends GuiScreen {

	final MapOverlay map;
	int lastWidth = 0;
	int lastHeight = 0;
	MapButton buttonCaves, buttonMonsters, buttonAnimals, buttonVillagers, buttonPets, buttonPlayers;
	MapButton buttonSave,buttonClose,buttonAlert,buttonBrowser;
	
	public MapOverlayOptions(MapOverlay map) {
		super();
		this.map = map;
	}

	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.controlList.clear();
        String on = Constants.getString("MapOverlay.on");
        String off = Constants.getString("MapOverlay.off");
        
   		buttonCaves = new MapButton(2,0,0,
   				Constants.getString("MapOverlay.show_caves", on),
   				Constants.getString("MapOverlay.show_caves", off),
   				map.showCaves); //$NON-NLS-1$ 
   		
		buttonSave = new MapButton(6,0,0,Constants.getString("MapOverlay.save_map")); //$NON-NLS-1$ 
		buttonClose = new MapButton(7,0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$ 
		buttonAlert = new MapButton(8,0,0,Constants.getString("MapOverlay.update_available")); //$NON-NLS-1$ 
		buttonAlert.drawButton = VersionCheck.getVersionIsChecked() && !VersionCheck.getVersionIsCurrent();
		buttonBrowser = new MapButton(9,0,0,Constants.getString("MapOverlay.use_browser")); //$NON-NLS-1$ 
		
		buttonMonsters = new MapButton(10,0,0,
				Constants.getString("MapOverlay.show_monsters", on),
				Constants.getString("MapOverlay.show_monsters", off),
				map.showMonsters); //$NON-NLS-1$ 
		
		buttonAnimals = new MapButton(11,0,0,
				Constants.getString("MapOverlay.show_animals", on),
				Constants.getString("MapOverlay.show_animals", off),
				map.showAnimals); //$NON-NLS-1$ 
		
		buttonVillagers = new MapButton(12,0,0,
				Constants.getString("MapOverlay.show_villagers", on),
				Constants.getString("MapOverlay.show_villagers", off),
				map.showVillagers); //$NON-NLS-1$ 
		
		buttonPets = new MapButton(13,0,0,
				Constants.getString("MapOverlay.show_pets", on),
				Constants.getString("MapOverlay.show_pets", off),
				map.showPets); //$NON-NLS-1$ 
		
		buttonPlayers = new MapButton(14,0,0,
				Constants.getString("MapOverlay.show_players", on),
				Constants.getString("MapOverlay.show_players", off),
				map.showPlayers); //$NON-NLS-1$ 
		
		// Check for hardcore
		if(map.hardcore) {
			buttonCaves.setToggled(false);
			buttonCaves.enabled = false;
			buttonCaves.setHoverText(Constants.getString("MapOverlay.disabled_in_hardcore")); //$NON-NLS-1$
			buttonMonsters.setToggled(false);
			buttonMonsters.enabled  =false;
			buttonMonsters.setHoverText(Constants.getString("MapOverlay.disabled_in_hardcore")); //$NON-NLS-1$
		}
		
		controlList.add(buttonCaves);
		controlList.add(buttonSave);
		controlList.add(buttonClose);
		if(buttonAlert.drawButton) {
			controlList.add(buttonAlert);
		}
		controlList.add(buttonBrowser);
		controlList.add(buttonMonsters);
		controlList.add(buttonAnimals);
		controlList.add(buttonVillagers);
		controlList.add(buttonPets);
		controlList.add(buttonPlayers);
    }
    
    /**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		
		if(controlList.isEmpty()) {
			initGui();
		}
		
		if(lastWidth!=width || lastHeight!=height) {
			
			lastWidth = width;
			lastHeight = height;
			
			int hgap = 160;
			int bx = this.width / 2 - hgap;
			int by = (this.height / 4);
			int row = 0;

			if(buttonAlert.drawButton) {
				layoutButton(buttonAlert, bx + hgap/2, by + (20*row++));
			}
			
			layoutButton(buttonCaves, bx, by + (20*row));			
			layoutButton(buttonMonsters, bx + hgap, by + (20*row++));

			layoutButton(buttonAnimals, bx, by + (20*row));			
			layoutButton(buttonVillagers, bx + hgap, by + (20*row++));
			
			layoutButton(buttonPets, bx, by + (20*row));			
			layoutButton(buttonPlayers, bx + hgap, by + (20*row++));	
			
			row++;
			layoutButton(buttonSave, bx, by + (20*row));			
			layoutButton(buttonBrowser, bx + hgap, by + (20*row++));
						
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
					map.eraseCachedMapImg();
				}
				break;
			}
			case 3: { // follow
				map.setFollow(!map.follow);
				if(map.follow) map.eraseCachedEntityImg();
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
				MapOverlay.launchWebsite();
				break;
			}
			case 9: { // browser
				MapOverlay.launchLocalhost();
				break;
			}
			case 10: { // monsters
				MapOverlay.showMonsters = !MapOverlay.showMonsters;
				buttonMonsters.setToggled(MapOverlay.showMonsters);
				break;
			}
			case 11: { // animals
				MapOverlay.showAnimals = !MapOverlay.showAnimals;
				buttonAnimals.setToggled(MapOverlay.showAnimals);
				break;
			}
			case 12: { // villagers
				MapOverlay.showVillagers = !MapOverlay.showVillagers;
				buttonVillagers.setToggled(MapOverlay.showVillagers);
				break;
			}
			case 13: { // pets
				MapOverlay.showPets = !MapOverlay.showPets;
				buttonPets.setToggled(MapOverlay.showPets);
				break;
			}
			case 14: { // players
				MapOverlay.showPlayers = !MapOverlay.showPlayers;
				buttonPlayers.setToggled(MapOverlay.showPlayers);
				break;
			}
		}
	}
    
    void close() {
		mc.displayGuiScreen(map);
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
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, Constants.getString("MapOverlay.options_title"), this.width / 2, 40, 16777215);
        layoutButtons();
        super.drawScreen(par1, par2, par3);
    }

}
