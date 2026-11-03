package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Message;
import com.unifor.monitoria.model.User;

import java.util.List;

public interface MessageDao extends GenericDao<Message, Integer> {

    List<Message> findByConversationIdOrderByTimestampAsc(Integer conversationId);

    List<Message> findByConversationIdInOrderByTimestampAsc(List<Integer> conversationIds);

    List<Message> findByReceiverAndIsReadFalse(User receiver);
}