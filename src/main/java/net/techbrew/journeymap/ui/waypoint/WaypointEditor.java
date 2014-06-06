package net.techbrew.journeymap.ui.waypoint;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.world.WorldProvider;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WorldData;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.*;
import net.techbrew.journeymap.ui.ScrollPane;
import net.techbrew.journeymap.ui.TextField;
import net.techbrew.journeymap.ui.map.MapOverlay;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

public class WaypointEditor extends JmUI
{

    String labelName = Constants.getString("Waypoint.name");
    String locationTitle = Constants.getString("Waypoint.location");
    String colorTitle = Constants.getString("Waypoint.color");
    String dimensionsTitle = Constants.getString("Waypoint.dimensions");
    String labelX = Constants.getString("Waypoint.x");
    String labelY = Constants.getString("Waypoint.y");
    String labelZ = Constants.getString("Waypoint.z");
    String labelR = Constants.getString("Waypoint.r");
    String labelG = Constants.getString("Waypoint.g");
    String labelB = Constants.getString("Waypoint.b");

    String currentLocation = "";

    private enum ButtonEnum
    {
        Randomize, Enable, Remove, Reset, Save, Close
    }

    ;

    private final TextureImpl wpTexture;
    private final TextureImpl colorPickTexture;

    private Button buttonRandomize;
    private Button buttonEnable;
    private Button buttonRemove;
    private Button buttonReset;
    private Button buttonSave;
    private Button buttonClose;

    private TextField fieldName;
    private TextField fieldR;
    private TextField fieldG;
    private TextField fieldB;
    private TextField fieldX;
    private TextField fieldY;
    private TextField fieldZ;

    private ArrayList<TextField> fieldList = new ArrayList<TextField>();

    private ArrayList<DimensionButton> dimButtonList = new ArrayList<DimensionButton>();
    private ScrollPane dimScrollPane;

    private Color currentColor;
    private Rectangle2D.Double colorPickRect;
    private BufferedImage colorPickImg;

    private final Waypoint originalWaypoint;
    private Waypoint editedWaypoint;
    private final boolean isNew;

    private ButtonList bottomButtons;

