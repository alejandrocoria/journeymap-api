package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.properties.PropertiesBase;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.CheckBox;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mark on 9/26/2014.
 */
public class ConfigManagerFactory implements IModGuiFactory
{
    public static final String ALL_CHAR = "\u00BB";

    @Override
    public void initialize(Minecraft minecraftInstance)
    {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return ConfigManager.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }

    static List<IConfigElement> getConfigElements()
    {
        List<BaseConfigElement> root = new ArrayList<BaseConfigElement>();
        List<ConfigCategoryElement> categories = new ArrayList<ConfigCategoryElement>();

        addConfigElements(root, categories, Config.Category.MiniMap, JourneyMap.getMiniMapProperties());
        //categories.get(0).getChildElements().add(0, new ConfigCategoryElement(Config.Category.MiniMap, null).setConfigEntryClass(new IConfigEt))

        addConfigElements(root, categories, Config.Category.FullMap, JourneyMap.getFullMapProperties());
        addConfigElements(root, categories, Config.Category.WebMap, JourneyMap.getWebMapProperties());
        addConfigElements(root, categories, Config.Category.Waypoint, JourneyMap.getWaypointProperties());
        addConfigElements(root, categories, Config.Category.Advanced, JourneyMap.getCoreProperties());

        Collections.sort(categories);

        int count = root.size();

        for (DummyConfigElement.DummyCategoryElement categoryElement : categories)
        {
            count += categoryElement.getChildElements().size();
            Collections.sort(categoryElement.getChildElements());
            categoryElement.setCustomListEntryClass(JmConfigCategoryEntry.class);
        }

        List<IConfigElement> elements = new ArrayList<IConfigElement>(categories.size());

        elements.addAll(categories);
        elements.addAll(root);

        JourneyMap.getLogger().info("Configurable properties: " + count);


        return elements;
    }

    protected static void addConfigElements(List<BaseConfigElement> root, List<ConfigCategoryElement> categories, Config.Category inheritedCategory, PropertiesBase properties)
    {
        Class<? extends PropertiesBase> propertiesClass = properties.getClass();
        for (Map.Entry<Config.Category, List<BaseConfigElement>> entry : buildConfigElements(null, inheritedCategory, propertiesClass, properties).entrySet())
        {
            Config.Category category = entry.getKey();
            if (category == Config.Category.General)
            {
                for (BaseConfigElement val : entry.getValue())
                {
                    root.add(val);
                }
            }
            else
            {
                if (category == Config.Category.Inherit)
                {
                    category = inheritedCategory;
                }

                categories.add(new ConfigCategoryElement(category, entry.getValue()));
            }
        }
    }

    protected static HashMap<Config.Category, List<BaseConfigElement>> buildConfigElements(HashMap<Config.Category, List<BaseConfigElement>> map, Config.Category inheritedCategory, Class<? extends PropertiesBase> propertiesClass, PropertiesBase properties)
    {
        if (map == null)
        {
            map = new HashMap<Config.Category, List<BaseConfigElement>>();
        }
        for (Field field : propertiesClass.getDeclaredFields())
        {
            if (field.isAnnotationPresent(Config.class))
            {
                Config config = field.getAnnotation(Config.class);
                BaseConfigElement fieldConfig = null;

                if (field.getType().equals(AtomicBoolean.class))
                {
                    fieldConfig = BooleanConfigElement.create(properties, field);
                }
                else if (field.getType().equals(AtomicInteger.class))
                {
                    fieldConfig = IntegerConfigElement.create(properties, field);
                }
                else if (field.getType().equals(AtomicReference.class))
                {
                    if (!config.stringListProvider().equals(Config.NoStringProvider.class))
                    {
                        fieldConfig = StringConfigElement.create(properties, field);
                    }
                    else
                    {
                        fieldConfig = EnumConfigElement.create(properties, field);
                    }
                }

                if (fieldConfig != null)
                {
                    Config.Category category = config.category();
                    if (category == Config.Category.Inherit)
                    {
                        category = inheritedCategory;
                    }

                    List<BaseConfigElement> list = map.get(category);
                    if (list == null)
                    {
                        list = new ArrayList<BaseConfigElement>();
                        map.put(category, list);
                    }
                    list.add(fieldConfig);
                }
                else
                {
                    JourneyMap.getLogger().warn(String.format("Unable to create config gui for %s.%s using %s", properties.getClass().getSimpleName(), field.getName(), config));
                }
            }
        }

        // Check for parent class
        Class parentClass = propertiesClass.getSuperclass();
        if (PropertiesBase.class.isAssignableFrom(parentClass))
        {
            map = buildConfigElements(map, inheritedCategory, (Class<? extends PropertiesBase>) parentClass, properties);
        }

        return map;
    }

