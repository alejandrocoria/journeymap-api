package net.techbrew.journeymap.ui.component;

/**
 * Created by Mark on 10/10/2014.
 */
public interface IPropertyHolder<H, T>
{
    public H getProperty();

    public T getPropertyValue();

    public void setPropertyValue(T value);
}
