/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import journeymap.client.Constants;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.dialog.OptionsManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keybinding handler.  Singleton enum to provide access to keybindings in current state.
 * This class uses a number of workaround to allow users to have keybindings that Minecraft
 * says conflict, but that don't.  Like J and CTRL-J.
 */
@ParametersAreNonnullByDefault
public enum KeyEventHandler implements EventHandlerManager.EventHandler
{
    INSTANCE;

    /**
     * Zoom in current map
     */
    public KeyBinding kbMapZoomin;

    /**
     * Zoom out current map
     */
    public KeyBinding kbMapZoomout;

    /**
     * Toggle current map type
     */
    public KeyBinding kbMapToggleType;

    /**
     * Create a waypoint in game
     */
    public KeyBinding kbCreateWaypoint;

    /**
     * Create a waypoint in fullscreen
     */
    public KeyBinding kbFullscreenCreateWaypoint;

    /**
     * Toggle fullscreen map
     */
    public KeyBinding kbFullscreenToggle;

    /**
     * Open waypoint manager
     */
    public KeyBinding kbWaypointManager;

    /**
     * Show/hide minimap
     */
    public KeyBinding kbMinimapToggle;

    /**
     * Switch minimap preset
     */
    public KeyBinding kbMinimapPreset;

    /**
     * Open Options Manager
     */
    public KeyBinding kbFullmapOptionsManager;

    /**
     * Open Map Actions
     */
    public KeyBinding kbFullmapActionsManager;

    /**
     * Pan fullscreen map north
     */
    public KeyBinding kbFullmapPanNorth;

    /**
     * Pan fullscreen map south
     */
    public KeyBinding kbFullmapPanSouth;

    /**
     * Pan fullscreen map east
     */
    public KeyBinding kbFullmapPanEast;

    /**
     * Pan fullscreen map west
     */
    public KeyBinding kbFullmapPanWest;

    /**
     * Comparator sorts KBA's by KeyModifier order - which means Keybindings with KeyModifier.NONE go last.
     */
    private Comparator<KeyBindingAction> kbaComparator = Comparator.comparingInt(KeyBindingAction::order);

    /**
     * Keybindings and actions for just minimap preview in Options Manager.
     */
    private final ListMultimap<Integer, KeyBindingAction> minimapPreviewActions = MultimapBuilder.hashKeys().arrayListValues(2).build();

    /**
     * Keybindings and actions when in-game.
     */
    private final ListMultimap<Integer, KeyBindingAction> inGameActions = MultimapBuilder.hashKeys().arrayListValues(2).build();

    /**
     * Keybindings and actions when in GUI
     */
    private final ListMultimap<Integer, KeyBindingAction> inGuiActions = MultimapBuilder.hashKeys().arrayListValues(2).build();

    /**
     * Minecraft client.
     */
    private Minecraft mc = FMLClientHandler.instance().getClient();

    /**
     * A keybinding has changed, re-sorting the list to ensure those with
     * modifiers are checked first.
     */
    private boolean sortActionsNeeded = true;

    private Logger logger = Journeymap.getLogger();

