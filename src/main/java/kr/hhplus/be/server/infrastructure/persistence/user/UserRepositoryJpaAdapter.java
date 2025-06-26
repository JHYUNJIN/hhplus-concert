package kr.hhplus.be.server.infrastructure.persistence.user;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // 스프링 빈으로 등록
public class UserRepositoryJpaAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryJpaAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(String userId) {
        return userJpaRepository.findById(userId);
    }

    @Override
    public Optional<User> findByIdForUpdate(String userId) {
        return userJpaRepository.findByIdWithPessimisticLock(userId);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    @Override
    public boolean existsById(String userId) {
        return userJpaRepository.existsById(userId);
    }

    @Override
    public void deleteAll() {
        userJpaRepository.deleteAll();
    }
}