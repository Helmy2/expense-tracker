plugins {
    alias(libs.plugins.kmpSharedCore)
}

dependencies {
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.kotlinx.datetime)
    commonTestImplementation(kotlin("test"))
}