    public WaypointEditor(Waypoint waypoint, boolean isNew, Class<? extends JmUI> returnClass)
    {
        super(Constants.getString(isNew ? "Waypoint.new_title" : "Waypoint.edit_title"), returnClass);
        this.originalWaypoint = waypoint;
        this.editedWaypoint = new Waypoint(originalWaypoint);
        this.isNew = isNew;
        this.wpTexture = waypoint.getTexture();
        this.colorPickTexture = TextureCache.instance().getColorPicker();
        this.colorPickRect = new Rectangle2D.Double(0, 0, colorPickTexture.width, colorPickTexture.height);
        this.colorPickImg = colorPickTexture.getImage();
        Keyboard.enableRepeatEvents(true);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        try
        {
            int dimension = mc.thePlayer.worldObj.provider.dimensionId;

            // Update player pos
            String pos = Constants.getString("MapOverlay.location_xzy",
                    Integer.toString((int) mc.thePlayer.posX),
                    Integer.toString((int) mc.thePlayer.posZ),
                    Integer.toString((int) mc.thePlayer.posY));
            currentLocation = Constants.getString("Waypoint.current_location", " " + pos);

            if (this.fieldList.isEmpty())
            {
                FontRenderer fr = getFontRenderer();

                fieldName = new TextField(originalWaypoint.getName(), fr, 160, 20);
                //fieldName.setMinLength(1);
                fieldName.setFocused(true);
                if (isNew)
                {
                    fieldName.setCursorPositionEnd();
                    fieldName.setSelectionPos(0);
                }
                fieldList.add(fieldName);

                int width9chars = getFontRenderer().getStringWidth("-30000000") + 10;
                int width3chars = getFontRenderer().getStringWidth("255") + 10;
                int h = 20;

                fieldX = new TextField(originalWaypoint.getX(dimension), fr, width9chars, h, true, true);
                fieldX.setClamp(-30000000, 30000000);
                fieldList.add(fieldX);

                fieldZ = new TextField(originalWaypoint.getZ(dimension), fr, width9chars, h, true, true);
                fieldZ.setClamp(-30000000, 30000000);
                fieldList.add(fieldZ);

                int y = originalWaypoint.getY(dimension);
                fieldY = new TextField(y < 0 ? "" : y, fr, width3chars, h, true, true);
                fieldY.setClamp(0, mc.theWorld.getHeight() - 1);
                fieldY.setMinLength(1);
                fieldList.add(fieldY);

                fieldR = new TextField("", fr, width3chars, h, true, false);
                fieldR.setClamp(0, 255);
                fieldR.setMaxStringLength(3);
                fieldList.add(fieldR);

                fieldG = new TextField("", fr, width3chars, h, true, false);
                fieldG.setClamp(0, 255);
                fieldG.setMaxStringLength(3);
                fieldList.add(fieldG);

                fieldB = new TextField("", fr, width3chars, h, true, false);
                fieldB.setClamp(0, 255);
                fieldB.setMaxStringLength(3);
                fieldList.add(fieldB);

                Collection<Integer> wpDims = originalWaypoint.getDimensions();
                int buttonId = ButtonEnum.values().length;

                for (WorldProvider provider : WorldData.getDimensionProviders(WaypointStore.instance().getLoadedDimensions()))
                {
                    int dim = provider.dimensionId;
                    String dimName = provider.getDimensionName();
                    dimButtonList.add(new DimensionButton(buttonId++, dim, dimName, wpDims.contains(dim)));
                }

                dimScrollPane = new ScrollPane(mc, 0, 0, dimButtonList, dimButtonList.get(0).getHeight(), 4);
            }

            if (this.buttonList.isEmpty())
            {
                String on = Constants.getString("MapOverlay.on");
                String off = Constants.getString("MapOverlay.off");
                String enableOn = Constants.getString("Waypoint.enable", on);
                String enableOff = Constants.getString("Waypoint.enable", off);

                buttonRandomize = new Button(ButtonEnum.Randomize, Constants.getString("Waypoint.randomize")); //$NON-NLS-1$

                buttonEnable = new Button(ButtonEnum.Enable, enableOn, enableOff, true); //$NON-NLS-1$
                buttonEnable.setToggled(originalWaypoint.isEnable());

                buttonRemove = new Button(ButtonEnum.Remove, Constants.getString("Waypoint.remove")); //$NON-NLS-1$
                buttonRemove.setEnabled(!isNew);
                buttonRemove.setNoDisableText(true);

                buttonReset = new Button(ButtonEnum.Reset, Constants.getString("Waypoint.reset")); //$NON-NLS-1$
                buttonSave = new Button(ButtonEnum.Save, Constants.getString("Waypoint.save")); //$NON-NLS-1$
                buttonSave.setNoDisableText(true);

                String closeLabel = isNew ? "Waypoint.cancel" : "MapOverlay.close";
                buttonClose = new Button(ButtonEnum.Close, Constants.getString(closeLabel));

                buttonList.add(buttonEnable);
                buttonList.add(buttonRandomize);
                buttonList.add(buttonRemove);
                buttonList.add(buttonReset);
                buttonList.add(buttonSave);
                buttonList.add(buttonClose);

                bottomButtons = new ButtonList(buttonRemove, buttonSave, buttonClose);
                bottomButtons.setUniformWidths(getFontRenderer());

                setFormColor(originalWaypoint.getColor());

                validate();
            }


        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe(LogFormatter.toString(t));
            UIManager.getInstance().closeAll();
        }
    }

