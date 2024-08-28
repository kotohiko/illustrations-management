package org.jacob.im.obfo.utils.collector;

import org.jacob.im.obfo.constants.OBFOConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacob Suen
 * @since 12:49 Aug 24, 2024
 */
public class DirectoriesIterator {

    public static void main(String[] args) {
        List<String> subdirectories = new ArrayList<>();
        printSubdirectories(OBFOConstants.MY_GALLERY_PATH, subdirectories);
        // 将结果写入文件
        writeToFile(subdirectories);
    }

    public static void printSubdirectories(String directory, List<String> subdirectories) {
        File rootDir = new File(directory);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Directory does not exist or is not a directory: " + directory);
            return;
        }
        // 递归遍历目录
        traverseDirectory(rootDir, subdirectories);
    }

    private static void traverseDirectory(File directory, List<String> subdirectories) {
        // 获取目录下的所有文件和子目录
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    subdirectories.add(file.getAbsolutePath());
                    // 递归遍历子目录
                    traverseDirectory(file, subdirectories);
                }
            }
        }
    }

    private static void writeToFile(List<String> subdirectories) {
        Path outputPath = Paths.get(OBFOConstants.PATH_COLLECTION_TXT);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
            for (String subdirectory : subdirectories) {
                writer.println(subdirectory);
            }
        } catch (IOException e) {
            System.out.println("出现了异常");
        }
    }
}