package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO extends UserResponseDTO {

    public UserProfileResponseDTO(UserResponseDTO base, String refreshToken) {
        super(base.getId(), base.getNom(), base.getPrenom(), base.getEmail(),
                base.getUsername(), base.getActif(), base.getServiceLineName(), base.getServiceLineId(),
                base.getRoles(), base.getServiceLine(), base.getAuditLogs());
        this.refreshToken = refreshToken;
    }

    private String refreshToken;
}