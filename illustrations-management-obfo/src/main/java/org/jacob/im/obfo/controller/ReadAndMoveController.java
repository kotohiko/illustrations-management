package org.jacob.im.obfo.controller;

import org.jacob.im.common.constants.IMCommonConstants;
import org.jacob.im.common.helper.IMCommonHelper;
import org.jacob.im.common.response.ResManager;
import org.jacob.im.ifp.api.IFPParsingApi;
import org.jacob.im.obfo.constants.OBFOConstants;
import org.jacob.im.obfo.service.ReadAndMoveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This class is responsible for handling the main execution logic
 * for reading YAML configuration files and moving files based on the specified paths.
 * It uses a {@link BufferedReader} to read user input from the console and processes the input
 * to perform file operations.
 *
 * @author Kotohiko
 * @since 14:38 Sep 12, 2024
 */
public class ReadAndMoveController {

    /**
     * The logger instance used for logging messages related to the {@link ReadAndMoveController} class.
     * This logger is configured to log messages at various levels (e.g., debug, info, error) and can be
     * used throughout the class to provide detailed information about the watcher's operations.
     */
    private static final Logger logger = LoggerFactory.getLogger(ReadAndMoveController.class);

    /**
     * The main execution method of the {@link ReadAndMoveController} class. This method handles user input,
     * reads YAML configuration files, and performs file moving operations based on the provided paths.
     */
    public static void mainPart() {
        System.out.print(OBFOConstants.WELCOME_LINE);
        try (BufferedReader in = IMCommonHelper.consoleReader()) {
            String targetPathKey;
            Yaml yaml = new Yaml();
            while (true) {
                System.out.print(ResManager.loadResString("ReadAndMoveController_0"));
                if ((targetPathKey = in.readLine()) == null) {
                    break;
                }

                boolean switchToIFP = IFPParsingApi.getAndParse(targetPathKey);
                if (isValidPath(targetPathKey)) {
                    openFolder(targetPathKey);
                    System.out.println(IMCommonConstants.SEPARATOR_LINE);
                } else if (switchToIFP) {
                    System.out.println(IMCommonConstants.SEPARATOR_LINE);
                } else {
                    // Load a YAML file into a Java object.
                    Map<String, String> pathsData = yaml.load(ReadAndMoveService.loadYamlFile());
                    String defaultSourcePath = pathsData.get("Default source path");
                    if (defaultSourcePath == null || defaultSourcePath.isEmpty()) {
                        logger.error(ResManager.loadResString("ReadAndMoveController_1"));
                    } else {
                        ReadAndMoveService.filesMove(defaultSourcePath, pathsData, targetPathKey);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(ResManager.loadResString("ReadAndMoveController_2"), e);
        }
        ReadAndMoveService.endLinePrintAndReboot();
    }

    /**
     * Opens a folder using the explorer.exe command.
     *
     * @param path The path of the folder to be opened.
     */
    private static void openFolder(String path) {
        try {
            // Create a ProcessBuilder instance with the command to execute
            ProcessBuilder builder = new ProcessBuilder(OBFOConstants.EXPLORER_EXE, path);
            builder.start();
            logger.info(ResManager.loadResString("ReadAndMoveController_4", path));
        } catch (IOException e) {
            logger.error(ResManager.loadResString("ReadAndMoveController_3"));
        }
    }

    /**
     * Checks if the given path is valid.
     *
     * @param path The path to validate.
     * @return true if the path is valid, false otherwise.
     */
    private static boolean isValidPath(String path) {
        try {
            Path p = Paths.get(path);
            return Files.exists(p);
        } catch (InvalidPathException e) {
            return false;
        }
    }
}