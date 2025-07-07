package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private String message;
    private boolean success;
    
    public MessageResponseDTO(String message) {
        this.message = message;
        this.success = true;
    }
    
    public static MessageResponseDTO success(String message) {
        return new MessageResponseDTO(message, true);
    }
    
    public static MessageResponseDTO error(String message) {
        return new MessageResponseDTO(message, false);
    }
}

