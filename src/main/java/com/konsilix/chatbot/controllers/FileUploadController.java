package com.konsilix.chatbot.controllers;

//import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.konsilix.utils.File;
import com.konsilix.services.StorageService;
import com.konsilix.services.StorageFileNotFoundException;
import com.konsilix.zk.ZkService;

import static com.konsilix.utils.Utils.isTextFile;
import static com.konsilix.zk.ZkDemoUtil.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;

@Slf4j // this gives access to a logger called "log"
@Controller
//@CrossOrigin(origins = "http://localhost:4200")
public class FileUploadController {
    private final StorageService storageService;
    private final ZkService zkService; // Make final

    // A single constructor for all dependencies is cleaner.
    // The separate @Autowired field is now redundant.
    @Autowired
    public FileUploadController(StorageService storageService, ZkService zkService) {
        this.storageService = storageService;
        this.zkService = zkService;
    }

//    @GetMapping("/files") // the index of the site/service
//    public String listUploadedFiles(Model model) throws IOException {
//
//        //List<String> liveNodes =
//
//        model.addAttribute("hostname", getMyHostname());
//        model.addAttribute("files",
//                storageService.loadAll().map(
//                        path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                                "serveFile", path.getFileName().toString()).build().toUri().toString()
//                ).collect(Collectors.toList()));
//        log.info(String.format("listUploadedFiles: %s\n", model.toString()));
//        return (model.toString());
//    }

    // TODO - when implemented in ang app, remove uploadForm.html in resources/static
//    @GetMapping("/files") // the index of the site/service
//    public String listUploadedFiles(Model model) throws IOException {
//
//        //List<String> liveNodes =
//
//        model.addAttribute("hostname", getMyHostname());
//        model.addAttribute("files",
//                storageService.loadAll().map(
//                        path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                                "serveFile", path.getFileName().toString()).build().toUri().toString()
//                ).collect(Collectors.toList()));
//        log.info(String.format("listUploadedFiles: %s\n", model.toString()));
//        return "uploadForm.html";
//    }

//    @GetMapping("/files") // if authenticated, return list files already uploaded
//    @ResponseBody // w/o this, the return value is treated as a view name
//    public List<String> listUploadedFiles() throws IOException {
//        List<String> returnList = storageService.loadAll().map(
//                        path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                                "serveFile", path.getFileName().toString()).build().toUri().toString()
//                ).collect(Collectors.toList());
//        log.info(String.format("listUploadedFiles: %s\n", returnList.toString()));
//        return returnList;
//    }

    @GetMapping("/files")
    @ResponseBody
    public List<File> listUploadedFiles() throws IOException {
        
        // the hostname is used to create the urls.
        String hostname = getMyHostname();
        List<File> returnList = storageService.loadAll().map(
                path -> {
                    String filename = path.getFileName().toString();
                    String fileUrl = MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                            "serveFile", filename).build().toUri().toString();
                    return new File(filename, fileUrl); // return the object with the file.
                }
        ).collect(Collectors.toList());

        log.info("listUploadedFiles: {}\n", returnList.toString());
        return returnList;
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);

        if (file == null)
            return ResponseEntity.notFound().build();

        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();

        log.info("Sending file {} to requester {}",filename, remoteAddress);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/files")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                   RedirectAttributes redirectAttributes) {
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRemoteAddr();
        String fileName = file.getOriginalFilename();

        try {
            if (file.isEmpty()) {
                log.error("File is empty: {}", fileName);
                return ResponseEntity.badRequest().body("{\"message\": \"Failed to store empty file: " + fileName + "\"}");
            }

            boolean isFileText = isTextFile(file);
            if (!isFileText) {
                log.error("File is not a supported (text) file: {}", fileName);
                return ResponseEntity.badRequest().body("{\"message\": \"File is not a supported (text) file: " + fileName + "\"}");
            }

            storageService.store(file);
            log.info("Successfully uploaded: {}", fileName);
            return ResponseEntity.ok("{\"message\": \"Successfully uploaded: " + fileName + "\"}");
        } catch (Exception e) {
            log.error("Failed to upload file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Failed to upload file: " + e.getMessage() + "\"}");
        }
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/files/{filename:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        try {
            storageService.delete(filename); // Call the delete method in your StorageService
            return ResponseEntity.ok("{\"message\": \"File deleted successfully: " + filename + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Failed to delete file: " + filename + "\"}");
        }
    }
}
