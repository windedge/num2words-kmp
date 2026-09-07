import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.mavenPublish)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
kotlin {
    jvmToolchain(21)

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    js(IR) {
        nodejs()
    }

    wasmJs {
        nodejs()
    }

    // Native targets：桌面 + iOS，主力验证仍在 JVM，Native/JS/Wasm 跑编译 + smoke
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // 保持 common 依赖纯净，无第三方依赖；
            // 禁止 java.* / BigDecimal，只用纯 Kotlin（Regex、expect/actual 如确有必要）
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    // coordinates are auto-derived by the vanniktech plugin from GROUP/POM_ARTIFACT_ID/VERSION_NAME
}