plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":protocol-core"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}

application {
    mainClass.set("com.fabrick.ecr17.simulator.MainKt")
}

tasks.test {
    useJUnit()
}
