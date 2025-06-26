package kr.hhplus.be.server.infrastructure.web.user;

import kr.hhplus.be.server.application.user.UserService;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.web.user.dto.UserChargeWebRequest;
import kr.hhplus.be.server.infrastructure.web.user.dto.UserWebResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 새로운 사용자를 등록합니다.
     * 초기 금액은 0으로 설정됩니다.
     * POST /api/users
     * @return 생성된 사용자 정보
     */
    @PostMapping
    public ResponseEntity<UserWebResponse> registerUser() {
        // 초기 금액 0으로 사용자 등록
        User newUser = userService.registerUser(BigDecimal.ZERO);
        return new ResponseEntity<>(UserWebResponse.from(newUser), HttpStatus.CREATED);
    }

    /**
     * 특정 ID의 사용자 정보를 조회합니다.
     * GET /api/users/{userId}
     * @param userId 조회할 사용자의 ID
     * @return 조회된 사용자 정보 (UserResponse DTO)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserWebResponse> getUser(@PathVariable String userId) {
        // 서비스 계층에서 User 객체를 직접 가져옵니다.
        // User를 찾지 못하면 UserService에서 UserException(NOT_FOUND)을 던지고,
        // GlobalExceptionHandler가 이를 처리하여 404 NOT_FOUND 응답을 반환할 것입니다.
        User user = userService.getUser(userId);
        // 가져온 User 객체를 UserResponse DTO로 변환합니다.
        UserWebResponse response = UserWebResponse.from(user);
        // 변환된 DTO를 OK 상태 코드와 함께 반환합니다.
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 모든 사용자 정보를 조회합니다.
     * GET /api/users
     * @return 모든 사용자 정보 리스트
     */
    @GetMapping
    public ResponseEntity<List<UserWebResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserWebResponse> responses = users.stream()
                .map(UserWebResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * 사용자의 잔액을 충전합니다.
     * POST /api/users/charge
     * @param request 충전 요청 정보 (userId, amount)
     * @return 업데이트된 사용자 정보
     */
    @PostMapping("/charge")
    public ResponseEntity<UserWebResponse> chargeBalance(@Valid @RequestBody UserChargeWebRequest request) {
        try {
            User updatedUser = userService.chargeBalance(request.getUserId(), request.getAmount());
            return new ResponseEntity<>(UserWebResponse.from(updatedUser), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 예외 메시지를 포함하는 더 구체적인 오류 응답 고려
        }
    }
    // 잔액 사용 API는 예약/결제 플로우에 통합되므로 여기서는 생략
}