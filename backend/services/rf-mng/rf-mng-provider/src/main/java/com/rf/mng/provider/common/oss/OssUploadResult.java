package com.rf.mng.provider.common.oss;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * OSS 上传结果。
 */
@Data
public class OssUploadResult implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件访问地址。 */
    private String url;

    /** OSS 对象 key。 */
    private String objectKey;

    /** 原始文件名。 */
    private String fileName;
}
