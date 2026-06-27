plugins {
    alias(libs.plugins.kmpSharedCore)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.ksp)
}

dependencies {
    commonMainApi(libs.androidx.room.runtime)
    commonMainApi(libs.androidx.datastore.preferences.core)
    commonMainApi(libs.ktor.client.core)
    commonMainImplementation(libs.androidx.sqlite.bundled)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(projects.shared.core.domain)
    commonMainImplementation(libs.ktor.client.content.negotiation)
    commonMainImplementation(libs.ktor.serialization.kotlinx.json)
    commonMainImplementation(libs.ktor.client.logging)
    commonMainImplementation(libs.ktor.client.auth)

    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)

    add("androidMainImplementation", libs.ktor.client.okhttp)
    add("iosArm64MainImplementation", libs.ktor.client.darwin)
    add("iosSimulatorArm64MainImplementation", libs.ktor.client.darwin)
}

room {
    schemaDirectory("$projectDir/schemas")
}