    /**
     * Center buttons in UI.
     */
    @Override
    protected void layoutButtons()
    {
        // Buttons
        initGui();

        final FontRenderer fr = getFontRenderer();

        // Margins
        final int vpad = 5;
        final int hgap = fr.getStringWidth("X") * 3;
        final int vgap = fieldX.getHeight() + vpad;
        final int startY = Math.max(30, (this.height - 200) / 2);

        // Determine dimension button spacing requirement
        int dcw = fr.getStringWidth(dimensionsTitle);
        dcw = 8 + Math.max(dcw, dimScrollPane.getFitWidth(fr));

        // Set column dimensions
        final int leftWidth = hgap * 2 + fieldX.getWidth() + fieldY.getWidth() + fieldZ.getWidth();
        final int rightWidth = dcw;
        final int totalWidth = leftWidth + 10 + rightWidth;
        final int leftX = ((this.width - totalWidth) / 2);
        final int leftXEnd = leftX + leftWidth;
        final int rightX = leftXEnd + 10;
        final int rightXEnd = rightX + rightWidth;

        // Left column starting Y
        int leftRow = startY;
        drawLabel(labelName, leftX, leftRow);
        leftRow += 12;
        fieldName.setWidth(leftWidth);
        fieldName.setX(leftX);
        fieldName.setY(leftRow);
        if (!fieldName.isFocused())
        {
            fieldName.setSelectionPos(fieldName.getText().length());
        }
        fieldName.drawTextBox();

        // Coordinates
        leftRow += vgap + vpad;
        drawLabel(locationTitle, leftX, leftRow);
        leftRow += 12;
        drawLabelAndField(labelX, fieldX, leftX, leftRow);
        drawLabelAndField(labelZ, fieldZ, fieldX.getX() + fieldX.getWidth() + hgap, leftRow);
        drawLabelAndField(labelY, fieldY, fieldZ.getX() + fieldZ.getWidth() + hgap, leftRow);

        // Color
        leftRow += vgap + vpad;
        drawLabel(colorTitle, leftX, leftRow);
        leftRow += 12;
        drawLabelAndField(labelR, fieldR, leftX, leftRow);
        drawLabelAndField(labelG, fieldG, fieldR.getX() + fieldR.getWidth() + hgap, leftRow);
        drawLabelAndField(labelB, fieldB, fieldG.getX() + fieldG.getWidth() + hgap, leftRow);
        buttonRandomize.setWidth(4 + Math.max(fieldB.getX() + fieldB.getWidth() - fieldR.getX(), 10 + fr.getStringWidth(buttonRandomize.displayString)));
        buttonRandomize.setPosition(fieldR.getX() - 2, leftRow += vgap);

        // Color picker
        int cpY = fieldB.getY();
        int cpSize = buttonRandomize.getY() + buttonRandomize.getHeight() - cpY - 2;
        int cpHAreaX = fieldB.getX() + fieldB.getWidth();
        int cpHArea = (fieldName.getX() + fieldName.getWidth()) - (cpHAreaX);
        int cpX = cpHAreaX + (cpHArea - cpSize);
        drawColorPicker(cpX, cpY, cpSize);

        // WP icon
        int iconX = cpHAreaX + ((cpX - cpHAreaX) / 2) - (wpTexture.width / 2) + 1;
        int iconY = buttonRandomize.getY() - vpad / 2;
        drawWaypoint(iconX, iconY);

        // Enable
        leftRow += (vgap);
        buttonEnable.fitWidth(fr);
        buttonEnable.setWidth(Math.max(leftWidth / 2, buttonEnable.getWidth()));
        buttonEnable.setPosition(leftX - 2, leftRow);

        // Reset
        buttonReset.setWidth(leftWidth - buttonEnable.getWidth() - 2);
        buttonReset.setPosition(leftXEnd - buttonReset.getWidth() + 2, leftRow);

        // Dimensions column
        int rightRow = startY;

        // Dimensions label
        drawLabel(dimensionsTitle, rightX, rightRow);
        rightRow += (12);

        // Dimension buttons in the scroll pane
        int scrollHeight = (buttonReset.getY() + buttonReset.getHeight() - 2) - rightRow;
        dimScrollPane.position(dcw, scrollHeight, 0, 0, rightX, rightRow);

        // Remove(Cancel) / Save
        int totalRow = Math.max(leftRow + vgap, rightRow + vgap);

        bottomButtons.layoutFilledHorizontal(fr, leftX - 2, totalRow, rightXEnd + 2, 4, true);
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);

        validate();

        layoutButtons();

        dimScrollPane.drawScreen(x, y, par3);


