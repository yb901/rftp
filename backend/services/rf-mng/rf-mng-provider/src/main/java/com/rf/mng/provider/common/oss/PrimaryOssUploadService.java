package com.rf.mng.provider.common.oss;

/**
 * Primary OSS 上传服务。
 */
public interface PrimaryOssUploadService {

    /**
     * 上传文件到 Primary OSS。
     *
     * @param objectPrefix 对象前缀
     * @param originalFilename 原始文件名
     * @param content 文件内容
     * @return 上传结果
     */
    OssUploadResult upload(String objectPrefix, String originalFilename, byte[] content);

    /**
     * 根据访问地址读取 Primary OSS 文件内容。
     *
     * @param fileUrl 文件访问地址
     * @return 文件内容
     */
    byte[] getObjectContentByUrl(String fileUrl);
}
