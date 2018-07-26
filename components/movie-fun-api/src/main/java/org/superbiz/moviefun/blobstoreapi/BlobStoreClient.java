package org.superbiz.moviefun.blobstoreapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public class BlobStoreClient {


    private String blobStoreUrl;
    private RestOperations restOperations;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BlobStoreClient(String blobStoreUrl, RestOperations restOperations) {
        logger.info("Setting blob urrl {} , blobstore url {}", blobStoreUrl);

        this.blobStoreUrl = blobStoreUrl;
        this.restOperations = restOperations;
    }


    public Optional<byte[]> getCover(String albumId) {

        String url = blobStoreUrl + "/" + albumId + "/cover";
        logger.info("Getting cover from album {} , blobstore url {}", url);
        byte[] body = null;
        try {
            body = restOperations.getForEntity(url, byte[].class).getBody();
        } catch (RestClientException e) {
            logger.error("error retreiving cover", e);
        }
        return Optional.of(body);
    }

    public void putCover(Long albumId , MultipartFile uploadedFile) {

        String url = blobStoreUrl + "/covers/" + albumId + "/cover";

        logger.info("uploading cover from album {} , blobstore url {}", url);

        try{

            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("file", new ByteArrayResource(uploadedFile.getBytes()));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(map, headers);

            restOperations.exchange(url, HttpMethod.POST, requestEntity,String.class);
        }catch (RestClientException e){
            logger.error("error uploading cover", e);
        } catch (IOException e) {
            logger.error("error uploading cover", e);
        }

    }

}
