/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.ui.option;

import java.util.List;

/**
 * Created by Mark on 9/25/2014.
 */
public interface StringListProvider
{
    public List<String> getStrings();

    public String getDefaultString();
}
