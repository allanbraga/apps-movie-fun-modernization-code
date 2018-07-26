package org.superbiz.moviefun.albumsapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstoreapi.BlobStoreClient;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AlbumsClient albumsClient;
    private final BlobStoreClient blobStoreClient;

    public AlbumsController(AlbumsClient albumsClient, BlobStoreClient blobStoreClient) {
        this.albumsClient = albumsClient;
        this.blobStoreClient = blobStoreClient;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsClient.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsClient.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable Long albumId, @RequestParam("file") MultipartFile uploadedFile) {
        logger.info("Uploading cover for album with id {}", albumId);

        if (uploadedFile.getSize() > 0) {
            blobStoreClient.putCover(albumId,uploadedFile);
        }

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) {
        Optional<byte[]> maybeCoverBlob = blobStoreClient.getCover(getCoverBlobName(albumId));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE));
        headers.setContentLength(maybeCoverBlob.get().length);
        return new HttpEntity<>(maybeCoverBlob.get(), headers);
    }

    private String getCoverBlobName(@PathVariable long albumId) {
        return format("covers/%d", albumId);
    }
}