    /**
     * Defines and registers all keybindings and their actions.
     */
    KeyEventHandler()
    {
        // Zoom in current map
        kbMapZoomin = register("key.journeymap.zoom_in", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_EQUALS);
        setAction(minimapPreviewActions, kbMapZoomin, () -> MiniMap.state().zoomIn());
        setAction(inGuiActions, kbMapZoomin, () -> getFullscreen().zoomIn());

        // Zoom out current map
        kbMapZoomout = register("key.journeymap.zoom_out", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_MINUS);
        setAction(minimapPreviewActions, kbMapZoomout, () -> MiniMap.state().zoomOut());
        setAction(inGuiActions, kbMapZoomout, () -> getFullscreen().zoomOut());

        // Toggle current map type
        kbMapToggleType = register("key.journeymap.minimap_type", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_LBRACKET);
        setAction(minimapPreviewActions, kbMapToggleType, () -> MiniMap.state().toggleMapType());
        setAction(inGuiActions, kbMapToggleType, () -> getFullscreen().toggleMapType());

        // All minimap preview actions are also used in-game
        inGameActions.putAll(minimapPreviewActions);

        // Create a waypoint in game
        kbCreateWaypoint = register("key.journeymap.create_waypoint", KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_B);
        setAction(inGameActions, kbCreateWaypoint, () -> {
            UIManager.INSTANCE.openWaypointEditor(Waypoint.of(mc.player), true, null);
        });

        // Create a waypoint in fullscreen
        kbFullscreenCreateWaypoint = register("key.journeymap.fullscreen_create_waypoint", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_B);
        setAction(inGuiActions, kbFullscreenCreateWaypoint, () -> getFullscreen().createWaypointAtMouse());

        // Toggle fullscreen map
        kbFullscreenToggle = register("key.journeymap.map_toggle_alt", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_J);
        setAction(inGameActions, kbFullscreenToggle, UIManager.INSTANCE::openFullscreenMap);
        setAction(inGuiActions, kbFullscreenToggle, UIManager.INSTANCE::closeAll);

        // Open waypoint manager
        kbWaypointManager = register("key.journeymap.fullscreen_waypoints", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_B);
        setAction(inGameActions, kbWaypointManager, () -> UIManager.INSTANCE.openWaypointManager(null, null));
        setAction(inGuiActions, kbWaypointManager, () -> UIManager.INSTANCE.openWaypointManager(null, getFullscreen()));

        // Show/hide minimap
        kbMinimapToggle = register("key.journeymap.minimap_toggle_alt", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Keyboard.KEY_J);
        setAction(inGameActions, kbMinimapToggle, UIManager.INSTANCE::toggleMinimap);

        // Switch minimap preset
        kbMinimapPreset = register("key.journeymap.minimap_preset", KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_BACKSLASH);
        setAction(inGameActions, kbMinimapPreset, UIManager.INSTANCE::switchMiniMapPreset);

        // Open Options Manager
        kbFullmapOptionsManager = register("key.journeymap.fullscreen_options", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_O);
        setAction(inGuiActions, kbFullmapOptionsManager, () -> UIManager.INSTANCE.openOptionsManager(getFullscreen()));

        // Open Map Actions
        kbFullmapActionsManager = register("key.journeymap.fullscreen_actions", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_A);
        setAction(inGuiActions, kbFullmapActionsManager, UIManager.INSTANCE::openMapActions);

        // Pan fullscreen map north
        kbFullmapPanNorth = register("key.journeymap.fullscreen.north", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_UP);
        setAction(inGuiActions, kbFullmapPanNorth, () -> getFullscreen().moveCanvas(0, -16));

        // Pan fullscreen map south
        kbFullmapPanSouth = register("key.journeymap.fullscreen.south", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_DOWN);
        setAction(inGuiActions, kbFullmapPanSouth, () -> getFullscreen().moveCanvas(0, 16));

        // Pan fullscreen map east
        kbFullmapPanEast = register("key.journeymap.fullscreen.east", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_RIGHT);
        setAction(inGuiActions, kbFullmapPanEast, () -> getFullscreen().moveCanvas(16, 0));

        // Pan fullscreen map west
        kbFullmapPanWest = register("key.journeymap.fullscreen.west", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_LEFT);
        setAction(inGuiActions, kbFullmapPanWest, () -> getFullscreen().moveCanvas(-16, -0));
    }

    /**
     * Associates keybinding to an action in a given list.
     *
     * @param multimap   map of keycode to list of keybindings
     * @param keyBinding key binding
     * @param action     action
     */
    private void setAction(ListMultimap<Integer, KeyBindingAction> multimap, KeyBinding keyBinding, Runnable action)
    {
        multimap.put(keyBinding.getKeyCode(), new KeyBindingAction(keyBinding, action));
    }

    /**
     * Creates and registers a Keybinding.
     *
     * @param description        Keybinding description
     * @param keyConflictContext context for use
     * @param keyModifier        key modifier
     * @param keyCode            key code
     * @return the keybinding
     */
    private KeyBinding register(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, int keyCode)
    {
        String category = keyConflictContext == KeyConflictContext.GUI
                ? Constants.getString("jm.common.hotkeys_keybinding_fullscreen_category")
                : Constants.getString("jm.common.hotkeys_keybinding_category");

        KeyBinding kb = new UpdateAwareKeyBinding(description, keyConflictContext, keyModifier, keyCode, category);
        try
        {
            ClientRegistry.registerKeyBinding(kb);
        }
        catch (Throwable t)
        {
            ChatLog.announceError("Unexpected error when registering keybinding : " + kb);
        }
        return kb;
    }


    /**
     * Handle keyboard event in-game.
     *
     * @param event the event
     */
    @SubscribeEvent()
    public void onGameKeyboardEvent(InputEvent.KeyInputEvent event)
    {
        final int key = Keyboard.getEventKey();
        if (Keyboard.isKeyDown(key))
        {
            onInputEvent(inGameActions, key, true);
        }
    }

    /**
     * Handle keyboard event in GUI.
     * @param event
     */
    @SubscribeEvent()
    public void onGuiKeyboardEvent(GuiScreenEvent.KeyboardInputEvent.Post event)
    {
        final int key = Keyboard.getEventKey();
        if (Keyboard.isKeyDown(key))
        {
            if (inFullscreenWithoutChat())
            {
                onInputEvent(inGuiActions, key, true);
            }
            else if (inMinimapPreview())
            {
                if (onInputEvent(minimapPreviewActions, key, false))
                {
                    ((OptionsManager) mc.currentScreen).refreshMinimapOptions();
                }
            }
        }
    }

