plugins {
    id 'application'
    id 'java'
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.8.1'

    // AWS
    implementation platform('software.amazon.awssdk:bom:2.13.13')
    implementation 'software.amazon.awssdk:sqs'
    implementation 'software.amazon.awssdk:regions'
    implementation 'software.amazon.awssdk:auth'

    // Neo4j
    implementation'org.neo4j.driver:neo4j-java-driver:4.1.1'

    // MOEA
    implementation 'org.moeaframework:moeaframework:2.12'

    // CSV
    implementation 'com.opencsv:opencsv:5.2'

    // MATRIX OPERATIONS
    implementation group: 'org.ejml', name: 'ejml-all', version: '0.40'

    // Gson
    implementation 'com.google.code.gson:gson:2.8.6'

    // ITACA Evaluator
    // implementation fileTree('C:\\Users\\apaza\\repos\\seakers\\decision-graph\\jars-old') { include '*.jar' }
    // implementation fileTree('C:\\Users\\apaza\\repos\\seakers\\decision-graph\\jars') { include '*.jar' }
    implementation fileTree('jars') { include '*.jar' }
}

distTar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
distZip {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

application {
    mainClass = 'app.App'
}

project.buildDir = '/decisions/build'