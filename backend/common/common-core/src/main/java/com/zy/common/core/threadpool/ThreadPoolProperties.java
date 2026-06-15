package com.zy.common.core.threadpool;

/**
 * 通用线程池配置属性
 *
 * @author zzy
 * @date 2026/05/18
 */
public class ThreadPoolProperties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "threadpool";

    /**
     * 单个线程池配置
     */
    public static class Pool {

        /**
         * 是否启用线程池
         */
        private Boolean enabled = false;

        /**
         * 核心线程数
         */
        private Integer coreSize = 1;

        /**
         * 最大线程数
         */
        private Integer maxSize = 1;

        /**
         * 队列大小
         */
        private Integer queueSize = 0;

        /**
         * 空闲线程存活秒数
         */
        private Long keepAliveSeconds = 60L;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getCoreSize() {
            return coreSize;
        }

        public void setCoreSize(Integer coreSize) {
            this.coreSize = coreSize;
        }

        public Integer getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
        }

        public Integer getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(Integer queueSize) {
            this.queueSize = queueSize;
        }

        public Long getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(Long keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }
    }
}
