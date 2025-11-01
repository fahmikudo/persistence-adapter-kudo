package id.fahmikudo.persistence.repository;

import id.fahmikudo.persistence.entity.User;
import id.fahmikudo.persistence.request.UserRequest;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository using Testcontainers
 *
 * This demonstrates best practices for testing database repositories:
 * - Uses Testcontainers for real MySQL database
 * - Tests CRUD operations
 * - Tests custom query methods
 * - Tests pagination
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("territory_db")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("schema.sql");

    private static AnnotationConfigApplicationContext context;
    private static UserRepository userRepository;

    @BeforeAll
    static void setUp() {
        // Override the datasource properties with testcontainer values
        System.setProperty("spring.datasource.url", mysql.getJdbcUrl());
        System.setProperty("spring.datasource.username", mysql.getUsername());
        System.setProperty("spring.datasource.password", mysql.getPassword());
        System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");

        // Initialize Spring context
        context = new AnnotationConfigApplicationContext();
        context.scan("id.fahmikudo.persistence");
        context.refresh();

        // Get repository bean
        userRepository = context.getBean(UserRepository.class);
    }

    @AfterAll
    static void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @BeforeEach
    void cleanUpData() throws Exception {
        // Clean up test data before each test (except initial sample data)
        var session = userRepository.getSession();
        session.getJdbcTemplate().update(
                "DELETE FROM users WHERE username NOT IN ('admin', 'john_doe', 'jane_smith', 'manager', 'bob_wilson')"
        );
    }

    // ============ CRUD OPERATIONS TESTS ============

    @Test
    @Order(1)
    @DisplayName("Test save - insert new user")
    void testSaveInsertNewUser() throws Exception {
        // Given
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("testuser@example.com");
        newUser.setPassword("password123");
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setActive(true);
        newUser.setRole("USER");
        newUser.setCreatedBy("test");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertNotNull(savedUser.getId(), "User ID should be generated");
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("testuser@example.com", savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("Test save - update existing user")
    void testSaveUpdateExistingUser() throws Exception {
        // Given - First create a user
        User newUser = new User();
        newUser.setUsername("updatetest");
        newUser.setEmail("updatetest@example.com");
        newUser.setPassword("password123");
        newUser.setRole("USER");
        newUser.setCreatedBy("test");
        User savedUser = userRepository.save(newUser);

        // When - Update the user
        savedUser.setFirstName("Updated");
        savedUser.setLastName("Name");
        savedUser.setUpdatedBy("test");
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        assertNotNull(updatedUser.getUpdatedAt());
    }

    @Test
    @Order(3)
    @DisplayName("Test find with no filters - returns all users")
    void testFindWithNoFilters() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession());

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertTrue(users.size() >= 5, "Should have at least 5 users from sample data");
    }

    @Test
    @Order(4)
    @DisplayName("Test find with username filter")
    void testFindWithUsernameFilter() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .username("admin");

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).getUsername());
    }

    @Test
    @Order(5)
    @DisplayName("Test find with multiple usernames")
    void testFindWithMultipleUsernames() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .usernames(Arrays.asList("admin", "john_doe", "jane_smith"));

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertEquals(3, users.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test find with active filter")
    void testFindWithActiveFilter() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .active(true);

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertTrue(users.size() >= 5);
        assertTrue(users.stream().allMatch(User::getActive));
    }

    @Test
    @Order(7)
    @DisplayName("Test find with role filter")
    void testFindWithRoleFilter() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .role("ADMIN");

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertEquals(1, users.size());
        assertEquals("ADMIN", users.get(0).getRole());
    }

    @Test
    @Order(8)
    @DisplayName("Test count with no filters")
    void testCountWithNoFilters() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession());

        // When
        int count = userRepository.count(request);

        // Then
        assertTrue(count >= 5, "Should have at least 5 users");
    }

    @Test
    @Order(9)
    @DisplayName("Test count with filters")
    void testCountWithFilters() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .role("USER");

        // When
        int count = userRepository.count(request);

        // Then
        assertEquals(3, count, "Should have 3 users with USER role");
    }

    // ============ PAGINATION TESTS ============

    @Test
    @Order(10)
    @DisplayName("Test pagination - page 1")
    void testPaginationPage1() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession());
        request.setOffset(0);
        request.setLimit(2);

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertEquals(2, users.size());
    }

    @Test
    @Order(11)
    @DisplayName("Test pagination - page 2")
    void testPaginationPage2() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession());
        request.setOffset(2);
        request.setLimit(2);

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertTrue(users.size() > 0);
    }

    @Test
    @Order(12)
    @DisplayName("Test pagination with count")
    void testPaginationWithCount() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .active(true);
        request.setOffset(0);
        request.setLimit(3);

        // When
        Optional<List<User>> result = userRepository.find(request);
        int totalCount = userRepository.count(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertEquals(3, users.size());
        assertTrue(totalCount >= 5);

        // Calculate pagination info
        int totalPages = (int) Math.ceil((double) totalCount / 3);
        assertTrue(totalPages >= 2);
    }

    // ============ CUSTOM QUERY METHODS TESTS ============

    @Test
    @Order(13)
    @DisplayName("Test findByUsername - found")
    void testFindByUsernameFound() {
        // When
        Optional<User> result = userRepository.findByUsername("admin");

        // Then
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals("admin@example.com", result.get().getEmail());
    }

    @Test
    @Order(14)
    @DisplayName("Test findByUsername - not found")
    void testFindByUsernameNotFound() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @Order(15)
    @DisplayName("Test findByEmail - found")
    void testFindByEmailFound() {
        // When
        Optional<User> result = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
        assertEquals("john.doe@example.com", result.get().getEmail());
    }

    @Test
    @Order(16)
    @DisplayName("Test findByEmail - not found")
    void testFindByEmailNotFound() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @Order(17)
    @DisplayName("Test findActiveUsers")
    void testFindActiveUsers() {
        // When
        List<User> activeUsers = userRepository.findActiveUsers();

        // Then
        assertNotNull(activeUsers);
        assertTrue(activeUsers.size() >= 5);
        assertTrue(activeUsers.stream().allMatch(User::getActive));
    }

    @Test
    @Order(18)
    @DisplayName("Test findByRole")
    void testFindByRole() {
        // When
        List<User> adminUsers = userRepository.findByRole("ADMIN");
        List<User> regularUsers = userRepository.findByRole("USER");

        // Then
        assertEquals(1, adminUsers.size());
        assertEquals("admin", adminUsers.get(0).getUsername());
        assertEquals(3, regularUsers.size());
    }

    @Test
    @Order(19)
    @DisplayName("Test findByRole - manager")
    void testFindByRoleManager() {
        // When
        List<User> managerUsers = userRepository.findByRole("MANAGER");

        // Then
        assertEquals(1, managerUsers.size());
        assertEquals("manager", managerUsers.get(0).getUsername());
    }

    // ============ EDGE CASES & ERROR HANDLING ============

    @Test
    @Order(20)
    @DisplayName("Test find with empty result")
    void testFindWithEmptyResult() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .username("nonexistent_user_xyz");

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    @Order(21)
    @DisplayName("Test count returns zero for non-existent filter")
    void testCountReturnsZero() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .role("NONEXISTENT_ROLE");

        // When
        int count = userRepository.count(request);

        // Then
        assertEquals(0, count);
    }

    @Test
    @Order(22)
    @DisplayName("Test complex filters - role and active")
    void testComplexFilters() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .role("USER")
                .active(true);

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertEquals(3, users.size());
        assertTrue(users.stream().allMatch(u -> "USER".equals(u.getRole()) && u.getActive()));
    }

    @Test
    @Order(23)
    @DisplayName("Test search keyword")
    void testSearchKeyword() throws Exception {
        // Given
        UserRequest request = new UserRequest(userRepository.getSession())
                .searchKeyword("john");

        // When
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertTrue(users.size() > 0);
        assertTrue(users.stream().anyMatch(u -> u.getUsername().contains("john")));
    }

    @Test
    @Order(24)
    @DisplayName("Test find by IDs")
    void testFindByIds() throws Exception {
        // Given - First get some user IDs
        UserRequest allUsersRequest = new UserRequest(userRepository.getSession())
                .usernames(Arrays.asList("admin", "john_doe"));
        List<User> allUsers = userRepository.find(allUsersRequest).get();
        List<Long> ids = allUsers.stream().map(User::getId).toList();

        // When
        UserRequest request = new UserRequest(userRepository.getSession())
                .ids(ids);
        Optional<List<User>> result = userRepository.find(request);

        // Then
        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertEquals(2, users.size());
    }

    // ============ TRANSACTION & CONSISTENCY TESTS ============

    @Test
    @Order(25)
    @DisplayName("Test data consistency after save")
    void testDataConsistencyAfterSave() throws Exception {
        // Given
        User newUser = new User();
        newUser.setUsername("consistency_test");
        newUser.setEmail("consistency@example.com");
        newUser.setPassword("password123");
        newUser.setFirstName("Consistency");
        newUser.setLastName("Test");
        newUser.setRole("USER");
        newUser.setActive(true);
        newUser.setCreatedBy("test");

        // When
        User savedUser = userRepository.save(newUser);
        Optional<User> foundUser = userRepository.findByUsername("consistency_test");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("Consistency", foundUser.get().getFirstName());
        assertEquals("Test", foundUser.get().getLastName());
    }

    @Test
    @Order(26)
    @DisplayName("Test update reflects in subsequent queries")
    void testUpdateReflectsInQueries() throws Exception {
        // Given - Create a user
        User newUser = new User();
        newUser.setUsername("update_reflection_test");
        newUser.setEmail("updatereflection@example.com");
        newUser.setPassword("password123");
        newUser.setRole("USER");
        newUser.setCreatedBy("test");
        User savedUser = userRepository.save(newUser);

        // When - Update the user
        savedUser.setRole("MANAGER");
        savedUser.setUpdatedBy("test");
        userRepository.save(savedUser);

        // Then - Verify the update
        Optional<User> foundUser = userRepository.findByUsername("update_reflection_test");
        assertTrue(foundUser.isPresent());
        assertEquals("MANAGER", foundUser.get().getRole());
    }
}

