package com.konsilix.zk;

import com.konsilix.utils.StringSerializer;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import static com.konsilix.zk.ZkDemoUtil.*;

/** @author "Bikas Katwal" 26/03/19 */
@Slf4j
public class ZkServiceImpl implements ZkService {


    //@Getter(value = AccessLevel.PUBLIC) //TODO rob
    private ZkClient zkClient;

    @Getter
    @Setter
    private String hostPort;

    public ZkServiceImpl(String hostPort, int sessionTimeout, int connectionTimeout) {
        this.setHostPort(hostPort);
        zkClient = new ZkClient(hostPort, sessionTimeout, connectionTimeout, new StringSerializer());
        log.info(String.format("*** zkClient = %s\n", zkClient.toString()));
    }

    public void closeConnection() {
        zkClient.close();
    }

    @Override
    public String getLeaderNodeData() {
        return zkClient.readData(ELECTION_MASTER, true);
    }

    @Override
    public void electForMaster() {
        if (!zkClient.exists(ELECTION_NODE)) {
            zkClient.create(ELECTION_MASTER, "election node", CreateMode.PERSISTENT);
        }
        try {
            zkClient.create(
                    ELECTION_MASTER,
                    getHostPortOfServer(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
        } catch (ZkNodeExistsException e) {
            log.error("Master already created!!, {}", e);
        }
    }

    @Override
    public boolean masterExists() {
        return zkClient.exists(ELECTION_MASTER);
    }

    @Override
    public void addToLiveNodes(String nodeName, String data) {
        if (!zkClient.exists(LIVE_NODES)) {
            zkClient.create(LIVE_NODES, "all live nodes are displayed here", CreateMode.PERSISTENT);
        }
        String childNode = LIVE_NODES.concat("/").concat(nodeName);
        if (zkClient.exists(childNode)) {
            return;
        }
        zkClient.create(childNode, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    @Override
    public List<String> getLiveNodes() {
        if (!zkClient.exists(LIVE_NODES)) {
            throw new RuntimeException("No node /liveNodes exists");
        }
        return zkClient.getChildren(LIVE_NODES);
    }

    @Override
    public void addToAllNodes(String nodeName, String data) {
        if (!zkClient.exists(ALL_NODES)) {
            zkClient.create(ALL_NODES, "all live nodes are displayed here", CreateMode.PERSISTENT);
        }
        log.info(String.format("*** nodeName = %s ; data = %s\n", nodeName, data));
        String childNode = ALL_NODES.concat("/").concat(nodeName);
        if (zkClient.exists(childNode)) {
            return;
        }
        zkClient.create(childNode, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Override
    public List<String> getAllNodes() {
        if (!zkClient.exists(ALL_NODES)) {
            throw new RuntimeException("No node /allNodes exists");
        }
        return zkClient.getChildren(ALL_NODES);
    }

    @Override
    public void deleteNodeFromCluster(String node) {
        zkClient.delete(ALL_NODES.concat("/").concat(node));
        zkClient.delete(LIVE_NODES.concat("/").concat(node));
    }

    @Override
    public void createAllParentNodes() {
        if (!zkClient.exists(ALL_NODES)) {
            zkClient.create(ALL_NODES, "all live nodes are displayed here", CreateMode.PERSISTENT);
            log.debug("Created all nodes parent node");
        }
        if (!zkClient.exists(LIVE_NODES)) {
            zkClient.create(LIVE_NODES, "all live nodes are displayed here", CreateMode.PERSISTENT);
            log.debug("Created live nodes");
        }
        if (!zkClient.exists(ELECTION_NODE)) {
            zkClient.create(ELECTION_NODE, "election node", CreateMode.PERSISTENT);
            log.debug("Created election node");
        }
        if (!zkClient.exists(DATA)) {
            zkClient.create(DATA, "data status node", CreateMode.PERSISTENT);
            log.debug("Created data node");
        }
        if (!zkClient.exists(APP)) {
            zkClient.create(APP, "app logic node", CreateMode.PERSISTENT);
            log.debug("Created app node");
        }
        if (!zkClient.exists(FILES)) {
            zkClient.create(FILES, "file logic node", CreateMode.PERSISTENT);
            log.debug("Created files node");
        }
    }

    @Override
    public String getLeaderNodeData2() {
        if (!zkClient.exists(ELECTION_NODE_2)) {
            throw new RuntimeException("No node /election2 exists");
        }
        List<String> nodesInElection = zkClient.getChildren(ELECTION_NODE_2);
        Collections.sort(nodesInElection);
        String masterZNode = nodesInElection.get(0);
        return getZNodeData(ELECTION_NODE_2.concat("/").concat(masterZNode));
    }

    @Override
    public String getZNodeData(String path) {
        return zkClient.readData(path, null);
    }

    @Override
    public void setZNodeData(String path, String data) {
        zkClient.writeData(path, data);
        log.debug("Data set to znode: path = {}, data = {}", path, data);
    }

    @Override
    public void createNodeInElectionZnode(String data) {
        if (!zkClient.exists(ELECTION_NODE_2)) {
            zkClient.create(ELECTION_NODE_2, "election node", CreateMode.PERSISTENT);
        }
        zkClient.create(ELECTION_NODE_2.concat("/node"), data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    @Override
    public void createNodeInFilesZnode(String data) {
        String parentPath = FILES;
        String filePath = parentPath + "/" + data;
        System.out.println("FILE NAME = ___"+data+"___");
        if (!zkClient.exists(FILES)) {
            zkClient.create(FILES, "files under management", CreateMode.PERSISTENT);
        }
        zkClient.create(filePath, filePath, CreateMode.PERSISTENT);
    }

    public void deleteNodeInFilesZnode(String data) {
        String parentPath = FILES;
        String filePath = parentPath + "/" + data;
        System.out.println("FILE NAME = ___"+data+"___");
        if (zkClient.exists(filePath)) {
            zkClient.delete(filePath);
        }
    }


    @Override
    public void registerChildrenChangeWatcher(String path, IZkChildListener iZkChildListener) {
        zkClient.subscribeChildChanges(path, iZkChildListener);
    }

    @Override
    public void registerZkSessionStateListener(IZkStateListener iZkStateListener) {
        zkClient.subscribeStateChanges(iZkStateListener);
    }

    @Override
    public void registerDataChangeWatcher(String path, IZkDataListener iZkDataListener) {
        zkClient.subscribeDataChanges(DATA, iZkDataListener);
    }

    /*
     * DocChatBot Logic
     */
    @Override
    public void createNodeInAppZnode(String nodeName, String data) throws KeeperException.NodeExistsException {
        zkClient.create(APP.concat("/").concat(nodeName), data, CreateMode.PERSISTENT);
    }

    /*
     * File Logic
     */
    @Override
    public void createNodeInFilesZnode(String nodeName, String data) throws KeeperException.NodeExistsException {
        zkClient.create(FILES.concat("/").concat(nodeName), data, CreateMode.PERSISTENT);
    }
}