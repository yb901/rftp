plugins {
    `java-library`
}

dependencies {
    implementation(project(":common:common-utils"))
    // Jakarta annotations (@PreDestroy etc.)
    compileOnly("jakarta.annotation:jakarta.annotation-api")

    implementation(libs.commons.lang3)
    implementation(libs.commons.collections4)
    implementation(libs.caffeine)
    implementation(libs.fastjson2)

    compileOnly(libs.rocketmq.client)
    compileOnly("org.springframework.boot:spring-boot")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework:spring-webmvc")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly(libs.slf4j.api)
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
    compileOnly("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    testImplementation("org.springframework:spring-web")
    testImplementation("org.springframework:spring-webmvc")
    testImplementation("jakarta.servlet:jakarta.servlet-api")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    compileOnly(libs.dubbo.spring.boot.starter)
    compileOnly(libs.xxl.job.core)

    compileOnly("org.springframework.boot:spring-boot-starter-data-redis")
    compileOnly("org.springframework.boot:spring-boot-starter-integration")
    compileOnly("org.springframework.integration:spring-integration-redis")
    compileOnly(libs.mybatis.spring.boot.starter)

    compileOnly(libs.aliyun.oss)
}
