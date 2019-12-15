package journeymap.client.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class TextBoxButton extends Button
{
    protected TextBox textBox;

    public TextBoxButton(String text)
    {
        super(text);
    }

    public TextBoxButton(Object text, FontRenderer fontRenderer, int width, int height)
    {
        this(text, fontRenderer, width, height, false, false);
    }

    public TextBoxButton(Object text, FontRenderer fontRenderer, int width, int height, boolean isNumeric, boolean negative)
    {
        super(text.toString());
        textBox = new TextBox(text, fontRenderer, width, height - 4, isNumeric, negative);
    }

    public String getText()
    {
        return textBox.getText();
    }

    public String getSelectedText()
    {
        return textBox.getSelectedText();
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks)
    {
        textBox.setMinLength(1);
        textBox.setX(this.getX());
        textBox.setY(this.getY());
        textBox.drawTextBox();
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
    {
        textBox.setFocused(true);
        return textBox.mouseClicked(mouseX, mouseY, 0);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode)
    {
        return textBox.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public boolean isMouseOver()
    {
        return textBox.isFocused();
    }

    @Override
    public void setVisible(boolean visible)
    {
        textBox.setVisible(visible);
        super.setVisible(visible);
    }

    @Override
    public int getCenterX()
    {
        return textBox.getCenterX();
    }

    @Override
    public int getRightX()
    {
        return textBox.getRightX();
    }

    @Override
    public int getBottomY()
    {
        return textBox.getBottomY();
    }

    @Override
    public int getMiddleY()
    {
        return textBox.getMiddleY();
    }

    @Override
    public int getWidth()
    {
        if (textBox != null)
        {
            return textBox.getWidth();
        }
        return this.width;
    }

    @Override
    public int getHeight()
    {
        if (textBox != null)
        {
            return textBox.getHeight();
        }
        return this.height;
    }

    public void setText(String text)
    {
        textBox.setText(text);
    }
}