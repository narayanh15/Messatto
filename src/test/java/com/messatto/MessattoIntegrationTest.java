package com.messatto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messatto.domain.entity.AppUser;
import com.messatto.domain.enums.Role;
import com.messatto.repository.FeedbackRepository;
import com.messatto.repository.MealAttendanceRepository;
import com.messatto.repository.MealRepository;
import com.messatto.repository.QrTokenRepository;
import com.messatto.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MessattoIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private MealAttendanceRepository attendanceRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private QrTokenRepository qrTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("app.seed-default-super-admin", () -> "false");
        registry.add("app.jwt.secret", () -> "messatto-test-secret-change-me-please-32-bytes-minimum");
    }

    @BeforeEach
    void cleanDatabase() {
        feedbackRepository.deleteAll();
        attendanceRepository.deleteAll();
        qrTokenRepository.deleteAll();
        mealRepository.deleteAll();
        userRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();

        userRepository.save(new AppUser(
                "Mess Admin",
                "admin@messatto.local",
                null,
                passwordEncoder.encode("Admin@123"),
                Role.MESS_ADMIN,
                "Hostel A",
                true
        ));
        userRepository.save(new AppUser(
                "Student One",
                "student@messatto.local",
                "CS-001",
                passwordEncoder.encode("Student@123"),
                Role.STUDENT,
                "Hostel A",
                true
        ));
    }

    @Test
    void coreMealQrAttendanceFeedbackFlowWorks() throws Exception {
        String adminToken = login("admin@messatto.local", "Admin@123");
        String studentToken = login("student@messatto.local", "Student@123");

        JsonNode meal = performJson(post("/admin/meals")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "mealDate", "2026-06-19",
                        "mealType", "BREAKFAST",
                        "vegMenu", "Idli, sambar, chutney",
                        "nonVegMenu", "Egg curry, toast",
                        "startTime", "07:00:00",
                        "endTime", "09:00:00"
                ))));
        String mealId = meal.get("id").asText();

        JsonNode qr = performJson(post("/admin/meals/{mealId}/qr", mealId)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "mealChoice", "VEG",
                        "ttlSeconds", 900
                ))));
        String token = qr.get("token").asText();

        mockMvc.perform(post("/attendance/scan")
                        .header(HttpHeaders.AUTHORIZATION, bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyRecorded").value(false))
                .andExpect(jsonPath("$.mealChoice").value("VEG"));

        mockMvc.perform(post("/attendance/scan")
                        .header(HttpHeaders.AUTHORIZATION, bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyRecorded").value(true));

        mockMvc.perform(post("/feedback")
                        .header(HttpHeaders.AUTHORIZATION, bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mealId", mealId,
                                "tasteRating", 4,
                                "hygieneRating", 5,
                                "quantityRating", 4,
                                "serviceRating", 5,
                                "comment", "Good breakfast service",
                                "anonymous", true
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/feedback")
                        .header(HttpHeaders.AUTHORIZATION, bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "mealId", mealId,
                                "tasteRating", 5,
                                "hygieneRating", 5,
                                "quantityRating", 5,
                                "serviceRating", 5,
                                "anonymous", false
                        ))))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/admin/dashboard/live")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .param("date", "2026-06-19")
                        .param("mealType", "BREAKFAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAttendance").value(1))
                .andExpect(jsonPath("$[0].vegCount").value(1))
                .andExpect(jsonPath("$[0].nonVegCount").value(0));

        JsonNode summary = performJson(get("/admin/feedback/summary")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .param("mealId", mealId));
        assertThat(summary.get(0).get("totalFeedback").asInt()).isEqualTo(1);
        assertThat(summary.get(0).get("comments").get(0).get("anonymous").asBoolean()).isTrue();
        assertThat(summary.get(0).get("comments").get(0).get("studentName").isNull()).isTrue();
    }

    private String login(String email, String password) throws Exception {
        JsonNode response = performJson(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "email", email,
                        "password", password
                ))));
        return response.get("accessToken").asText();
    }

    private JsonNode performJson(org.springframework.test.web.servlet.RequestBuilder requestBuilder) throws Exception {
        String json = mockMvc.perform(requestBuilder)
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(json);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-06-19T07:30:00Z"), ZoneId.of("UTC"));
        }
    }
}
