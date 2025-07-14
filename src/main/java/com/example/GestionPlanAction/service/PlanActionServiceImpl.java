package com.example.GestionPlanAction.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.GestionPlanAction.dto.ExerciseDTO;
import com.example.GestionPlanAction.dto.PlanActionDTO;
import com.example.GestionPlanAction.dto.ResponsableDTO;
import com.example.GestionPlanAction.dto.VariableActionDTO;
import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.repository.PlanActionRepository;

@Service
public class PlanActionServiceImpl implements PlanActionService {

	@Autowired
	private PlanActionRepository repository;

	@Autowired
	private VariableActionService variableActionServicerepo;

	@Override
	public List<PlanActionDTO> getAll() {

		List<Object[]> plans = repository.findAllplans();


		List<PlanActionDTO> pdto = new ArrayList<>();
		
	
	
		for (Object[] plan : plans) {
			List<VariableActionDTO> variableActions = new ArrayList<>();
			
			PlanActionDTO planActionDTO = new PlanActionDTO();
			ResponsableDTO responsableDTO = new ResponsableDTO();
			VariableActionDTO variableAction = new VariableActionDTO();
			ExerciseDTO exerciceDTO = new ExerciseDTO();
			if(plan[7] == null) {
			// Skip if the first element is null
			}else {
			variableAction.setDescription((String) plan[12]);
			variableAction.setVaMere((String) plan[11]);
			variableAction.setId((Long) plan[8]);
			variableAction.setPoids((float) plan[7]);
			variableAction.setFige((Boolean) plan[5]);
			
			variableAction.setResonableid(responsableDTO);
			//variableAction.setNiveau((int) plan[6]);
			responsableDTO.setId((Long) plan[9]);

			}

			planActionDTO.setId((Long) plan[1]);
			planActionDTO.setDescription((String) plan[2]);
			planActionDTO.setTitre((String) plan[3]);
			planActionDTO.setStatut((String) plan[4]);
			exerciceDTO.setAnnee((Integer) plan[13]);
			exerciceDTO.setVerrouille((Boolean) plan[14]);
			exerciceDTO.setId((Long) plan[15]);
			planActionDTO.setExercice(exerciceDTO);
		//	exerciceDTO.setAnnee((Integer) plan[5]);
		//	exerciceDTO.setVerrouille((Integer) plan[6]);
			//exerciceDTO.setId((Long) plan[7]);
			
			variableActions.add(variableAction);
			planActionDTO.setVariableActions(variableActions);
		
		
			
			pdto.add(planActionDTO);
			
			System.out.println("PlanAction récupéré: " + planActionDTO.toString());
		}

		
	
		return pdto;
	}

	@Override
	public PlanAction getById(Long id) {
		return repository.findById(id).orElseThrow(() -> new RuntimeException("PlanAction non trouvé"));
	}

	@Override
	public PlanAction create(PlanAction planAction) {
		if (planAction.getStatut() == null) {
			planAction.setStatut(StatutPlanAction.EN_COURS_PLANIFICATION);
		}
		System.out.println("Création d'un nouveau PlanAction: " + planAction.toString());
		PlanAction getplan = new PlanAction();
		// getplanaction(planAction.getTitre(), planAction.getDescription(),
		// planAction.getStatut(), planAction.getExercice());
		// getplanaction.setVariableActions(new ArrayList<VariableAction>());
		getplan.setDescription(planAction.getDescription());
		getplan.setTitre(planAction.getTitre());
		getplan.setStatut(planAction.getStatut());
		getplan.setExercice(planAction.getExercice());
		PlanAction pp = repository.save(getplan);
		System.out.println("PlanAction créé avec succès: " + pp.toString());
		List<VariableAction> variableActions = new ArrayList<>();
		variableActions.addAll(planAction.getVariableActions());

		if (variableActions.isEmpty()) {
			variableActions.add(new VariableAction());
		}
		else {
			System.out.println("Nombre de VariableAction associées: " + variableActions.toString());

			List<VariableAction> variableActionsToSave = new ArrayList<>();

			for (VariableAction va : variableActions) {
				PlanAction planActionEntity = new PlanAction();
				planActionEntity.setId(pp.getId());

				va.setPlanAction(planActionEntity);

				variableActionsToSave.add(va);
			}

			variableActionServicerepo.save(variableActionsToSave);
			System.out.println("VariableActions associées au PlanAction: " + variableActionsToSave.toString());
			return getplan;
		}
		return getplan;

		
	}

	@Override
	public PlanAction update(Long id, PlanAction updated) {
		PlanAction existing = getById(id);
		existing.setTitre(updated.getTitre());
		existing.setDescription(updated.getDescription());
		existing.setStatut(updated.getStatut());
		existing.setExercice(updated.getExercice());
		return repository.save(existing);
	}

	public PlanAction updateStatus(Long id, String status) {
		PlanAction plan = getById(id);
		plan.setStatut(StatutPlanAction.valueOf(status));
		return repository.save(plan);
	}

	@Override
	public void delete(Long id) {
		repository.deleteById(id);
	}
}
