package com.zy.common.core.oss;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public interface AliOssService {

    String getEndpointName();

    void putObject(String objectKey, InputStream inputStream);

    void putObject(String objectKey, byte[] bytes);

    InputStream getObjectStream(String objectKey);

    byte[] getObjectContent(String objectKey);

    void writeObjectContent(String objectKey, OutputStream outputStream);

    void deleteObject(String objectKey);

    boolean doesObjectExist(String objectKey);

    URL generatePresignedUrl(String objectKey, int expireSeconds);

    void copyObject(String sourceBucket, String sourceKey, String targetKey);
}
