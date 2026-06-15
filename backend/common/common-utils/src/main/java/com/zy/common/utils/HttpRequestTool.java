package com.zy.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP 请求工具类
 * <p>
 * 提供获取客户端真实 IP、User-Agent 以及相关解析方法。
 * </p>
 */
public class HttpRequestTool {

    private static final List<String> INVALID_IP_IDENTIFIERS = Arrays.asList(
            "", "unknown", "null", "localhost"
    );

    /**
     * 获取客户端真实 IP 地址
     *
     * @param request HttpServletRequest 对象
     * @return 客户端 IP 字符串
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;

        ip = parseFirstValidIp(request.getHeader("x-forwarded-for"));
        if (isInvalidIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("Cdn-Src-Ip");
        }
        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        ip = normalizeLocalhost(ip);
        return ip;
    }

    private static String parseFirstValidIp(String headerValue) {
        if (headerValue == null || headerValue.trim().isEmpty()) {
            return null;
        }
        String[] ips = headerValue.split(",");
        for (String part : ips) {
            String trimmed = part.trim();
            if (!isInvalidIp(trimmed)) {
                return trimmed;
            }
        }
        return null;
    }

    private static boolean isInvalidIp(String ip) {
        if (ip == null) return true;
        String lowerIp = ip.trim().toLowerCase();
        return lowerIp.isEmpty()
                || "unknown".equals(lowerIp)
                || "null".equals(lowerIp)
                || "localhost".equals(lowerIp);
    }

    private static String normalizeLocalhost(String ip) {
        if (ip == null) return null;
        String trimmed = ip.trim();
        if ("::1".equals(trimmed) || "::".equals(trimmed) || trimmed.startsWith("0:0:0:0")) {
            return "127.0.0.1";
        }
        return trimmed;
    }

    // ==================== User-Agent 相关 ====================

    /**
     * 获取请求的 User-Agent 原始字符串
     *
     * @param request HttpServletRequest 对象
     * @return User-Agent 字符串，若不存在则返回空字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent == null ? "" : userAgent;
    }

    /**
     * 判断是否为移动端设备（基于 User-Agent 简单关键字匹配）
     *
     * @param request HttpServletRequest 对象
     * @return true 表示移动端，false 表示 PC 端或无法判断
     */
    public static boolean isMobile(HttpServletRequest request) {
        String userAgent = getUserAgent(request).toLowerCase();
        return userAgent.contains("mobile")
                || userAgent.contains("android")
                || userAgent.contains("iphone")
                || userAgent.contains("ipad")
                || userAgent.contains("ipod")
                || userAgent.contains("windows phone");
    }

    /**
     * 获取浏览器名称（极简版，仅识别常见浏览器）
     *
     * @param request HttpServletRequest 对象
     * @return 浏览器名称，如 "Chrome", "Firefox", "Safari", "Edge", "IE", "Unknown"
     */
    public static String getBrowserName(HttpServletRequest request) {
        String ua = getUserAgent(request).toLowerCase();
        if (ua.contains("edg")) return "Edge";
        if (ua.contains("chrome")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari")) return "Safari";
        if (ua.contains("trident") || ua.contains("msie")) return "IE";
        return "Unknown";
    }

    /**
     * 获取操作系统名称（极简版，仅识别常见系统）
     *
     * @param request HttpServletRequest 对象
     * @return 操作系统名称，如 "Windows", "Mac OS", "Linux", "Android", "iOS", "Unknown"
     */
    public static String getOsName(HttpServletRequest request) {
        String ua = getUserAgent(request).toLowerCase();
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os")) return "Mac OS";
        if (ua.contains("linux")) return "Linux";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) return "iOS";
        return "Unknown";
    }
}