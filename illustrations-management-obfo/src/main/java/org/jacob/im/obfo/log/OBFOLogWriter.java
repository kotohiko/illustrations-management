package org.jacob.im.obfo.log;

import org.jacob.im.common.IMCommonApi;
import org.jacob.im.obfo.constants.OBFOConstants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Kotohiko
 * @since 17:14 Sep 08, 2024
 */
public class OBFOLogWriter {

    private static final String LOG_FILE_EXCEPTION = "The log file cannot be found,"
            + " please check if the file name or path is configured correctly.";

    public static void filesAddedLogWriter(Path fileWithAbsPath, int fileCount) {
        // Write the number of files and date to log file.
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(OBFOConstants.PATH_OF_UNCLASSIFIED_REMAINING_IMAGES_LOG, Boolean.TRUE))) {
            bw.write(IMCommonApi.getRealTime()
                    + " INFO [Client] - New files added: "
                    + fileWithAbsPath.getFileName()
                    + "; Remaining unclassified images: "
                    + fileCount + "\n");
        } catch (IOException e) {
            System.out.println(LOG_FILE_EXCEPTION);
        }
    }

    public static void filesMoveLogWriter(int fileCount) {
        // Record the file count and date in the log file.
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(OBFOConstants.PATH_OF_UNCLASSIFIED_REMAINING_IMAGES_LOG, Boolean.TRUE))) {
            bw.write(IMCommonApi.getRealTime()
                    + " INFO [Client] - File(s) has/have been moved; "
                    + "Remaining unclassified images: "
                    + fileCount + "\n");
        } catch (IOException e) {
            System.out.println(LOG_FILE_EXCEPTION);
        }
    }
}