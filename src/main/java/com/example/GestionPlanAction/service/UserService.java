package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.UserResponseDTO;
import com.example.GestionPlanAction.dto.UserWithProfilesDTO;
import com.example.GestionPlanAction.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<UserResponseDTO> getAll();
    UserResponseDTO getById(Long id);
    User create(User user);
    User update(Long id, User user);
    void delete(Long id);

    User createWithRelations(User user, Long serviceLineId, Set<Long> profileIds);
    User updateWithRelations(Long id, User user, Long serviceLineId, Set<Long> profileIds);

    List<UserWithProfilesDTO> getAllUsersWithProfiles();
    User findEntityById(Long id);
}
