package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.EntityKey;
import net.techbrew.journeymap.data.PlayerData;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.waypoint.WaypointHelper;
import org.lwjgl.input.Keyboard;

public class MapOverlayOptions extends JmUI {

	int lastWidth = 0;
	int lastHeight = 0;
	
	private enum ButtonEnum {Caves,Monsters,Animals,Villagers,Pets,Players,Waypoints,Grid,Webserver,MiniMap, KeyboardHelp, Close};

	Button buttonCaves, buttonMonsters, buttonAnimals, buttonVillagers, buttonPets, buttonPlayers, buttonWaypoints, buttonGrid, buttonWebserver, buttonMiniMap, buttonKeyboardHelp, buttonClose;
	
	public MapOverlayOptions() {
		super(Constants.getString("MapOverlay.options"));
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
        
   		buttonCaves = new Button(ButtonEnum.Caves.ordinal(),0,0,
   				Constants.getString("MapOverlay.show_caves", on),
   				Constants.getString("MapOverlay.show_caves", off),
   				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_CAVES)); //$NON-NLS-1$ 
   		
		buttonClose = new Button(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$
				
		buttonMonsters = new Button(ButtonEnum.Monsters.ordinal(),0,0,
				Constants.getString("MapOverlay.show_monsters", on),
				Constants.getString("MapOverlay.show_monsters", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_MOBS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonAnimals = new Button(ButtonEnum.Animals.ordinal(),0,0,
				Constants.getString("MapOverlay.show_animals", on),
				Constants.getString("MapOverlay.show_animals", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_ANIMALS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonVillagers = new Button(ButtonEnum.Villagers.ordinal(),0,0,
				Constants.getString("MapOverlay.show_villagers", on),
				Constants.getString("MapOverlay.show_villagers", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_VILLAGERS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPets = new Button(ButtonEnum.Pets.ordinal(),0,0,
				Constants.getString("MapOverlay.show_pets", on),
				Constants.getString("MapOverlay.show_pets", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_PETS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPlayers = new Button(ButtonEnum.Players.ordinal(),0,0,
				Constants.getString("MapOverlay.show_players", on),
				Constants.getString("MapOverlay.show_players", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_PLAYERS)); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonWaypoints = new Button(ButtonEnum.Waypoints.ordinal(),0,0,
				Constants.getString("MapOverlay.show_waypoints", on),
				Constants.getString("MapOverlay.show_waypoints", off),
				PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_WAYPOINTS)); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWaypoints.enabled = WaypointHelper.waypointsEnabled();

		boolean webserverOn = PropertyManager.getInstance().getBoolean(PropertyManager.Key.WEBSERVER_ENABLED);
		buttonWebserver = new Button(ButtonEnum.Webserver.ordinal(),0,0,
				Constants.getString("MapOverlay.enable_webserver", on),
				Constants.getString("MapOverlay.enable_webserver", off),
				webserverOn); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWebserver.setToggled(webserverOn);

        buttonMiniMap = new Button(ButtonEnum.MiniMap.ordinal(),0,0,
                Constants.getString("MapOverlay.minimap")); //$NON-NLS-1$ //$NON-NLS-2$

        buttonKeyboardHelp = new Button(ButtonEnum.KeyboardHelp.ordinal(), 0, 0,
                Constants.getString("MapOverlay.hotkeys_button"));

        boolean gridOn = PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_GRID);
        buttonGrid = new Button(ButtonEnum.Grid.ordinal(),0,0,
                Constants.getString("MapOverlay.show_grid", on),
                Constants.getString("MapOverlay.show_grid", off),
                gridOn); //$NON-NLS-1$  //$NON-NLS-2$
				
		if(!FeatureManager.isAllowed(Feature.MapCaves)) {
			buttonCaves.setToggled(false);
			buttonCaves.enabled = false;
		}
			
		if(!FeatureManager.isAllowed(Feature.RadarMobs)) {
			buttonMonsters.setToggled(false);
			buttonMonsters.enabled = false;
		}
			
		if(!FeatureManager.isAllowed(Feature.RadarAnimals)) {
			buttonAnimals.setToggled(false);
			buttonAnimals.enabled = false;
			buttonPets.setToggled(false);
			buttonPets.enabled = false;
		}
		
		if(!FeatureManager.isAllowed(Feature.RadarVillagers)) {
			buttonVillagers.setToggled(false);
			buttonVillagers.enabled = false;
		}

		if(!FeatureManager.isAllowed(Feature.RadarPlayers)) {
			buttonPlayers.setToggled(false);
			buttonPlayers.enabled = false;
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
        buttonList.add(buttonKeyboardHelp);
        buttonList.add(buttonMiniMap);
    }

    /**
	 * Center buttons in UI.
	 */
    @Override
    protected void layoutButtons() {
		// Buttons
		
		if(buttonList.isEmpty()) {
			initGui();
		}
		
		if(lastWidth!=width || lastHeight!=height) {
			
			lastWidth = width;
			lastHeight = height;
			
			final int hgap = 4;
			final int vgap = 3;
			final int bx = (this.width - hgap)/2;
			final int by = this.height / 4;
			
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

            buttonKeyboardHelp.below(buttonWebserver, vgap).centerHorizontalOn(bx);

			buttonClose.below(buttonKeyboardHelp, vgap).centerHorizontalOn(bx);

		}	
	}
	
    @Override
    protected void actionPerformed(GuiButton guibutton) { // actionPerformed
    	
    	final ButtonEnum id = ButtonEnum.values()[guibutton.id];
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
            case KeyboardHelp: {
                UIManager.getInstance().openMapHotkeyHelp();
                break;
            }
		}
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

}
