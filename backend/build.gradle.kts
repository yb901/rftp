import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("java-library")
    alias(libs.plugins.spring.boot) apply false
}

val lombok = libs.lombok
val javaVersion = "21"
val mapperCaseIdBatchUpdatePattern = Regex("""(?is)<update\b[^>]*>.*?\bCASE\s+id\b.*?</update>""")

val checkMapperCaseIdBatchUpdate by tasks.registering {
    group = "verification"
    description = "禁止 Mapper XML 使用 CASE id 批量更新，改用 SqlBatchCommitUtils.baseBatchAndIsFalseIfFailOne。"

    doLast {
        val forbiddenFiles = fileTree(rootDir) {
            include("**/src/main/resources/mapper/**/*.xml")
        }.files.filter { mapperFile ->
            mapperCaseIdBatchUpdatePattern.containsMatchIn(mapperFile.readText())
        }
        if (forbiddenFiles.isNotEmpty()) {
            val paths = forbiddenFiles.joinToString(System.lineSeparator()) { mapperFile ->
                mapperFile.relativeTo(rootDir).path
            }
            throw GradleException("Mapper XML 禁止使用 CASE id 批量更新，请改用 SqlBatchCommitUtils.baseBatchAndIsFalseIfFailOne：${System.lineSeparator()}$paths")
        }
    }
}

allprojects {
    group = "com.zy"
    version = "1.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }

    dependencies {
        implementation(platform(SpringBootPlugin.BOM_COORDINATES))
        compileOnly(platform(SpringBootPlugin.BOM_COORDINATES))
        runtimeOnly(platform(SpringBootPlugin.BOM_COORDINATES))
        testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))
        testRuntimeOnly(platform(SpringBootPlugin.BOM_COORDINATES))

        compileOnly(lombok)
        annotationProcessor(lombok)
        implementation("javax.annotation:javax.annotation-api:1.3.2")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.mockito:mockito-core")
        testImplementation("org.mockito:mockito-junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.named("check") {
        dependsOn(checkMapperCaseIdBatchUpdate)
    }
}
