package com.konsilix.zk.watchers;

import com.konsilix.chatbot.models.ClusterInfo;
import com.konsilix.zk.ZkDemoUtil;
import com.konsilix.zk.ZkService;
import com.konsilix.zk.services.DocumentTextExtractor;
import com.konsilix.zk.services.StorageService;
import com.konsilix.zk.services.vertexai.RagService;
import com.konsilix.zk.util.TextChunker;
import com.konsilix.zk.util.ZkPersistenceUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Setter
public class JobListener implements IZkChildListener {

    private ZkService zkService;
    private StorageService storageService;
    private DocumentTextExtractor documentTextExtractor;
    private TextChunker textChunker;
    private RagService ragService;
    private ZkPersistenceUtil zkPersistenceUtil;

    @Override
    public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
        log.info("Job list changed. New jobs available: {}", currentChildren);

        String myNode = ZkDemoUtil.getMyHostname();
        if (Objects.equals(ClusterInfo.getClusterInfo().getMaster(), myNode)) {
            log.info("This is the master, not a worker. Ignoring job change.");
            return;
        }

        String myNodeId = myNode.replace(":", "_");

        for (String jobZNode : currentChildren) {
            String jobPath = ZkDemoUtil.FILES_NODE + "/" + jobZNode;
            String jobData = zkService.getZNodeData(jobPath);

            if (jobData != null && jobData.equals("pending")) {
                try {
                    zkService.setZNodeData(jobPath, "in progress by " + myNode);
                    log.info("Worker {} successfully claimed job: {}", myNode, jobZNode);

                    processJob(jobZNode, myNodeId);

                } catch (Exception e) {
                    log.error("Failed to process job {} for worker {}.", jobZNode, myNode, e);
                }
            }
        }
    }

    private void processJob(String filename, String workerNodeId) {
        log.info("Worker {} is processing file: {}", workerNodeId, filename);

        try {
            Path filePath = storageService.load(filename);
            byte[] content = storageService.loadAsBytes(filePath);
            String extractedText = documentTextExtractor.extractText(filename, content);

            if (extractedText.isEmpty()) {
                log.warn("No text extracted from file: " + filename);
                return;
            }

            List<String> allChunks = textChunker.chunkText(extractedText);
            log.info("File '{}' was chunked into {} chunks. Embedding...", filename, allChunks.size());

            Map<String, Map<String, Object>> partialKnowledgeBase = new ConcurrentHashMap<>();
            for (int i = 0; i < allChunks.size(); i++) {
                String chunkText = allChunks.get(i);
                List<Float> embedding = ragService.embedText(chunkText);

                if (!embedding.isEmpty()) {
                    String chunkId = filename + "-" + i;
                    Map<String, Object> chunkData = new ConcurrentHashMap<>();
                    chunkData.put("text", chunkText);
                    chunkData.put("embedding", embedding);
                    chunkData.put("sourceFileName", filename);
                    partialKnowledgeBase.put(chunkId, chunkData);
                } else {
                    log.warn("Failed to get embedding for chunk " + filename + "-" + i);
                }
            }

            String kbPath = ZkDemoUtil.KNOWLEDGE_BASE_NODE + "/" + workerNodeId + "/knowledge_base.ser";
            zkPersistenceUtil.save(kbPath, partialKnowledgeBase);
            log.info("Worker {} saved partial knowledge base to {}.", workerNodeId, kbPath);

            String readyPath = ZkDemoUtil.KNOWLEDGE_BASE_NODE + "/" + workerNodeId + "/knowledge_base.READY";
            zkService.createEphemeralNode(readyPath, "completed");
            log.info("Worker {} completed job for file {}. Signaling completion.", workerNodeId, filename);

        } catch (IOException e) {
            log.error("Error processing file " + filename, e);
        }
    }
}
