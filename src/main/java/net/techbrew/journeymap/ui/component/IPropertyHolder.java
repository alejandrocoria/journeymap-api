package net.techbrew.journeymap.ui.component;

/**
 * Created by Mark on 10/10/2014.
 */
public interface IPropertyHolder<T>
{
    public T getPropertyValue();

    public void setPropertyValue(T value);
}
