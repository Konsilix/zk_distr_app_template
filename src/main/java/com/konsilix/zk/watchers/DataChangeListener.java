package com.konsilix.zk.watchers;

import com.konsilix.zk.ZkService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;

@Setter
@Slf4j
public class DataChangeListener implements IZkDataListener {
    private ZkService zkService;

    @Override
    public void handleDataChange(String s, Object o) throws Exception {
        log.info("handleDataChange: s = {} ; o = {}", s, o);
    }

    @Override
    public void handleDataDeleted(String s) throws Exception {
        log.info("handleDataDeleted: s = {}", s);
    }
}