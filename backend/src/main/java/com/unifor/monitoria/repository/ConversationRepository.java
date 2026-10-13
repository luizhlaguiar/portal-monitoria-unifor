package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    Optional<Conversation> findByUserAndMonitor(com.unifor.monitoria.model.User user, com.unifor.monitoria.model.User monitor);

    List<Conversation> findByMonitor(com.unifor.monitoria.model.User monitor);

    Optional<Conversation> findByUserAndMonitorOrUserAndMonitor(
        com.unifor.monitoria.model.User user1, com.unifor.monitoria.model.User monitor1,
        com.unifor.monitoria.model.User user2, com.unifor.monitoria.model.User monitor2
    );
}
