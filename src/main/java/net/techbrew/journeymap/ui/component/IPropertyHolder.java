/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

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
