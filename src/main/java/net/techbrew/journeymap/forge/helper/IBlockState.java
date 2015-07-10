/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.forge.helper;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;

import java.util.Collection;

/**
 * Backported from 1.8's net.minecraft.block.state.IBlockState
 */
public interface IBlockState
{
    /**
     * Get the names of all properties defined for this BlockState
     */
    Collection getPropertyNames();

    /**
     * Get the value of the given Property for this BlockState
     */
    Comparable getValue(IProperty property);

    /**
     * Get a version of this BlockState with the given Property now set to the given value
     */
    IBlockState withProperty(IProperty property, Comparable value);

    /**
     * Create a version of this BlockState with the given property cycled to the next value in order. If the property
     * was at the highest possible value, it is set to the lowest one instead.
     */
    IBlockState cycleProperty(IProperty property);

    /**
     * Get all properties of this BlockState. The returned Map maps from properties (IProperty) to the respective
     * current value.
     */
    ImmutableMap getProperties();

    Block getBlock();
}