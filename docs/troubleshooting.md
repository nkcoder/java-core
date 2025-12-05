## Upgrade gradle

```sh
$ ./gradlew wrapper --gradle-version=9.2.1 --distribution-type=bin
Starting a Gradle Daemon, 1 incompatible and 1 stopped Daemons could not be reused, use --status for details

BUILD SUCCESSFUL in 3s
1 actionable task: 1 executed
```

## Preview features

Need to add the following config to build.gradle.kts

```java
// For preview features
tasks.withType<JavaCompile> {
 options.compilerArgs.addAll(listOf("--enable-preview", "-Xlint:preview"))
}

tasks.withType<JavaExec> {
 jvmArgs("--enable-preview")
}
```

Intellij Idea: Project Structure -> Project Settings -> Project: language level (25 preview).