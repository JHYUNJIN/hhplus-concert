package kr.hhplus.be.server.infrastructure.api.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.user.UserService;
import kr.hhplus.be.server.application.user.dto.RegisterUserCommand;
import kr.hhplus.be.server.application.user.dto.UserCommand;
import kr.hhplus.be.server.application.user.dto.UserQueryResult;
import kr.hhplus.be.server.infrastructure.api.user.dto.request.UserChargeWebRequest;
import kr.hhplus.be.server.infrastructure.api.user.dto.request.UserRegisterWebRequest;
import kr.hhplus.be.server.infrastructure.api.user.dto.response.UserWebResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "유저 관련 API")
public class UserController {

    private final UserService userService;
    /**
     * 새로운 사용자를 등록합니다.
     * 클라이언트로부터 사용자 ID를 받습니다. 초기 금액은 0으로 설정됩니다.
     * POST /api/v1/users
     * @param request 사용자 등록 요청 정보 (userId 포함)
     * @return 생성된 사용자 정보
     */
    @PostMapping // 이 메서드가 POST /api/v1/users 요청을 처리합니다.
    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다. 클라이언트에서 사용자 ID를 받습니다. 초기 금액은 0으로 설정됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "사용자 등록 성공",
                    content = @Content(schema = @Schema(implementation = UserWebResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ResponseStatus(HttpStatus.CREATED) // HTTP 201 Created 응답
    public ResponseEntity<UserWebResponse> registerUser(
            @Valid @RequestBody UserRegisterWebRequest request // 요청 본문에서 UserRegisterWebRequest를 받음
    ) {
        // 1. Web Request DTO (UserRegisterWebRequest)를 application 계층의 Command DTO로 변환
        RegisterUserCommand command = new RegisterUserCommand(request.getUserId(), BigDecimal.ZERO); // 초기 금액 0 고정

        // 2. application 계층의 UserService 호출
        UserQueryResult userQueryResult = userService.registerUser(command);

        // 3. UserQueryResult를 Web Response DTO로 변환하여 반환
        return new ResponseEntity<>(UserWebResponse.from(userQueryResult), HttpStatus.CREATED);
    }

    /**
     * 특정 ID의 사용자 정보를 조회합니다.
     * GET /api/v1/users/{userId}
     * @param userId 조회할 사용자의 ID
     * @return 조회된 사용자 정보 (UserResponse DTO)
     */
    @GetMapping("/{userId}")
    @Operation(
            summary = "사용자 조회",
            description = "특정 ID의 사용자의 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserWebResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<UserWebResponse> getUser(@PathVariable String userId) {
        // 가져온 User 객체를 UserResponse DTO로 변환합니다.
        UserCommand userCommand = new UserCommand(userId);
        UserQueryResult userQueryResult = userService.getUser(userCommand.getUserId());
        UserWebResponse response = UserWebResponse.from(userQueryResult);
        // 변환된 DTO를 OK 상태 코드와 함께 반환합니다.
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 모든 사용자 정보를 조회합니다.
     * GET /api/v1/users
     * @return 모든 사용자 정보 리스트 (UserWebResponse DTO)
     */
    @GetMapping
    @Operation(
            summary = "모든 사용자 조회",
            description = "등록된 모든 사용자의 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserWebResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<List<UserWebResponse>> getAllUsers() {
        // 1. UserService에서 List<UserQueryResult>를 가져옵니다.
        List<UserQueryResult> userQueryResults = userService.getAllUsers();

        // 2. UserQueryResult 리스트를 UserWebResponse DTO 리스트로 변환합니다.
        List<UserWebResponse> responses = userQueryResults.stream()
                .map(UserWebResponse::from) // UserWebResponse.from(UserQueryResult) 사용
                .collect(Collectors.toList());

        // 3. 변환된 DTO 리스트를 OK 상태 코드와 함께 반환합니다.
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * 사용자의 잔액을 충전합니다.
     * POST /api/v1/users/charge
     * @param request 충전 요청 정보 (userId, amount)
     * @return 업데이트된 사용자 정보
     */
    @PostMapping("/charge")
    @Operation(
            summary = "사용자 잔액 충전",
            description = "사용자의 잔액을 충전합니다. 요청 본문에서 사용자 ID와 충전 금액을 받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "충전 성공",
                    content = @Content(schema = @Schema(implementation = UserWebResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (예: 금액이 0 이하)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @ResponseStatus(HttpStatus.OK) // HTTP 200 OK 응답
    public ResponseEntity<UserWebResponse> chargeBalance(@Valid @RequestBody UserChargeWebRequest request) {
        try {
            UserQueryResult userQueryResult = userService.chargeBalance(request.getUserId(), request.getAmount());
            return new ResponseEntity<>(UserWebResponse.from(userQueryResult), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 예외 메시지를 포함하는 더 구체적인 오류 응답 고려
        }
    }
    // 잔액 사용 API는 예약/결제 플로우에 통합되므로 여기서는 생략
}