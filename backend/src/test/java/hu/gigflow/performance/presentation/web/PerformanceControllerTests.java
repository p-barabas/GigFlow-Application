package hu.gigflow.performance.presentation.web;

import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;
import hu.gigflow.performance.infrastructure.configuration.PerformanceUseCaseConfiguration;
import hu.gigflow.performance.infrastructure.persistence.InMemoryPerformanceRepository;
import hu.gigflow.performance.infrastructure.persistence.SamplePerformanceData;
import hu.gigflow.performance.infrastructure.security.DemoUsersConfiguration;
import hu.gigflow.performance.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerformanceController.class)
@ActiveProfiles("demo")
@Import({
        PerformanceUseCaseConfiguration.class,
        PerformanceResponseMapper.class,
        SessionController.class,
        SecurityConfiguration.class,
        DemoUsersConfiguration.class,
        PerformanceControllerTests.PersistenceTestConfiguration.class
})
class PerformanceControllerTests {

    private static final String AUTUMN_CONCERT_ID = "00000000-0000-0000-0000-000000000001";
    private static final String ANNA_PASSWORD = UUID.randomUUID().toString();
    private static final String BENCE_PASSWORD = UUID.randomUUID().toString();
    private static final String CSILLA_PASSWORD = UUID.randomUUID().toString();
    private static final String LEADER_PASSWORD = UUID.randomUUID().toString();

