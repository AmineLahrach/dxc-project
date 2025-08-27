package com.example.GestionPlanAction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileUpdateDTO extends UserProfileDTO{
    public String newPassword;
}
