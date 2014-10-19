package net.techbrew.journeymap;

import com.google.common.base.Joiner;

/**
 * Created by Mark on 10/18/2014.
 */
public class Version implements Comparable<Version>
{
    public final int major;
    public final int minor;
    public final int micro;
    public final String patch;

    public Version(int major, int minor, int micro)
    {
        this(major, minor, micro, "");
    }

    public Version(int major, int minor, int micro, String patch)
    {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.patch = patch != null ? patch : "";
    }

    public static Version from(String major, String minor, String micro, String patch, Version defaultVersion)
    {
        try
        {
            return new Version(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(micro), patch);
        }
        catch (Exception e)
        {
            return defaultVersion;
        }
    }

    @Override
    public String toString()
    {
        return Joiner.on(".").join(major, minor, micro + patch);
    }

    public String toMajorMinorString()
    {
        return Joiner.on(".").join(major, minor);
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

        Version version = (Version) o;

        if (major != version.major)
        {
            return false;
        }
        if (micro != version.micro)
        {
            return false;
        }
        if (minor != version.minor)
        {
            return false;
        }
        if (!patch.equals(version.patch))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + micro;
        result = 31 * result + patch.hashCode();
        return result;
    }

    public boolean isAfter(Version other)
    {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(Version other)
    {
        int result = Integer.compare(major, other.major);
        if (result != 0)
        {
            result = Integer.compare(minor, other.minor);
        }
        if (result != 0)
        {
            result = Integer.compare(micro, other.micro);
        }
        if (result != 0)
        {
            if (patch.length() == 0 || other.patch.length() == 0)
            {
                // TODO test this
                result = Integer.compare(other.patch.length(), patch.length());
            }
            else
            {
                result = patch.compareTo(other.patch);
            }
        }
        return result;
    }
}
