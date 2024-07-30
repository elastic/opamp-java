plugins {
    id("java-library")
    alias(libs.plugins.protobuf)
    alias(libs.plugins.spotless)
}

sourceSets {
    main {
        proto {
            srcDir("../../opamp-spec/proto")
        }
    }
}

spotless {
    java {
        target("src/*/java/**/*.java")
        licenseHeaderFile(file("license_header.txt"))
    }
}

afterEvaluate {
    tasks.named("classes").configure {
        dependsOn("spotlessApply")
    }
}

dependencies {
    implementation(libs.protobuf)
    implementation(libs.protobuf.util)
    implementation(libs.okhttp)
    implementation(libs.uuidCreator)
    compileOnly(libs.autoValue.annotations)
    annotationProcessor(libs.autoValue.processor)
    testImplementation(libs.jupiter)
    testImplementation(libs.bundles.mockito)
    testImplementation(libs.assertj)
}

tasks.test {
    useJUnitPlatform()
}