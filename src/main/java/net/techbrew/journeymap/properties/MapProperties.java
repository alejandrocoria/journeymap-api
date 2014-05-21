package net.techbrew.journeymap.properties;

/**
 * Shared Properties for the various map types.
 */
public abstract class MapProperties extends PropertiesBase implements Comparable<MapProperties>
{
    protected boolean showCaves = true; // PREF_SHOW_CAVES(Boolean.class,"preference_show_caves", true), //$NON-NLS-1$
    protected boolean showMobs = true; // PREF_SHOW_MOBS(Boolean.class,"preference_show_mobs", true), //$NON-NLS-1$
    protected boolean showAnimals = true; // PREF_SHOW_ANIMALS(Boolean.class,"preference_show_animals", true), //$NON-NLS-1$
    protected boolean showVillagers = true; // PREF_SHOW_VILLAGERS(Boolean.class,"preference_show_villagers", true), //$NON-NLS-1$
    protected boolean showPets = true; // PREF_SHOW_PETS(Boolean.class,"preference_show_pets", true), //$NON-NLS-1$
    protected boolean showPlayers = true; // PREF_SHOW_PLAYERS(Boolean.class,"preference_show_players", true), //$NON-NLS-1$
    protected boolean showWaypoints = true; // PREF_SHOW_WAYPOINTS(Boolean.class,"preference_show_waypoints", true), //$NON-NLS-1$
    protected boolean showGrid = true; // PREF_SHOW_GRID(Boolean.class,"preference_show_grid", true), //$NON-NLS-1$

    public boolean isShowCaves()
    {
        return showCaves;
    }

    public void setShowCaves(boolean showCaves)
    {
        this.showCaves = showCaves;
        save();
    }

    public boolean toggleShowCaves()
    {
        setShowCaves(!showCaves);
        return showCaves;
    }

    public boolean isShowMobs()
    {
        return showMobs;
    }

    public void setShowMobs(boolean showMobs)
    {
        this.showMobs = showMobs;
        save();
    }

    public boolean toggleShowMobs()
    {
        setShowMobs(!showMobs);
        return showMobs;
    }

    public boolean isShowAnimals()
    {
        return showAnimals;
    }

    public void setShowAnimals(boolean showAnimals)
    {
        this.showAnimals = showAnimals;
        save();
    }

    public boolean toggleShowAnimals()
    {
        setShowAnimals(!showAnimals);
        return showAnimals;
    }

    public boolean isShowVillagers()
    {
        return showVillagers;
    }

    public void setShowVillagers(boolean showVillagers)
    {
        this.showVillagers = showVillagers;
        save();
    }

    public boolean toggleShowVillagers()
    {
        setShowVillagers(!showVillagers);
        return showVillagers;
    }

    public boolean isShowPets()
    {
        return showPets;
    }

    public void setShowPets(boolean showPets)
    {
        this.showPets = showPets;
        save();
    }

    public boolean toggleShowPets()
    {
        setShowPets(!showPets);
        return showPets;
    }

    public boolean isShowPlayers()
    {
        return showPlayers;
    }

    public void setShowPlayers(boolean showPlayers)
    {
        this.showPlayers = showPlayers;
        save();
    }

    public boolean toggleShowPlayers()
    {
        setShowPlayers(!showPlayers);
        return showPlayers;
    }

    public boolean isShowWaypoints()
    {
        return showWaypoints;
    }

    public void setShowWaypoints(boolean showWaypoints)
    {
        this.showWaypoints = showWaypoints;
        save();
    }

    public boolean toggleShowWaypoints()
    {
        setShowWaypoints(!showWaypoints);
        return showWaypoints;
    }

    public boolean isShowGrid()
    {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid)
    {
        this.showGrid = showGrid;
        save();
    }

    public boolean toggleShowGrid()
    {
        setShowGrid(!showGrid);
        return showGrid;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MapProperties that = (MapProperties) o;
        return this.compareTo(that)==0;
    }

    @Override
    public int hashCode()
    {
        int result = (showCaves ? 1 : 0);
        result = 31 * result + (showMobs ? 1 : 0);
        result = 31 * result + (showAnimals ? 1 : 0);
        result = 31 * result + (showVillagers ? 1 : 0);
        result = 31 * result + (showPets ? 1 : 0);
        result = 31 * result + (showPlayers ? 1 : 0);
        result = 31 * result + (showWaypoints ? 1 : 0);
        result = 31 * result + (showGrid ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(MapProperties other)
    {
        return Integer.compare(this.hashCode(), other.hashCode());
    }
}
