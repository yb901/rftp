package com.zy.common.core.oss;

import java.util.Set;

public interface AliOssManager {

    AliOssService getService(String endpointName);

    Set<String> getEndpointNames();

    void copyObject(String sourceEndpointName, String sourceKey, String targetEndpointName, String targetKey);
}
