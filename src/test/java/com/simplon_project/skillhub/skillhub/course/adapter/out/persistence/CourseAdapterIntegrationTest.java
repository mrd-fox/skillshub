//package com.simplon_project.skillhub.skillhub.course.adapter.out.persistence;
//
//import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.CourseAdapter;
//import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
//import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
//import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.SectionEntity;
//import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
//import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
//import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
//import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
//import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
//import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.Statement;
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Testcontainers(disabledWithoutDocker = true)
//@ActiveProfiles("test")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DisplayName("CourseAdapter Integration Tests (single Postgres container, 3 databases)")
//class CourseAdapterIntegrationTest {
//
//    private static final String COURSE_DB = "course_service_test";
//    private static final String STORAGE_DB = "storage_service_test";
//    private static final String USER_DB = "user_service_test";
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
//            // English comments only: project rule
//            .withDatabaseName("postgres")
//            .withUsername("test")
//            .withPassword("test");
//
//    private static volatile boolean databasesCreated = false;
//
//    @DynamicPropertySource
//    static void overrideProperties(DynamicPropertyRegistry registry) {
//        ensureDatabasesCreated();
//
//        registry.add("spring.datasource.course.url", () -> jdbcUrlForDb(postgres, COURSE_DB));
//        registry.add("spring.datasource.course.username", postgres::getUsername);
//        registry.add("spring.datasource.course.password", postgres::getPassword);
//
//        registry.add("spring.datasource.storage.url", () -> jdbcUrlForDb(postgres, STORAGE_DB));
//        registry.add("spring.datasource.storage.username", postgres::getUsername);
//        registry.add("spring.datasource.storage.password", postgres::getPassword);
//
//        registry.add("spring.datasource.user.url", () -> jdbcUrlForDb(postgres, USER_DB));
//        registry.add("spring.datasource.user.username", postgres::getUsername);
//        registry.add("spring.datasource.user.password", postgres::getPassword);
//
//        // English comments only: project rule
//        // Prevent Rabbit listeners from starting during tests.
//        registry.add("course.rabbitmq.video-polling.enabled", () -> "false");
//        registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
//        registry.add("spring.rabbitmq.listener.direct.auto-startup", () -> "false");
//    }
//
//    private static synchronized void ensureDatabasesCreated() {
//        if (databasesCreated) {
//            return;
//        }
//
//        try (Connection connection = DriverManager.getConnection(
//                postgres.getJdbcUrl(),
//                postgres.getUsername(),
//                postgres.getPassword()
//        )) {
//            createDatabaseIfMissing(connection, COURSE_DB);
//            createDatabaseIfMissing(connection, STORAGE_DB);
//            createDatabaseIfMissing(connection, USER_DB);
//            databasesCreated = true;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create test databases", e);
//        }
//    }
//
//    private static void createDatabaseIfMissing(Connection connection, String dbName) throws Exception {
//        // English comments only: project rule
//        // Postgres does not support "CREATE DATABASE IF NOT EXISTS", so we use a DO block.
//        String sql = """
//                DO $$
//                BEGIN
//                   IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = '%s') THEN
//                      EXECUTE format('CREATE DATABASE %I', '%s');
//                   END IF;
//                END
//                $$;
//                """.formatted(dbName, dbName);
//
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute(sql);
//        }
//    }
//
//    private static String jdbcUrlForDb(PostgreSQLContainer<?> container, String dbName) {
//        String baseUrl = container.getJdbcUrl();
//        int lastSlash = baseUrl.lastIndexOf('/');
//        if (lastSlash < 0) {
//            throw new IllegalStateException("Unexpected JDBC url format: " + baseUrl);
//        }
//        return baseUrl.substring(0, lastSlash + 1) + dbName;
//    }
//
//    @Autowired
//    private CourseAdapter courseAdapter;
//
//    @Autowired
//    private JpaCourseRepository courseJpaRepository;
//
//    private static final String COURSE_TITLE = "Introduction to Spring Boot";
//    private static final String COURSE_DESCRIPTION = "Learn Spring Boot from scratch";
//    private static final Long COURSE_PRICE = 99L;
//    private static final String EXTERNAL_AUTHOR_ID = "author-123";
//
//    @BeforeEach
//    void setUp() {
//        courseJpaRepository.deleteAll();
//        courseJpaRepository.flush();
//    }
//
//    @AfterEach
//    void tearDown() {
//        courseJpaRepository.deleteAll();
//    }
//
//    private Course createCourse(String title) {
//        return Course.builder()
//                .title(title)
//                .description(COURSE_DESCRIPTION)
//                .price(COURSE_PRICE)
//                .status(CourseStatusEnum.DRAFT)
//                .externalUserId(EXTERNAL_AUTHOR_ID)
//                .build();
//    }
//
//    private CourseEntity createCourseEntity(String title) {
//        return CourseEntity.builder()
//                .courseId(EntityId.of(UUID.randomUUID()))
//                .title(title)
//                .description(COURSE_DESCRIPTION)
//                .price(COURSE_PRICE)
//                .status(CourseStatusEnum.DRAFT)
//                .externalUserId(EXTERNAL_AUTHOR_ID)
//                .sections(new HashSet<>())
//                .build();
//    }
//
//    private CourseEntity createPublishedCourseEntity(String title) {
//        return CourseEntity.builder()
//                .courseId(EntityId.of(UUID.randomUUID()))
//                .title(title)
//                .description(COURSE_DESCRIPTION)
//                .price(COURSE_PRICE)
//                .status(CourseStatusEnum.PUBLISHED)
//                .externalUserId(EXTERNAL_AUTHOR_ID)
//                .sections(new HashSet<>())
//                .build();
//    }
//
//    @Nested
//    @DisplayName("saveCourse")
//    class SaveCourse {
//
//        @Test
//        @DisplayName("should save a new course successfully")
//        @Transactional("courseTxManager")
//        void saveCourse_withValidCourse_shouldPersistAndReturn() {
//            Course course = createCourse(COURSE_TITLE);
//
//            Course savedCourse = courseAdapter.saveCourse(course);
//
//            assertNotNull(savedCourse);
//            assertNotNull(savedCourse.getId());
//            assertEquals(COURSE_TITLE, savedCourse.getTitle());
//
//            Optional<CourseEntity> foundEntity = courseJpaRepository.findById(
//                    EntityId.fromString(savedCourse.getId().asString())
//            );
//            assertTrue(foundEntity.isPresent());
//            assertEquals(COURSE_TITLE, foundEntity.get().getTitle());
//        }
//    }
//
//    @Nested
//    @DisplayName("assertCourseNotExists")
//    class AssertCourseNotExists {
//
//        @Test
//        @DisplayName("should throw when course with same title exists (exact match)")
//        @Transactional("courseTxManager")
//        void assertCourseNotExists_whenTitleExists_shouldThrowException() {
//            CourseEntity existingEntity = createCourseEntity(COURSE_TITLE);
//            courseJpaRepository.saveAndFlush(existingEntity);
//
//            Course newCourse = createCourse(COURSE_TITLE);
//
//            assertThrows(CourseAlreadyExistsException.class,
//                    () -> courseAdapter.assertCourseNotExists(newCourse));
//        }
//    }
//
//    @Nested
//    @DisplayName("loadPublicCourseDetail")
//    class LoadPublicCourseDetail {
//
//        @Test
//        @DisplayName("should include sections in detail")
//        @Transactional("courseTxManager")
//        void loadPublicCourseDetail_shouldIncludeSections() {
//            CourseEntity courseEntity = createPublishedCourseEntity(COURSE_TITLE);
//
//            SectionEntity section1 = SectionEntity.builder()
//                    .sectionId(EntityId.of(UUID.randomUUID()))
//                    .title("Section 1")
//                    .position(1)
//                    .course(courseEntity)
//                    .chapters(new HashSet<>())
//                    .build();
//
//            SectionEntity section2 = SectionEntity.builder()
//                    .sectionId(EntityId.of(UUID.randomUUID()))
//                    .title("Section 2")
//                    .position(2)
//                    .course(courseEntity)
//                    .chapters(new HashSet<>())
//                    .build();
//
//            courseEntity.getSections().add(section1);
//            courseEntity.getSections().add(section2);
//
//            CourseEntity savedEntity = courseJpaRepository.saveAndFlush(courseEntity);
//            Id courseId = Id.of(savedEntity.getCourseId().value().toString());
//
//            Optional<PublicCourseDetail> detailOpt = courseAdapter.loadPublicCourseDetail(courseId);
//
//            assertTrue(detailOpt.isPresent());
//            assertEquals(2, detailOpt.get().getSections().size());
//        }
//    }
//}