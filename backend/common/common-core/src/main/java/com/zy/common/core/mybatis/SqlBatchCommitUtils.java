package com.zy.common.core.mybatis;

import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * MyBatis 批量提交工具。
 *
 * @author zzy
 * @date 2026/05/25
 */
public final class SqlBatchCommitUtils {

    private SqlBatchCommitUtils() {
    }

    /**
     * 批量提交 SQL，任意一条执行结果不满足条件时返回 false。
     *
     * @param sqlSessionTemplate SQL 会话模板
     * @param consumer           批量 SQL 执行逻辑
     * @param resultSuccess      单条 SQL 影响行数成功判断
     * @return 是否全部执行成功
     */
    public static boolean baseBatchAndIsFalseIfFailOne(SqlSessionTemplate sqlSessionTemplate,
                                                       Consumer<SqlSession> consumer,
                                                       Function<Integer, Boolean> resultSuccess) {
        if (sqlSessionTemplate == null || consumer == null || resultSuccess == null) {
            return false;
        }
        try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
            // 一个批量 SqlSession 内保持同类 SQL，避免批处理被拆散。
            consumer.accept(sqlSession);
            List<BatchResult> batchResults = sqlSession.flushStatements();
            return isAllSuccess(batchResults, resultSuccess);
        }
    }

    /**
     * 判断批量执行结果是否全部成功。
     *
     * @param batchResults  批量执行结果
     * @param resultSuccess 单条 SQL 影响行数成功判断
     * @return 是否全部成功
     */
    private static boolean isAllSuccess(List<BatchResult> batchResults, Function<Integer, Boolean> resultSuccess) {
        if (batchResults == null || batchResults.isEmpty()) {
            return false;
        }
        for (BatchResult batchResult : batchResults) {
            if (!isBatchResultSuccess(batchResult, resultSuccess)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断单个批量结果是否成功。
     *
     * @param batchResult   批量结果
     * @param resultSuccess 单条 SQL 影响行数成功判断
     * @return 是否成功
     */
    private static boolean isBatchResultSuccess(BatchResult batchResult, Function<Integer, Boolean> resultSuccess) {
        if (batchResult == null || batchResult.getUpdateCounts() == null || batchResult.getUpdateCounts().length == 0) {
            return false;
        }
        for (int updateCount : batchResult.getUpdateCounts()) {
            if (!Boolean.TRUE.equals(resultSuccess.apply(updateCount))) {
                return false;
            }
        }
        return true;
    }
}
