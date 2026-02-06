package com.simplon_project.skillhub.skillhub.course.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Skillshub - ArchUnit rules
 * <p>
 * Scope:
 * - Strict hexagonal architecture (adapter / application / domain) without layeredArchitecture()
 * - Spring Modulith boundaries (course, user, storage modules)
 * - "common" must remain framework-agnostic (except messaging/aop) and be the only shared surface
 * <p>
 * Constraints:
 * - Commands may use Jakarta (e.g., jakarta.validation.*). This is allowed.
 * - We avoid layeredArchitecture() because older ArchUnit versions produce massive false positives
 * and lack ignoreDependency() helpers.
 */
@AnalyzeClasses(
        packages = "com.simplon_project.skillhub.skillhub",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
public class SkillshubArchitectureRulesTest {

    // ---------------------------------------------------------
    // 1) Spring stereotypes policy
    // ---------------------------------------------------------

    @ArchTest
    void springServiceAnnotations_areOnlyInApplicationLayer(JavaClasses classes) {
        noClasses()
                .that()
                .areAnnotatedWith(Service.class)
                .should()
                .resideOutsideOfPackages(
                        "..application.usecase..",
                        "..application.worker.."
                )
                .because("@Service is allowed in application/usecase and application/worker. " +
                        "Adapters should use @Component instead.")
                .check(classes);
    }

    @ArchTest
    void adapters_shouldUseComponentNotService(JavaClasses classes) {
        classes()
                .that()
                .resideInAnyPackage("..adapter..")
                .and()
                .areAnnotatedWith(Service.class)
                .should()
                .beAnnotatedWith(Component.class)
                .because("Adapters should use @Component annotation, not @Service. " +
                        "@Service is reserved for application layer (usecases and workers).")
                .allowEmptyShould(true)
                .check(classes);
    }

    // ---------------------------------------------------------
    // 2) Hexagonal dependency rules (internal-only, no external noise)
    // ---------------------------------------------------------
    // Adapter In: may depend on Application, Domain, Common. MUST NOT depend on Adapter Out.
    @ArchTest
    void adapterIn_shouldNotDependOnAdapterOut(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..adapter.in..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..adapter.out..")
                .because("Adapter In must call Application ports, not Adapter Out directly.")
                .check(classes);
    }

    // Adapter Out: must not depend on Adapter In.
    @ArchTest
    void adapterOut_shouldNotDependOnAdapterIn(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..adapter.out..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..adapter.in..")
                .because("Adapter Out must not depend on Adapter In.")
                .check(classes);
    }

    // Application: must not depend on adapters.
    @ArchTest
    void application_shouldNotDependOnAdapters(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..adapter.in..", "..adapter.out..")
                .because("Application must not depend on adapters (in or out).")
                .check(classes);
    }

    // Domain: must not depend on application nor adapters (pure business).
    @ArchTest
    void domain_shouldNotDependOnApplicationOrAdapters(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..application..", "..adapter..")
                .because("Domain must not depend on application or adapters.")
                .check(classes);
    }

    // Domain: must remain pure (no Spring/Jakarta/JPA/Hibernate) except @NamedInterface.
    @ArchTest
    void domain_shouldNotDependOnSpringOrJakarta(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .and()
                .areNotAnnotatedWith(NamedInterface.class)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "jakarta..",
                        "javax..",
                        "org.hibernate..",
                        "org.springframework.."
                )
                .because("Domain must remain pure (no Spring/Jakarta/JPA/Hibernate dependencies). " +
                        "Exception: @NamedInterface from Spring Modulith is allowed on domain types.")
                .check(classes);
    }

    // ---------------------------------------------------------
    // 3) Ports + Commands framework policy
    // ---------------------------------------------------------
    // Commands may use Jakarta -> we do NOT ban jakarta.. here.
    @ArchTest
    void applicationPortsAndCommands_shouldNotDependOnSpringOrHibernateOrJavax(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAnyPackage("..application.port..", "..application.command..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "javax..", "org.hibernate..")
                .because("Ports and Commands must remain framework-agnostic. Jakarta is allowed; Spring/Hibernate/javax are not.")
                .check(classes);
    }

    // Ports must not expose entities (contract must be domain-only).
    @ArchTest
    void applicationPorts_shouldNotExposeJpaEntities(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..application.port..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..entity..")
                .because("Ports should expose domain objects, not JPA entities. Mapping belongs to adapter/out/persistence.")
                .allowEmptyShould(true)
                .check(classes);
    }

    // ---------------------------------------------------------
    // 4) Common module constraints
    // ---------------------------------------------------------
    @ArchTest
    void common_shouldRemainFrameworkAgnostic(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..common..")
                .and()
                .resideOutsideOfPackages(
                        "..common.messaging..",
                        "..common.aop.."
                )
                .and()
                .doNotHaveSimpleName("package-info")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta..",
                        "javax..",
                        "org.hibernate.."
                )
                .because("Common is shared between modules; it must be framework-agnostic. " +
                        "Exceptions: common.messaging and common.aop may use Spring for cross-cutting concerns. " +
                        "package-info is excluded to allow Spring Modulith @ApplicationModule.")
                .check(classes);
    }

    // ---------------------------------------------------------
    // 5) Modulith boundaries: course, user, storage isolation
    // ---------------------------------------------------------

    @ArchTest
    void courseModule_mustNotDependOnUserModule(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..course..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..user..")
                .because("Course and User are separate Modulith modules; share only through common.")
                .check(classes);
    }

    @ArchTest
    void courseModule_mustNotDependOnStorageModule(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..course..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..storage..")
                .because("Course and Storage are separate Modulith modules; share only through common.")
                .check(classes);
    }

    @ArchTest
    void userModule_mustNotDependOnCourseModule(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..user..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..course..")
                .because("User and Course are separate Modulith modules; share only through common.")
                .check(classes);
    }

    @ArchTest
    void userModule_mustNotDependOnStorageModule(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..user..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..storage..")
                .because("User and Storage are separate Modulith modules; share only through common.")
                .check(classes);
    }

    @ArchTest
    void storageModule_mustNotDependOnCourseModule(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..storage..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..course..")
                .because("Storage and Course are separate Modulith modules; share only through common.")
                .check(classes);
    }

    @ArchTest
    void storageModule_mustNotDependOnUserModule(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..storage..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..user..")
                .because("Storage and User are separate Modulith modules; share only through common.")
                .check(classes);
    }
}
