package com.zy.common.core.es;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ES 索引管理器。
 *
 * <p>从 classpath:es/{indexName}.json 读取索引配置，并由调用方显式触发创建、更新或校验。</p>
 *
 * @author zzy
 * @date 2026/04/24 23:50
 */
@Slf4j
public class EsIndexInitializer {

    private static final String ES_CONFIG_PATH_PREFIX = "classpath*:es/";

    private static final Pattern INDEX_NAME_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]*");

    private final ElasticsearchOperations elasticsearchOperations;

    private final ResourcePatternResolver resourcePatternResolver;

    private final EsIndexSettingsOverrideProperties settingsOverrideProperties;

    private final String[] activeProfiles;

    public EsIndexInitializer(ElasticsearchOperations elasticsearchOperations) {
        this(elasticsearchOperations, new EsIndexSettingsOverrideProperties(), new String[0],
                new PathMatchingResourcePatternResolver());
    }

    public EsIndexInitializer(ElasticsearchOperations elasticsearchOperations,
                              EsIndexSettingsOverrideProperties settingsOverrideProperties,
                              Environment environment) {
        this(elasticsearchOperations, settingsOverrideProperties,
                environment == null ? new String[0] : environment.getActiveProfiles(),
                new PathMatchingResourcePatternResolver());
    }

    EsIndexInitializer(ElasticsearchOperations elasticsearchOperations,
                       EsIndexSettingsOverrideProperties settingsOverrideProperties,
                       String[] activeProfiles,
                       ResourcePatternResolver resourcePatternResolver) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.settingsOverrideProperties = settingsOverrideProperties == null
                ? new EsIndexSettingsOverrideProperties() : settingsOverrideProperties;
        this.activeProfiles = activeProfiles == null ? new String[0] : activeProfiles.clone();
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 执行索引操作。
     *
     * @param action    操作类型：create/update/sync/validate
     * @param indexName 索引名
     * @return 操作结果
     */
    public IndexOperationResult operate(String action, String indexName) {
        IndexAction indexAction = IndexAction.from(action);
        return switch (indexAction) {
            case CREATE -> create(indexName);
            case UPDATE -> update(indexName);
            case SYNC -> sync(indexName);
            case VALIDATE -> validate(indexName);
        };
    }

    /**
     * 判断操作是否会写 ES 索引结构。
     *
     * @param action 操作类型
     * @return 判断结果
     */
    public static boolean requiresConfirm(String action) {
        IndexAction indexAction = IndexAction.from(action);
        return indexAction == IndexAction.CREATE || indexAction == IndexAction.UPDATE || indexAction == IndexAction.SYNC;
    }

    /**
     * 校验索引配置文件和 ES 索引状态。
     *
     * @param indexName 索引名
     * @return 校验结果
     */
    public IndexOperationResult validate(String indexName) {
        IndexConfig config = loadIndexConfig(indexName);
        IndexOperations ops = indexOps(config.indexName());
        boolean exists = ops.exists();
        String message = exists ? "索引配置存在，ES索引已存在" : "索引配置存在，但ES索引不存在";
        return new IndexOperationResult(config.indexName(), IndexAction.VALIDATE.value(), exists, exists, exists,
                message, config.resourceDescription());
    }

    /**
     * 索引不存在时创建索引。
     *
     * @param indexName 索引名
     * @return 创建结果
     */
    public IndexOperationResult create(String indexName) {
        IndexConfig config = loadIndexConfig(indexName);
        IndexOperations ops = indexOps(config.indexName());
        boolean existsBefore = ops.exists();
        if (existsBefore) {
            return new IndexOperationResult(config.indexName(), IndexAction.CREATE.value(), true, true, true,
                    "索引已存在，未重复创建", config.resourceDescription());
        }

        String settings = buildCreateSettings(config);
        boolean created = settings == null ? ops.create() : ops.create(Document.parse(settings));
        boolean mappingUpdated = created && ops.putMapping(Document.parse(config.mappings()));
        boolean existsAfter = ops.exists();
        boolean success = created && mappingUpdated && existsAfter;
        String message = success ? "索引创建成功" : "索引创建未完全成功，请查看应用日志和ES状态";
        log.info("ES索引创建结果, indexName={}, created={}, mappingUpdated={}, existsAfter={}",
                config.indexName(), created, mappingUpdated, existsAfter);
        return new IndexOperationResult(config.indexName(), IndexAction.CREATE.value(), success, existsBefore, existsAfter,
                message, config.resourceDescription());
    }

    /**
     * 更新已存在索引的 mapping，不更新 settings。
     *
     * @param indexName 索引名
     * @return 更新结果
     */
    public IndexOperationResult update(String indexName) {
        IndexConfig config = loadIndexConfig(indexName);
        IndexOperations ops = indexOps(config.indexName());
        boolean existsBefore = ops.exists();
        if (!existsBefore) {
            return new IndexOperationResult(config.indexName(), IndexAction.UPDATE.value(), false, false, false,
                    "索引不存在，无法更新mapping", config.resourceDescription());
        }

        boolean mappingUpdated = ops.putMapping(Document.parse(config.mappings()));
        boolean existsAfter = ops.exists();
        String message = mappingUpdated ? "索引mapping更新成功，settings未更新" : "索引mapping更新失败";
        log.info("ES索引mapping更新结果, indexName={}, mappingUpdated={}", config.indexName(), mappingUpdated);
        return new IndexOperationResult(config.indexName(), IndexAction.UPDATE.value(), mappingUpdated, existsBefore, existsAfter,
                message, config.resourceDescription());
    }

    /**
     * 同步索引：不存在则创建，存在则更新 mapping。
     *
     * @param indexName 索引名
     * @return 同步结果
     */
    public IndexOperationResult sync(String indexName) {
        IndexConfig config = loadIndexConfig(indexName);
        IndexOperations ops = indexOps(config.indexName());
        boolean existsBefore = ops.exists();
        if (!existsBefore) {
            String settings = buildCreateSettings(config);
            boolean created = settings == null ? ops.create() : ops.create(Document.parse(settings));
            boolean mappingUpdated = created && ops.putMapping(Document.parse(config.mappings()));
            boolean existsAfter = ops.exists();
            boolean success = created && mappingUpdated && existsAfter;
            String message = success ? "索引不存在，已创建索引" : "索引不存在，创建未完全成功";
            log.info("ES索引同步创建结果, indexName={}, created={}, mappingUpdated={}, existsAfter={}",
                    config.indexName(), created, mappingUpdated, existsAfter);
            return new IndexOperationResult(config.indexName(), IndexAction.SYNC.value(), success, existsBefore, existsAfter,
                    message, config.resourceDescription());
        }

        boolean mappingUpdated = ops.putMapping(Document.parse(config.mappings()));
        boolean existsAfter = ops.exists();
        String message = mappingUpdated ? "索引已存在，mapping已更新，settings未更新" : "索引已存在，mapping更新失败";
        log.info("ES索引同步mapping结果, indexName={}, mappingUpdated={}", config.indexName(), mappingUpdated);
        return new IndexOperationResult(config.indexName(), IndexAction.SYNC.value(), mappingUpdated, existsBefore, existsAfter,
                message, config.resourceDescription());
    }

    private IndexOperations indexOps(String indexName) {
        return elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
    }

    private String buildCreateSettings(IndexConfig config) {
        JSONObject settings = config.settings() == null ? new JSONObject() : JSON.parseObject(config.settings());
        if (settingsOverrideProperties.isEnabledFor(activeProfiles) && settingsOverrideProperties.hasOverrides()) {
            if (settingsOverrideProperties.getNumberOfShards() != null) {
                settings.put("number_of_shards", settingsOverrideProperties.getNumberOfShards());
            }
            if (settingsOverrideProperties.getNumberOfReplicas() != null) {
                settings.put("number_of_replicas", settingsOverrideProperties.getNumberOfReplicas());
            }
            if (settingsOverrideProperties.getRefreshInterval() != null
                    && !settingsOverrideProperties.getRefreshInterval().isBlank()) {
                settings.put("refresh_interval", settingsOverrideProperties.getRefreshInterval().trim());
            }
            log.info("ES索引settings覆盖已生效, indexName={}, activeProfiles={}, numberOfShards={}, numberOfReplicas={}, refreshInterval={}",
                    config.indexName(), String.join(",", activeProfiles), settingsOverrideProperties.getNumberOfShards(),
                    settingsOverrideProperties.getNumberOfReplicas(), settingsOverrideProperties.getRefreshInterval());
        }
        return settings.isEmpty() ? null : settings.toJSONString();
    }

    private IndexConfig loadIndexConfig(String indexName) {
        validateIndexName(indexName);
        try {
            Resource resource = resolveIndexResource(indexName);
            String content = readResourceContent(resource);
            JSONObject root = JSON.parseObject(content);
            JSONObject indexConfig = findIndexConfig(root, indexName);
            JSONObject settings = indexConfig.getJSONObject("settings");
            JSONObject mappings = indexConfig.getJSONObject("mappings");
            if (mappings == null || mappings.isEmpty()) {
                throw new IllegalArgumentException("ES索引配置缺少mappings: " + indexName);
            }
            return new IndexConfig(indexName, settings == null ? null : settings.toJSONString(),
                    mappings.toJSONString(), resource.getDescription());
        } catch (IOException e) {
            throw new IllegalStateException("读取ES索引配置失败: " + indexName, e);
        }
    }

    private Resource resolveIndexResource(String indexName) throws IOException {
        Resource[] resources = resourcePatternResolver.getResources(ES_CONFIG_PATH_PREFIX + indexName + ".json");
        if (resources.length == 0) {
            throw new IllegalArgumentException("ES索引配置文件不存在: es/" + indexName + ".json");
        }
        if (resources.length > 1) {
            throw new IllegalStateException("ES索引配置文件重复: es/" + indexName + ".json");
        }
        return resources[0];
    }

    private JSONObject findIndexConfig(JSONObject root, String indexName) {
        if (root.containsKey("settings") || root.containsKey("mappings")) {
            return root;
        }
        JSONObject wrappedConfig = root.getJSONObject(indexName);
        if (wrappedConfig == null) {
            throw new IllegalArgumentException("ES索引配置格式错误，未找到settings/mappings或索引节点: " + indexName);
        }
        return wrappedConfig;
    }

    private String readResourceContent(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("读取ES索引配置文件失败, resource={}", resource, e);
            throw new IllegalStateException("读取ES索引配置文件失败: " + resource, e);
        }
    }

    private void validateIndexName(String indexName) {
        if (indexName == null || indexName.isBlank()) {
            throw new IllegalArgumentException("ES索引名不能为空");
        }
        if (".".equals(indexName) || "..".equals(indexName) || !INDEX_NAME_PATTERN.matcher(indexName).matches()) {
            throw new IllegalArgumentException("ES索引名非法: " + indexName);
        }
    }

    private enum IndexAction {
        CREATE("create"),
        UPDATE("update"),
        SYNC("sync"),
        VALIDATE("validate");

        private final String value;

        IndexAction(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        static IndexAction from(String action) {
            if (action == null || action.isBlank()) {
                throw new IllegalArgumentException("ES索引操作不能为空");
            }
            String normalizedAction = action.trim().toLowerCase(Locale.ROOT);
            for (IndexAction item : values()) {
                if (item.value.equals(normalizedAction)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("不支持的ES索引操作: " + action);
        }
    }

    private record IndexConfig(String indexName, String settings, String mappings, String resourceDescription) {
    }

    public record IndexOperationResult(String indexName, String action, boolean success, boolean existsBefore,
                                       boolean existsAfter, String message, String resource) {
    }
}
