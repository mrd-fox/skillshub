package com.simplon_project.skillhub.skillhub.user.adapter.in.web;

import com.simplon_project.skillhub.skillhub.user.adapter.in.web.mapper.UserResponseMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.in.web.request.UpdateUserRequest;
import com.simplon_project.skillhub.skillhub.user.adapter.in.web.response.UserResponse;
import com.simplon_project.skillhub.skillhub.user.application.port.in.CreateUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.GetUserByExternalIdPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.GetUserByIdPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.UpdateUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByExternalIdCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByIdCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserPort createUserPort;
    private final GetUserByIdPort getUserByIdPort;
    private final GetUserByExternalIdPort getUserByExternalIdPort;
    private final UpdateUserPort updateUserPort;

    @Operation(
            summary = "Create a new user",
            description = """
                    This endpoint creates a new user using metadata injected by the Gateway through HTTP headers.
                    The frontend never sends this data directly — all headers come from a validated Keycloak session.
                    """,
            parameters = {
                    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, required = true, description = "External Keycloak user ID (UUID)", example = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2"),
                    @Parameter(name = "X-User-FirstName", in = ParameterIn.HEADER, description = "User's first name", example = "Marina"),
                    @Parameter(name = "X-User-LastName", in = ParameterIn.HEADER, description = "User's last name", example = "Darde"),
                    @Parameter(name = "X-User-Email", in = ParameterIn.HEADER, required = true, description = "User email address", example = "marina.darde@3wa.io"),
                    @Parameter(name = "X-User-Address", in = ParameterIn.HEADER, description = "User address", example = "12 rue des Lilas"),
                    @Parameter(name = "X-User-City", in = ParameterIn.HEADER, description = "City", example = "Paris"),
                    @Parameter(name = "X-User-Country", in = ParameterIn.HEADER, description = "Country", example = "France"),
                    @Parameter(name = "X-User-PostalCode", in = ParameterIn.HEADER, description = "Postal code", example = "75000"),
                    @Parameter(name = "X-User-PhoneNumber", in = ParameterIn.HEADER, description = "Phone number", example = "+33612345678"),
                    @Parameter(name = "X-User-Roles", in = ParameterIn.HEADER, description = "Comma-separated list of user roles", example = "STUDENT,TUTOR")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "User successfully created", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid or missing headers", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(HttpServletRequest request) {
        var command = UserHelper.extractCommandFromHeaders(request);
        var created = createUserPort.create(command);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponseMapper.mapToResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = """
                    Retrieves a user from the internal User Service database using the internal User ID.
                    This endpoint does NOT query IAM.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Internal User ID (UUID format, generated by the User Service)",
                            example = "1f26c07d-9a1a-4b9a-8a5e-3e05e09197c9",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully retrieved",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid ID format"
                    )
            }
    )
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        var command = new GetUserByIdCommand(id);
        var user = getUserByIdPort.getUserById(command);
        var response = UserResponseMapper.mapToResponse(user);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/external/{externalId}")
    @Operation(
            summary = "Get user by external ID",
            description = """
                    Retrieves a user from the internal User Service database using the external ID (Keycloak subject).
                    This endpoint does NOT query IAM.
                    """,
            parameters = {
                    @Parameter(
                            name = "externalId",
                            description = "External ID (UUID format, Keycloak sub)",
                            example = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully retrieved",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid externalId format"
                    )
            }
    )
    public ResponseEntity<UserResponse> getByExternalId(@PathVariable String externalId) {
        var command = new GetUserByExternalIdCommand(externalId);
        var user = getUserByExternalIdPort.getUserByExternalId(command);
        var response = UserResponseMapper.mapToResponse(user);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update a user",
            description = "Updates only the provided fields. Roles are additive only (TUTOR allowed)."
    )
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping(
            value = "/external/{externalId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse updateUser(
            @Parameter(description = "Keycloak external user id (UUID)", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable String externalId,
            @RequestBody UpdateUserRequest request
    ) {
        var command = request.toUpdateUserCommand(externalId);
        var updated = updateUserPort.update(command);
        return UserResponseMapper.mapToResponse(updated);
    }
}
