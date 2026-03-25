package stupid.hackthon.register.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stupid.hackthon.register.domain.User;
import stupid.hackthon.register.domain.UserChat;
import stupid.hackthon.register.dto.AddChatIdRequest;
import stupid.hackthon.register.dto.MessageResponse;
import stupid.hackthon.register.dto.UserChatIdsResponse;
import stupid.hackthon.register.exception.BadRequestException;
import stupid.hackthon.register.exception.NotFoundException;
import stupid.hackthon.register.repository.UserChatRepository;
import stupid.hackthon.register.repository.UserRepository;

import java.util.List;

@Service
public class UserChatService {

    private final UserChatRepository userChatRepository;
    private final UserRepository userRepository;

    public UserChatService(UserChatRepository userChatRepository, UserRepository userRepository) {
        this.userChatRepository = userChatRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageResponse addChatId(Long userId, AddChatIdRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userChatRepository.existsByUserIdAndChatId(userId, request.chatId().trim())) {
            throw new BadRequestException("Chat id already exists for this user");
        }

        UserChat userChat = new UserChat();
        userChat.setUser(user);
        userChat.setChatId(request.chatId());
        userChatRepository.save(userChat);
        return new MessageResponse("Chat id added successfully");
    }

    @Transactional(readOnly = true)
    public UserChatIdsResponse getChatIds(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<String> chatIds = userChatRepository.findAllByUserIdOrderByIdAsc(userId)
                .stream()
                .map(UserChat::getChatId)
                .toList();

        return new UserChatIdsResponse(userId, chatIds);
    }
}