    @DynamicPropertySource
    static void demoPasswords(DynamicPropertyRegistry properties) {
        properties.add("gigflow.security.demo-users.anna-password", () -> ANNA_PASSWORD);
        properties.add("gigflow.security.demo-users.bence-password", () -> BENCE_PASSWORD);
        properties.add("gigflow.security.demo-users.csilla-password", () -> CSILLA_PASSWORD);
        properties.add("gigflow.security.demo-users.leader-password", () -> LEADER_PASSWORD);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestPerformanceRepository repository;

    @BeforeEach
    void resetRepository() {
        repository.reset();
    }

    @TestConfiguration
    static class PersistenceTestConfiguration {

        @Bean
        TestPerformanceRepository performanceRepository() {
            return new TestPerformanceRepository();
        }
    }

    static final class TestPerformanceRepository implements PerformanceRepository {

        private InMemoryPerformanceRepository delegate;
        private boolean failOnFindAll;

        void reset() {
            delegate = new InMemoryPerformanceRepository();
            SamplePerformanceData.loadInto(delegate);
            failOnFindAll = false;
        }

        void empty() {
            delegate = new InMemoryPerformanceRepository();
        }

        void failOnFindAll() {
            failOnFindAll = true;
        }

        @Override
        public Optional<Performance> findById(PerformanceId performanceId) {
            return delegate.findById(performanceId);
        }

        @Override
        public List<Performance> findAll() {
            if (failOnFindAll) {
                throw new IllegalStateException("A teszt repository nem érhető el.");
            }
            return delegate.findAll();
        }

        @Override
        public void save(Performance performance) {
            delegate.save(performance);
        }
    }

    @Test
    @WithMockUser(username = "anna", roles = "MEMBER")
    void listsMultiplePerformancesUsingThePublicContract() throws Exception {
        mockMvc.perform(get("/api/performances"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(AUTUMN_CONCERT_ID))
                .andExpect(jsonPath("$[0].title").value("Őszi koncert"))
                .andExpect(jsonPath("$[0].startsAt").value("2099-10-17T18:00:00Z"))
                .andExpect(jsonPath("$[0].venue").value("Városi Művelődési Ház"))
                .andExpect(jsonPath("$[0].responses.anna").value("ACCEPTED"))
                .andExpect(jsonPath("$[0].responses.bence").value("TENTATIVE"))
                .andExpect(jsonPath("$[0].responseTimes").doesNotExist())
                .andExpect(jsonPath("$[0].invitedMemberIds.length()").value(3))
                .andExpect(jsonPath("$[1].id").value("00000000-0000-0000-0000-000000000002"))
                .andExpect(jsonPath("$[1].startsAt").value("2099-11-06T20:30:00Z"));
    }

    @Test
    @WithMockUser(username = "anna", roles = "MEMBER")
    void returnsOkWithAnEmptyJsonArrayWhenNoPerformancesExist() throws Exception {
        repository.empty();

        mockMvc.perform(get("/api/performances"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(username = "anna", roles = "MEMBER")
    void translatesUnexpectedListFailureToProblemDetail() throws Exception {
        repository.failOnFindAll();

        mockMvc.perform(get("/api/performances"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("The performance list could not be loaded."))
                .andExpect(jsonPath("$.instance").value("/api/performances"));
    }

    @Test
    @WithMockUser(username = "leader", roles = "LEADER")
    void returnsCreatedResponseAndLocationForValidRequest() throws Exception {
        mockMvc.perform(post("/api/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Téli kamarazene",
                                  "venue": "Bartók Terem",
                                  "startsAt": "2099-12-12T19:00:00Z",
                                  "invitedMemberIds": ["anna", "bence", "csilla"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(
                        "^/api/performances/[0-9a-fA-F-]{36}$")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Téli kamarazene"))
                .andExpect(jsonPath("$.venue").value("Bartók Terem"))
                .andExpect(jsonPath("$.startsAt").value("2099-12-12T19:00:00Z"))
                .andExpect(jsonPath("$.invitedMemberIds.length()").value(3))
                .andExpect(jsonPath("$.responses").isEmpty());

        mockMvc.perform(get("/api/performances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'Téli kamarazene')]").exists());
    }

    @Test
    @WithMockUser(username = "leader", roles = "LEADER")
    void returnsDocumentedProblemDetailForInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": " ",
                                  "venue": "Bartók Terem",
                                  "startsAt": "2099-12-12T19:00:00Z",
                                  "invitedMemberIds": ["anna"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://gigflow.example/problems/validation-error"))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("The request contains invalid fields."))
                .andExpect(jsonPath("$.instance").value("/api/performances"))
                .andExpect(jsonPath("$.violations[0].field").value("title"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be blank"));
    }

    @Test
    @WithMockUser(username = "leader", roles = "LEADER")
    void requiresAtLeastOneInvitedMemberWhenCreatingPerformance() throws Exception {
        mockMvc.perform(post("/api/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Téli kamarazene",
                                  "venue": "Bartók Terem",
                                  "startsAt": "2099-12-12T19:00:00Z",
                                  "invitedMemberIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.violations[0].field").value("invitedMemberIds"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be empty"));
    }

    @Test
    @WithMockUser(username = "leader", roles = "LEADER")
    void returnsDocumentedConflictForRepeatedCreateRequest() throws Exception {
        String request = validCreateRequest();
        String location = mockMvc.perform(post("/api/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
        String createdId = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(post("/api/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://gigflow.example/problems/performance-conflict"))
                .andExpect(jsonPath("$.title").value("Performance conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.instance").value("/api/performances"))
                .andExpect(jsonPath("$.conflictingPerformanceId").value(createdId));
    }

    @Test
    @WithMockUser(username = "anna", roles = "MEMBER")
    void recordsMemberResponseThroughApplicationUseCase() throws Exception {
        mockMvc.perform(put("/api/performances/{id}/response", AUTUMN_CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availability\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responses.anna").value("ACCEPTED"));

        mockMvc.perform(put("/api/performances/{id}/response", AUTUMN_CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availability\":\"TENTATIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.responses.anna").value("TENTATIVE"));
    }

    @Test
    @WithMockUser(username = "anna", roles = "MEMBER")
    void rejectsUnsupportedAvailabilityUsingDocumentedValidationProblem() throws Exception {
        mockMvc.perform(put("/api/performances/{id}/response", AUTUMN_CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availability\":\"MAYBE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://gigflow.example/problems/validation-error"))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value(
                        "/api/performances/" + AUTUMN_CONCERT_ID + "/response"))
                .andExpect(jsonPath("$.violations[0].field").value("availability"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("must be ACCEPTED, TENTATIVE or DECLINED"));
    }

    @Test
    @WithMockUser(username = "anna", roles = "MEMBER")
    void returnsNotFoundForMissingPerformance() throws Exception {
        mockMvc.perform(put("/api/performances/00000000-0000-0000-0000-000000000099/response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availability\":\"ACCEPTED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://gigflow.example/problems/performance-not-found"))
                .andExpect(jsonPath("$.title").value("Performance not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.instance").value(
                        "/api/performances/00000000-0000-0000-0000-000000000099/response"));
    }

    @Test
    @WithMockUser(username = "denes", roles = "MEMBER")
    void returnsConflictForMemberWhoWasNotInvited() throws Exception {
        mockMvc.perform(put("/api/performances/{id}/response", AUTUMN_CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availability\":\"ACCEPTED\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://gigflow.example/problems/member-not-invited"))
                .andExpect(jsonPath("$.title").value("Invitation conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.instance").value(
                        "/api/performances/" + AUTUMN_CONCERT_ID + "/response"));
    }

    @Test
    void requiresAuthenticationForPerformanceList() throws Exception {
        mockMvc.perform(get("/api/performances"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"GigFlow\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "anna", roles = "MEMBER")
    void returnsAuthenticatedUserSession() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("anna"))
                .andExpect(jsonPath("$.displayName").value("Anna"))
                .andExpect(jsonPath("$.memberId").value("anna"))
                .andExpect(jsonPath("$.roles[0]").value("MEMBER"));
    }

    @Test
    @WithMockUser(username = "leader", roles = "LEADER")
    void returnsLeaderSessionWithoutMemberIdentity() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Zenekarvezető"))
                .andExpect(jsonPath("$.memberId").doesNotExist())
                .andExpect(jsonPath("$.roles[0]").value("LEADER"));
    }

    @Test
    @WithMockUser(username = "leader", roles = "LEADER")
    void allowsLeaderToListPerformances() throws Exception {
        mockMvc.perform(get("/api/performances"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatesDemoMembersWithEncodedInMemoryPasswords() throws Exception {
        mockMvc.perform(get("/api/performances").with(httpBasic("anna", ANNA_PASSWORD)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/performances").with(httpBasic("bence", BENCE_PASSWORD)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/performances").with(httpBasic("csilla", CSILLA_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void exposesOnlyApplicationRolesInBasicAuthenticatedSession() throws Exception {
        mockMvc.perform(get("/api/session").with(httpBasic("anna", ANNA_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("MEMBER"));
    }

    @Test
    void grantsLeaderRoleToEncodedDemoUser() throws Exception {
        mockMvc.perform(post("/api/performances")
                        .with(httpBasic("leader", LEADER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsInvalidBasicPassword() throws Exception {
        mockMvc.perform(get("/api/performances")
                        .with(httpBasic("anna", UUID.randomUUID().toString())))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"GigFlow\""));
    }

    @Test
    void requiresAuthenticationToCreatePerformance() throws Exception {
        mockMvc.perform(post("/api/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "anna", roles = "MEMBER")
    void forbidsMemberFromCreatingPerformance() throws Exception {
        mockMvc.perform(post("/api/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void requiresAuthenticationToRespond() throws Exception {
        mockMvc.perform(put("/api/performances/{id}/response", AUTUMN_CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availability\":\"ACCEPTED\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "leader", roles = "LEADER")
    void forbidsLeaderFromRespondingAsAnotherMember() throws Exception {
        mockMvc.perform(put("/api/performances/{id}/response", AUTUMN_CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availability\":\"ACCEPTED\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void requiresAuthenticationForUnknownApiPath() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void keepsStaticFrontendPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    private String validCreateRequest() {
        return """
                {
                  "title": "Biztonsági tesztkoncert",
                  "venue": "Bartók Terem",
                  "startsAt": "2099-12-12T19:00:00Z",
                  "invitedMemberIds": ["anna", "bence", "csilla"]
                }
                """;
    }
}
