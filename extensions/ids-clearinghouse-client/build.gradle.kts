plugins {
    `java-library`
    `maven-publish`
}

val edcVersion: String by project
val edcGroup: String by project
val jupiterVersion: String by project
val mockitoVersion: String by project
val assertj: String by project
val okHttpVersion: String by project
val jsonVersion: String by project

dependencies {
    implementation("${edcGroup}:control-plane-core:${edcVersion}")
    implementation("${edcGroup}:ids-spi:${edcVersion}")
    implementation("${edcGroup}:ids-api-multipart-dispatcher-v1:${edcVersion}")
    implementation("${edcGroup}:ids-api-configuration:${edcVersion}")
    implementation("${edcGroup}:ids-jsonld-serdes:${edcVersion}")
    implementation("${edcGroup}:http-spi:${edcVersion}")

    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    implementation("org.json:json:${jsonVersion}")

    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val sovityEdcExtensionGroup: String by project
group = sovityEdcExtensionGroup

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
