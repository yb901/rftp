pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/spring")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/spring")
        mavenCentral()
    }
}

rootProject.name = "rfpt-backend"

include("common:common-core")
include("common:common-utils")
include("services:rfpt-mng:rfpt-mng-api")
include("services:rfpt-mng:rfpt-mng-provider")
include("services:rfpt-performance:rfpt-performance-api")
include("services:rfpt-performance:rfpt-performance-provider")
