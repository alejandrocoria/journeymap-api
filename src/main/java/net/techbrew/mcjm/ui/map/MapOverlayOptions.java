package net.techbrew.mcjm.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.feature.Feature;
import net.techbrew.mcjm.feature.FeatureManager;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.ui.JmUI;
import net.techbrew.mcjm.ui.MapButton;
import net.techbrew.mcjm.ui.UIManager;
import org.lwjgl.input.Keyboard;

public class MapOverlayOptions extends JmUI {
	
	final String title;
	int lastWidth = 0;
	int lastHeight = 0;
	
	private enum ButtonEnum {Caves,Monsters,Animals,Villagers,Pets,Players,Waypoints,Grid,Webserver,MiniMap,Close};
	MapButton buttonCaves, buttonMonsters, buttonAnimals, buttonVillagers, buttonPets, buttonPlayers, buttonWaypoints, buttonGrid, buttonWebserver, buttonMiniMap, buttonClose;
	
	public MapOverlayOptions() {
		title = Constants.getString("MapOverlay.options");
	}

	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
        this.field_146292_n.clear();
        String on = Constants.getString("MapOverlay.on");
        String off = Constants.getString("MapOverlay.off");
        
   		buttonCaves = new MapButton(ButtonEnum.Caves.ordinal(),0,0,
   				Constants.getString("MapOverlay.show_caves", on),
   				Constants.getString("MapOverlay.show_caves", off),
   				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_CAVES)); //$NON-NLS-1$ 
   		
		buttonClose = new MapButton(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$ 
				
		buttonMonsters = new MapButton(ButtonEnum.Monsters.ordinal(),0,0,
				Constants.getString("MapOverlay.show_monsters", on),
				Constants.getString("MapOverlay.show_monsters", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_MOBS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonAnimals = new MapButton(ButtonEnum.Animals.ordinal(),0,0,
				Constants.getString("MapOverlay.show_animals", on),
				Constants.getString("MapOverlay.show_animals", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_ANIMALS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonVillagers = new MapButton(ButtonEnum.Villagers.ordinal(),0,0,
				Constants.getString("MapOverlay.show_villagers", on),
				Constants.getString("MapOverlay.show_villagers", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_VILLAGERS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPets = new MapButton(ButtonEnum.Pets.ordinal(),0,0,
				Constants.getString("MapOverlay.show_pets", on),
				Constants.getString("MapOverlay.show_pets", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_PETS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPlayers = new MapButton(ButtonEnum.Players.ordinal(),0,0,
				Constants.getString("MapOverlay.show_players", on),
				Constants.getString("MapOverlay.show_players", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_PLAYERS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonWaypoints = new MapButton(ButtonEnum.Waypoints.ordinal(),0,0,
				Constants.getString("MapOverlay.show_waypoints", on),
				Constants.getString("MapOverlay.show_waypoints", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_WAYPOINTS)); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWaypoints.enabled = WaypointHelper.waypointsEnabled();
		
		boolean webserverOn = PropertyManager.getInstance().getBoolean(PropertyManager.Key.WEBSERVER_ENABLED);
		buttonWebserver = new MapButton(ButtonEnum.Webserver.ordinal(),0,0,
				Constants.getString("MapOverlay.enable_webserver", on),
				Constants.getString("MapOverlay.enable_webserver", off),
				webserverOn); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWebserver.setToggled(webserverOn);

        buttonMiniMap = new MapButton(ButtonEnum.MiniMap.ordinal(),0,0,
				Constants.getString("MapOverlay.minimap")); //$NON-NLS-1$ //$NON-NLS-2$

        boolean gridOn = PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_GRID);
        buttonGrid = new MapButton(ButtonEnum.Grid.ordinal(),0,0,
                Constants.getString("MapOverlay.show_grid", on),
                Constants.getString("MapOverlay.show_grid", off),
                gridOn); //$NON-NLS-1$  //$NON-NLS-2$
				
		if(!FeatureManager.isAllowed(Feature.MapCaves)) {
			buttonCaves.setToggled(false);
			buttonCaves.enabled = false;
			buttonCaves.setHoverText(Constants.getString("MapOverlay.disabled_feature")); //$NON-NLS-1$
		}
			
		if(!FeatureManager.isAllowed(Feature.RadarMobs)) {
			buttonMonsters.setToggled(false);
			buttonMonsters.enabled = false;
			buttonMonsters.setHoverText(Constants.getString("MapOverlay.disabled_feature")); //$NON-NLS-1$
		}
			
		if(!FeatureManager.isAllowed(Feature.RadarAnimals)) {
			buttonAnimals.setToggled(false);
			buttonAnimals.enabled = false;
			buttonAnimals.setHoverText(Constants.getString("MapOverlay.disabled_feature")); //$NON-NLS-1$
			
			buttonPets.setToggled(false);
			buttonPets.enabled = false;
			buttonPets.setHoverText(Constants.getString("MapOverlay.disabled_feature")); //$NON-NLS-1$			
		}
		
		if(!FeatureManager.isAllowed(Feature.RadarVillagers)) {
			buttonVillagers.setToggled(false);
			buttonVillagers.enabled = false;
			buttonVillagers.setHoverText(Constants.getString("MapOverlay.disabled_feature")); //$NON-NLS-1$
		}

		if(!FeatureManager.isAllowed(Feature.RadarPlayers)) {
			buttonPlayers.setToggled(false);
			buttonPlayers.enabled = false;
			buttonPlayers.setHoverText(Constants.getString("MapOverlay.disabled_feature")); //$NON-NLS-1$
		}
		
		field_146292_n.add(buttonCaves);
		field_146292_n.add(buttonClose);		
		field_146292_n.add(buttonMonsters);
		field_146292_n.add(buttonAnimals);
		field_146292_n.add(buttonVillagers);
		field_146292_n.add(buttonPets);
		field_146292_n.add(buttonPlayers);
		field_146292_n.add(buttonGrid);
		field_146292_n.add(buttonWaypoints);
		field_146292_n.add(buttonWebserver);
        field_146292_n.add(buttonMiniMap);
    }

    /**
	 * Center buttons in UI.
	 */
	void layoutButtons() {
		// Buttons
		
		if(field_146292_n.isEmpty()) {
			initGui();
		}
		
		if(lastWidth!=field_146294_l || lastHeight!=field_146295_m) {
			
			lastWidth = field_146294_l;
			lastHeight = field_146295_m;
			
			final int hgap = 4;
			final int vgap = 3;
			final int bx = (this.field_146294_l - hgap)/2;
			final int by = this.field_146295_m / 4;
			
			buttonCaves.leftOf(bx).setY(by);
			buttonMonsters.rightOf(buttonCaves, hgap).setY(by);
			
			buttonAnimals.below(buttonCaves, vgap).leftOf(bx);
			buttonVillagers.rightOf(buttonAnimals, hgap).below(buttonMonsters, vgap);

			buttonPets.below(buttonAnimals, vgap).leftOf(bx);
			buttonPlayers.rightOf(buttonPets, hgap).below(buttonVillagers, vgap);

			buttonGrid.below(buttonPets, vgap).leftOf(bx);
			buttonWaypoints.rightOf(buttonGrid, hgap).below(buttonPlayers, vgap);

            buttonMiniMap.below(buttonGrid, vgap).leftOf(bx);
			buttonWebserver.rightOf(buttonMiniMap, vgap).below(buttonWaypoints, vgap);
			
			buttonClose.below(buttonWebserver, vgap*4).centerHorizontalOn(bx);

		}	
	}
	
    @Override
    protected void func_146284_a(GuiButton guibutton) { // actionPerformed
    	
    	final ButtonEnum id = ButtonEnum.values()[guibutton.field_146127_k];
    	switch(id) {
			case Caves: { // caves				
				buttonCaves.setToggled(PropertyManager.toggle(PropertyManager.Key.PREF_SHOW_CAVES));
				boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
				if(underground) {
					MapOverlay.state().requireRefresh();
				}
				break;
			}
			case Close: {
				UIManager.getInstance().openMap();
				break;
			}
			case Monsters: { 
				buttonMonsters.setToggled(PropertyManager.toggle(PropertyManager.Key.PREF_SHOW_MOBS));
				break;
			}
			case Animals: { 
				buttonAnimals.setToggled(PropertyManager.toggle(PropertyManager.Key.PREF_SHOW_ANIMALS));
				break;
			}
			case Villagers: { 
				buttonVillagers.setToggled(PropertyManager.toggle(PropertyManager.Key.PREF_SHOW_VILLAGERS));
				break;
			}
			case Pets: {
				buttonPets.setToggled(PropertyManager.toggle(PropertyManager.Key.PREF_SHOW_PETS));
				break;
			}
			case Players: { 
				buttonPlayers.setToggled(PropertyManager.toggle(PropertyManager.Key.PREF_SHOW_PLAYERS));
				break;
			}
			case Waypoints: { 
				buttonWaypoints.setToggled(PropertyManager.toggle(PropertyManager.Key.PREF_SHOW_WAYPOINTS));
				break;
			}
			case Grid: { 
				buttonGrid.setToggled(PropertyManager.toggle(PropertyManager.Key.PREF_SHOW_GRID));
				MapOverlay.state().requireRefresh();
				break;
			}
			case Webserver: { 
				buttonWebserver.setToggled(PropertyManager.toggle(PropertyManager.Key.WEBSERVER_ENABLED));
				JourneyMap.getInstance().toggleWebserver(buttonWebserver.getToggled(), true);
				break;
			}
            case MiniMap: {
                UIManager.getInstance().openMiniMapOptions();
                break;
            }
		}
	}
    
    @Override
	public void updateScreen() {
		super.updateScreen();
	}

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        func_146270_b(0);
        
        layoutButtons();
        
        super.drawScreen(par1, par2, par3);
        
        int y = this.field_146295_m / 4 - 18;
        drawCenteredString(this.field_146289_q, title , this.field_146294_l / 2, y, 16777215);
    }
    
    @Override
    public void func_146270_b(int layer)
	{
        super.func_146278_c(layer); // super.drawBackground(0);
        MapOverlay.drawMapBackground(this);
        super.func_146270_b(layer); // super.drawDefaultBackground();

        super.drawLogo();
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
