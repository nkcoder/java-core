plugins {
    id("java")
    id("com.diffplug.spotless") version "8.1.0"
}

group = "org.nkcoder"
version = "1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}


// For preview features
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview", "-Xlint:preview"))
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// spotless configuration for code formatting
spotless {
    java {
        importOrder()
        removeUnusedImports()

        // Choose one formatters: google or palantir
        palantirJavaFormat().formatJavadoc(true)
        formatAnnotations()
        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()

        target("src/**/*.java")
    }
}


tasks.test {
    useJUnitPlatform()
}
