package org.jacob.im.obfo.monitor;

import org.jacob.im.obfo.constants.OBFOConstants;
import org.jacob.im.obfo.monitor.core.NewFilesAddedWatcher;

import java.nio.file.Paths;

/**
 * Adding files operation monitor.
 *
 * @author Kotohiko
 * @since 10:16 Sep 03, 2024
 */
public class MonitorStarter {
    public static String monitorStarter() {
        NewFilesAddedWatcher addFileMonitor = new NewFilesAddedWatcher(Paths
                .get(OBFOConstants.UNCLASSIFIED_REMAINING_IMAGES_FOLDER_PATH), 5);
        return addFileMonitor.toString();
    }
}