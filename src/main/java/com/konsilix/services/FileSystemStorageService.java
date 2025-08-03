package com.konsilix.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

import com.konsilix.zk.ZkService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileSystemStorageService implements StorageService {
    private final Path rootLocation;

    private final ZkService zkService; // Make final

    // Use a single constructor for all required dependencies
    @Autowired
    public FileSystemStorageService(StorageProperties properties, ZkService zkService) {
        this.zkService = zkService; // Assign zkService

        if(properties.getLocation() == null || properties.getLocation().trim().isEmpty()){
            throw new StorageException("File upload location can not be Empty.");
        }

        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {
        try {
            String fname = file.getOriginalFilename();
            log.info(fname);
            log.info("Root Storage Location: {}", rootLocation);
            if (!Files.exists(rootLocation)) {
                log.error("Storage directory does not exist: {}", rootLocation);
                throw new StorageException("Storage directory does not exist.");
            }

            if (file.isEmpty()) {
                log.error("File is empty: {}", fname);
                throw new StorageException("Failed to store empty file.");
            }

            Path destinationFile = this.rootLocation.resolve(
                Paths.get(Objects.requireNonNull(fname)))
                    .normalize().toAbsolutePath();

            log.info("Storing file {} at: {}", fname, destinationFile.toAbsolutePath());

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                log.error("File storage path is outside the intended directory.");
                throw new StorageException("Cannot store file outside current directory.");
            }

            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("File {} successfully stored: {}", fname, destinationFile);
            zkService.createNodeInFilesZnode(fname);
        } catch (IllegalArgumentException e1) {
            log.error(e1.getMessage(),e1);
        } catch (IOException e2) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e2);
            throw new StorageException("Failed to store file.", e2);
        }
    }


    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            log.info("Deleting file: {}", filename);
            zkService.deleteNodeInFilesZnode(filename);
            log.info("Done deleting file from ZK node. {}", filename);
            Path fileToDelete = load(filename);
            Files.deleteIfExists(fileToDelete);
            log.info("File deleted successfully: {}", filename);
        } catch (IOException e1) {
            log.error("Failed to delete file: {}", filename, e1);
            throw new StorageException("Failed to delete file: " + filename, e1);
        } catch (Exception e2) {
            log.error(e2.getMessage(),e2);
        }
    }
}
