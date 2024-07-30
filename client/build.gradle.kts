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
        licenseHeader(getLicenseHeader())
    }
}

afterEvaluate {
    tasks.named("classes").configure {
        dependsOn("spotlessApply")
    }
}

fun getLicenseHeader(): String {
    return "/*\n" +
            " * Licensed to Elasticsearch B.V. under one or more contributor\n" +
            " * license agreements. See the NOTICE file distributed with\n" +
            " * this work for additional information regarding copyright\n" +
            " * ownership. Elasticsearch B.V. licenses this file to you under\n" +
            " * the Apache License, Version 2.0 (the \"License\"); you may\n" +
            " * not use this file except in compliance with the License.\n" +
            " * You may obtain a copy of the License at\n" +
            " *\n" +
            " *\thttp://www.apache.org/licenses/LICENSE-2.0\n" +
            " *\n" +
            " * Unless required by applicable law or agreed to in writing,\n" +
            " * software distributed under the License is distributed on an\n" +
            " * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
            " * KIND, either express or implied.  See the License for the\n" +
            " * specific language governing permissions and limitations\n" +
            " * under the License.\n" +
            " */\n"
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