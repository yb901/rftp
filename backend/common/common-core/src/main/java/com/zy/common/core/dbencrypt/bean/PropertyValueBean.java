package com.zy.common.core.dbencrypt.bean;

import com.zy.common.core.dbencrypt.config.DbEncryptColumnRule;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 属性值及加密规则实体类
 *
 * @author zzy
 * @date 2026/05/04
 */
@Data
public class PropertyValueBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否为foreach循环参数
     */
    private boolean isFeach;

    /**
     * 是否为foreach对象（嵌套对象）
     */
    private boolean isFeachObj;

    /**
     * 参数索引与值的映射（用于处理集合类型的foreach参数）
     */
    private Map<Integer, Object> valueMap;

    /**
     * 对应的列加密规则
     */
    private DbEncryptColumnRule rule;
}
