plugins {
    id("java")
    alias(libs.plugins.protobuf)
}

sourceSets {
    main {
        proto {
            srcDir("../opamp-spec/proto")
        }
    }
}

dependencies {
    implementation(libs.protobuf)
    implementation(libs.protobuf.util)
    implementation(libs.okhttp)
    implementation(libs.uuidCreator)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.mockito)
}

tasks.test {
    useJUnitPlatform()
}