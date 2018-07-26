package org.superbiz.moviefun.blobstore;

import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.lang.String.format;

@RestController
@RequestMapping("/covers")
public class BlobStoreController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BlobStore blobStore;

    public BlobStoreController(BlobStore blobStore) {
        this.blobStore = blobStore;
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> maybeCoverBlob = blobStore.get(getCoverBlobName(albumId));
        Blob coverBlob = maybeCoverBlob.orElseGet(this::buildDefaultCoverBlob);

        byte[] imageBytes = IOUtils.toByteArray(coverBlob.inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(coverBlob.contentType));
        headers.setContentLength(imageBytes.length);

        return new HttpEntity<>(imageBytes, headers);
    }

    @PostMapping("/{albumId}/cover")
    public void uploadCover(@PathVariable Long albumId, @RequestParam("file") MultipartFile uploadedFile) {
        logger.debug("Uploading cover for album with id {}", albumId);

        if (uploadedFile.getSize() > 0) {
            try {
                tryToUploadCover(albumId, uploadedFile);

            } catch (IOException e) {
                logger.warn("Error while uploading album cover", e);
            }
        }
    }

    private void tryToUploadCover(@PathVariable Long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        Blob coverBlob = new Blob(
                getCoverBlobName(albumId),
                uploadedFile.getInputStream(),
                uploadedFile.getContentType()
        );
        blobStore.put(coverBlob);
    }

    private Blob buildDefaultCoverBlob() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream("default-cover.jpg");

        return new Blob("default-cover", input, MediaType.IMAGE_JPEG_VALUE);
    }


    private String getCoverBlobName(@PathVariable long albumId) {
        return format("covers/%d", albumId);
    }

}
