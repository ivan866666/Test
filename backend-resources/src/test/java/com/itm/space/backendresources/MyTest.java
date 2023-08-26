package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "Ivan", password = "Ivan", authorities = "ROLE_MODERATOR")
public class MyTest extends BaseIntegrationTest {
    @MockBean
    private Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realmItm;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private MappingsRepresentation mappingsRepresentation;

    private UserRequest testUserRequest;
    private UserRequest testInvalidUserRequest;
    private RealmResource realmResourceMock;
    private UsersResource usersResourceMock;
    private UserRepresentation userRepresentationMock;
    private UserResource userResourceMock;
    private UUID testId;

    @BeforeEach
    void initNecessaryMocks() {
        testUserRequest = new UserRequest("Ivan", "Ivan@gmail.com", "weri", "Ivan", "Krulov");
        testInvalidUserRequest = new UserRequest("Ivan", "Ivangmail.com", "", "Ivuna", "K");
        realmResourceMock = mock(RealmResource.class);
        usersResourceMock = mock(UsersResource.class);
        userRepresentationMock = mock(UserRepresentation.class);
        userResourceMock = mock(UserResource.class);
        testId = UUID.randomUUID();
    }


    @Test
    @SneakyThrows
    public void helloControllerTest() {
        MockHttpServletResponse mockHttpServletResponse = mvc.perform(get("/api/users/hello"))
                .andReturn()
                .getResponse();
        assertEquals(HttpStatus.OK.value(), mockHttpServletResponse.getStatus());
        assertEquals("Ivan", mockHttpServletResponse.getContentAsString());
    }

    @Test
    @SneakyThrows
    public void userCreatedTest() {
        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any())).thenReturn(Response.status(Response.Status.CREATED).build());
        when(userRepresentationMock.getId()).thenReturn(UUID.randomUUID().toString());
        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"), testUserRequest))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(keycloak).realm(realmItm);
        verify(realmResourceMock).users();
        verify(usersResourceMock).create(any(UserRepresentation.class));
    }
    @Test
    @SneakyThrows
    public void UserCreatedTestFail(){
        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"), testInvalidUserRequest))
                .andDo(print())
                .andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }
    @Test
    @SneakyThrows
    public void getUserByIdTest() {

        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(String.valueOf(testId))).thenReturn(userResourceMock);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(String.valueOf(testId));
        userRepresentation.setFirstName("Ivan");
        userRepresentation.setLastName("Ivan");
        userRepresentation.setEmail("Ivan@gmail.com");

        when(userResourceMock.toRepresentation()).thenReturn(userRepresentation);
        when(userResourceMock.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);

        MockHttpServletResponse response = mvc.perform(get("/api/users/" + testId))
                .andDo(print())
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andReturn()
                .getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    @SneakyThrows
    public void getUserByIdTestFail() {
        UUID userId = UUID.randomUUID();
        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(mock(UsersResource.class));

        MockHttpServletResponse response = mvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isInternalServerError())
                .andDo(print())
                .andReturn().getResponse();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
    }
}