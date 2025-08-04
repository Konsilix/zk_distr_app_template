package com.konsilix.services;

import com.konsilix.zk.configuration.BeanConfiguration;

import com.konsilix.chatbot.models.ClusterInfo;
import com.konsilix.chatbot.models.File;
import com.konsilix.zk.ZkService;
import com.konsilix.zk.watchers.JobListener;
import com.konsilix.zk.ZkDemoUtil;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.konsilix.zk.ZkDemoUtil.*;

/** @author "Bikas Katwal" 26/03/19 */
/** @author "Rob Marano" 2023/05/01 - updated, expanded */
@Slf4j
@Component
public class OnStartUpApplication implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    BeanConfiguration beanConfiguration;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired private ZkService zkService;

    @Qualifier("allNodesChangeListener")
    @Autowired private IZkChildListener allNodesChangeListener;

    @Qualifier("liveNodeChangeListener")
    @Autowired private IZkChildListener liveNodeChangeListener;

    @Qualifier("masterChangeListener")
    @Autowired private IZkChildListener masterChangeListener;

    @Qualifier("connectStateChangeListener")
    @Autowired private IZkStateListener connectStateChangeListener;

    @Qualifier("dataChangeListener")
    @Autowired private IZkDataListener dataChangeListener;

    @Qualifier("jobListener")
    @Autowired private JobListener jobListener;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info(String.format("*** [OnStartUpApplication] contextRefreshedEvent: %s\n", contextRefreshedEvent));
        try {

            zkService.createAllParentNodes();

            zkService.addToAllNodes(beanConfiguration.getZkHostPort(), "cluster node");
            ClusterInfo.getClusterInfo().getAllNodes().clear();
            ClusterInfo.getClusterInfo().getAllNodes().addAll(zkService.getAllNodes());

            String leaderElectionAlgo = System.getProperty("leader.algo");

            if (isEmpty(leaderElectionAlgo) || "2".equals(leaderElectionAlgo)) {
                zkService.createNodeInElectionZnode(beanConfiguration.getZkHostPort());
                ClusterInfo.getClusterInfo().setMaster(zkService.getLeaderNodeData2());
            } else {
                if (!zkService.masterExists()) {
                    zkService.electForMaster();
                } else {
                    ClusterInfo.getClusterInfo().setMaster(zkService.getLeaderNodeData());
                }
            }

            syncDataFromMaster();

            zkService.addToLiveNodes(beanConfiguration.getZkHostPort(), "cluster node");
            ClusterInfo.getClusterInfo().getLiveNodes().clear();
            ClusterInfo.getClusterInfo().getLiveNodes().addAll(zkService.getLiveNodes());

            if (isEmpty(leaderElectionAlgo) || "2".equals(leaderElectionAlgo)) {
                zkService.registerChildrenChangeWatcher(ELECTION_NODE_2, masterChangeListener);
            } else {
                zkService.registerChildrenChangeWatcher(ELECTION_NODE, masterChangeListener);
            }
            zkService.registerChildrenChangeWatcher(LIVE_NODES, liveNodeChangeListener);
            zkService.registerChildrenChangeWatcher(ALL_NODES, allNodesChangeListener);
            zkService.registerZkSessionStateListener(connectStateChangeListener);
            zkService.registerDataChangeWatcher(DATA, dataChangeListener);

            zkService.registerChildrenChangeWatcher(FILES_NODE, jobListener);

        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new RuntimeException("Startup failed!!", e);
        }
    }

    private void syncDataFromMaster() {
        log.info(String.format("--- [OnStartUpApplication] syncDataFromMaster()\n"));
        if (beanConfiguration.getZkHostPort().equals(ClusterInfo.getClusterInfo().getMaster())) {
            log.info(String.format("--- [OnStartUpApplication] syncDataFromMaster() OK - ON MASTER.\n"));
            return;
        }
        String requestUrl;
        requestUrl = "http://".concat(ClusterInfo.getClusterInfo().getMaster().concat("/files"));
        List<File> files = restTemplate.getForObject(requestUrl, List.class);
        log.info(String.format("--- [OnStartUpApplication] syncDataFromMaster() - files: %s\n", files));
        if (files != null) {
            DataStorage.getFileListFromStorage().addAll(files);
        }
    }
}
