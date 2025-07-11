package com.konsilix.zk.watchers;

import static com.konsilix.zk.ZkDemoUtil.getHostPortOfServer;
import static com.konsilix.zk.ZkDemoUtil.isEmpty;

import com.konsilix.theApp.models.File;
import com.konsilix.zk.ZkService;
import com.konsilix.theApp.models.ClusterInfo;
import com.konsilix.services.DataStorage;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.springframework.web.client.RestTemplate;

/** @author "Bikas Katwal" 02/04/19 */
@Slf4j
@Setter
public class ConnectStateChangeListener implements IZkStateListener {

    private ZkService zkService;
    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void handleStateChanged(KeeperState state) throws Exception {
        log.info(state.name()); // 1. disconnected, 2. expired, 3. SyncConnected
    }

    @Override
    public void handleNewSession() throws Exception {
        log.info("connected to zookeeper");

        // sync data from master
        syncDataFromMaster();

        // add new znode to /live_nodes to make it live
        zkService.addToLiveNodes(getHostPortOfServer(), "cluster node");
        ClusterInfo.getClusterInfo().getLiveNodes().clear();
        ClusterInfo.getClusterInfo().getLiveNodes().addAll(zkService.getLiveNodes());

        // re try creating znode under /election
        // this is needed, if there is only one server in cluster
        String leaderElectionAlgo = System.getProperty("leader.algo");
        if (isEmpty(leaderElectionAlgo) || "2".equals(leaderElectionAlgo)) {
            zkService.createNodeInElectionZnode(getHostPortOfServer());
            ClusterInfo.getClusterInfo().setMaster(zkService.getLeaderNodeData2());
        } else {
            if (!zkService.masterExists()) {
                zkService.electForMaster();
            } else {
                ClusterInfo.getClusterInfo().setMaster(zkService.getLeaderNodeData());
            }
        }
    }

    @Override
    public void handleSessionEstablishmentError(Throwable error) throws Exception {
        log.info("could not establish session");
    }

    private void syncDataFromMaster() {
        // BKTODO need try catch here for session not found
        if (getHostPortOfServer().equals(ClusterInfo.getClusterInfo().getMaster())) {
            return;
        }
        String requestUrl;
        requestUrl = "http://".concat(ClusterInfo.getClusterInfo().getMaster().concat("/persons"));
        List<File> files = restTemplate.getForObject(requestUrl, List.class);
        DataStorage.getFileListFromStorage().clear();
        DataStorage.getFileListFromStorage().addAll(files);
    }
}
