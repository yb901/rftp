package com.zy.common.core.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PageQuery implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer page = 1;
    private Integer size = 10;
}
