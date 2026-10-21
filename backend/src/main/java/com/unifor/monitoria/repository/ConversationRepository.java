package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Conversation;
import com.unifor.monitoria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    Optional<Conversation> findByUserAndMonitor(User user, User monitor);

    List<Conversation> findByMonitor(User monitor);
    List<Conversation> findByUser(User user);

    Optional<Conversation> findByUserAndMonitorOrUserAndMonitor(
        User user1, User monitor1,
        User user2, User monitor2
    );
}