        DrawUtil.drawLabel(currentLocation, width / 2, height, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.BLACK, 255, Color.lightGray, 255, 1, true);

        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            guibutton.drawButton(this.mc, x, y);
        }

        drawTitle();
        drawLogo();
    }

    protected void drawWaypoint(int x, int y)
    {
        DrawUtil.drawColoredImage(wpTexture, 255, currentColor, x, y - (wpTexture.height / 2));
    }

    protected void drawColorPicker(int x, int y, float size)
    {
        int sizeI = (int) size;
        drawRect(x - 1, y - 1, x + sizeI + 1, y + sizeI + 1, -6250336);

        if (colorPickRect.width != size)
        {
            // Updated scaled image only when necessary
            Image image = colorPickTexture.getImage().getScaledInstance(sizeI, sizeI, Image.SCALE_FAST);
            colorPickImg = new BufferedImage(sizeI, sizeI, BufferedImage.TYPE_INT_RGB);

            Graphics g = colorPickImg.createGraphics();
            g.drawImage(image, 0, 0, sizeI, sizeI, null);
            g.dispose();
        }
        colorPickRect.setRect(x, y, size, size);
        float scale = size / colorPickTexture.width;
        DrawUtil.drawImage(colorPickTexture, x, y, false, scale);


        //drawRect(x, y, x + sizeI, y + sizeI, -16777216);
    }

    protected void drawLabelAndField(String label, TextField field, int x, int y)
    {
        field.setX(x);
        field.setY(y);
        FontRenderer fr = getFontRenderer();
        int width = fr.getStringWidth(label) + 4;
        drawString(getFontRenderer(), label, x - width, y + (field.getHeight() - 8) / 2, Color.cyan.getRGB());
        field.drawTextBox();
    }

    protected void drawLabel(String label, int x, int y)
    {
        drawString(getFontRenderer(), label, x, y, Color.cyan.getRGB());
    }

    protected void keyTyped(char par1, int par2)
    {
        switch (par2)
        {
            case Keyboard.KEY_ESCAPE:
                closeAndReturn();
                return;
            case Keyboard.KEY_RETURN:
                save();
                return;
            case Keyboard.KEY_TAB:
                validate();
                onTab();
                return;
            default:
                break;
        }

        for (GuiTextField field : fieldList)
        {
            boolean done = field.textboxKeyTyped(par1, par2);
            if (done)
            {
                break;
            }
        }

        updateWaypointFromForm();
        validate();
    }

    @Override
    protected void mouseClickMove(int par1, int par2, int par3, long par4)
    {
        checkColorPicker(par1, par2);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (GuiTextField field : fieldList)
        {
            field.mouseClicked(mouseX, mouseY, mouseButton);
        }

        checkColorPicker(mouseX, mouseY);

        Button button = dimScrollPane.mouseClicked(mouseX, mouseY, mouseButton);
        if (button != null)
        {
            actionPerformed(button);
        }
    }

    protected void checkColorPicker(int mouseX, int mouseY)
    {
        if (colorPickRect.contains(mouseX, mouseY))
        {
            int x = mouseX - (int) colorPickRect.x;
            int y = mouseY - (int) colorPickRect.y;
            setFormColor(new Color(colorPickImg.getRGB(x, y)));
        }
    }

    protected void setFormColor(Color color)
    {
        //if(color!=null && color.equals(currentColor)) return;

        currentColor = color;
        fieldR.setText(Integer.toString(color.getRed()));
        fieldG.setText(Integer.toString(color.getGreen()));
        fieldB.setText(Integer.toString(color.getBlue()));
        updateWaypointFromForm();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (dimButtonList.contains(guibutton))
        {
            DimensionButton dimButton = (DimensionButton) guibutton;
            dimButton.toggle();
            updateWaypointFromForm();
        }
        else
        {
            final ButtonEnum id = ButtonEnum.values()[guibutton.id];
            switch (id)
            {

                case Randomize:
                {
                    setRandomColor();
                    break;
                }
                case Enable:
                {
                    buttonEnable.toggle();
                    break;
                }
                case Remove:
                {
                    remove();
                    break;
                }
                case Reset:
                {
                    resetForm();
                    break;
                }
                case Save:
                {
                    save();
                    break;
                }
                case Close:
                {
                    refreshAndClose(originalWaypoint);
                    break;
                }
            }
        }
    }

    protected void setRandomColor()
    {
        editedWaypoint.setRandomColor();
        setFormColor(editedWaypoint.getColor());
    }

    protected void onTab()
    {
        boolean focusNext = false;
        boolean foundFocus = false;
        for (TextField field : fieldList)
        {
            if (focusNext)
            {
                field.setFocused(true);
                foundFocus = true;
                break;
            }
            if (field.isFocused())
            {
                field.setFocused(false);
                field.clamp();
                focusNext = true;
            }
        }
        if (!foundFocus)
        {
            fieldList.get(0).setFocused(true);
        }
    }

    protected boolean validate()
    {
        boolean valid = true;
        if (fieldName != null)
        {
            valid = fieldName.hasMinLength();
        }

        if (valid && fieldY != null)
        {
            valid = fieldY.hasMinLength();
        }

        if (this.buttonSave != null)
        {
            this.buttonSave.setEnabled(valid && (isNew || !originalWaypoint.equals(editedWaypoint)));
        }

        return valid;
    }

    protected void remove()
    {
        WaypointStore.instance().remove(originalWaypoint);
        refreshAndClose(null);
    }

    protected void save()
    {
        if (!validate())
        {
            return;
        }
        updateWaypointFromForm();
        WaypointStore.instance().remove(originalWaypoint);
        WaypointStore.instance().save(editedWaypoint);
        refreshAndClose(editedWaypoint);
    }

    protected void resetForm()
    {
        this.editedWaypoint = new Waypoint(originalWaypoint);
        dimButtonList.clear();
        fieldList.clear();
        buttonList.clear();
        initGui();
        validate();
    }

    protected void updateWaypointFromForm()
    {
        currentColor = new Color(getSafeColorInt(fieldR), getSafeColorInt(fieldG), getSafeColorInt(fieldB));
        editedWaypoint.setColor(currentColor);
        fieldName.setTextColor(editedWaypoint.getSafeColor().getRGB());

        ArrayList<Integer> dims = new ArrayList<Integer>();
        for (DimensionButton db : dimButtonList)
        {
            if (db.getToggled())
            {
                dims.add(db.dimension);
            }
        }
        editedWaypoint.setDimensions(dims);
        editedWaypoint.setEnable(buttonEnable.getToggled());
        editedWaypoint.setName(fieldName.getText());

        editedWaypoint.setLocation(getSafeCoordInt(fieldX), getSafeCoordInt(fieldY), getSafeCoordInt(fieldZ), mc.thePlayer.dimension);
    }

    protected int getSafeColorInt(TextField field)
    {
        field.clamp();
        String text = field.getText();
        if (text == null || text.isEmpty())
        {
            return 0;
        }

        int val = 0;
        try
        {
            val = Integer.parseInt(text);
        }
        catch (NumberFormatException e)
        {
        }

        return Math.max(0, Math.min(255, val));
    }

    protected int getSafeCoordInt(TextField field)
    {
        String text = field.getText();
        if (text == null || text.isEmpty() || text.equals("-"))
        {
            return 0;
        }

        int val = 0;
        try
        {
            val = Integer.parseInt(text);
        }
        catch (NumberFormatException e)
        {
        }
        return val;
    }

    protected void refreshAndClose(Waypoint focusWaypoint)
    {
        if (returnClass != null && returnClass.equals(WaypointManager.class))
        {
            UIManager.getInstance().openWaypointManager(focusWaypoint, MapOverlay.class);
            return;
        }

        //DataCache.instance().forceRefresh(WaypointsData.class);
        MapOverlay.state().requireRefresh();
        closeAndReturn();
    }

    @Override
    protected void closeAndReturn()
    {
        if (returnClass == null)
        {
            UIManager.getInstance().closeAll();
        }
        else
        {
            UIManager.getInstance().open(returnClass);
        }
    }

    class DimensionButton extends Button
    {
        public final int dimension;

        DimensionButton(int id, int dimension, String dimensionName, boolean toggled)
        {
            super(id, 0, 0, String.format("%s: %s", dimensionName, Constants.getString("MapOverlay.on")), String.format("%s: %s", dimensionName, Constants.getString("MapOverlay.off")), toggled);
            this.dimension = dimension;
            setToggled(toggled);
        }
    }
}
