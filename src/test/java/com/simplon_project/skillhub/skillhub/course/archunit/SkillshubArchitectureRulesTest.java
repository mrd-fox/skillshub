package com.simplon_project.skillhub.skillhub.course.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Skillshub - ArchUnit rules
 * <p>
 * Scope:
 * - Strict hexagonal architecture (adapter / application / domain)
 * - Spring Modulith boundaries (course, user, storage modules)
 * - "common" must remain framework-agnostic (except messaging/aop) and be the only shared surface
 */
@AnalyzeClasses(
        packages = "com.simplon_project.skillhub.skillhub",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
public class SkillshubArchitectureRulesTest {

    // ----------------------------
    // 1) Hexagonal layering rules
    // ----------------------------

    @ArchTest
    void hexagonalArchitecture_isRespected(JavaClasses classes) {
        layeredArchitecture()
                .consideringAllDependencies()

                .layer("Adapter In").definedBy("..adapter.in..")
                .layer("Adapter Out").definedBy("..adapter.out..")
                .layer("Application").definedBy("..application..")
                .layer("Domain").definedBy("..domain..")
                .layer("Common").definedBy("..common..")

                .whereLayer("Adapter In").mayOnlyAccessLayers("Application", "Domain", "Common")
                .whereLayer("Adapter Out").mayOnlyAccessLayers("Application", "Domain", "Common")
                .whereLayer("Application").mayOnlyAccessLayers("Domain", "Common")
                .whereLayer("Domain").mayOnlyAccessLayers("Common")

                .check(classes);
    }

    // ---------------------------------------------------------
    // 2) Strict rule: @Service mainly in application layer
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

    // ---------------------------------------------------------------------
    // 3) Dependency prohibitions
    // ---------------------------------------------------------------------

    @ArchTest
    void noPackage_shouldDependOnJetBrains(JavaClasses classes) {
        noClasses()
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.jetbrains..")
                .because("JetBrains annotations are not part of the runtime architecture contract.")
                .allowEmptyShould(true)
                .check(classes);
    }

    @ArchTest
    void domain_shouldNotDependOnSpringOrJakarta(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "jakarta..",
                        "javax..",
                        "org.hibernate.."
                )
                .orShould()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..")
                .andShould().notHaveSimpleName("NamedInterface")
                .because("Domain must remain pure (no Spring/Jakarta/JPA/Hibernate dependencies). " +
                        "Exception: @NamedInterface from Spring Modulith is allowed to expose domain enums.")
                .check(classes);
    }

    @ArchTest
    void applicationPortsAndCommands_shouldNotDependOnSpringOrJakarta(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAnyPackage(
                        "..application.port..",
                        "..application.command.."
                )
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "javax..",
                        "org.hibernate.."
                )
                .because("Ports and Commands must remain framework-agnostic. " +
                        "Exception: Jakarta Validation is allowed for declarative validation.")
                .check(classes);
    }

    @ArchTest
    void applicationPorts_shouldNotExposeJpaEntities(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..application.port..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..entity..")
                .because("Ports should expose domain objects, not JPA entities. " +
                        "Use domain models and let adapters handle entity mapping.")
                .allowEmptyShould(true)
                .check(classes);
    }

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
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta..",
                        "javax..",
                        "org.hibernate.."
                )
                .because("Common is shared between modules; it must be framework-agnostic. " +
                        "Exceptions: common.messaging and common.aop may use Spring for cross-cutting concerns.")
                .check(classes);
    }

    // -------------------------------------------------------------------
    // 4) Modulith boundaries: course, user and storage must not import each other
    // -------------------------------------------------------------------

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

    // -------------------------------------------------------------------
    // 5) Extra strictness: forbid direct coupling between adapters
    // -------------------------------------------------------------------

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

    @ArchTest
    void adapterOut_shouldNotDependOnAdapterIn(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..adapter.out..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..adapter.in..")
                .because("Adapters must not form cycles; both depend inward toward Application/Domain.")
                .check(classes);
    }
}
