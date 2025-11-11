package com.simplon_project.skillhub.skillhub.user.adapter.in.web;

import com.simplon_project.skillhub.skillhub.user.adapter.common.UserHelper;
import com.simplon_project.skillhub.skillhub.user.adapter.in.web.mapper.UserResponseMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.in.web.response.UserResponse;
import com.simplon_project.skillhub.skillhub.user.application.port.in.CreateUserPort;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserPort createUserPort;

    @Operation(
            summary = "Create a new user",
            description = """
                    This endpoint creates a new user using metadata injected by the Gateway through HTTP headers.
                    The frontend never sends this data directly — all headers come from a validated Keycloak session.
                    """,
            parameters = {
                    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, required = true, description = "External Keycloak user ID (UUID)", example = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2"),
                    @Parameter(name = "X-User-FirstName", in = ParameterIn.HEADER, required = true, description = "User's first name", example = "Marina"),
                    @Parameter(name = "X-User-LastName", in = ParameterIn.HEADER, required = true, description = "User's last name", example = "Darde"),
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
}
