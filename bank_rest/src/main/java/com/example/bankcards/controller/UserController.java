package com.example.bankcards.controller;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static globalConstants.AuthorizationConstants.HAS_ROLE_ADMIN;
import static globalConstants.AuthorizationConstants.OWNER_OR_ADMIN;
import static globalConstants.EndpointConstants.USERS_BASE_PATH;
import static globalConstants.EndpointConstants.USERS_OWN_PATH;
import static globalConstants.MessageConstants.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Создание пользователя", description = "Создание нового пользователя (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        UserDTO userDTO = userService.createUser(createUserRequest);
        return ResponseEntity.ok(ApiResponse.success(CREATE_USER_SUCCESS_MESSAGE, userDTO, USERS_BASE_PATH));
    }

    @GetMapping
    @Operation(summary = "Получение всех пользователей",
            description = "Получение списка всех пользователей (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(GET_USERS_SUCCESS_MESSAGE, users, USERS_BASE_PATH));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение пользователя по ID", description = "Получение информации о пользователе по его ID")
    @PreAuthorize(OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> getUserById(@PathVariable("id") Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(GET_USER_SUCCESS_MESSAGE,
                userDTO, USERS_BASE_PATH + "/" + id));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Получение пользователя по username",
            description = "Получение информации о пользователе по username (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> getUserByUsername(@PathVariable String username) {
        UserDTO userDTO = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(GET_USER_SUCCESS_MESSAGE,
                userDTO, USERS_BASE_PATH + "/username/" + username));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Получение пользователей по роли",
            description = "Получение списка пользователей по роли (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> getUsersByRole(@PathVariable Role role) {
        List<UserDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(GET_USERS_SUCCESS_MESSAGE,
                users, USERS_BASE_PATH + "/role/" + role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление пользователя", description = "Обновление информации о пользователе")
    @PreAuthorize(OWNER_OR_ADMIN)
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest updateUserRequest) {
        UserDTO userDTO = userService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(ApiResponse.success(UPDATE_USER_SUCCESS_MESSAGE,
                userDTO, USERS_BASE_PATH + "/" + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление пользователя", description = "Деактивация пользователя (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(DELETE_USER_SUCCESS_MESSAGE, null,
                USERS_BASE_PATH + "/" + id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Активация пользователя",
            description = "Активация ранее деактивированного пользователя (доступно только ADMIN)")
    @PreAuthorize(HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(ACTIVATE_USER_SUCCESS_MESSAGE, null,
                USERS_BASE_PATH + "/" + id + "/activate"));
    }

    @GetMapping("/me")
    @Operation(summary = "Получение текущего пользователя",
            description = "Получение информации о текущем аутентифицированном пользователе")
    public ResponseEntity<ApiResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long currentUserId = userPrincipal.getId();
        UserDTO userDTO = userService.getUserById(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(GET_CURRENT_USER_SUCCESS_MESSAGE,
                userDTO, USERS_OWN_PATH));
    }
}
