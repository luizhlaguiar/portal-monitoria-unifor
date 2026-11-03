package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.ConversationDao;
import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.model.Conversation;
import com.unifor.monitoria.model.User;

import java.util.List;
import java.util.Optional;

@Dao
public class ConversationDaoImpl extends JpaGenericDao<Conversation, Integer> implements ConversationDao {

    private static final String BETWEEN =
            "select c from Conversation c where ((c.user = :u1 and c.monitor = :u2) or (c.user = :u2 and c.monitor = :u1))";

    private static final String OF_USER =
            "select c from Conversation c where (c.user = :user or c.monitor = :user)";

    @Override
    public Optional<Conversation> findBetweenUsers(User u1, User u2) {
        return queryFirst(BETWEEN + " order by c.id", "u1", u1, "u2", u2);
    }

    @Override
    public List<Conversation> findBetweenUsersInDiscipline(User u1, User u2, Integer disciplineId) {
        return queryList(BETWEEN + " and c.discipline.id = :did order by c.id", "u1", u1, "u2", u2, "did", disciplineId);
    }

    @Override
    public List<Conversation> findBetweenUsersWithoutDiscipline(User u1, User u2) {
        return queryList(BETWEEN + " and c.discipline is null order by c.id", "u1", u1, "u2", u2);
    }

    @Override
    public List<Conversation> findAllBetweenUsers(User u1, User u2) {
        return queryList(BETWEEN + " order by c.id", "u1", u1, "u2", u2);
    }

    @Override
    public List<Conversation> findByUserInDiscipline(User user, Integer disciplineId) {
        return queryList(OF_USER + " and c.discipline.id = :did", "user", user, "did", disciplineId);
    }

    @Override
    public List<Conversation> findByMonitor(User monitor) {
        return queryList("select c from Conversation c where c.monitor = :monitor", "monitor", monitor);
    }

    @Override
    public List<Conversation> findAllConversationsByUser(User user) {
        return queryList(OF_USER, "user", user);
    }
}