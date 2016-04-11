package journeymap.common.migrate;

import journeymap.common.version.Version;

import java.util.concurrent.Callable;

/**
 * Describes a task to be run on initialization for the purpose
 * of migrating data, configs, etc.
 */
public interface MigrationTask extends Callable<Boolean>
{
    /**
     * Whether the task should be run for the currentVersion
     *
     * @param currentVersion the current version
     * @return true if the task should be run
     */
    public boolean isActive(Version currentVersion);
}
