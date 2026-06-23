plugins {
    id("org.springframework.boot")
}

springBoot {
    mainClass = "com.rf.mng.provider.RfMngApplication"
}

tasks.jar {
    enabled = false
}

dependencies {
    implementation("com.zy:common-core")
    implementation("com.zy:common-utils")
    implementation(project(":services:rf-mng:rf-mng-api"))
    implementation(project(":services:rf-performance:rf-performance-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.dubbo.spring.boot.starter)
    implementation(libs.dubbo.registry.nacos)
    implementation(libs.spring.cloud.starter.alibaba.nacos.config)
    implementation(libs.mybatis.spring.boot.starter)
    implementation(libs.mysql.connector.j)
    implementation(libs.hutool.all)
    implementation(libs.commons.lang3)
    implementation(libs.fastjson2)
    implementation(libs.easyexcel)
    implementation(libs.totp)
    implementation("jakarta.validation:jakarta.validation-api")

    testRuntimeOnly(libs.mysql.connector.j)
}
