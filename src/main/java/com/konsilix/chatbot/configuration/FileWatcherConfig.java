package com.konsilix.chatbot.configuration;

import com.konsilix.utils.Utils;
import com.konsilix.chatbot.listeners.AppFileChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// SUGGESTION: @Scope("singleton") is redundant, as @Configuration beans are singletons by default.
// import org.springframework.context.annotation.Scope;

import java.io.File;
import java.time.Duration;

@Slf4j
@Configuration
// @Scope("singleton") // This is not needed.
public class FileWatcherConfig {
    public static final long DEFAULT_POLL_INTERVAL_MS = 2000L;
    public static final long DEFAULT_QUIET_PERIOD_MS = 1000L;

    // SUGGESTION: The checkAndCreateDirectory call below handles this, so this TODO can be removed.
    @Value("${app.storage.location}")
    private String location;

    @Value("${server.port}")
    private Integer serverPort;

    // TODO: add listener for file DELETE, MOVE, etc

    @Bean
    public FileSystemWatcher fileSystemWatcher() {
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher(true,
                Duration.ofMillis(DEFAULT_POLL_INTERVAL_MS), Duration.ofMillis(DEFAULT_QUIET_PERIOD_MS));

        // This logic correctly handles the initial condition.
        if (!Utils.checkAndCreateDirectory(location)) {
            log.info("Storage directory '{}' was created.", location);
        } else {
            log.info("Using existing storage directory: '{}'", location);
        }

        fileSystemWatcher.addSourceDirectory(new File(location));
        fileSystemWatcher.addListener(new AppFileChangeListener(serverPort));
        fileSystemWatcher.start();

        // SUGGESTION: Use the SLF4J logger for consistency instead of System.out.
        log.info("Started FileSystemWatcher on directory: '{}'", location);
        return fileSystemWatcher;
    }
}