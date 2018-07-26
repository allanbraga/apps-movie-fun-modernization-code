package org.superbiz.moviefun.blobstoreapi;

import java.io.InputStream;

public class BlobInfo {

    public final String name;
    public final InputStream inputStream;
    public final String contentType;

    public BlobInfo(String name, InputStream inputStream, String contentType) {
        this.name = name;
        this.inputStream = inputStream;
        this.contentType = contentType;
    }
}
