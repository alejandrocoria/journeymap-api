package net.techbrew.journeymap.ui.waypoint;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.common.DimensionManager;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.model.WaypointHelper;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.*;
import net.techbrew.journeymap.ui.ScrollPane;
import net.techbrew.journeymap.ui.TextField;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class WaypointManager extends JmUI {

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

	private enum ButtonEnum {Randomize, Enable, Remove, Reset, Save};

    private final TextureImpl wpTexture;
    private final TextureImpl colorPickTexture;

    private Button buttonRandomize;
    private Button buttonEnable;
    private Button buttonRemove;
    private Button buttonReset;
    private Button buttonSave;

    private TextField fieldName;
    private TextField fieldR;
    private TextField fieldG;
    private TextField fieldB;
    private TextField fieldX;
    private TextField fieldY;
    private TextField fieldZ;

    private ArrayList<GuiTextField> fieldList = new ArrayList<GuiTextField>();

    private ArrayList<DimensionButton> dimButtonList = new ArrayList<DimensionButton>();
    private ScrollPane dimScrollPane;

    private Color currentColor;
    private Random random = new Random();
    private Rectangle2D.Double colorPickRect;
    private BufferedImage colorPickImg;

	public WaypointManager() {
		super(Constants.getString("Waypoint.manage"));

        this.wpTexture = TextureCache.instance().getWaypoint();
        this.colorPickTexture = TextureCache.instance().getColorPicker();
        this.colorPickRect = new Rectangle2D.Double(0,0,colorPickTexture.width, colorPickTexture.height);
        this.colorPickImg = colorPickTexture.getImage();
        Keyboard.enableRepeatEvents(true);
	}

	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
	public void initGui()
    {
        if(this.fieldList.isEmpty())
        {
            FontRenderer fr = getFontRenderer();


            dimScrollPane = new ScrollPane(mc, 0, 0 , dimButtonList);
        }

        if(this.buttonList.isEmpty())
        {
            String on = Constants.getString("MapOverlay.on");
            String off = Constants.getString("MapOverlay.off");
            String enableOn = Constants.getString("Waypoint.enable", on);
            String enableOff = Constants.getString("Waypoint.enable", off);

            buttonRandomize = new Button(ButtonEnum.Randomize.ordinal(),0,0,Constants.getString("Waypoint.randomize")); //$NON-NLS-1$

            buttonEnable = new Button(ButtonEnum.Enable.ordinal(),0,0, enableOn, enableOff, true); //$NON-NLS-1$
            buttonEnable.setToggled(waypoint.getEnable());

            String closeLabel = isNew ? "Waypoint.cancel" : "Waypoint.remove";
            buttonRemove = new Button(ButtonEnum.Remove.ordinal(),0,0,Constants.getString(closeLabel)); //$NON-NLS-1$

            buttonReset = new Button(ButtonEnum.Reset.ordinal(),0,0,Constants.getString("Waypoint.reset")); //$NON-NLS-1$

            buttonSave = new Button(ButtonEnum.Save.ordinal(),0,0,Constants.getString("Waypoint.save")); //$NON-NLS-1$
            buttonSave.noDisableText = true;

            buttonList.add(buttonEnable);
            buttonList.add(buttonRandomize);
            buttonList.add(buttonRemove);
            buttonList.add(buttonReset);
            buttonList.add(buttonSave);
        }
    }
    
    /**
	 * Center buttons in UI.
	 */
    @Override
    protected void layoutButtons() {
		// Buttons

        initGui();
        final FontRenderer fr = getFontRenderer();

        // Margins
        final int vpad = 5;
        final int hgap = fr.getStringWidth("X")*3;
        final int vgap = fieldX.getHeight() + vpad;
        final int startY = Math.max(45, this.height/5);

        // Determine dimension button spacing requirement
        int dcw = fr.getStringWidth(dimensionsTitle);
        dcw = 8 + Math.max(dcw, dimScrollPane.getFitWidth(fr));

        // Set column dimensions
        final int leftWidth = hgap*2 + fieldX.getWidth() + fieldY.getWidth() + fieldZ.getWidth();
        final int rightWidth = dcw;
        final int totalWidth = leftWidth + 10 + rightWidth;
        final int leftX = ((this.width - totalWidth)/2);
        final int leftXEnd = leftX + leftWidth;
        final int rightX = leftXEnd + 10;
        final int rightXEnd = rightX + rightWidth;

        // Left column starting Y
        int leftRow = startY;
        drawLabel(labelName, leftX, leftRow);
        leftRow+=12;
        fieldName.setWidth(leftWidth);
        fieldName.setX(leftX);
        fieldName.setY(leftRow);
        if(!fieldName.isFocused())
        {
            fieldName.setSelectionPos(fieldName.getText().length());
        }
        fieldName.drawTextBox();

        // Coordinates
        leftRow+=vgap+vpad;
        drawLabel(locationTitle, leftX, leftRow);
        leftRow+=12;
        drawLabelAndField(labelX, fieldX, leftX, leftRow);
        drawLabelAndField(labelZ, fieldZ, fieldX.getX() + fieldX.getWidth() + hgap, leftRow);
        drawLabelAndField(labelY, fieldY, fieldZ.getX() + fieldZ.getWidth() + hgap, leftRow);

        // Color
        leftRow+=vgap+vpad;
        drawLabel(colorTitle, leftX, leftRow);
        leftRow+=12;
        drawLabelAndField(labelR, fieldR, leftX, leftRow);
        drawLabelAndField(labelG, fieldG, fieldR.getX() + fieldR.getWidth() + hgap, leftRow);
        drawLabelAndField(labelB, fieldB, fieldG.getX() + fieldG.getWidth() + hgap, leftRow);
        buttonRandomize.setWidth(4 + Math.max(fieldB.getX()+fieldB.getWidth() - fieldR.getX(), 10 + fr.getStringWidth(buttonRandomize.displayString)));
        buttonRandomize.setPosition(fieldR.getX()-2, leftRow += vgap);

        // Color picker
        int cpY = fieldB.getY();
        int cpSize = buttonRandomize.getY() + buttonRandomize.getHeight() - cpY - 2;
        int cpHAreaX = fieldB.getX() + fieldB.getWidth();
        int cpHArea = (fieldName.getX() + fieldName.getWidth()) - (cpHAreaX);
        int cpX = cpHAreaX + (cpHArea-cpSize);
        drawColorPicker(cpX, cpY, cpSize);

        // WP icon
        int iconX = cpHAreaX + ((cpX - cpHAreaX)/2) - (wpTexture.width/2) + 1;
        int iconY = buttonRandomize.getY() - vpad/2;
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
        int scrollHeight = (buttonReset.getY() + buttonReset.getHeight() -2) - rightRow;
        dimScrollPane.position(dcw, scrollHeight, 0, scrollHeight, rightX, rightRow);

        // Remove(Cancel) / Save
        int totalRow = Math.max(leftRow + vgap, rightRow + vgap);

        buttonRemove.setWidth((totalWidth / 2) - 4);
        buttonRemove.setPosition(leftX -2, totalRow);

        buttonSave.setWidth((totalWidth / 2) - 4);
        buttonSave.setPosition(rightXEnd + 3 - buttonSave.getWidth(), totalRow);
	}

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        drawBackground(0);
        layoutButtons();

        dimScrollPane.drawScreen(x, y, par3);

        for (int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k);
            guibutton.drawButton(this.mc, x, y);
        }

        drawLogo();
        drawTitle();
    }

    protected void drawWaypoint(int x, int y)
    {
        DrawUtil.drawColoredImage(wpTexture, 255, currentColor, x, y - (wpTexture.height / 2));
    }

    protected void drawColorPicker(int x, int y, float size)
    {
        int sizeI = (int) size;
        drawRect(x - 1, y - 1, x + sizeI + 1, y + sizeI + 1, -6250336);

        if(colorPickRect.width!=size) {
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
        drawString(getFontRenderer(), label, x - width, y + (field.getHeight()-8)/2, Color.lightGray.getRGB());
        field.drawTextBox();
    }

    protected void drawLabel(String label, int x, int y)
    {
        drawString(getFontRenderer(), label, x , y , Color.lightGray.getRGB());
    }

    protected void keyTyped(char par1, int par2)
    {
        switch(par2)
        {
            case Keyboard.KEY_ESCAPE :
                UIManager.getInstance().openMap();
                return;
            case Keyboard.KEY_RETURN :
                save();
                return;
            case Keyboard.KEY_TAB :
                validate();
                onTab();
                return;
            default:
                break;
        }

        for(GuiTextField field : fieldList) {
            boolean done = field.textboxKeyTyped(par1, par2);
            if(done) break;
        }

        validate();
    }

    @Override
    protected void mouseClickMove(int par1, int par2, int par3, long par4) {
        checkColorPicker(par1, par2);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for(GuiTextField field : fieldList) {
            field.mouseClicked(mouseX, mouseY, mouseButton);
        }

        checkColorPicker(mouseX, mouseY);

        Button button = dimScrollPane.mouseClicked(mouseX, mouseY, mouseButton);
        if(button!=null)
        {
            actionPerformed(button);
        }
    }

    protected void checkColorPicker(int mouseX, int mouseY) {
        if(colorPickRect.contains(mouseX, mouseY)) {
            int x = mouseX - (int) colorPickRect.x;
            int y = mouseY - (int) colorPickRect.y;
            setFormColor(new Color(colorPickImg.getRGB(x, y)));
        }
    }

    protected void setFormColor(Color color) {
        currentColor = color;
        fieldR.setText(Integer.toString(color.getRed()));
        fieldG.setText(Integer.toString(color.getGreen()));
        fieldB.setText(Integer.toString(color.getBlue()));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) { // actionPerformed

        if(dimButtonList.contains(guibutton)) {
            DimensionButton dimButton = (DimensionButton) guibutton;
            dimButton.toggle();
        } else {
            final ButtonEnum id = ButtonEnum.values()[guibutton.id];
            switch(id) {

                case Randomize: {
                    setFormColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                    break;
                }
                case Enable: {
                    buttonEnable.toggle();
                    break;
                }
                case Remove: {
                    WaypointHelper.removeNative(waypoint);
                    refreshAndClose();
                    break;
                }
                case Reset: {
                    initGui();
                    break;
                }
                case Save: {
                    save();
                    break;
                }
            }
        }
	}

    protected void onTab()
    {
        boolean focusNext = false;
        boolean foundFocus = false;
        for(GuiTextField field : fieldList) {
            if(focusNext)
            {
                field.setFocused(true);
                foundFocus = true;
                break;
            }
            if(field.isFocused())
            {
                field.setFocused(false);
                focusNext = true;
            }
        }
        if(!foundFocus) fieldList.get(0).setFocused(true);
    }

    protected boolean validate()
    {
        boolean valid = true;
        valid = fieldName.hasMinLength();
        this.buttonSave.enabled = valid;
        return valid;
    }

    protected void save()
    {
        if(!validate()) return;
        updateWaypointFromForm();
        WaypointHelper.addNative(waypoint);
        refreshAndClose();
    }

    protected void updateWaypointFromForm()
    {
        waypoint.setColor(currentColor);

        ArrayList<Integer> dims = new ArrayList<Integer>();
        for(DimensionButton db : dimButtonList)
        {
            if(db.getToggled()) dims.add(db.dimension);
        }
        waypoint.setDimensions(dims);
        waypoint.setDisplay(fieldName.getText());
        waypoint.setEnable(buttonEnable.getToggled());
        waypoint.setName(fieldName.getText());
        waypoint.setLocation(Integer.parseInt(fieldX.getText()), Integer.parseInt(fieldY.getText()), Integer.parseInt(fieldZ.getText()));
    }

    protected void refreshAndClose() {
        DataCache.instance().forceRefresh(WaypointsData.class);
        MapOverlay.state().requireRefresh();
        UIManager.getInstance().openMap();
    }
}
