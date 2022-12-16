package com.advise_clothes.backend.service.interfaces;

import com.advise_clothes.backend.domain.Session;

import java.util.Optional;

public interface SessionServiceInterface {
    public Optional<Session> findBySessionKey(Session session);
    public boolean isExist(Session session);
    public Session create(Session session);
    public Session delete(Session session);
}
