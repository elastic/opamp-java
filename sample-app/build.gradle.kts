plugins {
    id("application")
}

application {
    mainClass.set("co.elastic.opamp.sample.Main")
}

dependencies {
    implementation(project(":elastic-client"))
}