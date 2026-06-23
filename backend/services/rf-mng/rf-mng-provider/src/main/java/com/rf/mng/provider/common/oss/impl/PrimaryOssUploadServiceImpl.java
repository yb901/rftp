package com.rf.mng.provider.common.oss.impl;

import com.rf.mng.provider.common.oss.OssUploadResult;
import com.rf.mng.provider.common.oss.PrimaryOssUploadService;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.zy.common.core.oss.AliOssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Primary OSS 上传服务实现。
 */
@Slf4j
@Service
public class PrimaryOssUploadServiceImpl implements PrimaryOssUploadService {

    /** 上海时区。 */
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    /** 日期目录格式。 */
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    /** 文件名时间格式。 */
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmssSSS");

    /** Primary OSS 服务。 */
    @Resource(name = "primaryAliOssService")
    private AliOssService primaryAliOssService;

    /** Primary OSS 访问地址前缀。 */
    @Value("${rf-mng.oss-primary-access-url-prefix:https://rf-mng.oss-cn-hangzhou.aliyuncs.com}")
    private String ossPrimaryAccessUrlPrefix;

    /** 当前应用名，用作 OSS 对象 key 系统命名空间。 */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 上传文件到 Primary OSS。
     *
     * @param objectPrefix 对象前缀
     * @param originalFilename 原始文件名
     * @param content 文件内容
     * @return 上传结果
     */
    @Override
    public OssUploadResult upload(String objectPrefix, String originalFilename, byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessException(ErrorCode.E999001, "上传文件内容不能为空");
        }
        String objectKey = buildObjectKey(objectPrefix, originalFilename);
        primaryAliOssService.putObject(objectKey, content);
        boolean exists = primaryAliOssService.doesObjectExist(objectKey);
        if (!exists) {
            throw new BusinessException(ErrorCode.E999002, "OSS文件上传后校验失败");
        }

        OssUploadResult result = new OssUploadResult();
        result.setUrl(buildAccessUrl(objectKey));
        result.setObjectKey(objectKey);
        result.setFileName(originalFilename);
        log.info("Primary OSS 文件上传成功, objectPrefix={}, originalFilename={}, objectKey={}, url={}",
                objectPrefix, originalFilename, objectKey, result.getUrl());
        return result;
    }

    /**
     * 根据访问地址读取 Primary OSS 文件内容。
     *
     * @param fileUrl 文件访问地址
     * @return 文件内容
     */
    @Override
    public byte[] getObjectContentByUrl(String fileUrl) {
        String objectKey = extractObjectKey(fileUrl);
        return primaryAliOssService.getObjectContent(objectKey);
    }

    /**
     * 构造 OSS 对象 key。
     *
     * @param objectPrefix 对象前缀
     * @param originalFilename 原始文件名
     * @return 对象 key
     */
    private String buildObjectKey(String objectPrefix, String originalFilename) {
        String prefix = normalizeObjectPrefix(objectPrefix);
        LocalDateTime now = LocalDateTime.now(SHANGHAI_ZONE);
        String datePath = now.format(DATE_PATH_FORMATTER);
        String timestamp = now.format(FILE_TIME_FORMATTER);
        return prefix + "/" + datePath + "/" + getSafeFileNameWithoutExtension(originalFilename)
                + "-" + timestamp + "-" + randomSuffix() + getFileExtension(originalFilename);
    }

    /**
     * 提取安全的不含后缀文件名。
     *
     * @param originalFilename 原始文件名
     * @return 安全的不含后缀文件名
     */
    private String getSafeFileNameWithoutExtension(String originalFilename) {
        if (isBlank(originalFilename)) {
            return "file";
        }
        String filename = originalFilename.trim();
        int index = filename.lastIndexOf('.');
        if (index <= 0) {
            return normalizeSafeFilename(filename);
        }
        return normalizeSafeFilename(filename.substring(0, index));
    }

    /**
     * 构造访问地址。
     *
     * @param objectKey 对象 key
     * @return 访问地址
     */
    private String buildAccessUrl(String objectKey) {
        return trimTrailingSlash(ossPrimaryAccessUrlPrefix) + "/" + objectKey;
    }

    /**
     * 从访问地址提取 OSS 对象 key。
     *
     * @param fileUrl 文件访问地址
     * @return OSS 对象 key
     */
    private String extractObjectKey(String fileUrl) {
        if (isBlank(fileUrl)) {
            throw new BusinessException(ErrorCode.E999001, "文件地址不能为空");
        }
        String value = fileUrl.trim();
        String accessUrlPrefix = trimTrailingSlash(ossPrimaryAccessUrlPrefix);
        String objectKey;
        if (!isBlank(accessUrlPrefix) && value.startsWith(accessUrlPrefix + "/")) {
            objectKey = value.substring(accessUrlPrefix.length() + 1);
        } else {
            int protocolIndex = value.indexOf("://");
            if (protocolIndex >= 0) {
                int pathIndex = value.indexOf('/', protocolIndex + 3);
                objectKey = pathIndex >= 0 ? value.substring(pathIndex + 1) : "";
            } else {
                objectKey = value;
            }
        }
        int queryIndex = objectKey.indexOf('?');
        if (queryIndex >= 0) {
            objectKey = objectKey.substring(0, queryIndex);
        }
        objectKey = URLDecoder.decode(objectKey.replace("+", "%2B"), StandardCharsets.UTF_8);
        if (isBlank(objectKey)) {
            throw new BusinessException(ErrorCode.E999001, "文件地址不合法");
        }
        return objectKey;
    }

    /**
     * 标准化 OSS 对象前缀。
     *
     * @param prefix 配置前缀
     * @return 对象前缀
     */
    private String normalizeObjectPrefix(String prefix) {
        String systemName = normalizePathSegment(applicationName);
        if (isBlank(systemName)) {
            throw new BusinessException(ErrorCode.E999001, "应用名称不能为空");
        }
        String value = isBlank(prefix) ? "uploads" : prefix.trim();
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        value = trimTrailingSlash(value);
        if (value.equals(systemName) || value.startsWith(systemName + "/")) {
            return value;
        }
        return systemName + "/" + value;
    }

    /**
     * 标准化路径片段。
     *
     * @param value 原始文本
     * @return 路径片段
     */
    private String normalizePathSegment(String value) {
        if (isBlank(value)) {
            return "";
        }
        return value.trim()
                .replaceAll("[^A-Za-z0-9_-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * 标准化安全文件名。
     *
     * @param value 原始文件名
     * @return 安全文件名
     */
    private String normalizeSafeFilename(String value) {
        String result = normalizePathSegment(value);
        return isBlank(result) ? "file" : result;
    }

    /**
     * 生成随机后缀。
     *
     * @return 随机后缀
     */
    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    /**
     * 提取安全文件扩展名。
     *
     * @param originalFilename 原始文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String originalFilename) {
        if (isBlank(originalFilename)) {
            return "";
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        String extension = originalFilename.substring(index + 1)
                .replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase(Locale.ROOT);
        return extension.isEmpty() ? "" : "." + extension;
    }

    /**
     * 去除末尾斜杠。
     *
     * @param value 原始文本
     * @return 处理后文本
     */
    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 判断文本是否为空。
     *
     * @param value 文本
     * @return 是否为空
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
