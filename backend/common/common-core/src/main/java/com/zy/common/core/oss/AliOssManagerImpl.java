package com.zy.common.core.oss;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AliOssManagerImpl implements AliOssManager {

    private final Map<String, AliOssService> services;

    public AliOssManagerImpl(Map<String, AliOssService> serviceBeans) {
        this.services = normalizeServices(serviceBeans);
    }

    @Override
    public AliOssService getService(String endpointName) {
        AliOssService service = services.get(endpointName);
        if (service == null) {
            throw new AliOssException("OSS endpoint service not configured: " + endpointName);
        }
        return service;
    }

    @Override
    public Set<String> getEndpointNames() {
        return services.keySet();
    }

    @Override
    public void copyObject(String sourceEndpointName, String sourceKey, String targetEndpointName, String targetKey) {
        AliOssService sourceService = getService(sourceEndpointName);
        AliOssService targetService = getService(targetEndpointName);
        try (InputStream inputStream = sourceService.getObjectStream(sourceKey)) {
            targetService.putObject(targetKey, inputStream);
        } catch (IOException e) {
            throw new AliOssException("Failed to copy object between OSS endpoints: "
                    + sourceEndpointName + "/" + sourceKey + " -> " + targetEndpointName + "/" + targetKey, e);
        }
    }

    private Map<String, AliOssService> normalizeServices(Map<String, AliOssService> serviceBeans) {
        Map<String, AliOssService> normalized = new LinkedHashMap<>();
        serviceBeans.values().forEach(service -> normalized.put(service.getEndpointName(), service));
        return Map.copyOf(normalized);
    }
}
