package com.qridaba.qridabaplatform.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    private LocalFileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(fileStorageService, "baseUrl", baseUrl);
        fileStorageService.init();
    }

    @Test
    void init_ShouldCreateDirectories() {
        assertThat(Files.exists(tempDir.resolve("avatars"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("items"))).isTrue();
    }

    @Test
    void storeFile_WhenValid_ShouldSaveFileAndReturnUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes());

        String url = fileStorageService.storeFile(file, "avatars");

        assertThat(url).contains(baseUrl + "/uploads/avatars/");
        assertThat(url).endsWith(".jpg");

        // Verify file actually exists
        String filename = url.substring(url.lastIndexOf("/") + 1);
        Path filePath = tempDir.resolve("avatars").resolve(filename);
        assertThat(Files.exists(filePath)).isTrue();
        assertThat(Files.readAllBytes(filePath)).isEqualTo("test content".getBytes());
    }

    @Test
    void storeFile_WhenEmpty_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.storeFile(file, "avatars"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteFile_WhenExists_ShouldDelete() throws IOException {
        Path filePath = tempDir.resolve("avatars").resolve("to-delete.jpg");
        Files.write(filePath, "content".getBytes());
        String fileUrl = baseUrl + "/uploads/avatars/to-delete.jpg";

        fileStorageService.deleteFile(fileUrl);

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void deleteFile_WhenNotExists_ShouldNotThrow() {
        String fileUrl = baseUrl + "/uploads/avatars/non-existent.jpg";
        fileStorageService.deleteFile(fileUrl);
        // Should not throw exception
    }
}
