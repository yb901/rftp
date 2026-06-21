plugins {
    id("org.springframework.boot")
}

springBoot {
    mainClass = "com.zy.rfpt.mng.provider.RfptMngApplication"
}

tasks.jar {
    enabled = false
}

dependencies {
    implementation(project(":common:common-core"))
    implementation(project(":common:common-utils"))
    implementation(project(":services:rfpt-mng:rfpt-mng-api"))
    implementation(project(":services:rfpt-performance:rfpt-performance-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.dubbo.spring.boot.starter)
    implementation(libs.dubbo.registry.nacos)
    implementation(libs.spring.cloud.starter.alibaba.nacos.config)
    implementation(libs.mybatis.spring.boot.starter)
    implementation(libs.mysql.connector.j)
    implementation(libs.hutool.all)
    implementation(libs.commons.lang3)
    implementation(libs.fastjson2)
    implementation(libs.okhttp)
    implementation("jakarta.validation:jakarta.validation-api")

    testRuntimeOnly(libs.mysql.connector.j)
}
