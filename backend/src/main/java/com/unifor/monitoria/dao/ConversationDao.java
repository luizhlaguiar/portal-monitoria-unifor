package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Conversation;
import com.unifor.monitoria.model.User;

import java.util.List;
import java.util.Optional;

public interface ConversationDao extends GenericDao<Conversation, Integer> {

    Optional<Conversation> findBetweenUsers(User u1, User u2);

    List<Conversation> findBetweenUsersInDiscipline(User u1, User u2, Integer disciplineId);

    List<Conversation> findBetweenUsersWithoutDiscipline(User u1, User u2);

    List<Conversation> findAllBetweenUsers(User u1, User u2);

    List<Conversation> findByUserInDiscipline(User user, Integer disciplineId);

    List<Conversation> findByMonitor(User monitor);

    List<Conversation> findAllConversationsByUser(User user);
}