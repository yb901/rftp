package com.zy.common.core.oss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CopyObjectRequest;
import com.aliyun.oss.model.OSSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.zy.common.core.oss.AliOssProperties.Endpoint;

public class AliOssServiceImpl implements AliOssService {

    private static final Logger log = LoggerFactory.getLogger(AliOssServiceImpl.class);

    private final String endpointName;
    private final Endpoint endpointConfig;
    private final OSS client;

    public AliOssServiceImpl(String endpointName, Endpoint endpointConfig) {
        this.endpointName = endpointName;
        this.endpointConfig = endpointConfig;
        validateEndpoint(endpointName, endpointConfig);
        com.aliyun.oss.ClientBuilderConfiguration clientConfig = new com.aliyun.oss.ClientBuilderConfiguration();
        this.client = new com.aliyun.oss.OSSClientBuilder().build(
                endpointConfig.getUrl(), endpointConfig.getAccessKeyId(), endpointConfig.getAccessKeySecret(), clientConfig);
        log.info("Initialized OSS client for endpoint: {} (bucket: {})", endpointName, endpointConfig.getBucket());
    }

    @Override
    public String getEndpointName() {
        return endpointName;
    }

    @Override
    public void putObject(String objectKey, InputStream inputStream) {
        Assert.notNull(inputStream, "inputStream must not be null");
        validateObjectKey(objectKey);
        try {
            client.putObject(endpointConfig.getBucket(), objectKey, inputStream);
        } catch (Exception e) {
            throw AliOssException.wrap("Failed to upload object: " + objectKey, e);
        }
    }

    @Override
    public void putObject(String objectKey, byte[] bytes) {
        Assert.notNull(bytes, "bytes must not be null");
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            putObject(objectKey, is);
        } catch (IOException e) {
            throw new AliOssException("Failed to create InputStream for object: " + objectKey, e);
        }
    }

    @Override
    public InputStream getObjectStream(String objectKey) {
        validateObjectKey(objectKey);
        try {
            OSSObject ossObject = client.getObject(endpointConfig.getBucket(), objectKey);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            throw AliOssException.wrap("Failed to get object: " + objectKey, e);
        }
    }

    @Override
    public byte[] getObjectContent(String objectKey) {
        try (InputStream is = getObjectStream(objectKey); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new AliOssException("Failed to read object content: " + objectKey, e);
        }
    }

    @Override
    public void writeObjectContent(String objectKey, OutputStream outputStream) {
        Assert.notNull(outputStream, "outputStream must not be null");
        try (InputStream is = getObjectStream(objectKey)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new AliOssException("Failed to write object content: " + objectKey, e);
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        validateObjectKey(objectKey);
        try {
            client.deleteObject(endpointConfig.getBucket(), objectKey);
        } catch (Exception e) {
            throw AliOssException.wrap("Failed to delete object: " + objectKey, e);
        }
    }

    @Override
    public boolean doesObjectExist(String objectKey) {
        validateObjectKey(objectKey);
        try {
            return client.doesObjectExist(endpointConfig.getBucket(), objectKey);
        } catch (Exception e) {
            throw AliOssException.wrap("Failed to check object existence: " + objectKey, e);
        }
    }

    @Override
    public URL generatePresignedUrl(String objectKey, int expireSeconds) {
        validateObjectKey(objectKey);
        if (expireSeconds <= 0) {
            throw new IllegalArgumentException("expireSeconds must be greater than 0");
        }
        try {
            return client.generatePresignedUrl(endpointConfig.getBucket(), objectKey, new Date(System.currentTimeMillis() + expireSeconds * 1000L));
        } catch (Exception e) {
            throw AliOssException.wrap("Failed to generate presigned URL: " + objectKey, e);
        }
    }

    @Override
    public void copyObject(String sourceBucket, String sourceKey, String targetKey) {
        validateObjectKey(sourceKey);
        validateObjectKey(targetKey);
        if (!StringUtils.hasText(sourceBucket)) {
            throw new IllegalArgumentException("sourceBucket must not be blank");
        }
        try {
            CopyObjectRequest copyRequest = new CopyObjectRequest(sourceBucket, sourceKey, endpointConfig.getBucket(), targetKey);
            client.copyObject(copyRequest);
        } catch (Exception e) {
            throw AliOssException.wrap("Failed to copy object: " + sourceKey + " -> " + targetKey, e);
        }
    }

    public void shutdown() {
        try {
            client.shutdown();
            log.info("Shutdown OSS client for endpoint: {}", endpointName);
        } catch (Exception e) {
            log.warn("Error shutting down OSS client for endpoint: {}", endpointName, e);
        }
    }

    private void validateEndpoint(String endpointName, Endpoint config) {
        if (config == null) {
            throw new AliOssException("OSS endpoint not configured: " + endpointName);
        }
        if (!StringUtils.hasText(config.getUrl())) {
            throw new AliOssException("OSS endpoint url must not be blank: " + endpointName);
        }
        if (!StringUtils.hasText(config.getAccessKeyId())) {
            throw new AliOssException("OSS accessKeyId must not be blank: " + endpointName);
        }
        if (!StringUtils.hasText(config.getAccessKeySecret())) {
            throw new AliOssException("OSS accessKeySecret must not be blank: " + endpointName);
        }
        if (!StringUtils.hasText(config.getBucket())) {
            throw new AliOssException("OSS bucket must not be blank: " + endpointName);
        }
    }

    private void validateObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("objectKey must not be blank");
        }
    }
}
