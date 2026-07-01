package com.veloservice.ordenes.infraestructure.storage;

import com.veloservice.ordenes.application.port.MediaStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
public class FileSystemMediaStorage implements MediaStoragePort {
    private final Path root;
    private final String publicBaseUrl;

    public FileSystemMediaStorage(
            @Value("${app.media.storage-root:/tmp/veloservice-media}") String storageRoot,
            @Value("${app.media.public-base-url:https://media.veloservice.invalid}") String publicBaseUrl
    ) {
        this.root = Path.of(storageRoot).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public StoredMedia store(UUID tallerId, UUID ordenId, String contentType, byte[] content) throws IOException {
        String extension = extensionFor(contentType);
        String key = tallerId + "/ordenes/" + ordenId + "/" + UUID.randomUUID() + extension;
        Path destination = root.resolve(key).normalize();
        if (!destination.startsWith(root)) {
            throw new IOException("Ruta de almacenamiento invalida");
        }
        Files.createDirectories(destination.getParent());
        Files.write(destination, content, StandardOpenOption.CREATE_NEW);
        return new StoredMedia(key, publicBaseUrl + "/" + key);
    }

    @Override
    public void delete(String key) {
        try {
            Path destination = root.resolve(key).normalize();
            if (destination.startsWith(root)) {
                Files.deleteIfExists(destination);
            }
        } catch (IOException ignored) {
            // Best-effort compensation after a failed database write.
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            case "video/quicktime" -> ".mov";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }
}
