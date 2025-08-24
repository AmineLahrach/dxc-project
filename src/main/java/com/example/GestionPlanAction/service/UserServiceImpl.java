package com.example.GestionPlanAction.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.GestionPlanAction.dto.*;
import com.example.GestionPlanAction.security.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.ServiceLine;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.ProfilRepository;
import com.example.GestionPlanAction.repository.ServiceLineRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.security.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private ServiceLineRepository serviceLineRepository;

	@Autowired
	private ProfilRepository profilRepository;

	@Autowired
	private AuditService auditService; // Inject AuditService

	@Autowired
	private EmailService emailService;

	@Autowired
	private JwtUtils jwtUtils;

	@Override
	public List<UserResponseDTO> getAll() {
		return repository.findAll() // ← use fetch-join
				.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
	}

	@Override
	public UserResponseDTO getById(Long id) {
		User u = repository.findByIdWithProfiles(id) // ← already fetches profils+serviceLine
				.orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + id));
		
		UserResponseDTO dto = convertToResponseDTO(u);
		
		// Add audit logs for this user
		List<Audit> audits = auditService.getAuditsForEntity("User", id);
		dto.setAuditLogs(auditService.formatAuditsForDisplay(audits));
		
		return dto;
	}

	@Override
	public User create(User user) {
		return repository.save(user);
	}

	@Override
	public User update(Long id, User updated) {
		User existing = findEntityById(id);
		existing.setNom(updated.getNom());
		existing.setPrenom(updated.getPrenom());
		existing.setEmail(updated.getEmail());
		existing.setUsername(updated.getUsername());
		existing.setMotDePasse(updated.getMotDePasse());
		existing.setProfils(updated.getProfils());
		existing.setServiceLine(updated.getServiceLine());
		return repository.save(existing);
	}

	@Override
	public void delete(Long id) {
		repository.deleteById(id);
	}

	@Override
	public User createWithRelations(User user, UserProfileDTO userProfileDTO) {
		if (userProfileDTO.getServiceLine() != null) {
			user.setServiceLine(serviceLineRepository.findById(userProfileDTO.getServiceLine()).orElse(null));
		}
		if (userProfileDTO.getRoles() != null && !userProfileDTO.getRoles().isEmpty()) {
			user.setProfils(new HashSet<>(profilRepository.findAllById(userProfileDTO.getRoles())));
		}

		User savedUser = repository.save(user);

		// Log user creation
		Long currentUserId = SecurityUtils.getCurrentUserId();
		User currentUser = repository.findById(currentUserId).orElse(null);
		if (currentUser != null) {
			StringBuilder details = new StringBuilder("Created user: ")
					.append(user.getNom())
					.append(" ")
					.append(user.getPrenom());

			if (user.getServiceLine() != null) {
				details.append("; Service Line: ").append(user.getServiceLine().getNom());
			}

			if (!user.getProfils().isEmpty()) {
				details.append("; Profiles: ")
						.append(user.getProfils().stream()
								.map(Profil::getNom)
								.collect(Collectors.joining(", ")));
			}

			auditService.logAction("user_created", currentUser, details.toString(), "User", savedUser.getId());
			emailService.sendUserRegistrationEmail(user.getEmail(), user.getUsername(), userProfileDTO.getMotDePasse());
		}

		return savedUser;
	}

	@Override
	@Transactional
	public UserResponseDTO updateWithRelations(Long id, UserProfileDTO user) {
	    User existing = findEntityById(id);
	    
	    // Store original values for audit logging
	    String oldName = existing.getNom();
	    String oldPrenom = existing.getPrenom();
	    String oldEmail = existing.getEmail();
	    String oldUsername = existing.getUsername();
	    Boolean oldActif = existing.getActif();
	    Set<Long> oldProfilIds = existing.getProfils().stream()
	            .map(Profil::getId)
	            .collect(Collectors.toSet());
	    Long oldServiceLineId = existing.getServiceLine() != null ? existing.getServiceLine().getId() : null;

	    // Update basic fields
	    existing.setNom(user.getNom());
	    existing.setPrenom(user.getPrenom());
	    existing.setEmail(user.getEmail());
	    existing.setUsername(user.getUsername());
	    existing.setMotDePasse(user.getMotDePasse());
	    existing.setActif(user.getActif());

	    // ✅ SAFE: Update profils using managed entities
	    Set<Profil> managedProfils = new HashSet<>();
	    if (user.getRoles() != null && !user.getRoles().isEmpty()) {
	        // First, get managed Profil entities from database
	        for (Long profilId : user.getRoles()) {
	            Profil managedProfil = profilRepository.findById(profilId)
	                    .orElseThrow(() -> new RuntimeException("Profil introuvable avec l'ID: " + profilId));
	            managedProfils.add(managedProfil);
	        }

	        // Clear existing profils safely
	        existing.getProfils().clear();

	        // Add managed profils
	        existing.getProfils().addAll(managedProfils);
	    }

	    // ✅ SAFE: Update service line using managed entity
	    ServiceLine managedServiceLine = null;
	    if (user.getServiceLine() != null) {
	        managedServiceLine = serviceLineRepository.findById(user.getServiceLine()).orElseThrow(
	                () -> new RuntimeException("Ligne de service introuvable avec l'ID: " + user.getServiceLine()));
	        existing.setServiceLine(managedServiceLine);
	    }

	    User savedUser = repository.save(existing);
	    
	    // Log user update
	    Long currentUserId = SecurityUtils.getCurrentUserId();
	    User currentUser = repository.findById(currentUserId).orElse(null);
	    if (currentUser != null) {
	        StringBuilder details = new StringBuilder("Updated user: ")
	            .append(savedUser.getNom())
	            .append(" ")
	            .append(savedUser.getPrenom());
	        
	        if (!oldName.equals(savedUser.getNom()) || !oldPrenom.equals(savedUser.getPrenom())) {
	            details.append("; Name changed from: ")
	                .append(oldName)
	                .append(" ")
	                .append(oldPrenom);
	        }
	        
	        if (!oldEmail.equals(savedUser.getEmail())) {
	            details.append("; Email changed");
	        }
	        
	        if (!oldUsername.equals(savedUser.getUsername())) {
	            details.append("; Username changed");
	        }
	        
	        if (oldActif != savedUser.getActif()) {
	            details.append("; Status changed from: ")
	                .append(oldActif ? "Active" : "Inactive")
	                .append(" to ")
	                .append(savedUser.getActif() ? "Active" : "Inactive");
	        }
	        
	        // Check for profile changes
	        Set<Long> newProfilIds = savedUser.getProfils().stream()
	                .map(Profil::getId)
	                .collect(Collectors.toSet());
	        if (!oldProfilIds.equals(newProfilIds)) {
	            details.append("; Profiles updated");
	        }
	        
	        // Check for service line changes
	        Long newServiceLineId = savedUser.getServiceLine() != null ? savedUser.getServiceLine().getId() : null;
	        if ((oldServiceLineId != null && !oldServiceLineId.equals(newServiceLineId)) ||
	            (oldServiceLineId == null && newServiceLineId != null)) {
	            details.append("; Service Line updated");
	        }
	        
	        auditService.logAction("user_updated", currentUser, details.toString(), "User", savedUser.getId());
	    }
	    
	    return convertToResponseDTO(savedUser);
	}

	@Override
	public User findEntityById(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + id));
	}

	public List<UserWithProfilesDTO> getAllUsersWithProfiles() {
		List<UserWithProfilesDTO> flatList = repository.findAllUserWithProfilesAsDTO();

		Map<Long, UserWithProfilesDTO> userMap = new HashMap<>();

		for (UserWithProfilesDTO flat : flatList) {
			UserWithProfilesDTO dto = userMap.get(flat.getId());

			// Build ProfilDTO from profile columns (not from getProfils)
			ProfilDTO profilDTO = new ProfilDTO(flat.getProfilId(), flat.getProfilNom());

			if (dto == null) {
				dto = new UserWithProfilesDTO(
					flat.getId(),
					flat.getNom(),
					flat.getActif(),
					flat.getEmail(),
					flat.getPrenom(),
					flat.getUsername(),
					flat.getServiceLineId(),
					flat.getServiceLineName(),
					flat.getProfilId(),
					flat.getProfilNom()
				);
				userMap.put(flat.getId(), dto);
			}
			// Add profile if profilId is not null
			if (flat.getProfilId() != null) {
				dto.getProfils().add(profilDTO);
			}
		}

		return new ArrayList<>(userMap.values());
	}

	public UserResponseDTO updateUserStatus(Long id, Boolean actif) {
		User user = findEntityById(id);
		Boolean oldStatus = user.getActif();
		user.setActif(actif);
		User savedUser = repository.save(user);
		
		// Log user status change
		Long currentUserId = SecurityUtils.getCurrentUserId();
		User currentUser = repository.findById(currentUserId).orElse(null);
		if (currentUser != null) {
			String details = String.format("Changed user status for %s %s from %s to %s",
				user.getNom(),
				user.getPrenom(),
				oldStatus ? "Active" : "Inactive",
				actif ? "Active" : "Inactive");
			
			auditService.logAction("user_status_changed", currentUser, details, "User", savedUser.getId());
		}
		
		return convertToResponseDTO(savedUser);
	}

	public void bulkDelete(List<Long> ids) {
		for (Long id : ids) {
			delete(id);
		}
	}

	private UserResponseDTO convertToResponseDTO(User user) {
		var dto = new UserResponseDTO();
		dto.setId(user.getId());
		dto.setNom(user.getNom());
		dto.setPrenom(user.getPrenom());
		dto.setEmail(user.getEmail());
		dto.setUsername(user.getUsername());
		dto.setActif(user.getActif());

		Set<ProfilDTO> profilDTOs = user.getProfils().stream()
				.map(profil -> new ProfilDTO(profil.getId(), profil.getNom())).collect(Collectors.toSet());
		dto.setRoles(profilDTOs);

		// Convert service line
		if (user.getServiceLine() != null) {
			dto.setServiceLine(new ServiceLineDTO(user.getServiceLine().getId(), user.getServiceLine().getNom()));
		}

		return dto;
	}

	public String getUserFullNameById(Long id) {
	    User user = findEntityById(id);
	    if (user != null) {
	        String nom = user.getNom() != null ? user.getNom() : "";
	        String prenom = user.getPrenom() != null ? user.getPrenom() : "";
	        return (nom + " " + prenom).trim();
	    }
	    return "unassigned";
	}

	@Override
	public UserProfileResponseDTO updateProfile(Long id, UserProfileUpdateDTO dto) {
		User existing = findEntityById(id);

		if (!StringUtils.isEmpty(dto.getUsername())){
			existing.setUsername(dto.getUsername());
		}

		if (!StringUtils.isEmpty(dto.getEmail())){
			existing.setEmail(dto.getEmail());
		}

		if (!StringUtils.isEmpty(dto.getMotDePasse()) && !StringUtils.isEmpty(dto.getNewPassword())){
			if (new BCryptPasswordEncoder().matches(dto.getMotDePasse(), existing.getMotDePasse())){
				existing.setMotDePasse(new BCryptPasswordEncoder().encode(dto.newPassword));
			} else {
				throw new RuntimeException("Existing password doesn't match!");
			}
		}

		repository.save(existing);
		String token = jwtUtils.generateTokenFromUsernameWithRoles(existing.getUsername(),
				existing.getProfils().stream()
						.map(p -> new SimpleGrantedAuthority(p.getNom())).toList());
		UserResponseDTO responseDTO = convertToResponseDTO(existing);
		return new UserProfileResponseDTO(responseDTO, token);
	}
}
