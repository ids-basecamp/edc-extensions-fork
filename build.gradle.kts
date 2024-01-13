plugins {
    id("java")
    id("checkstyle")
    id("maven-publish")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

val downloadArtifact: Configuration by configurations.creating {
    isTransitive = false
}

val javaVersion: String by project
val identityHubVersion: String by project
val registrationServiceVersion: String by project

// task that downloads the RegSrv CLI and IH CLI
val getJars by tasks.registering(Copy::class) {
    outputs.upToDateWhen { false } //always download

    from(downloadArtifact)
            // strip away the version string
            .rename { s ->
                s.replace("-${identityHubVersion}", "")
                        .replace("-${registrationServiceVersion}", "")
                        .replace("-all", "")
            }
    into(layout.projectDirectory.dir("libs/cli-tools"))
}

// run the download jars task after the "jar" task
tasks {
    jar {
        finalizedBy(getJars)
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    checkstyle {
        toolVersion = "10.9.3"
        configFile = rootProject.file("docs/dev/checkstyle/checkstyle-config.xml")
        configDirectory.set(rootProject.file("docs/dev/checkstyle"))
        maxErrors = 0 // does not tolerate errors
    }

    repositories {
        val gitHubUserName: String? by project
        val gitHubUserPassword: String? by project
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/ids-basecamp/ids-infomodel-java")
            credentials {
                username = gitHubUserName
                password = gitHubUserPassword
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/ids-basecamp/gradle-plugins-fork")
            credentials {
                username = gitHubUserName
                password = gitHubUserPassword
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/ids-basecamp/edc-fork")
            credentials {
                username = gitHubUserName
                password = gitHubUserPassword
            }
        }
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "maven-publish")

    val sovityEdcExtensionsVersion: String by project
    val publishUserName: String? by project
    val publishUserPassword: String? by project

    version = sovityEdcExtensionsVersion

    publishing {
        repositories {
            maven {
                name = "GitHub"
                url = uri("https://maven.pkg.github.com/ids-basecamp/edc-extensions-fork")
                credentials {
                    username = publishUserName
                    password = publishUserPassword
                }
            }
        }
    }
}
