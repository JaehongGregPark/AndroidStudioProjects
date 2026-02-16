// ❗ 아무 플러그인도 적용하지 않음
// ❗ android {} 절대 금지

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
