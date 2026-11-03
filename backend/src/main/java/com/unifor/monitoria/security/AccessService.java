package com.unifor.monitoria.security;

import com.unifor.monitoria.dao.MatriculaDao;
import com.unifor.monitoria.dao.MonitoringSessionDao;
import com.unifor.monitoria.model.MonitoringSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Regras de "quem pode mexer em que". Admin passa em tudo. */
@Component
public class AccessService {

    private final MatriculaDao matriculaDao;
    private final MonitoringSessionDao monitoringSessionDao;

    public AccessService(MatriculaDao matriculaDao, MonitoringSessionDao monitoringSessionDao) {
        this.matriculaDao = matriculaDao;
        this.monitoringSessionDao = monitoringSessionDao;
    }

    /** Aluno matriculado, monitor da disciplina ou admin. */
    public boolean canAccessDiscipline(AuthUser user, Integer disciplineId) {
        if (disciplineId == null) return false;
        if (user.isAdmin()) return true;
        return matriculaDao.existsByAlunoIdAndDisciplinaId(user.id(), disciplineId)
                || monitoringSessionDao.existsByMonitorAndDiscipline(user.id(), disciplineId);
    }

    /** Monitor da disciplina (ou admin). */
    public boolean isMonitorOf(AuthUser user, Integer disciplineId) {
        if (disciplineId == null) return false;
        if (user.isAdmin()) return true;
        return monitoringSessionDao.existsByMonitorAndDiscipline(user.id(), disciplineId);
    }

    /** Dono da monitoria (o monitor que a criou) ou admin. */
    public boolean ownsSession(AuthUser user, MonitoringSession session) {
        if (user.isAdmin()) return true;
        return session != null && session.getMonitor() != null
                && user.id().equals(session.getMonitor().getId());
    }

    public void requireDisciplineAccess(AuthUser user, Integer disciplineId) {
        if (!canAccessDiscipline(user, disciplineId)) throw forbidden();
    }

    public void requireMonitorOf(AuthUser user, Integer disciplineId) {
        if (!isMonitorOf(user, disciplineId)) throw forbidden();
    }

    public void requireOwnsSession(AuthUser user, MonitoringSession session) {
        if (!ownsSession(user, session)) throw forbidden();
    }

    /** O proprio usuario (ou admin). */
    public void requireSelfOrAdmin(AuthUser user, Integer userId) {
        if (!user.isAdmin() && !user.id().equals(userId)) throw forbidden();
    }

    /** Somente o proprio usuario (conteudo privado, como o chat: nem admin le o de outros). */
    public void requireSelf(AuthUser user, Integer userId) {
        if (!user.id().equals(userId)) throw forbidden();
    }

    public static ResponseStatusException forbidden() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
    }
}
