package com.konsilix.theApp.configuration;

import com.konsilix.utils.Utils;
import com.konsilix.theApp.listeners.AppFileChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.time.Duration;

@Slf4j // this gives access to a logger called "log"
@Configuration
@Scope("singleton")
public class FileWatcherConfig {
    //    private static final Logger logger = LoggerFactory.getLogger(FileWatcherConfig.class);
    public static final long DEFAULT_POLL_INTERVAL_MS = 2000L;
    public static final long DEFAULT_QUIET_PERIOD_MS = 1000L;

    // TODO: need to code around initial condition if upload-dir is not created
    @Value("${storage.location}")
    private String location;

    @Value("${server.port}")
    private Integer serverPort;

    // TODO: add listener for file DELETE, MOVE, etc

    @Bean
    public FileSystemWatcher fileSystemWatcher() {
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher(true,
                Duration.ofMillis(DEFAULT_POLL_INTERVAL_MS), Duration.ofMillis(DEFAULT_QUIET_PERIOD_MS));

        if (! Utils.checkAndCreateDirectory(location)) {
            log.info("{} directory was created.",location);
        }
        fileSystemWatcher.addSourceDirectory(new File(location));
        fileSystemWatcher.addListener( new AppFileChangeListener(serverPort) ) ;

        fileSystemWatcher.start() ;

        System.out.println( "started fileSystemWatcher" ) ;
        return(fileSystemWatcher);
    }
}