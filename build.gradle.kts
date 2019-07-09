plugins {
    java
    application
}

group = "com.tuthill"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "com.tuthill.dupecrawl.DupeCrawl"
}

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}