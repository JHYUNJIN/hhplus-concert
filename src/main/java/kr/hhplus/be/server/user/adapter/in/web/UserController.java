package kr.hhplus.be.server.user.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.user.adapter.in.web.response.UserPointResponse;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.adapter.in.web.request.ChargePointRequest;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.user.usecase.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "유저 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "유저 생성",
            description = "유저 생성 API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "유저 생성 성공",
                    content = @Content(schema = @Schema(implementation = UserPointResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (예: 유저 정보가 유효하지 않음)"
            )
    })
    @PostMapping
    public ResponseEntity<UserPointResponse> createUser(

    ) throws CustomException {
        User newUser = new User(new BigDecimal(20000), LocalDateTime.now(), LocalDateTime.now());
        User createdUser = userService.createUser(newUser);

        return ResponseEntity.status(201).body(UserPointResponse.from(createdUser));
    }

    @Operation(
            summary = "유저 포인트 조회",
            description = "{userId}에 해당하는 유저의 포인트 잔액 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserPointResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 찾을 수 없음"
            )
    })
    @GetMapping("/{userId}/points")
    public ResponseEntity<UserPointResponse> getPoint(
            @PathVariable UUID userId
    ) throws CustomException {
        User user = userService.getUser(userId);

        return ResponseEntity.ok(UserPointResponse.from(user));
    }

    @Operation(
            summary = "유저 포인트 충전",
            description = "{userId}에 해당하는 유저의 포인트 충전"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "충전 성공",
                    content = @Content(schema = @Schema(implementation = UserPointResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "음수값 충전 등 잘못된 요청"
            )
    })
    @PostMapping("/{userId}/points")
    public ResponseEntity<UserPointResponse> chargePoint(
            @PathVariable UUID userId,
            @RequestBody ChargePointRequest request
    ) throws CustomException {
        User user = userService.chargePoint(userId, request.point());

        return ResponseEntity.ok(UserPointResponse.from(user));
    }
}
