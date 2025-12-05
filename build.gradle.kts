plugins {
    id("java")
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

tasks.test {
    useJUnitPlatform()
}
