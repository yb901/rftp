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

rootProject.name = "rf-backend"

include("common:common-core")
include("common:common-utils")
include("services:rf-mng:rf-mng-api")
include("services:rf-mng:rf-mng-provider")
include("services:rf-performance:rf-performance-api")
include("services:rf-performance:rf-performance-provider")
