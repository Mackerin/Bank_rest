package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.testConstants.AnotherUserTestConstants.*;
import static com.example.bankcards.testConstants.MessageTestConstants.*;
import static com.example.bankcards.testConstants.UserTestConstants.*;
import static globalConstants.MessageConstants.EMAIL_ALREADY_EXISTS;
import static globalConstants.MessageConstants.USERNAME_ALREADY_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service tests")
class UserServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;
    private User testUser;
    private User anotherUser;
    private CreateUserRequest createUserRequest;
    private CreateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .phoneNumber(TEST_PHONE_NUMBER)
                .role(Role.ROLE_USER)
                .active(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        anotherUser = User.builder()
                .id(2L)
                .username(ANOTHER_USERNAME)
                .email(ANOTHER_EMAIL)
                .password(ANOTHER_PASSWORD)
                .firstName(ANOTHER_FIRST_NAME)
                .lastName(ANOTHER_LAST_NAME)
                .phoneNumber(ANOTHER_PHONE_NUMBER)
                .role(Role.ROLE_USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(ANOTHER_USERNAME);
        createUserRequest.setEmail(ANOTHER_EMAIL);
        createUserRequest.setPassword(ANOTHER_PASSWORD);
        createUserRequest.setFirstName(ANOTHER_FIRST_NAME);
        createUserRequest.setLastName(ANOTHER_LAST_NAME);
        createUserRequest.setPhoneNumber(ANOTHER_PHONE_NUMBER);
        createUserRequest.setRole(Role.ROLE_USER);

        updateUserRequest = new CreateUserRequest();
        updateUserRequest.setEmail(UPDATE_EMAIL);
        updateUserRequest.setFirstName(UPDATE_FIRST_NAME);
        updateUserRequest.setLastName(UPDATE_LAST_NAME);
        updateUserRequest.setPhoneNumber(UPDATE_PHONE_NUMBER);
        updateUserRequest.setPassword(NEW_PASSWORD);
    }

    @Test
    @DisplayName("Успешное создание пользователя с уникальными username и email")
    void createUserWithValidRequestTest() {
        when(userRepository.existsByUsername(ANOTHER_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(ANOTHER_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(ANOTHER_PASSWORD)).thenReturn(ENCODE_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(anotherUser);
        UserDTO result = userService.createUser(createUserRequest);
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(ANOTHER_USERNAME, result.getUsername());
        assertEquals(ANOTHER_EMAIL, result.getEmail());
        assertEquals(ANOTHER_FIRST_NAME, result.getFirstName());
        assertEquals(ANOTHER_LAST_NAME, result.getLastName());
        assertEquals(Role.ROLE_USER, result.getRole());
        assertTrue(result.getActive());
        verify(userRepository).existsByUsername(ANOTHER_USERNAME);
        verify(userRepository).existsByEmail(ANOTHER_EMAIL);
        verify(passwordEncoder).encode(ANOTHER_PASSWORD);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с существующим username выбрасывает ValidationException")
    void createUserWithExistingUsernameTest() {
        when(userRepository.existsByUsername(ANOTHER_USERNAME)).thenReturn(true);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.createUser(createUserRequest));
        assertEquals(USERNAME_ALREADY_EXISTS, exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Получение всех пользователей возвращает пустой список, если нет пользователей")
    void getAllUsersWhenNoUsersTest() {
        when(userRepository.findAll()).thenReturn(List.of());
        List<UserDTO> result = userService.getAllUsers();
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Получение пользователей по роли возвращает пустой список, если нет совпадений")
    void getUsersByRoleWhenNoUsersTest() {
        when(userRepository.findByRole(Role.ROLE_ADMIN)).thenReturn(List.of());
        List<UserDTO> result = userService.getUsersByRole(Role.ROLE_ADMIN);
        assertTrue(result.isEmpty());
        verify(userRepository).findByRole(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Создание пользователя с существующим email выбрасывает ValidationException")
    void createUserWithExistingEmailTest() {
        when(userRepository.existsByUsername(ANOTHER_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(ANOTHER_EMAIL)).thenReturn(true);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.createUser(createUserRequest));
        assertEquals(EMAIL_ALREADY_EXISTS, exception.getMessage());
        verify(userRepository).existsByUsername(ANOTHER_USERNAME);
        verify(userRepository).existsByEmail(ANOTHER_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя без указания роли использует ROLE_USER по умолчанию")
    void createUserWithoutRoleTest() {
        createUserRequest.setRole(null);
        when(userRepository.existsByUsername(ANOTHER_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(ANOTHER_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(ANOTHER_PASSWORD)).thenReturn(ENCODE_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(anotherUser);
        UserDTO result = userService.createUser(createUserRequest);
        assertEquals(Role.ROLE_USER, result.getRole());
    }

    @Test
    @DisplayName("Получение пользователя по ID для существующего пользователя")
    void getUserByIdWithValidIdTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        UserDTO result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_FIRST_NAME, result.getFirstName());
        assertEquals(TEST_LAST_NAME, result.getLastName());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение несуществующего пользователя по ID выбрасывает UserNotFoundException")
    void getUserByIdWithInvalidIdTest() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(999L));
        assertEquals(USER_NOT_FOUND_BY_ID, exception.getMessage());
    }

    @Test
    @DisplayName("Создание пользователя с null паролем выбрасывает исключение")
    void createUserWithNullPasswordTest() {
        createUserRequest.setPassword(null);
        when(userRepository.existsByUsername(ANOTHER_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(ANOTHER_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(null)).thenThrow(new IllegalArgumentException(PASSWORD_CANNOT_BE_NULL));
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(createUserRequest));
    }

    @Test
    @DisplayName("Получение пользователя по username для существующего пользователя")
    void getUserByUsernameWithValidUsernameTest() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        UserDTO result = userService.getUserByUsername(TEST_USERNAME);
        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_EMAIL, result.getEmail());
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Получение несуществующего пользователя по username выбрасывает UserNotFoundException")
    void getUserByUsernameWithInvalidUsernameTest() {
        when(userRepository.findByUsername(NON_EXIST_USERNAME)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserByUsername(NON_EXIST_USERNAME));
        assertEquals(USER_NOT_FOUND_BY_USERNAME, exception.getMessage());
    }

    @Test
    @DisplayName("Получение всех пользователей возвращает список UserDTO")
    void getAllUsersTest() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, anotherUser));
        List<UserDTO> result = userService.getAllUsers();
        assertEquals(2, result.size());
        assertEquals(TEST_USERNAME, result.get(0).getUsername());
        assertEquals(ANOTHER_USERNAME, result.get(1).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Получение пользователей по роли возвращает отфильтрованный список")
    void getUsersByRoleTest() {
        when(userRepository.findByRole(Role.ROLE_USER)).thenReturn(Arrays.asList(testUser, anotherUser));
        List<UserDTO> result = userService.getUsersByRole(Role.ROLE_USER);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(user -> user.getRole() == Role.ROLE_USER));
        verify(userRepository).findByRole(Role.ROLE_USER);
    }

    @Test
    @DisplayName("Успешное обновление пользователя с валидными данными")
    void updateUserWithValidDataTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(UPDATE_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_ENCODE_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDTO result = userService.updateUser(1L, updateUserRequest);
        assertNotNull(result);
        assertEquals(UPDATE_EMAIL, result.getEmail());
        assertEquals(UPDATE_FIRST_NAME, result.getFirstName());
        assertEquals(UPDATE_LAST_NAME, result.getLastName());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail(UPDATE_EMAIL);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя без смены пароля не кодирует пароль")
    void updateUserWithoutPasswordChangeTest() {
        updateUserRequest.setPassword(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(UPDATE_EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDTO result = userService.updateUser(1L, updateUserRequest);
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Обновление пользователя с пустым паролем не кодирует пароль")
    void updateUserWithEmptyPasswordTest() {
        updateUserRequest.setPassword("");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(UPDATE_EMAIL)).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        userService.updateUser(1L, updateUserRequest);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Обновление пользователя с существующим email выбрасывает ValidationException")
    void updateUserWithExistingEmailTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(UPDATE_EMAIL)).thenReturn(true);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.updateUser(1L, updateUserRequest));
        assertEquals(EMAIL_ALREADY_EXISTS, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Обновление email на тот же самый не проверяет уникальность")
    void updateUserWithSameEmailTest() {
        updateUserRequest.setEmail(TEST_EMAIL);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_ENCODE_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDTO result = userService.updateUser(1L, updateUserRequest);
        assertNotNull(result);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Обновление пользователя не изменяет его роль")
    void updateUserDoesNotChangeRoleTest() {
        updateUserRequest.setRole(Role.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(UPDATE_EMAIL)).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UserDTO result = userService.updateUser(1L, updateUserRequest);
        assertEquals(Role.ROLE_USER, result.getRole());
    }

    @Test
    @DisplayName("Успешная деактивация пользователя")
    void deleteUserTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        userService.deleteUser(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user -> !user.getActive()));
    }

    @Test
    @DisplayName("Деактивация несуществующего пользователя выбрасывает UserNotFoundException")
    void deleteUserWithInvalidIdTest() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(999L));
        assertEquals(USER_NOT_FOUND_BY_ID, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Успешная активация пользователя")
    void activateUserTest() {
        testUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        userService.activateUser(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user -> user.getActive()));
    }

    @Test
    @DisplayName("Активация несуществующего пользователя выбрасывает UserNotFoundException")
    void activateUserWithInvalidIdTest() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.activateUser(999L));
        assertEquals(USER_NOT_FOUND_BY_ID, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}