package kr.hhplus.be.server.dummy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DummyController {

    private final DummyDateGenerator dummyDateGenerator;

    /**
     * 더미 데이터를 생성하는 API
     * @return ResponseEntity<Void>
     */
    @PostMapping("/api/v1/dummy")
    @Operation(
            summary = "더미 데이터 생성",
            description = "테스트를 위한 더미 데이터를 생성합니다."
    )
    @Tag(name = "더미 데이터 API", description = "테스트용 더미 데이터를 생성하는 API")
    public ResponseEntity<Void> generateDummy() {
        dummyDateGenerator.generateDummyData();
        return ResponseEntity.ok(null);
    }
}
