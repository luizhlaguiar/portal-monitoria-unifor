package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Conversation;
import com.unifor.monitoria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    @Query("SELECT c FROM Conversation c WHERE (c.user = :u1 AND c.monitor = :u2) OR (c.user = :u2 AND c.monitor = :u1)")
    Optional<Conversation> findBetweenUsers(@Param("u1") User u1, @Param("u2") User u2);

    List<Conversation> findByMonitor(User monitor);
}
 