package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.*;
import com.example.GestionPlanAction.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<UserResponseDTO> getAll();
    UserResponseDTO getById(Long id);
    User create(User user);
    User update(Long id, User user);
    void delete(Long id);
    void bulkDelete(List<Long> ids);
    User createWithRelations(User user, UserProfileDTO userDto);
    UserResponseDTO updateWithRelations(Long id, UserProfileDTO userDto);
    UserResponseDTO updateUserStatus(Long id, Boolean actif);
    List<UserWithProfilesDTO> getAllUsersWithProfiles();
    User findEntityById(Long id);
    String getUserFullNameById(Long id);

    UserProfileResponseDTO updateProfile(Long id, UserProfileUpdateDTO dto);
}