    /**
     * CategoryEntry
     * <p/>
     * Provides an entry that consists of a GuiButton for navigating to the child category GuiConfig screen.
     */
    public static class JmConfigCategoryEntry extends GuiConfigEntries.ListEntryBase
    {
        protected GuiScreen childScreen;
        protected final Button btnSelectCategory;

        public JmConfigCategoryEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
        {
            super(owningScreen, owningEntryList, configElement);

            this.childScreen = this.buildChildScreen();

            this.btnSelectCategory = new Button(0, I18n.format(name) + "...");
            this.btnSelectCategory.setDefaultStyle(false);
            this.btnSelectCategory.setDrawBackground(false);
            this.btnSelectCategory.setDrawFrame(false);

            this.tooltipHoverChecker = new HoverChecker(this.btnSelectCategory, 800);

            this.drawLabel = false;
        }

        /**
         * This method is called in the constructor and is used to set the childScreen field.
         */
        protected GuiScreen buildChildScreen()
        {
            return new GuiConfig(this.owningScreen, this.configElement.getChildElements(), this.owningScreen.modID,
                    owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(),
                    owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(), this.owningScreen.title,
                    ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name));
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected)
        {
            this.btnSelectCategory.centerHorizontalOn(x + (listWidth / 2));

            this.btnSelectCategory.yPosition = y;
            this.btnSelectCategory.enabled = enabled();

            this.btnSelectCategory.drawButton(this.mc, mouseX, mouseY);

            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, tessellator, mouseX, mouseY, isSelected);
        }

        @Override
        public void drawToolTip(int mouseX, int mouseY)
        {
            boolean canHover = mouseY < this.owningScreen.entryList.bottom && mouseY > this.owningScreen.entryList.top;

            if (this.tooltipHoverChecker.checkHover(mouseX, mouseY, canHover))
            {
                this.owningScreen.drawToolTip(toolTip, mouseX, mouseY);
            }

            super.drawToolTip(mouseX, mouseY);

        }

        /**
         * Returns true if the mouse has been pressed on this control.
         */
        @Override
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            if (this.btnSelectCategory.mousePressed(this.mc, x, y))
            {
                btnSelectCategory.func_146113_a(mc.getSoundHandler());
                Minecraft.getMinecraft().displayGuiScreen(childScreen);
                return true;
            }
            else
            {
                return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
            }
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        @Override
        public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            this.btnSelectCategory.mouseReleased(x, y);
        }

        @Override
        public boolean isDefault()
        {
            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null)
            {
                return ((GuiConfig) childScreen).entryList.areAllEntriesDefault(true);
            }

            return true;
        }

        @Override
        public void setToDefault()
        {
            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null)
            {
                ((GuiConfig) childScreen).entryList.setAllToDefault(true);
            }
        }

        @Override
        public void keyTyped(char eventChar, int eventKey)
        {
        }

        @Override
        public void updateCursorCounter()
        {
        }

        @Override
        public void mouseClicked(int x, int y, int mouseEvent)
        {
        }

        @Override
        public boolean saveConfigElement()
        {
            boolean requiresRestart = false;

            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null)
            {
                requiresRestart = configElement.requiresMcRestart() && ((GuiConfig) childScreen).entryList.hasChangedEntry(true);

                if (((GuiConfig) childScreen).entryList.saveConfigElements())
                {
                    requiresRestart = true;
                }
            }

            return requiresRestart;
        }

        @Override
        public boolean isChanged()
        {
            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null)
            {
                return ((GuiConfig) childScreen).entryList.hasChangedEntry(true);
            }
            else
            {
                return false;
            }
        }

        @Override
        public void undoChanges()
        {
            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null)
            {
                ((GuiConfig) childScreen).entryList.undoAllChanges(true);
            }
        }

        @Override
        public boolean enabled()
        {
            return true;
        }

        @Override
        public int getLabelWidth()
        {
            return 0;
        }

        @Override
        public int getEntryRightBound()
        {
            return this.owningEntryList.width / 2 + 155 + 22 + 18;
        }

        @Override
        public String getCurrentValue()
        {
            return "";
        }

        @Override
        public String[] getCurrentValues()
        {
            return new String[]{getCurrentValue()};
        }
    }

    /**
     * ButtonEntry
     * <p/>
     * Provides a basic GuiButton entry to be used as a base for other entries that require a button for the value.
     */
    public static abstract class JmButtonEntry extends GuiConfigEntries.ListEntryBase
    {
        protected final Button btnValue;

        public JmButtonEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<?> configElement)
        {
            this(owningScreen, owningEntryList, configElement, new Button(0, configElement.get() != null ? I18n.format(String.valueOf(configElement.get())) : ""));
        }

        public JmButtonEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<?> configElement, Button button)
        {
            super(owningScreen, owningEntryList, configElement);
            this.btnValue = button;
        }

        /**
         * Updates the displayString of the value button.
         */
        public abstract void updateValueButtonText();

        /**
         * Called when the value button has been clicked.
         */
        public abstract void valueButtonPressed(int slotIndex);

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected)
        {
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, tessellator, mouseX, mouseY, isSelected);
            this.btnValue.width = drawLabel ? this.owningEntryList.controlWidth : (this.owningEntryList.controlWidth + this.owningEntryList.maxLabelTextWidth);
            this.btnValue.xPosition = drawLabel ? this.owningScreen.entryList.controlX : this.owningScreen.entryList.labelX + 16;
            this.btnValue.yPosition = y;
            this.btnValue.enabled = enabled();
            this.btnValue.drawButton(this.mc, mouseX, mouseY);
        }

        @Override
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            if (this.btnValue.mousePressed(this.mc, x, y))
            {
                btnValue.func_146113_a(mc.getSoundHandler());
                valueButtonPressed(index);
                updateValueButtonText();
                return true;
            }
            else
            {
                return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
            }
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        @Override
        public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
            this.btnValue.mouseReleased(x, y);
        }

        @Override
        public void keyTyped(char eventChar, int eventKey)
        {
        }

        @Override
        public void updateCursorCounter()
        {
        }

        @Override
        public void mouseClicked(int x, int y, int mouseEvent)
        {
        }
    }

    /**
     * CheckBooleanEntry
     * <p/>
     * Provides a Checkbutton that toggles between true and false.
     */
    public static class CheckBooleanEntry extends JmButtonEntry
    {
        protected final boolean beforeValue;
        protected boolean currentValue;

        public CheckBooleanEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<Boolean> configElement)
        {
            this(owningScreen, owningEntryList, configElement, new CheckBox(0, ""));
        }

        public CheckBooleanEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<?> configElement, Button button)
        {
            super(owningScreen, owningEntryList, configElement, button);
            this.beforeValue = Boolean.valueOf(configElement.get().toString());
            this.currentValue = beforeValue;
            this.btnValue.enabled = enabled();
            this.drawLabel = true;
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected)
        {
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, tessellator, mouseX, mouseY, isSelected);
            this.btnValue.width = drawLabel ? this.owningEntryList.controlWidth : (this.owningEntryList.controlWidth + this.owningEntryList.maxLabelTextWidth);
            this.btnValue.xPosition = drawLabel ? this.owningScreen.entryList.controlX : this.owningScreen.entryList.labelX + 16;
            this.btnValue.yPosition = y;
            this.btnValue.enabled = enabled();
            this.btnValue.drawButton(this.mc, mouseX, mouseY);
        }

        @Override
        public void updateValueButtonText()
        {
        }

        @Override
        public void valueButtonPressed(int slotIndex)
        {
            if (enabled())
            {
                currentValue = !currentValue;
            }
        }

        @Override
        public boolean isDefault()
        {
            return currentValue == Boolean.valueOf(configElement.getDefault().toString());
        }

        @Override
        public void setToDefault()
        {
            if (enabled())
            {
                currentValue = Boolean.valueOf(configElement.getDefault().toString());
                updateValueButtonText();
            }
        }

        @Override
        public boolean isChanged()
        {
            return currentValue != beforeValue;
        }

        @Override
        public void undoChanges()
        {
            if (enabled())
            {
                currentValue = beforeValue;
                updateValueButtonText();
            }
        }

        @Override
        public boolean saveConfigElement()
        {
            if (enabled() && isChanged())
            {
                configElement.set(currentValue);
                return configElement.requiresMcRestart();
            }
            return false;
        }

        @Override
        public Boolean getCurrentValue()
        {
            return currentValue;
        }

        @Override
        public Boolean[] getCurrentValues()
        {
            return new Boolean[]{getCurrentValue()};
        }
    }
}
