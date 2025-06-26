package kr.hhplus.be.server.domain.user; // 해당 기능 모듈의 repository 폴더

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    // 사용자 저장 및 업데이트
    User save(User user);

    // ID로 사용자 조회 (Optional로 감싸서 null 처리 용이)
    Optional<User> findById(String userId);

    // ID로 사용자 조회 (업데이트를 위한 락 포함)
    Optional<User> findByIdForUpdate(String userId);

    // 모든 사용자 조회
    List<User> findAll();

    // ID 존재 여부 확인
    boolean existsById(String userId);

    // 모든 사용자 삭제 (테스트 용도)
    void deleteAll();
}