package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByConversationIdOrderByTimestampAsc(Integer conversationId);
    Page<Message> findByConversationIdOrderByTimestampDesc(Integer conversationId, Pageable pageable);
    List<Message> findByReceiverAndIsReadFalse(com.unifor.monitoria.model.User receiver);
}
