// AGP also uses JavaPoet; keep the buildscript classpath compatible with Hilt.
buildscript { dependencies { classpath(libs.javapoet) } }

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
