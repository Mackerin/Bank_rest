package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static com.example.bankcards.testConstants.JsonTestConstants.*;
import static com.example.bankcards.testConstants.UserTestConstants.*;
import static com.example.bankcards.testConstants.AnotherUserTestConstants.*;
import static globalConstants.EndpointConstants.*;
import static globalConstants.MessageConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User controller tests")
@ActiveProfiles("test")
class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    private UserDTO userDTO;
    private CreateUserRequest createUserRequest;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        User mockUserEntity = new User();
        mockUserEntity.setId(1L);
        mockUserEntity.setUsername(TEST_USERNAME);
        mockUserEntity.setRole(Role.ROLE_USER);
        mockUserEntity.setPassword(TEST_PASSWORD);
        mockUserEntity.setEmail(TEST_EMAIL);
        mockUserEntity.setFirstName(TEST_FIRST_NAME);
        mockUserEntity.setLastName(TEST_LAST_NAME);
        mockUserEntity.setActive(true);

        testUserPrincipal = new UserPrincipal(mockUserEntity);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername(TEST_USERNAME);
        userDTO.setEmail(TEST_EMAIL);
        userDTO.setFirstName(TEST_FIRST_NAME);
        userDTO.setLastName(TEST_LAST_NAME);
        userDTO.setRole(Role.ROLE_USER);
        userDTO.setActive(true);

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(ANOTHER_USERNAME);
        createUserRequest.setPassword(ANOTHER_PASSWORD);
        createUserRequest.setEmail(ANOTHER_EMAIL);
        createUserRequest.setFirstName(ANOTHER_FIRST_NAME);
        createUserRequest.setLastName(ANOTHER_LAST_NAME);
        createUserRequest.setRole(Role.ROLE_USER);
        createUserRequest.setPhoneNumber(ANOTHER_PHONE_NUMBER);
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно создаёт нового пользователя")
    void createUserWithValidRequestTest() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDTO);
        mockMvc.perform(post(USERS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(CREATE_USER_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_ID).value(1L));
        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно активирует пользователя")
    void activateUserAsAdminTest() throws Exception {
        doNothing().when(userService).activateUser(1L);
        mockMvc.perform(patch(USERS_ACTIVATE_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(ACTIVATE_USER_SUCCESS_MESSAGE));
        verify(userService).activateUser(1L);
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = ROLE_USER)
    @DisplayName("Обычный пользователь не может создавать других пользователей")
    void createUserAsUserTest() throws Exception {
        mockMvc.perform(post(USERS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно получает список всех пользователей")
    void getAllUsersAsAdminTest() throws Exception {
        List<UserDTO> users = Arrays.asList(userDTO);
        when(userService.getAllUsers()).thenReturn(users);
        mockMvc.perform(get(USERS_BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_USERS_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_ARRAY_ID).value(1L));
        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = ROLE_USER)
    @DisplayName("Обычный пользователь не может просматривать список всех пользователей")
    void getAllUsersAsUserTest() throws Exception {
        mockMvc.perform(get(USERS_BASE_PATH))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Пользователь успешно получает свои данные")
    void getUserByIdAsOwnerTest() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDTO);
        mockMvc.perform(get(USERS_PATH_BY_ID)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_USER_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_ID).value(1L));
        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно получает пользователя по имени")
    void getUserByUsernameAsAdminTest() throws Exception {
        when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(userDTO);
        mockMvc.perform(get(USERS_PATH_BY_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_USER_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_USERNAME).value(TEST_USERNAME));
        verify(userService).getUserByUsername(TEST_USERNAME);
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно получает список пользователей по конкретной роли")
    void getUsersByRoleAsAdminTest() throws Exception {
        List<UserDTO> users = Arrays.asList(userDTO);
        when(userService.getUsersByRole(Role.ROLE_USER)).thenReturn(users);
        mockMvc.perform(get(USERS_PATH_BY_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_USERS_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_ARRAY_ROLE).value(Role.ROLE_USER.name()));
        verify(userService).getUsersByRole(Role.ROLE_USER);
    }

    @Test
    @DisplayName("Пользователь успешно обновляет свои данные")
    void updateUserAsOwnerTest() throws Exception {
        when(userService.updateUser(eq(1L), any(CreateUserRequest.class))).thenReturn(userDTO);
        mockMvc.perform(put(USERS_PATH_BY_ID)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(UPDATE_USER_SUCCESS_MESSAGE));
        verify(userService).updateUser(eq(1L), any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Пользователь не может обновлять чужие данные")
    void updateUserAsNonOwnerTest() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");
        UserPrincipal otherPrincipal = new UserPrincipal(otherUser);
        mockMvc.perform(put(USERS_PATH_BY_ID)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        otherPrincipal,
                                        null,
                                        otherPrincipal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isForbidden());
        verify(userService, never()).updateUser(anyLong(), any(CreateUserRequest.class));
    }

    @Test
    @WithMockUser(username = TEST_ADMIN, roles = ROLE_ADMIN)
    @DisplayName("Администратор успешно удаляет пользователя")
    void deleteUserAsAdminTest() throws Exception {
        doNothing().when(userService).deleteUser(1L);
        mockMvc.perform(delete(USERS_PATH_BY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(DELETE_USER_SUCCESS_MESSAGE));
        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("Авторизованный пользователь успешно получает свой текущий профиль")
    void getCurrentUserTest() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDTO);
        mockMvc.perform(get(USERS_OWN_PATH)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        testUserPrincipal,
                                        null,
                                        testUserPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_SUCCESS).value(true))
                .andExpect(jsonPath(JSON_PATH_MESSAGE).value(GET_CURRENT_USER_SUCCESS_MESSAGE))
                .andExpect(jsonPath(JSON_PATH_DATA_USERNAME).value(TEST_USERNAME));
        verify(userService).getUserById(1L);
    }
}