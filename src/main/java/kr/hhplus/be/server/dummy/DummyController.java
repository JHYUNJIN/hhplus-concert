//package kr.hhplus.be.server.dummy;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/dummy")
//@RequiredArgsConstructor
//@Tag(name = "더미 데이터 API", description = "테스트용 더미 데이터를 생성하는 API")
//public class DummyController {
//
//    private final ApplicationEventPublisher eventPublisher;
//
//    /**
//     * 더미 데이터를 생성하는 API
//     * @return ResponseEntity<Void>
//     */
//    @PostMapping
//    @Operation(
//            summary = "더미 데이터 생성",
//            description = "테스트를 위한 더미 데이터를 생성합니다."
//    )
//    public ResponseEntity<Void> generateDummy() {
//        eventPublisher.publishEvent(new DummyDataGeneratedEvent(this));
//        return ResponseEntity.ok(null);
//    }
//}
