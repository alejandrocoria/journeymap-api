/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;


import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import journeymap.client.Constants;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.dialog.OptionsManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;
import java.util.Map;

/**
 * Keybinding handler.
 */
public class KeyEventHandler implements EventHandlerManager.EventHandler
{
    public static String CATEGORY_ALL;
    public static String CATEGORY_FULLMAP;
    public static KeyBinding KB_FULLSCREEN;
    public static KeyBinding KB_MAP_ZOOMIN;
    public static KeyBinding KB_MAP_ZOOMOUT;
    public static KeyBinding KB_MAP_TOGGLE_TYPE;
    public static KeyBinding KB_MINIMAP_PRESET;
    public static KeyBinding KB_MINIMAP_TOGGLE;
    public static KeyBinding KB_CREATE_WAYPOINT;
    public static KeyBinding KB_WAYPOINT_MANAGER;
    public static KeyBinding KB_FULLMAP_OPTIONS_MANAGER;
    public static KeyBinding KB_FULLMAP_ACTIONS_MANAGER;
    public static KeyBinding KB_FULLMAP_PAN_NORTH;
    public static KeyBinding KB_FULLMAP_PAN_SOUTH;
    public static KeyBinding KB_FULLMAP_PAN_EAST;
    public static KeyBinding KB_FULLMAP_PAN_WEST;

    private static final Table<Integer, KeyBinding, Runnable> minimapKeymappings = createKeyMappingTable();
    private static final Table<Integer, KeyBinding, Runnable> gameKeymappings = createKeyMappingTable();
    private static final Table<Integer, KeyBinding, Runnable> guiKeymappings = createKeyMappingTable();
    private Minecraft mc = FMLClientHandler.instance().getClient();

    /**
     * Create a table of keycodes mapped to keybindings to actions.  Sorted so bindings with modifiers are first.
     *
     * @return table
     */
    public static Table<Integer, KeyBinding, Runnable> createKeyMappingTable() {
        return TreeBasedTable.create(Comparator.naturalOrder(),
                Comparator.comparingInt((KeyBinding keyBinding) -> keyBinding.getKeyModifier().ordinal()));
    }

    public KeyEventHandler() {
        CATEGORY_ALL = Constants.getString("jm.common.hotkeys_keybinding_category");
        CATEGORY_FULLMAP = Constants.getString("jm.common.hotkeys_keybinding_fullscreen_category");

        // Active in-game and Fullscreen
        KB_MAP_ZOOMIN = register("key.journeymap.zoom_in", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_EQUALS, CATEGORY_ALL);
        KB_MAP_ZOOMOUT = register("key.journeymap.zoom_out", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_MINUS, CATEGORY_ALL);
        KB_MAP_TOGGLE_TYPE = register("key.journeymap.minimap_type", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_LBRACKET, CATEGORY_ALL);
        KB_MINIMAP_PRESET = register("key.journeymap.minimap_preset", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_BACKSLASH, CATEGORY_ALL);
        KB_CREATE_WAYPOINT = register("key.journeymap.create_waypoint", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_B, CATEGORY_ALL);
        KB_FULLSCREEN = register("key.journeymap.map_toggle_alt", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_J, CATEGORY_ALL);

        // Active in-game or Fullscreen, but shouldn't be treated as conflicts because they have modifiers.
        KB_MINIMAP_TOGGLE = register("key.journeymap.minimap_toggle_alt", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Keyboard.KEY_J, CATEGORY_ALL);
        KB_WAYPOINT_MANAGER = register("key.journeymap.fullscreen_waypoints", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Keyboard.KEY_B, CATEGORY_ALL);

        // Active only in Fullscreen
        KB_FULLMAP_PAN_NORTH = register("key.journeymap.fullscreen.north", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_UP, CATEGORY_FULLMAP);
        KB_FULLMAP_PAN_SOUTH = register("key.journeymap.fullscreen.south", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_DOWN, CATEGORY_FULLMAP);
        KB_FULLMAP_PAN_EAST = register("key.journeymap.fullscreen.east", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_RIGHT, CATEGORY_FULLMAP);
        KB_FULLMAP_PAN_WEST = register("key.journeymap.fullscreen.west", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_LEFT, CATEGORY_FULLMAP);
        KB_FULLMAP_OPTIONS_MANAGER = register("key.journeymap.fullscreen_options", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_O, CATEGORY_FULLMAP);
        KB_FULLMAP_ACTIONS_MANAGER = register("key.journeymap.fullscreen_actions", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_A, CATEGORY_FULLMAP);

        // Minimap keymappings (in-game and used for minimap preview by options manager)
        setKeymap(minimapKeymappings, KB_MINIMAP_TOGGLE, UIManager.INSTANCE::toggleMinimap);
        setKeymap(minimapKeymappings, KB_MAP_ZOOMIN, () -> MiniMap.state().zoomIn());
        setKeymap(minimapKeymappings, KB_MAP_ZOOMOUT, () -> MiniMap.state().zoomOut());
        setKeymap(minimapKeymappings, KB_MAP_TOGGLE_TYPE, () -> MiniMap.state().toggleMapType());
        setKeymap(minimapKeymappings, KB_MINIMAP_PRESET, UIManager.INSTANCE::switchMiniMapPreset);

        // In-game keymappings
        setKeymap(gameKeymappings, KB_WAYPOINT_MANAGER, () -> UIManager.INSTANCE.openWaypointManager(null, null));
        setKeymap(gameKeymappings, KB_CREATE_WAYPOINT, () -> {
            UIManager.INSTANCE.openWaypointEditor(Waypoint.of(mc.player), true, null);
        });
        setKeymap(gameKeymappings, KB_FULLSCREEN, UIManager.INSTANCE::openFullscreenMap);

        // Fullscreen keymappings
        setKeymap(guiKeymappings, KB_FULLSCREEN, UIManager.INSTANCE::closeAll);
        setKeymap(guiKeymappings, KB_MAP_ZOOMIN, () -> getFullscreen().zoomIn());
        setKeymap(guiKeymappings, KB_MAP_ZOOMOUT, () -> getFullscreen().zoomOut());
        setKeymap(guiKeymappings, KB_MAP_TOGGLE_TYPE, () -> getFullscreen().toggleMapType());
        setKeymap(guiKeymappings, KB_CREATE_WAYPOINT, () -> getFullscreen().createWaypointAtMouse());
        setKeymap(guiKeymappings, KB_WAYPOINT_MANAGER, () -> UIManager.INSTANCE.openWaypointManager(null, getFullscreen()));
        setKeymap(guiKeymappings, KB_FULLMAP_OPTIONS_MANAGER, () -> UIManager.INSTANCE.openOptionsManager(getFullscreen()));
        setKeymap(guiKeymappings, KB_FULLMAP_ACTIONS_MANAGER, UIManager.INSTANCE::openMapActions);
        setKeymap(guiKeymappings, KB_FULLMAP_PAN_NORTH, () -> getFullscreen().moveCanvas(0, -16));
        setKeymap(guiKeymappings, KB_FULLMAP_PAN_WEST, () -> getFullscreen().moveCanvas(-16, -0));
        setKeymap(guiKeymappings, KB_FULLMAP_PAN_SOUTH, () -> getFullscreen().moveCanvas(0, 16));
        setKeymap(guiKeymappings, KB_FULLMAP_PAN_EAST, () -> getFullscreen().moveCanvas(16, 0));
    }

