plugins {
    id("org.springframework.boot")
}

springBoot {
    mainClass = "com.rfpt.performance.provider.RfptPerformanceApplication"
}

tasks.jar {
    enabled = false
}

dependencies {
    implementation(project(":common:common-core"))
    implementation(project(":common:common-utils"))
    implementation(project(":services:rfpt-performance:rfpt-performance-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.dubbo.spring.boot.starter)
    implementation(libs.dubbo.registry.nacos)
    implementation(libs.spring.cloud.starter.alibaba.nacos.config)
    implementation(libs.mybatis.spring.boot.starter)
    implementation(libs.mysql.connector.j)
    implementation(libs.hutool.all)
    implementation(libs.fastjson2)
    implementation(libs.commons.lang3)
    implementation(libs.easyexcel)
    implementation(libs.xxl.job.core)
    implementation(libs.aliyun.dysmsapi)
    implementation(libs.aliyun.captcha)

    testRuntimeOnly(libs.mysql.connector.j)
}
