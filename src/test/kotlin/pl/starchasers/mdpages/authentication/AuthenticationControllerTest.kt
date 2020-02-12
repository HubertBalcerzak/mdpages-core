package pl.starchasers.mdpages.authentication

import com.fasterxml.jackson.databind.ObjectMapper
import errorThrown
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import pl.starchasers.mdpages.user.UserService
import pl.starchasers.mdpages.authentication.dto.LoginDTO
import org.hamcrest.CoreMatchers.*
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.*
import pl.starchasers.mdpages.authentication.dto.TokenDTO
import success


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
internal class AuthenticationControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val userService: UserService,
    @Autowired private val tokenService: TokenService
) {

    @Autowired
    lateinit var mapper: ObjectMapper

    @BeforeAll
    fun createTestUser() {
        userService.createUser("testUser", "passw0rd")
    }


    @Nested
    @DisplayName("/api/auth/login endpoint")
    inner class Login() {

        @Test
        fun `Given valid data, should return refresh token`() {
            mockMvc.post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(LoginDTO("testUser", "passw0rd"))
            }.andDo {
                print()
            }.andExpect {
                status { isOk }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.success", equalTo(true))
                jsonPath("$.token", notNullValue())
            }
        }

        @Test
        fun `Given incorrect password, should return 401`() {
            mockMvc.post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(LoginDTO("testUser", "pasword"))
            }.andDo {
                print()
            }.let { errorThrown(it) }
        }

        @Test
        fun `Given missing fields, should return 401`() {
            mockMvc.post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = "{}"
            }.andDo {
                print()
            }.let { errorThrown(it) }
        }
    }

    @Nested
    inner class GetAccessToken {

        private var refreshToken = ""

        @BeforeEach
        fun issueTestRefreshToken() {
            refreshToken = tokenService.issueRefreshToken(userService.getUserByUsername("testUser"))
        }

        @AfterEach
        fun removeTestRefreshToken() {
            tokenService.invalidateRefreshToken(refreshToken)
        }

        @Test
        fun `Given valid refresh token, should return access token`() {
            mockMvc.post("/api/auth/getAccessToken") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(TokenDTO(refreshToken))
            }.andDo {
                print()
            }.andExpect {
                jsonPath("$.token", notNullValue())
            }.let { success(it) }
        }

        @Test
        fun `Given invalid token, should return 401`() {
            removeTestRefreshToken()
            mockMvc.post("/api/auth/getAccessToken") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(TokenDTO(refreshToken))
            }.andDo {
                print()
            }.let { errorThrown(it) }
        }

    }

    @Nested
    inner class RefreshToken {
        private var refreshToken = ""

        @BeforeEach
        fun issueTestRefreshToken() {
            refreshToken = tokenService.issueRefreshToken(userService.getUserByUsername("testUser"))
        }

        @AfterEach
        fun removeTestRefreshToken() {
            tokenService.invalidateRefreshToken(refreshToken)
        }


        @Test
        fun `Given valid refresh token, should return new refresh token`() {
            mockMvc.post("/api/auth/refreshToken") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(TokenDTO(refreshToken))
            }.andDo {
                print()
            }.andExpect {
                jsonPath("$.token", notNullValue())
            }.let { success(it) }
        }

        @Test
        fun `Given invalid refresh token, should return 401`() {
            removeTestRefreshToken()
            mockMvc.post("/api/auth/refreshToken") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(TokenDTO(refreshToken))
            }.andDo { print() }.let { errorThrown(it) }
        }
    }

    @Nested
    inner class Logout() {
        //TODO implement
    }

}