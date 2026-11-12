package com.asmln.narochny.archtest;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;


@AnalyzeClasses(packages = "com.asmln.narochny")
public class ArchitectureTest {
    @ArchTest
    static final ArchRule domainHasNoInfraDeps = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..persistence..", "..ingestion..", "..delivery..", "..outbox..");
    @ArchTest
    static final ArchRule noCyclesBetweenPackages =
            slices().matching("com.asmln.narochny.(*)..")
                    .should().beFreeOfCycles();
    @ArchTest
    static final ArchRule domainHasNoSpringData =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.data..",
                            "org.springframework.jdbc..",
                            "org.springframework.transaction..",
                            "jakarta.persistence..");
}
