package com.konsilix.zk.services;

import com.konsilix.zk.ZkService;
import com.konsilix.zk.ZkDemoUtil;
import com.konsilix.zk.services.vertexai.RagService;
import com.konsilix.zk.util.ZkPersistenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MasterCoordinatorService {

    private final ZkService zkService;
    private final StorageService storageService;
    private final ZkPersistenceUtil zkPersistenceUtil;
    private final RagService ragService;

    @Autowired
    public MasterCoordinatorService(ZkService zkService, StorageService storageService, ZkPersistenceUtil zkPersistenceUtil, RagService ragService) {
        this.zkService = zkService;
        this.storageService = storageService;
        this.zkPersistenceUtil = zkPersistenceUtil;
        this.ragService = ragService;
    }

    public void synchronizeDriveState() {
        log.info("Master node elected. Starting local file synchronization...");
        try {
            List<Path> filePaths = storageService.loadAll().collect(Collectors.toList());
            String fileList = filePaths.stream()
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.joining(","));
            zkService.setZNodeData(ZkDemoUtil.DATA, fileList);
            log.info("Local file state synchronized and stored in ZK.");

            createJobsFromDriveState(filePaths);

        } catch (IOException e) {
            log.error("Failed to synchronize local file state and create jobs.", e);
        }
    }

    private void createJobsFromDriveState(List<Path> filePaths) {
        for (Path path : filePaths) {
            String fileName = path.getFileName().toString();
            zkService.createNodeInFilesZnode(fileName, "pending");
            log.info("Created job ZNode for file: {}", fileName);
        }
    }

    public void monitorJobs() {
        List<String> jobs = zkService.getChildren(ZkDemoUtil.FILES_NODE);

        for (String jobZNode : jobs) {
            String jobPath = ZkDemoUtil.FILES_NODE + "/" + jobZNode;
            String jobData = zkService.getZNodeData(jobPath);

            if (jobData != null && jobData.startsWith("in progress")) {
                log.warn("Job {} timed out. Re-queuing.", jobZNode);
                zkService.setZNodeData(jobPath, "pending");
            }
        }
    }

    public void aggregateKnowledgeBase() {
        List<String> liveNodes = zkService.getLiveNodes();
        long totalJobs = zkService.getChildren(ZkDemoUtil.FILES_NODE).size();
        long completedJobs = 0;

        for (String node : liveNodes) {
            String readyPath = ZkDemoUtil.KNOWLEDGE_BASE_NODE + "/" + node + "/knowledge_base.READY";
            if (zkService.exists(readyPath)) {
                completedJobs++;
            }
        }

        if (completedJobs == totalJobs) {
            log.info("All workers have completed their tasks. Starting aggregation.");

            Map<String, Map<String, Object>> aggregatedKnowledgeBase = new ConcurrentHashMap<>();
            for (String node : liveNodes) {
                String kbPath = ZkDemoUtil.KNOWLEDGE_BASE_NODE + "/" + node + "/knowledge_base.ser";
                try {
                    Map<String, Map<String, Object>> partialKB = zkPersistenceUtil.load(kbPath);
                    if (partialKB != null) {
                        aggregatedKnowledgeBase.putAll(partialKB);
                    }
                } catch (IOException e) {
                    log.error("Failed to load partial knowledge base from worker {}.", node, e);
                }
            }

            try {
                String finalKbPath = ZkDemoUtil.KNOWLEDGE_BASE_NODE + "/kb.master.ser";
                zkPersistenceUtil.save(finalKbPath, aggregatedKnowledgeBase);
                ragService.loadKnowledgeBase();
                log.info("Final knowledge base aggregated and stored at {}.", finalKbPath);
            } catch (IOException e) {
                log.error("Failed to save final knowledge base.", e);
            }
        }
    }
}
