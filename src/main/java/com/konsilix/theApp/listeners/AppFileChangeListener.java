package com.konsilix.theApp.listeners;

import com.konsilix.theApp.models.ClusterInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.konsilix.zk.ZkDemoUtil.*;
import static com.konsilix.zk.ZkDemoUtil.getHostnameFromIpAddress;

@Slf4j
@Component
public class AppFileChangeListener implements FileChangeListener {
    private final Integer serverPort;

    private RestTemplate restTemplate = new RestTemplate();

    public AppFileChangeListener(@Value("${server.port}") Integer serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        String mainPath = changeSet.iterator().next().getSourceDirectory().toString();
        log.debug(String.format("--->>>---Main path: %s%n", mainPath));
//        List<String> appNodes = new ArrayList<>();
        List<String> liveNodes = ClusterInfo.getClusterInfo().getLiveNodes();
        log.debug(String.format("--->>>---live nodes: %s%n", liveNodes));

        List<String> appNodes  = new ArrayList<>(liveNodes.size());
        for (String node : liveNodes) {
            log.debug(String.format("--->>>--->>>---live node: %s%n", node));
            String hostPort = getIpFromHostPort(node);
            log.debug(String.format("--->>>--->>>---live hostPort: %s%n", hostPort));
            String myIpAddr = getMyIpAddress();
            if (! hostPort.equals(myIpAddr)) {
                String hostname = getHostnameFromIpAddress(hostPort);
                log.debug(String.format("--->>>--->>>---live hostname: %s%n", hostPort));
                appNodes.add(hostPort);
            }
        }
        log.debug(String.format("--->>>---live nodes: %s%n", liveNodes));
        log.debug(String.format("--->>>---app nodes: %s%n", appNodes));

        ResponseEntity<String> uploadResponse;

        for( ChangedFiles cfiles : changeSet ) {
            for( ChangedFile cfile : cfiles.getFiles() ) {
                String relativeFileName = cfile.getRelativeName();
                ChangedFile.Type fileType = cfile.getType();
                log.debug(String.format("File: %s%n", cfile));
                log.debug(String.format("File Relative Name: %s%n", relativeFileName));
                log.debug(String.format("File Type: %s%n", fileType));
                // process by changed file type
                switch(fileType) {
                    case ChangedFile.Type.ADD:
                        log.debug(String.format("File added: " + "/" + cfile));
                        // replicate to cluster nodes
//                        for (String appNode : appNodes) {
//                            log.debug(String.format("adding file %s to %s\n", relativeFileName,appNode));
//                            uploadResponse = replicateFile(appNode, cfile);
//                            log.debug(String.format("upload of %s from %s = %s", cfile, appNode, uploadResponse));
//                        }
                        break;
                    case ChangedFile.Type.DELETE:
                        log.debug(String.format("File delete: " + "/" + cfile));
                        // TODO: delete file on remote server, ??
                        break;
                    case ChangedFile.Type.MODIFY:
                        log.debug(String.format("File changed: " + "/" + cfile));
                        // replicate to cluster nodes
//                        for (String appNode : appNodes) {
//                            log.debug(String.format("updating file %s to %s\n", cfile.getFile().getName(),appNode));
//                            uploadResponse = replicateFile(appNode, cfile);
//                            log.debug(String.format("upload of %s from %s = %s", cfile, appNode, uploadResponse));
//                        }
                        break;
                    default:
                        log.debug(String.format("File unknown: " + "/" + cfile));
                        break;
                }
            }
        }
    }

//    protected ResponseEntity<String> replicateFile(String host, ChangedFile file) {
//        String relativeFileName = file.getRelativeName();
//        log.debug(String.format("--->--- AppFileChangeListener.replicateFile %s -> %s%n", relativeFileName, host));
//        ResponseEntity<String> response = new ResponseEntity(HttpStatus.OK);
//        String masterHostIp = getIpFromHostPort(ClusterInfo.getClusterInfo().getMaster());
//        String whoIsMaster = getHostnameFromIpAddress(masterHostIp); // check right before you need it
//        String whoAmI = getMyHostname();
//        log.debug(String.format("--->--- --->--- replicateFile(master = %s; me = %s) ; host = %s ; file = %s\n",
//                whoIsMaster, whoAmI, host, relativeFileName));
//        if ( !whoAmI.equals(whoIsMaster)) {
//            // TODO: LEFT OFF HERE - need to fix whoAmI....
//            log.debug(String.format("--->--- --->--- --->--- replicating file %s to %s\n",
//                    file.getFile().getName(), host));
//            String requestUrl;
////            requestUrl = "http://localhost:3000".concat("/");
//
////            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl,
////                    ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
////                            "attachment; filename=\"" + file.getFile().getName() + "\"").body(file)
////                    , String.class);
////            System.out.println(response.getBody());
////
////            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
////            map.add("file", fileSystemResource);
//
//
//            String filePath = file.getFile().getAbsolutePath();
//            System.out.println(filePath);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            FileSystemResource fileSystemResource = new FileSystemResource(filePath);
//            body.add("file", fileSystemResource);
//
//            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//            requestUrl = String.format("http://%s:%d/", host, this.serverPort);
//            response = restTemplate.postForEntity(requestUrl, requestEntity, String.class);
//
//            System.out.printf("--->--- --->--- --->--- response = %s\n", response);
//
//        }
//        return(response);
//    }
}