    private KeyBinding register(String description, net.minecraftforge.client.settings.IKeyConflictContext keyConflictContext, net.minecraftforge.client.settings.KeyModifier keyModifier, int keyCode, String category) {
        KeyBinding kb = new KeyBinding(description, keyConflictContext, keyModifier, keyCode, category);
        try {
            ClientRegistry.registerKeyBinding(kb);
        } catch (Throwable t) {
            ChatLog.announceError("Unexpected error when registering keybinding : " + kb);
        }
        return kb;
    }

    private void setKeymap(Table<Integer, KeyBinding, Runnable> table, KeyBinding keybinding, Runnable action) {
        table.put(keybinding.getKeyCode(), keybinding, action);
    }

    private Fullscreen getFullscreen() {
        return UIManager.INSTANCE.openFullscreenMap();
    }

    /**
     * On keyboard event in-game
     *
     * @param event the event
     */
    @SubscribeEvent()
    public void onGameKeyboardEvent(InputEvent.KeyInputEvent event) {
        int key = Keyboard.getEventKey();
        if (Keyboard.isKeyDown(key)) {
            return;
        }

        if (onKey(minimapKeymappings, key)) {
            return;
        }

        onKey(gameKeymappings, key);
    }

    @SubscribeEvent()
    public void onGuiKeyboardEvent(GuiScreenEvent.KeyboardInputEvent.Post event) {
        int key = Keyboard.getEventKey();
        if (Keyboard.isKeyDown(key)) {
            return;
        }

        if (inOptionsManager()) {
            // Minimap Preview
            onKey(minimapKeymappings, key);
            return;
        }

        if (inFullscreen() && !getFullscreen().isChatOpen()) {
            onKey(guiKeymappings, key);
        }
    }

    /**
     * Check keybindings for a match.
     *
     * @param key keycode
     * @return true if one of the bindings was used.
     */
    public static boolean onMinimapPreviewKeyboardEvent(final int key) {
//        if(!Keyboard.isKeyDown(key))
//        {
//            return onKey(minimapKeymappings, key);
//        }
        return false;
    }

    /**
     * Check keybindings in a mappings table for a match.
     *
     * @param table keymappings
     * @param key   key code
     * @return true if action triggered
     */
    private static boolean onKey(Table<Integer, KeyBinding, Runnable> table, final int key) {
        // Check keymap for assigned action
        if (table.containsRow(key)) {
            for (Map.Entry<KeyBinding, Runnable> entry : table.row(key).entrySet()) {
                if (entry.getKey().isActiveAndMatches(key)) {
                    entry.getValue().run();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean inGame() {
        return mc.currentScreen == null;
    }

    private boolean inFullscreen() {
        return mc.currentScreen instanceof Fullscreen;
    }

    private boolean inOptionsManager() {
        return mc.currentScreen instanceof OptionsManager;
    }

}

