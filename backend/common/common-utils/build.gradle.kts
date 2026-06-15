plugins {
    `java-library`
}

dependencies {
    implementation(libs.bouncycastle.bcprov)
    implementation(libs.hutool.all)
    implementation(libs.slf4j.api)

    compileOnly("org.springframework.boot:spring-boot-starter-web")
}

tasks.register<JavaExec>("sm4Util") {
    group = "application"
    description = "执行 SM4 密钥生成、配置加密和配置解密工具。"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.zy.common.utils.Sm4Util")
    val sm4Args = providers.gradleProperty("sm4Args").orNull
    if (!sm4Args.isNullOrBlank()) {
        args(sm4Args.split(" "))
    }
}
