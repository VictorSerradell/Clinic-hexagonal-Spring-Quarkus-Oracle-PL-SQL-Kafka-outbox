package com.clinic.application;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Guards the dependency rule: dependencies always point inwards (infrastructure -> application -> domain). */
@AnalyzeClasses(packages = "com.clinic", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainDependsOnNothingElse = noClasses().that().resideInAPackage("com.clinic.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.clinic.application..", "com.clinic.infrastructure..",
                    "org.springframework..", "jakarta..", "javax..", "io.quarkus..", "org.hibernate..");

    @ArchTest
    static final ArchRule applicationIsFrameworkFree = noClasses().that().resideInAPackage("com.clinic.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.clinic.infrastructure..",
                    "org.springframework..", "jakarta..", "javax..", "io.quarkus..", "org.hibernate..");
}
