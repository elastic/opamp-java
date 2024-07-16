plugins {
    id("java-library")
    alias(libs.plugins.protobuf)
}

sourceSets {
    main {
        proto {
            srcDir("../../opamp-spec/proto")
        }
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