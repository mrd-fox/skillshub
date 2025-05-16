package com.simplon_project.skillhub.skillhub.course.archunit;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@AnalyzeClasses(packages = "com.simplon_project.skillhub.skillhub", importOptions = {ImportOption.DoNotIncludeTests.class})
public class OnionRulesTests {

    @ArchTest
    static final ArchRule validateRegistrationContextArchitecture = onionArchitecture()
            .domainModels("..domain.model..", "..domain.policy..")
            .domainServices("..domain.service..")
            .applicationServices("..application..")
            .adapter("in", "..adapter.in..")
            .adapter("out", "..adapter.out..")
            .withOptionalLayers(true);
    @ArchTest
    static final ArchRule testDomainPackageSpringDependencies = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("org.springframework..");
    @ArchTest
    static final ArchRule testApplicationPackageSpringDependencies = noClasses()
            .that()
            .resideInAnyPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..")
            .andShould().beAnnotatedWith(Transactional.class);
}
