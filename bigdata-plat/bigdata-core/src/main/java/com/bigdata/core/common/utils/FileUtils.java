package com.bigdata.core.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Author: wangjiahao
 * Date: 2022/4/24
 * Description:
 */
@Slf4j
public class FileUtils {

    public static void createIfAbsent(@NonNull Path path) throws IOException {
        Assert.notNull(path, "Path must not be null");

        if (Files.notExists(path)) {
            // Create directories
            Files.createDirectories(path);
            log.debug("Created directory: [{}]", path);
        }
    }
}
