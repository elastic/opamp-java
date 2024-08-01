plugins {
    id("java-library")
    alias(libs.plugins.spotless)
}

spotless {
    java {
        target("src/*/java/**/*.java")
        licenseHeaderFile(rootProject.file("license_header.txt"))
    }
}

afterEvaluate {
    tasks.named("classes").configure {
        dependsOn("spotlessApply")
    }
}

dependencies {
    implementation(project(":client"))
    implementation(libs.protobuf)
    implementation(libs.dslJson)
    testImplementation(libs.jupiter)
    testImplementation(libs.bundles.mockito)
    testImplementation(libs.assertj)
}

tasks.test {
    useJUnitPlatform()
}