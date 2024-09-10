package org.jacob.im.obfo.monitor.core;

import org.jacob.im.obfo.constants.OBFOConstants;
import org.jacob.im.obfo.logger.OBFOLogger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A monitor that watches for changes and events related to files.
 *
 * @author Kotohiko
 * @since 16:29 Aug 18, 2024
 */
public class NewFilesAddedWatcher {

    private static final String WATCHER_THREAD_NAME = "NewFilesAddedWatcher";
    private static final String SERVER_LOG_INFO = "[Server] [Monitor service]";

    /**
     * A watch service that <em>watches</em> registered objects for changes and events.
     */
    private final WatchService watcher;

    /**
     * An object that may be used to locate a file in a file system.
     * It will typically represent a system dependent file path.
     */
    private final Path dir;

    /**
     * An {@link Executor} that provides methods to manage termination and
     * methods that can produce a {@link Future} for tracking progress of
     * one or more asynchronous tasks.
     */
    private final ExecutorService executor;

    private static final AtomicInteger THREAD_ID_SEQ = new AtomicInteger(0);

    /**
     * Constructor of {@link NewFilesAddedWatcher}.<p>
     * This will submit the {@link NewFilesAddedWatcher#startWatching()} method of the current object
     * as a task to the executor thread pool for asynchronous execution. This task will be executed
     * on a thread in the thread pool without blocking the current thread.<p>
     * If you don't understand the new writing style:{@code executor.submit(this::startWatching);},
     * you can refer to the old writing style:{@code executor.submit(() -> startWatching());}
     *
     * @param dir             The directory that needs to be monitored
     * @param numberOfThreads Number of threads
     */
    public NewFilesAddedWatcher(Path dir, int numberOfThreads) {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.dir = dir;
            // Create a fixed-size thread pool
            this.executor = Executors.newFixedThreadPool(numberOfThreads, r -> {
                Thread t = new Thread(r, WATCHER_THREAD_NAME + "-" + THREAD_ID_SEQ.incrementAndGet());
                // 设置为守护线程
                t.setDaemon(true);
                return t;
            });
            // Start monitoring the directory upon initialization
            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            executor.submit(this::startWatching);
            printPerformanceInfo();
        } catch (IOException e) {
            System.out.println("An IOException has occurred. Please identify and rectify the source of the problem.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Monitor core logic
     */
    private void startWatching() {
        try {
            while (true) {
                // Block and wait for event occurs
                var key = watcher.take();
                // Submit a task for each event in the thread pool.
                executor.submit(() -> {
                    var events = key.pollEvents();
                    for (WatchEvent<?> event : events) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        var ev = (WatchEvent<Path>) event;
                        var filename = dir.resolve(ev.context());
                        processFile(filename);
                    }
                });
                // Reset the key
                var valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            System.out.println("An InterruptedException occurred."
                    + " Please identify and rectify the source of the problem.");
            // May need to gracefully shut down the thread pool.
            shutdown();
        }
    }

    /**
     * Record the total of files
     *
     * @param fileWithAbsPath Files with absolute path
     */
    private void processFile(Path fileWithAbsPath) {
        var folder = new File(OBFOConstants.UNCLASSIFIED_REMAINING_IMAGES_FOLDER_PATH);
        var fileCount = 0;
        // Iterate through all files in the directory
        for (File allFiles : Objects.requireNonNull(folder.listFiles())) {
            if (allFiles.isFile()) {
                ++fileCount;
            }
        }
        OBFOLogger.filesAddedLogWriter(fileWithAbsPath, fileCount);
    }

    private void printPerformanceInfo() {
        // Print memory usage
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        System.out.println("Total Memory: " + totalMemory / 1024 / 1024 + " MB");
        System.out.println("Free Memory: " + freeMemory / 1024 / 1024 + " MB");
        System.out.println("Used Memory: " + usedMemory / 1024 / 1024 + " MB");

        // Print thread information related to Watcher threads
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.getAllThreadIds();
        System.out.println("Watcher Threads:");
        for (long id : threadIds) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(id);
            System.out.println("Thread Name: " + threadInfo.getThreadName()
                    + ", State: " + threadInfo.getThreadState());
        }
    }

    /**
     * Shut down the thread pool gracefully.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}