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
                JourneyMap.getInstance().fullMapProperties.isShowCaves()); //$NON-NLS-1$
   		
		buttonClose = new Button(ButtonEnum.Close.ordinal(),0,0,Constants.getString("MapOverlay.close")); //$NON-NLS-1$
				
		buttonMonsters = new Button(ButtonEnum.Monsters.ordinal(),0,0,
				Constants.getString("MapOverlay.show_monsters", on),
				Constants.getString("MapOverlay.show_monsters", off),
                JourneyMap.getInstance().fullMapProperties.isShowMobs()); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonAnimals = new Button(ButtonEnum.Animals.ordinal(),0,0,
				Constants.getString("MapOverlay.show_animals", on),
				Constants.getString("MapOverlay.show_animals", off),
                JourneyMap.getInstance().fullMapProperties.isShowAnimals()); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonVillagers = new Button(ButtonEnum.Villagers.ordinal(),0,0,
				Constants.getString("MapOverlay.show_villagers", on),
				Constants.getString("MapOverlay.show_villagers", off),
                JourneyMap.getInstance().fullMapProperties.isShowVillagers()); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPets = new Button(ButtonEnum.Pets.ordinal(),0,0,
				Constants.getString("MapOverlay.show_pets", on),
				Constants.getString("MapOverlay.show_pets", off),
                JourneyMap.getInstance().fullMapProperties.isShowPets()); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonPlayers = new Button(ButtonEnum.Players.ordinal(),0,0,
				Constants.getString("MapOverlay.show_players", on),
				Constants.getString("MapOverlay.show_players", off),
                JourneyMap.getInstance().fullMapProperties.isShowPlayers()); //$NON-NLS-1$  //$NON-NLS-2$
		
		buttonWaypoints = new Button(ButtonEnum.Waypoints.ordinal(),0,0,
				Constants.getString("MapOverlay.show_waypoints", on),
				Constants.getString("MapOverlay.show_waypoints", off),
                JourneyMap.getInstance().fullMapProperties.isShowWaypoints()); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWaypoints.enabled = WaypointsData.isAnyEnabled();

		boolean webserverOn = JourneyMap.getInstance().webMapProperties.isEnabled();
		buttonWebserver = new Button(ButtonEnum.Webserver.ordinal(),0,0,
				Constants.getString("MapOverlay.enable_webserver", on),
				Constants.getString("MapOverlay.enable_webserver", off),
				webserverOn); //$NON-NLS-1$  //$NON-NLS-2$
		buttonWebserver.setToggled(webserverOn);

        buttonMiniMap = new Button(ButtonEnum.MiniMap.ordinal(),0,0,
                Constants.getString("MapOverlay.minimap")); //$NON-NLS-1$ //$NON-NLS-2$

        buttonKeyboardHelp = new Button(ButtonEnum.KeyboardHelp.ordinal(), 0, 0,
                Constants.getString("MapOverlay.hotkeys_button"));

        boolean gridOn = JourneyMap.getInstance().fullMapProperties.isShowGrid();
        buttonGrid = new Button(ButtonEnum.Grid.ordinal(),0,0,
                Constants.getString("MapOverlay.show_grid", on),
                Constants.getString("MapOverlay.show_grid", off),
                gridOn); //$NON-NLS-1$  //$NON-NLS-2$

        double mapFontScale = JourneyMap.getInstance().fullMapProperties.getFontScale();
        buttonFont = new Button(ButtonEnum.Font.ordinal(), 0, 0,
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_small")),
                Constants.getString("MiniMap.font", Constants.getString("MiniMap.font_large")),
                (mapFontScale==1));

        boolean forceUnicode = JourneyMap.getInstance().fullMapProperties.isForceUnicode();
        buttonUnicode = new Button(ButtonEnum.Unicode.ordinal(), 0, 0,
                Constants.getString("MiniMap.force_unicode", on),
                Constants.getString("MiniMap.force_unicode", off), forceUnicode);

        boolean nativeWaypointManagement = JourneyMap.getInstance().configProperties.isWaypointManagementEnabled();
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
				buttonCaves.setToggled(JourneyMap.getInstance().fullMapProperties.toggleShowCaves());
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
				buttonMonsters.setToggled(JourneyMap.getInstance().fullMapProperties.toggleShowMobs());
				break;
			}
			case Animals: { 
				buttonAnimals.setToggled(JourneyMap.getInstance().fullMapProperties.toggleShowAnimals());
				break;
			}
			case Villagers: { 
				buttonVillagers.setToggled(JourneyMap.getInstance().fullMapProperties.toggleShowVillagers());
				break;
			}
			case Pets: {
				buttonPets.setToggled(JourneyMap.getInstance().fullMapProperties.toggleShowPets());
				break;
			}
			case Players: { 
				buttonPlayers.setToggled(JourneyMap.getInstance().fullMapProperties.toggleShowPlayers());
				break;
			}
			case Waypoints: { 
				buttonWaypoints.setToggled(JourneyMap.getInstance().fullMapProperties.toggleShowWaypoints());
				break;
			}
			case Grid: { 
				buttonGrid.setToggled(JourneyMap.getInstance().fullMapProperties.toggleShowGrid());
				MapOverlay.state().requireRefresh();
				break;
			}
			case Webserver: { 
				buttonWebserver.setToggled(JourneyMap.getInstance().webMapProperties.toggleEnabled());
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
                double newScale = (JourneyMap.getInstance().fullMapProperties.getFontScale()==1) ? 2 : 1;
                JourneyMap.getInstance().fullMapProperties.setFontScale(newScale);
                buttonFont.setToggled(newScale==1);
                break;
            }
            case Unicode: {
                buttonUnicode.setToggled(JourneyMap.getInstance().fullMapProperties.toggleForceUnicode());
                break;
            }
            case WaypointsManager: {
                boolean nativeWaypointManagement = !JourneyMap.getInstance().configProperties.isWaypointManagementEnabled();
                buttonWaypointsManager.setToggled(nativeWaypointManagement);
                JourneyMap.getInstance().configProperties.setWaypointManagementEnabled(nativeWaypointManagement);
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
