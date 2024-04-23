plugins {
    id("java")
}

group = "me.flame.scheduler"
version = "1.0-SNAPSHOT"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}