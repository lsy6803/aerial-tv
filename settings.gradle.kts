pluginManagement {
    repositories {
        // 中国网络：优先用阿里云镜像（已实测可用），gradlePluginPortal 作为兜底
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // google() 直连在本机被墙（HTTP 000），改用阿里云镜像；public 代理 Maven Central
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
    }
}
rootProject.name = "Aerial"
include(":app")
include(":lite")
