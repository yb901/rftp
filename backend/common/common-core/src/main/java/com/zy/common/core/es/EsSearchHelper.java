package com.zy.common.core.es;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * ES搜索工具（基于Spring Data Elasticsearch Criteria API）
 *
 * @author zzy
 * @date 2026/04/24 21:14
 */
public class EsSearchHelper {

    private final ElasticsearchOperations elasticsearchOperations;

    public EsSearchHelper(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * ES分页搜索
     *
     * @param indexName 索引名
     * @param criteria  查询条件
     * @param sortBy    排序字段
     * @param isDesc    是否倒序
     * @param page      页码（从1开始）
     * @param size      每页大小
     * @param clazz     实体类型
     * @return 搜索结果（包含总记录数和实体列表）
     */
    public <T> EsPageResult<T> search(String indexName, Criteria criteria, String sortBy, boolean isDesc,
                                      int page, int size, Class<T> clazz) {
        CriteriaQuery query;
        if (criteria != null) {
            query = new CriteriaQuery(criteria);
        } else {
            query = new CriteriaQuery(new Criteria());
        }

        query.setPageable(PageRequest.of(page - 1, size));

        if (sortBy != null) {
            query.addSort(isDesc ? Sort.by(Sort.Order.desc(sortBy)) : Sort.by(Sort.Order.asc(sortBy)));
        }

        SearchHits<T> searchHits = elasticsearchOperations.search(query, clazz, IndexCoordinates.of(indexName));

        List<T> results = new ArrayList<>();
        for (SearchHit<T> hit : searchHits) {
            results.add(hit.getContent());
        }

        return new EsPageResult<>(searchHits.getTotalHits(), results);
    }

    /**
     * 保存或更新文档
     */
    public <T> T save(String indexName, T entity) {
        return elasticsearchOperations.save(entity, IndexCoordinates.of(indexName));
    }

    /**
     * 批量保存或更新文档
     */
    public <T> Iterable<T> saveAll(String indexName, Iterable<T> entities) {
        return elasticsearchOperations.save(entities, IndexCoordinates.of(indexName));
    }

    /**
     * 删除文档
     */
    public void delete(String indexName, String id) {
        elasticsearchOperations.delete(id, IndexCoordinates.of(indexName));
    }

    /**
     * 根据ID查询文档
     */
    public <T> T findById(String indexName, String id, Class<T> clazz) {
        return elasticsearchOperations.get(id, clazz, IndexCoordinates.of(indexName));
    }

    /**
     * ES搜索结果封装
     */
    public record EsPageResult<T>(long total, List<T> list) {
    }
}
