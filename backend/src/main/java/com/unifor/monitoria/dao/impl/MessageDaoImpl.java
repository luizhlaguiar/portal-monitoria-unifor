package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.MessageDao;
import com.unifor.monitoria.model.Message;
import com.unifor.monitoria.model.User;

import java.util.List;

@Dao
public class MessageDaoImpl extends JpaGenericDao<Message, Integer> implements MessageDao {

    @Override
    public List<Message> findByConversationIdOrderByTimestampAsc(Integer conversationId) {
        return queryList("select m from Message m where m.conversation.id = :cid order by m.timestamp asc",
                "cid", conversationId);
    }

    @Override
    public List<Message> findByConversationIdInOrderByTimestampAsc(List<Integer> conversationIds) {
        return queryList("select m from Message m where m.conversation.id in :ids order by m.timestamp asc",
                "ids", conversationIds);
    }

    @Override
    public List<Message> findByReceiverAndIsReadFalse(User receiver) {
        return queryList("select m from Message m where m.receiver = :receiver and m.isRead = false",
                "receiver", receiver);
    }
}