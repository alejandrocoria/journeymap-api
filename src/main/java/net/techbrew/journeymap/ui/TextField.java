package net.techbrew.journeymap.ui;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

/**
 * Created by mwoodman on 3/1/14.
 */
public class TextField extends GuiTextField {

    // ReflectionHelper field indices
    protected static final int INDEX_X = 1;
    protected static final int INDEX_Y = 2;
    protected static final int INDEX_WIDTH = 3;
    protected static final int INDEX_HEIGHT = 4;

    protected final String numericRegex;
    protected final boolean numeric;
    protected final boolean allowNegative;
    protected int minLength;
    protected Integer clampMin;
    protected Integer clampMax;

    public TextField(Object text, FontRenderer fontRenderer, int width, int height) {
        this(text, fontRenderer, width, height, false, false);
    }

    public TextField(Object text, FontRenderer fontRenderer, int width, int height, boolean isNumeric, boolean negative) {
        super(fontRenderer, 0, 0, width, height);
        setText(text.toString());
        numeric = isNumeric;
        allowNegative = negative;

        String regex = null;
        if(numeric) {
            if(allowNegative) {
                regex = "[^-?\\d]";
            } else {
                regex = "[^\\d]";
            }
        }

        numericRegex = regex;
    }

    public void setClamp(Integer min, Integer max) {
        this.clampMin = min;
        this.clampMax = max;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    @Override
    public void writeText(String par1Str) {
        super.writeText(par1Str);
        if(numeric) {
            String fixed = getText().replaceAll(numericRegex, "");
            if(allowNegative) {
                // TODO: figure out how to make regex disallow "-" after first char
                String start = fixed.startsWith("-") ? "-" : "";
                fixed = start + fixed.replaceAll("-","");
            }
            super.setText(fixed);
        }
    }

    public void setText(Object object) {
        super.setText(object.toString());
    }

    public boolean isNumeric() {
        return numeric;
    }

    public boolean hasMinLength() {
        String text = getText();
        int textLen = text == null ? 0 : text.length();
        return minLength <= textLen;
    }

    public boolean textboxKeyTyped(char par1, int par2)
    {
        boolean res = super.textboxKeyTyped(par1, par2);
        if (numeric && this.isFocused()) {
            clamp();
        }
        return res;
    }

    /**
     * Draws the textbox
     */
    public void drawTextBox()
    {
        super.drawTextBox();
        if (this.getVisible())
        {
            if(!hasMinLength())
            {
                int red = Color.red.getRGB();
                int x1 = getX()-1;
                int y1 = getY()-1;
                int x2 = x1 + getWidth()+1;
                int y2 = y1 + getHeight()+1;

                drawRect(x1, y1, x2, y1+1, red);
                drawRect(x1, y2, x2, y2+1, red);

                drawRect(x1, y1, x1+1, y2, red);
                drawRect(x2, y1, x2+1, y2, red);
            }
        }

    }

    /**
     * If numeric field, clamp values in range.
     */
    public void clamp() {
        if(!numeric) return;

        String text = getText();
        if(clampMin!=null) {

            if(text==null || text.length()==0) {
                return;
            }

            try {
                setText(Math.max(clampMin,Integer.parseInt(text)));
            } catch(Exception e) {
                setText(clampMin);
            }
            if(clampMax!=null) {
                try {
                    setText(Math.min(clampMax,Integer.parseInt(text)));
                } catch(Exception e) {
                    setText(clampMax);
                }
            }
        }
    }

    public int getX()
    {
        return ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_X);
    }

    public void setX(int x)
    {
        ReflectionHelper.setPrivateValue(GuiTextField.class, this, x, INDEX_X);
    }

    public int getY()
    {
        return ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_Y);
    }

    public void setY(int y)
    {
        ReflectionHelper.setPrivateValue(GuiTextField.class, this, y, INDEX_Y);
    }

    public int getWidth() {
        return ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_WIDTH);
    }

    public void setWidth(int w) {
        ReflectionHelper.setPrivateValue(GuiTextField.class, this, w, INDEX_WIDTH);
    }

    public int getHeight() {
        return ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_HEIGHT);
    }

}
