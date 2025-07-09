package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<User> getAll();
    User getById(Long id);
    User create(User user);
    User update(Long id, User user);
    void delete(Long id);

    User createWithRelations(User user, Long serviceLineId, Set<Long> profileIds);
    User updateWithRelations(Long id, User user, Long serviceLineId, Set<Long> profileIds);
}