    /**
     * Handle keyboard event in GUI.
     *
     * @param event
     */
    @SubscribeEvent()
    public void onGuiMouseEvent(GuiScreenEvent.MouseInputEvent.Post event)
    {
        int key = -100 + Mouse.getEventButton();
        if (!Mouse.isButtonDown(key))
        {
            if (inFullscreenWithoutChat())
            {
                onInputEvent(inGuiActions, key, true);
            }
            else if (inMinimapPreview())
            {
                if (onInputEvent(minimapPreviewActions, key, false))
                {
                    ((OptionsManager) mc.currentScreen).refreshMinimapOptions();
                }
            }
        }
    }

    /**
     * Gets a list of keybindings appearing in inGuiActions
     * @return list sorted by Keybinding display order
     */
    public List<KeyBinding> getInGuiKeybindings()
    {
        List<KeyBinding> list = inGuiActions.values().stream().map(KeyBindingAction::getKeyBinding).collect(Collectors.toList());
        list.sort(Comparator.comparing((kb) -> Constants.getString(kb.getKeyDescription())));
        return list;
    }

    /**
     * Check keybindings for a match, and trigger the action.
     * @param multimap keymappings
     * @return true if action triggered
     */
    private boolean onInputEvent(Multimap<Integer, KeyBindingAction> multimap, int key, boolean useContext)
    {
        try
        {
            if (sortActionsNeeded)
            {
                sortActions();
            }

            for (KeyBindingAction kba : multimap.get(key))
            {
                if (kba.isActive(key, useContext))
                {
                    logger.debug("Firing " + kba);
                    kba.getAction().run();
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error checking keybinding", LogFormatter.toPartialString(e));
        }
        return false;
    }

    private void sortActions()
    {
        sortActions(minimapPreviewActions);
        sortActions(inGameActions);
        sortActions(inGuiActions);
        sortActionsNeeded = false;
    }

    private void sortActions(ListMultimap<Integer, KeyBindingAction> multimap)
    {
        List<KeyBindingAction> copy = new ArrayList<>(multimap.values());
        multimap.clear();
        for (KeyBindingAction kba : copy)
        {
            multimap.put(kba.getKeyBinding().getKeyCode(), kba);
        }
        for (Integer key : multimap.keySet())
        {
            multimap.get(key).sort(kbaComparator);
            Journeymap.getLogger().debug(multimap.get(key));
        }
    }

    private Fullscreen getFullscreen()
    {
        return UIManager.INSTANCE.openFullscreenMap();
    }

    private boolean inFullscreenWithoutChat()
    {
        return mc.currentScreen instanceof Fullscreen && !((Fullscreen) mc.currentScreen).isChatOpen();
    }

    private boolean inMinimapPreview()
    {
        return mc.currentScreen instanceof OptionsManager && ((OptionsManager) mc.currentScreen).previewMiniMap();
    }

    /**
     * Pairs a KeyBinding and a Runnable to execute.
     */
    static class KeyBindingAction
    {
        KeyBinding keyBinding;
        Runnable action;

        public KeyBindingAction(KeyBinding keyBinding, Runnable action)
        {
            this.keyBinding = keyBinding;
            this.action = action;
        }

        boolean isActive(int key, boolean useContext)
        {
            if (useContext)
            {
                return keyBinding.isActiveAndMatches(key);
            }
            else
            {
                return keyBinding.getKeyCode() == key && keyBinding.getKeyModifier().isActive();
            }
        }

        Runnable getAction()
        {
            return action;
        }

        KeyBinding getKeyBinding()
        {
            return keyBinding;
        }

        int order()
        {
            return keyBinding.getKeyModifier().ordinal();
        }

        @Override
        public String toString()
        {
            return "KeyBindingAction{" + keyBinding.getDisplayName() + " = " + Constants.getString(keyBinding.getKeyDescription()) + '}';
        }
    }

    /**
     * Without a Forge Event to announce keybinding changes, we can't do a one-time sort or key-to-KeyBinding hash.
     * This lets the user have keybindings like J and CTRL-J co-exist.
     */
    class UpdateAwareKeyBinding extends KeyBinding
    {
        UpdateAwareKeyBinding(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, int keyCode, String category)
        {
            super(description, keyConflictContext, keyModifier, keyCode, category);
        }

        @Override
        public void setKeyCode(int keyCode)
        {
            super.setKeyCode(keyCode);
            sortActionsNeeded = true;
        }

        @Override
        public void setKeyModifierAndCode(KeyModifier keyModifier, int keyCode)
        {
            super.setKeyModifierAndCode(keyModifier, keyCode);
            sortActionsNeeded = true;
        }
    }

}