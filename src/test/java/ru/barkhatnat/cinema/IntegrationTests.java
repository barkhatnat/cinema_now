package ru.barkhatnat.cinema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.barkhatnat.cinema.domain.Role;
import ru.barkhatnat.cinema.domain.enums.RoleName;
import ru.barkhatnat.cinema.dto.create.MovieCreateDto;
import ru.barkhatnat.cinema.dto.create.UserCreateDto;
import ru.barkhatnat.cinema.repository.MovieRepository;
import ru.barkhatnat.cinema.repository.RoleRepository;
import ru.barkhatnat.cinema.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    private String userToken;

    @BeforeEach
    void setup() throws Exception {
        movieRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        adminToken = registerAndLoginUser("admin@test.com", "password", RoleName.ROLE_ADMIN, "Admin", "Test", "Test");
        userToken = registerAndLoginUser("user@test.com", "user123", RoleName.ROLE_USER, "User", "Test", "Test");
    }

    private String registerAndLoginUser(String email, String password, RoleName roleName, String firstName, String lastName, String middleName) throws Exception {
        Role role = Role.builder().name(roleName).build();
        roleRepository.saveAndFlush(role);

        UserCreateDto userCreateDto = new UserCreateDto(email, password, firstName, lastName, middleName, role.getId());

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated());

        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.accessToken");
    }

    @Test
    void registrationCreatesUser() throws Exception {
        String token = registerAndLoginUser("myuser@test.com", "password", RoleName.ROLE_USER, "User", "Test", "Test");
        assertTrue(userRepository.findByEmail("myuser@test.com").isPresent());
    }

    @Test
    void loginReturnsToken() throws Exception {
        String loginJson = """
                {
                    "email": "admin@test.com",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void loginNotReturnsToken_invalidPassword() throws Exception {
        String loginJson = """
                {
                    "email": "admin@test.com",
                    "password": "1"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateMovie() throws Exception {
        MovieCreateDto movie = new MovieCreateDto("Test", 90L, "Desc");


        mockMvc.perform(post("/rest/admin/movies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.description").value("Desc"))
                .andExpect(jsonPath("$.duration").value(90));
    }

    @Test
    void userCannotAccessAdminEndpoint() throws Exception {
        MovieCreateDto movie = new MovieCreateDto("Test", 90L, "Desc");

        mockMvc.perform(post("/rest/admin/movies")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllMoviesReturnsListByAdmin() throws Exception {
        mockMvc.perform(get("/rest/movies")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllMoviesReturnsListByUser() throws Exception {
        mockMvc.perform(get("/rest/movies")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}