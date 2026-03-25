package stupid.hackthon.register.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stupid.hackthon.register.domain.UserChat;

import java.util.List;

public interface UserChatRepository extends JpaRepository<UserChat, Long> {

    boolean existsByUserIdAndChatId(Long userId, String chatId);

    List<UserChat> findAllByUserIdOrderByIdAsc(Long userId);
}
