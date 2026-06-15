package com.zy.common.core.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResp<T> implements Serializable {
    private List<T> list;
    private Pagination pagination;

    @Data
    public static class Pagination implements Serializable {
        private Integer page;
        private Integer size;
        private Long total;
    }

    public static <T> PageResp<T> of(List<T> list, Long total, Integer page, Integer size) {
        PageResp<T> response = new PageResp<>();
        response.setList(list);
        Pagination pagination = new Pagination();
        pagination.setPage(page);
        pagination.setSize(size);
        pagination.setTotal(total);
        response.setPagination(pagination);
        return response;
    }
}
