package net.techbrew.journeymap.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.techbrew.journeymap.model.Waypoint;

/**
 * Created by mwoodman on 12/26/13.
 */
public class EntityWaypoint extends Entity implements Comparable<EntityWaypoint> {

    private Waypoint waypoint;

    public EntityWaypoint(World world, Waypoint waypoint) {
        super(world);
        this.waypoint = waypoint;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    @Override
    protected void entityInit() {
        int dimension = Minecraft.getMinecraft().thePlayer.dimension;
        this.setPosition(waypoint.getX(dimension), waypoint.getY(dimension), waypoint.getZ(dimension));
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
