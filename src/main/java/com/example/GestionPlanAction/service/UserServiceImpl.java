package com.example.GestionPlanAction.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.GestionPlanAction.dto.ProfilDTO;
import com.example.GestionPlanAction.dto.ServiceLineDTO;
import com.example.GestionPlanAction.dto.UserProfileDTO;
import com.example.GestionPlanAction.dto.UserResponseDTO;
import com.example.GestionPlanAction.dto.UserWithProfilesDTO;
import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.ServiceLine;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.ProfilRepository;
import com.example.GestionPlanAction.repository.ServiceLineRepository;
import com.example.GestionPlanAction.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private ServiceLineRepository serviceLineRepository;

	@Autowired
	private ProfilRepository profilRepository;

	@Override
	public List<UserResponseDTO> getAll() {
		return repository.findAll() // ← use fetch-join
				.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
	}

	@Override
	public UserResponseDTO getById(Long id) {
		User u = repository.findByIdWithProfiles(id) // ← already fetches profils+serviceLine
				.orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + id));
		return convertToResponseDTO(u);
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
	public User createWithRelations(User user, Long serviceLineId, Set<Long> profileIds) {
		if (serviceLineId != null) {
			user.setServiceLine(serviceLineRepository.findById(serviceLineId).orElse(null));
		}
		if (profileIds != null && !profileIds.isEmpty()) {
			user.setProfils(new HashSet<>(profilRepository.findAllById(profileIds)));
		}
		return repository.save(user);
	}

	@Override
	@Transactional
	public UserResponseDTO updateWithRelations(Long id, UserProfileDTO user) {
		User existing = findEntityById(id);

		// Update basic fields
		existing.setNom(user.getNom());
		existing.setPrenom(user.getPrenom());
		existing.setEmail(user.getEmail());
		existing.setUsername(user.getUsername());
		existing.setMotDePasse(user.getMotDePasse());
		existing.setActif(user.getActif());

		// ✅ SAFE: Update profils using managed entities
		if (user.getRoles() != null && !user.getRoles().isEmpty()) {
			// First, get managed Profil entities from database
			Set<Profil> managedProfils = new HashSet<>();
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
		if (user.getServiceLine() != null) {
			ServiceLine managedServiceLine = serviceLineRepository.findById(user.getServiceLine()).orElseThrow(
					() -> new RuntimeException("Ligne de service introuvable avec l'ID: " + user.getServiceLine()));
			existing.setServiceLine(managedServiceLine);
		}

		User savedUser = repository.save(existing);
		return convertToResponseDTO(savedUser);
	}

	@Override
	public User findEntityById(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + id));
	}

	public List<UserWithProfilesDTO> getAllUsersWithProfiles() {

		List<Object[]> flatLists = new ArrayList<>();
		flatLists = repository.findAllUserWithProfilesAsObjectArray();

		List<UserWithProfilesDTO> userWithProfilesDTOs = new ArrayList<>();

		for (Object[] obj : flatLists) {

			UserWithProfilesDTO userWithProfilesDTO = new UserWithProfilesDTO();
			Set<ProfilDTO> profils = new HashSet<>();

			int index = IntStream.range(0, userWithProfilesDTOs.size())
					.filter(i -> userWithProfilesDTOs.get(i).getUsername().equals((String) obj[6])).findFirst()
					.orElse(-1);
			if (index != -1) {

				profils.add(new ProfilDTO((Long) obj[9], (String) obj[0]));

				userWithProfilesDTO = userWithProfilesDTOs.get(index);

				userWithProfilesDTO.getProfils().addAll(profils);
			} else {

				// Create a new DTO
				userWithProfilesDTO.setNom((String) obj[0]);
				userWithProfilesDTO.setActif((Boolean) obj[1]);
				userWithProfilesDTO.setEmail((String) obj[3]);
				userWithProfilesDTO.setId((Long) obj[2]);
				userWithProfilesDTO.setServiceLineId((String) obj[8]);
				userWithProfilesDTO.setPrenom((String) obj[5]);
				userWithProfilesDTO.setUsername((String) obj[6]);
				userWithProfilesDTO.setProfils(profils);
				userWithProfilesDTOs.add(userWithProfilesDTO);

				System.out.println("UserWithProfilesDTO nom: " + userWithProfilesDTO.toString());

			}

		}

		return userWithProfilesDTOs;
	}

	public UserResponseDTO updateUserStatus(Long id, Boolean actif) {
		User user = findEntityById(id);
		user.setActif(actif);
		user = repository.save(user);
		return convertToResponseDTO(user);
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
}
