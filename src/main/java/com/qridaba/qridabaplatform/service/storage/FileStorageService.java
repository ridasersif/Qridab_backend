package com.qridaba.qridabaplatform.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Store a file in sub-folder (e.g. "avatars" or "items").
     * Returns the public-accessible URL path.
     */
    String storeFile(MultipartFile file, String subFolder);

    /** Delete a file given its stored URL path */
    void deleteFile(String fileUrl);
}
