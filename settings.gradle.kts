rootProject.name = "opamp-java"
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}
include(":client")
include(":elastic-client")
include(":sample-app")