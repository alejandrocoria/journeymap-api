package net.techbrew.mcjm.waypoint;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.techbrew.mcjm.model.Waypoint;

/**
 * Created by mwoodman on 12/26/13.
 */
public class EntityWaypoint extends Entity implements Comparable<EntityWaypoint> {

    private Waypoint waypoint;

    public EntityWaypoint(World world, Waypoint waypoint) {
        super(world);
        this.waypoint = waypoint;
        this.setPosition(waypoint.getX(), waypoint.getY(), waypoint.getZ());
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound var1) {
        entityInit();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1) {
        entityInit();
    }

    @Override
    public int compareTo(EntityWaypoint o) {
        return waypoint.getId().compareTo(o.waypoint.getId());
    }
}
