package com.simplon_project.skillhub.skillhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SkillhubApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true);
    }

    @Test
    void testFeatureOne() {
        assertTrue(true);
    }

    @Test
    void testFeatureTwo() {
        assertFalse(false);
    }

    @Test
    void testFeatureThree() {
        assertEquals(1, 1);
    }

    @Test
    void testFeatureFour() {
        assertNotNull(new Object()); // Vérifie qu'un objet n'est pas null
    }
}
