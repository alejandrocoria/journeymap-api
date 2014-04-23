package net.techbrew.journeymap.ui.map;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.EntityKey;
import net.techbrew.journeymap.data.PlayerData;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.lwjgl.input.Keyboard;

public class MapOverlayOptions extends JmUI {

	int lastWidth = 0;
	int lastHeight = 0;
	
	private enum ButtonEnum {Caves,Monsters,Animals,Villagers,Pets,Players,Waypoints,WaypointsManager, Grid,Webserver,MiniMap,Font,Unicode, KeyboardHelp, Close};

	Button buttonCaves, buttonMonsters, buttonAnimals, buttonVillagers, buttonPets, buttonPlayers, buttonFont, buttonUnicode, buttonWaypoints;
    Button buttonGrid, buttonWebserver, buttonMiniMap, buttonKeyboardHelp, buttonWaypointsManager, buttonClose;

    ButtonList leftButtons;
    ButtonList rightButtons;
	
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
		buttonWaypoints.enabled = WaypointsData.isAnyEnabled();

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

        double mapFontScale = PropertyManager.getDoubleProp(PropertyManager.Key.PREF_FONTSCALE);
        MapOverlay.state().setMapFontScale(mapFontScale);
        buttonFont = new Button(ButtonEnum.Font.ordinal(), 0, 0,
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_small")),
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_large")),
                (mapFontScale==1));

        boolean forceUnicode = PropertyManager.getBooleanProp(PropertyManager.Key.PREF_FORCEUNICODE);
        buttonUnicode = new Button(ButtonEnum.Unicode.ordinal(), 0, 0,
                Constants.getString("MiniMap.force_unicode", on),
                Constants.getString("MiniMap.force_unicode", off), forceUnicode);
        MapOverlay.state().mapForceUnicode = forceUnicode;

        boolean nativeWaypointManagement = PropertyManager.getBooleanProp(PropertyManager.Key.NATIVE_WAYPOINTS_ENABLED);
        buttonWaypointsManager = new Button(ButtonEnum.WaypointsManager.ordinal(), 0, 0,
                Constants.getString("MapOverlay.waypoint_manager", on),
                Constants.getString("MapOverlay.waypoint_manager", off), nativeWaypointManagement);
				
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
        buttonList.add(buttonFont);
        buttonList.add(buttonUnicode);
        buttonList.add(buttonWaypointsManager);

        leftButtons = new ButtonList(buttonCaves, buttonAnimals, buttonPets, buttonGrid, buttonFont, buttonMiniMap, buttonWaypointsManager);
        rightButtons = new ButtonList(buttonMonsters, buttonVillagers, buttonPlayers, buttonWaypoints, buttonUnicode, buttonKeyboardHelp, buttonWebserver);

        ButtonList.equalizeWidths(getFontRenderer(), buttonList);
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
			final int bx = (this.width)/2;
			final int by = Math.max(30, this.height/6);

			leftButtons.layoutVertical(bx - (hgap/2), by, false, vgap);
            rightButtons.layoutVertical(bx + (hgap/2), by, true, vgap);

			buttonClose.below(leftButtons, vgap).centerHorizontalOn(bx);

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
            case Font: {
                double newScale = (PropertyManager.getDoubleProp(PropertyManager.Key.PREF_FONTSCALE)==1) ? 2 : 1;
                MapOverlay.state().setMapFontScale(newScale);
                PropertyManager.set(PropertyManager.Key.PREF_FONTSCALE, newScale);
                buttonFont.setToggled(newScale==1);
                break;
            }
            case Unicode: {
                boolean forceUnicode = !PropertyManager.getBooleanProp(PropertyManager.Key.PREF_FORCEUNICODE);
                buttonUnicode.setToggled(forceUnicode);
                PropertyManager.set(PropertyManager.Key.PREF_FORCEUNICODE, forceUnicode);
                MapOverlay.state().mapForceUnicode = forceUnicode;
                break;
            }
            case WaypointsManager: {
                boolean nativeWaypointManagement = !PropertyManager.getBooleanProp(PropertyManager.Key.NATIVE_WAYPOINTS_ENABLED);
                buttonWaypointsManager.setToggled(nativeWaypointManagement);
                PropertyManager.set(PropertyManager.Key.NATIVE_WAYPOINTS_ENABLED, nativeWaypointManagement);
                buttonWaypoints.enabled = WaypointsData.isAnyEnabled();
                if(buttonWaypoints.enabled)
                {
                    WaypointStore.instance().load();
                }
                else
                {
                    WaypointStore.instance().clear();
                }
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
