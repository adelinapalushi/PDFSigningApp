package com.springbootapp.demoo.util;

import com.springbootapp.demoo.config.TempStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    @Autowired
    private TempStorageProperties tempStorageProperties;

    public Path saveFile(byte[] fileBytes, String fileName) throws  java.io.IOException {
        Path tempDir = Paths.get(tempStorageProperties.getPath());
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
            logger.info("Temporary directory created: {}", tempDir.toString());
        }
        Path tempFile = tempDir.resolve(fileName);
        Files.write(tempFile, fileBytes);
        logger.info("File saved to temporary storage: {}", tempFile.toString());

        return tempFile;
    }
}
