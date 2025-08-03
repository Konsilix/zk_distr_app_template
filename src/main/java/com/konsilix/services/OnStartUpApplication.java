package com.konsilix.services;

import com.konsilix.zk.configuration.BeanConfiguration;

import com.konsilix.chatbot.models.ClusterInfo;
import com.konsilix.chatbot.models.File;
import com.konsilix.zk.ZkService;

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
@Slf4j // this gives access to a logger called "log"
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

    // TODO - check if "/files" node in ZK exists, if not create it; if so, don't try
    // avoid generating this exception
    // org.I0Itec.zkclient.exception.ZkNodeExistsException: org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists for /files

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info(String.format("*** [OnStartUpApplication] contextRefreshedEvent: %s\n", contextRefreshedEvent));
        try {

            // create all parent nodes /election, /all_nodes, /live_nodes, /app
            zkService.createAllParentNodes();

            // add this server to cluster by creating znode under /all_nodes, with name as "host:port"
//            String hostPort = getHostPortOfServer();
            zkService.addToAllNodes(beanConfiguration.getZkHostPort(), "cluster node");
            ClusterInfo.getClusterInfo().getAllNodes().clear();
            ClusterInfo.getClusterInfo().getAllNodes().addAll(zkService.getAllNodes());

            // check which leader election algorithm(1 or 2) need is used
            String leaderElectionAlgo = System.getProperty("leader.algo");

            // if approach 2 - create ephemeral sequential znode in /election
            // then get children of  /election and fetch least sequenced znode, among children znodes
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

            // sync person data from master
            //syncDataFromMaster();

            // add child znode under /live_node, to tell other servers that this server is ready to serve
            // read request
            zkService.addToLiveNodes(beanConfiguration.getZkHostPort(), "cluster node");
            ClusterInfo.getClusterInfo().getLiveNodes().clear();
            ClusterInfo.getClusterInfo().getLiveNodes().addAll(zkService.getLiveNodes());

            // register watchers for leader change, live nodes change, all nodes change and zk session
            // state change
            if (isEmpty(leaderElectionAlgo) || "2".equals(leaderElectionAlgo)) {
                zkService.registerChildrenChangeWatcher(ELECTION_NODE_2, masterChangeListener);
            } else {
                zkService.registerChildrenChangeWatcher(ELECTION_NODE, masterChangeListener);
            }
            zkService.registerChildrenChangeWatcher(LIVE_NODES, liveNodeChangeListener);
            zkService.registerChildrenChangeWatcher(ALL_NODES, allNodesChangeListener);
            zkService.registerZkSessionStateListener(connectStateChangeListener);
            // TODO
//            StringBuilder b = new StringBuilder();
//            DataStorage.getPersonListFromStorage().forEach(b::append);
//            zkService.registerDataChangeWatcher(b.toString().trim(), dataChangeListener);
            zkService.registerDataChangeWatcher(DATA, dataChangeListener);
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new RuntimeException("Startup failed!!", e);
        }
    }

    private void syncDataFromMaster() {
        log.info(String.format("--- [OnStartUpApplication] syncDataFromMaster()\n"));
        // once cluster running, run next block
        if (beanConfiguration.getZkHostPort().equals(ClusterInfo.getClusterInfo().getMaster())) {
            log.info(String.format("--- [OnStartUpApplication] syncDataFromMaster() OK - ON MASTER.\n"));
            return;
        }
        String requestUrl;
        requestUrl = "http://".concat(ClusterInfo.getClusterInfo().getMaster().concat("/files"));
//        requestUrl = "http://localhost:3000".concat("/files");
        List<File> files = restTemplate.getForObject(requestUrl, List.class);
        log.info(String.format("--- [OnStartUpApplication] syncDataFromMaster() - files: %s\n", files));
        // synched data in memory (RAM)
        if (files != null) {
            DataStorage.getFileListFromStorage().addAll(files);
        }
        // TODO add the sync if file systems from all app cluster nodes - NO LONGER NEEDED since we are using K8S PVC for storage
    }
}