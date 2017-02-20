package org.collectiveone.services;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.collectiveone.model.Activity;
import org.collectiveone.model.ActivityType;
import org.collectiveone.model.Decision;
import org.collectiveone.model.DecisionRealm;
import org.collectiveone.model.DecisionState;
import org.collectiveone.model.DecisionType;
import org.collectiveone.model.Goal;
import org.collectiveone.model.GoalAttachState;
import org.collectiveone.model.GoalIncreaseBudgetState;
import org.collectiveone.model.GoalParentState;
import org.collectiveone.model.GoalState;
import org.collectiveone.model.Project;
import org.collectiveone.model.User;
import org.collectiveone.model.Voter;
import org.collectiveone.repositories.DecisionDao;
import org.collectiveone.repositories.DecisionRealmDao;
import org.collectiveone.repositories.GoalDao;
import org.collectiveone.repositories.ProjectDao;
import org.collectiveone.repositories.UserDao;
import org.collectiveone.web.dto.Filters;
import org.collectiveone.web.dto.GoalDto;
import org.collectiveone.web.dto.GoalDtoListRes;
import org.collectiveone.web.dto.GoalTouchDto;
import org.collectiveone.web.dto.GoalUserWeightsDto;
import org.collectiveone.web.dto.GoalWeightsDataDto;
import org.collectiveone.web.dto.ObjectListRes;
import org.collectiveone.web.dto.VoterDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class GoalServiceImp {

	@Autowired
	protected GoalDao goalDao;
	
	@Autowired
	protected ProjectDao projectDao;
	
	@Autowired
	protected UserDao userDao;
	
	@Autowired 
	protected DecisionRealmDao decisionRealmDao;
	
	@Autowired
	protected DecisionDao decisionDao;
	
	
	@Autowired 
	protected DecisionRealmServiceImp decisionRealmService;
	
	@Autowired
	protected ActivityServiceImp activityService;
	
	@Transactional
	public Long goalCreate(GoalDto goalDto, GoalState state) throws IOException {
		if(!goalExist(goalDto.getGoalTag(), goalDto.getProjectName())) {
			Goal goal = new Goal();
			Project project = projectDao.get(goalDto.getProjectName());
			projectDao.save(project);

			goal.setCreationDate(new Timestamp(System.currentTimeMillis()));
			goal.setCreator(userDao.get(goalDto.getCreatorUsername()));
			goal.setDescription(goalDto.getDescription());
			goal.setProject(project);
			goal.setState(state);
			goal.setGoalTag(goalDto.getGoalTag());
			
			/* goal start attached, all the logic needed for when 
			 * detached is set later */
			goal.setAttachedState(GoalAttachState.ATTACHED);
			goal.setIncreaseBudgetState(GoalIncreaseBudgetState.NOT_PROPOSED);
			
			Long id = goalDao.save(goal);

			/* each goal has its own decision realm */
			DecisionRealm realm = new DecisionRealm();
			realm.setGoal(goal);
			
			/* the voters and weights of the decision realm are initialized to the parent goal realm */
			boolean hasParent = false;
			if(goalDto.getParentGoalTag() != null) {
				if(goalDto.getParentGoalTag() != "") {
					Goal parent = goalDao.get(goalDto.getParentGoalTag(), project.getName());
					if(parent != null) {
						hasParent = true;
						goal.setParent(parent);
						/* decision realm should be initialized to that of the parent goal */
						decisionRealmService.decisionRealmInitToOther(realm, decisionRealmDao.getFromGoalId(parent.getId()).getId());
					}
				}
			}
			
			/* or the are initialized to 1 (weights) and all project contributors are voters */
			if(!hasParent){
				decisionRealmService.decisionRealmInitToProject(realm, project.getId());
			}
				
			decisionRealmDao.save(realm);

			Activity act = null;
			
			/* if the created goal has parents, the control is taken
			 * in the realm of the parent, otherwise it is controlled
			 * by its own realm */
			DecisionRealm goalControlRealm = null;
			
			if(hasParent) {
				goalControlRealm = decisionRealmDao.getFromGoalId(goal.getParent().getId());
			} else {
				goalControlRealm = realm;
			}
			
			switch(state) {
				case PROPOSED:
					Decision create = new Decision();
					
					create.setCreator(userDao.get("collectiveone"));
					create.setCreationDate(new Timestamp(System.currentTimeMillis()));
					create.setDescription("create goal '"+goal.getGoalTag()+"'");
					create.setState(DecisionState.IDLE);
					create.setVerdictHours(36);
					create.setDecisionRealm(goalControlRealm);
					create.setFromState(GoalState.PROPOSED.toString());
					create.setToState(GoalState.ACCEPTED.toString());
					create.setProject(project);
					if(hasParent) create.setGoal(goal.getParent());
					else create.setGoal(goal);
					create.setType(DecisionType.GOAL);
					create.setAffectedGoal(goal);

					goal.setCreateDec(create);
					
					decisionDao.save(create);
					
					act = new Activity("proposed", 
							new Timestamp(System.currentTimeMillis()),
							project);
					
					break;
					
					
				case ACCEPTED:
					/* one goal is created in accepted state at project creation */
					
		            Decision delete = new Decision();
		            
		            delete.setCreator(userDao.get("collectiveone"));
		            delete.setCreationDate(new Timestamp(System.currentTimeMillis()));
		            delete.setDescription("delete goal '"+goal.getGoalTag()+"'");
		            delete.setState(DecisionState.IDLE);
		            delete.setVerdictHours(36);
		            delete.setDecisionRealm(goalControlRealm);
		            delete.setFromState(GoalState.ACCEPTED.toString());
		            delete.setToState(GoalState.DELETED.toString());
		            delete.setProject(project);
		            if(hasParent) delete.setGoal(goal.getParent());
					else delete.setGoal(goal);
		            delete.setType(DecisionType.GOAL);
		            delete.setAffectedGoal(goal);

		            goal.setDeleteDec(delete);
 				    decisionDao.save(delete);
					
					act = new Activity("proposed", 
							new Timestamp(System.currentTimeMillis()),
							project);

					break;
				
				default:
					break;
			}
			
			act.setGoal(goal);
			act.setType(ActivityType.GOAL);
			activityService.activitySaveAndNotify(act);
			
			return id;
			
		} else {
			return (long) -1;
		}
	}
	
	@Transactional
	public Long goalCreate(GoalDto goalDto) throws IOException {
		return goalCreate(goalDto,GoalState.PROPOSED);
	}

	@Transactional
	public GoalDto goalGetDto(Long goalId) {
		Goal goal = goalDao.get(goalId);
		GoalDto dto = goal.toDto();
		goalAddParentsAndSubgoals(dto);

		return dto;
	}

	@Transactional
	public boolean goalExist(String goalTag, String projectName) {
		Goal goal = goalDao.get(goalTag, projectName);
		if(goal != null) {
			return true;
		} else {
			return false;
		}
	}
	
	@Transactional
	public boolean goalExist(String goalTag, String projectName, GoalState state) {
		Goal goal = goalDao.get(goalTag, projectName, state);
		if(goal != null) {
			return true;
		} else {
			return false;
		}
	}
	
	@Transactional
	public GoalDto goalGetDto(String goalTag, String projectName) {
		Goal goal = goalDao.get(goalTag, projectName);
		GoalDto dto = null;
		if(goal != null) {
			dto = goal.toDto();
			goalAddParentsAndSubgoals(dto);
		}

		return dto;
	}

	@Transactional
	public GoalDtoListRes goalDtoGetFiltered(Filters filters) {
		ObjectListRes<Goal> goalsRes = goalDao.get(filters);

		GoalDtoListRes goalsDtosRes = new GoalDtoListRes();

		goalsDtosRes.setResSet(goalsRes.getResSet());
		goalsDtosRes.setGoalDtos(new ArrayList<GoalDto>());

		for(Goal goal : goalsRes.getObjects()) {
			GoalDto dto = goal.toDto();
			goalAddParentsAndSubgoals(dto);

			goalsDtosRes.getGoalDtos().add(dto);
		}

		return goalsDtosRes;
	}

	@Transactional
	public void goalAddParentsAndSubgoals(GoalDto goalDto) {
		List<String> parentGoalsTags = new ArrayList<String>();
		for(Goal parent : goalDao.getAllParents(goalDto.getId())) { 
			parentGoalsTags.add(parent.getGoalTag()); 
		}
		goalDto.setParentGoalsTags(parentGoalsTags);

		List<String> subGoalsTags = new ArrayList<String>();
		
		List<GoalState> states = new ArrayList<GoalState>();
		states.add(GoalState.PROPOSED);
		states.add(GoalState.ACCEPTED);
		
		for(Goal subgoal : goalDao.getSubgoals(goalDto.getId(),states)) { 
			subGoalsTags.add(subgoal.getGoalTag()); 
		}
		goalDto.setSubGoalsTags(subGoalsTags);
	}

	@Transactional
	public List<String> goalGetSuggestions(String query, List<String> projectNames) {
		if(projectNames.size() == 0) {
			projectNames = projectDao.getListEnabled();
		}
		return goalDao.getSuggestions(query, projectNames);
	}

	@Transactional
	public void goalsUpdateState() throws IOException {
		/* Update state of all not closed bids */
		List<Goal> goalsNotDeleted = goalDao.getNotDeleted();
		for(Goal goal : goalsNotDeleted) {
			goalUpdateState(goal.getId());
		}	
	}

	@Transactional
	public void goalUpdateState(Long goalId) throws IOException {
		goalFromProposedToAccepted(goalId);
		goalFromAcceptedToDeleted(goalId);
		goalUpdateNewParent(goalId);
		goalUpdateAttachment(goalId);
		goalUpdateBudget(goalId);
	}

	@Transactional
	public void goalFromProposedToAccepted(Long goalId) throws IOException {
		Goal goal = goalDao.get(goalId);
		goalDao.save(goal);

		/* Create decision */ 
		Decision create = goal.getCreateDec();

		/* update goal state based on create decision */

		Activity act = new Activity();
		act.setCreationDate(new Timestamp(System.currentTimeMillis()));
		act.setProject(goal.getProject());
		act.setGoal(goal);
		act.setType(ActivityType.GOAL);

		if(create != null) {
			switch(create.getState()){
			case OPEN: 
				break;
	
			case CLOSED_DENIED : 
				switch(goal.getState()) {
				case PROPOSED:
					goal.setState(GoalState.NOT_ACCEPTED);
					act.setEvent("not accepted");
					activityService.activitySaveAndNotify(act);
					break;
	
				default:
					break;
				}
	
				break;
	
			case CLOSED_ACCEPTED: 
				switch(goal.getState()) {
				case PROPOSED:
					goal.setState(GoalState.ACCEPTED);
					
					/* create delete decision */
		            Decision delete = new Decision();
		            
		            DecisionRealm realm = decisionRealmDao.getFromGoalId(goal.getId());
		            
		            delete.setCreator(userDao.get("collectiveone"));
		            delete.setCreationDate(new Timestamp(System.currentTimeMillis()));
		            delete.setDescription("delete goal '"+goal.getGoalTag()+"'");
		            delete.setState(DecisionState.IDLE);
		            delete.setVerdictHours(36);
		            delete.setDecisionRealm(realm);
		            delete.setFromState(GoalState.ACCEPTED.toString());
		            delete.setToState(GoalState.DELETED.toString());
		            delete.setProject(goal.getProject());
		            delete.setGoal(goal);
		            delete.setType(DecisionType.GOAL);
		            delete.setAffectedGoal(goal);
		            
		            goal.setDeleteDec(delete);
		            decisionDao.save(delete);
					
		            /* register event */
		            act.setEvent("accepted");
		            activityService.activitySaveAndNotify(act);
					
					
					break;
					
				default:
					break;
				}
	
			default:
				break;
			}
		}
	}

	@Transactional
	public void goalFromAcceptedToDeleted(Long goalId) throws IOException {
		Goal goal = goalDao.get(goalId);
		goalDao.save(goal);

		/* Update accept decision */ 
		Decision delete = goal.getDeleteDec();

		Activity act = new Activity();
		act.setCreationDate(new Timestamp(System.currentTimeMillis()));
		act.setProject(goal.getProject());
		act.setGoal(goal);
		act.setType(ActivityType.GOAL);

		/* update goal state based on delete decision */

		if(delete != null) {
			switch(delete.getState()){
			case OPEN: 
				break;
	
			case CLOSED_DENIED : 
				switch(goal.getState()) {
				case PROPOSED:
					break;
	
				case ACCEPTED:
					break;				
	
				default:
					break;
				}
	
				break;
	
			case CLOSED_ACCEPTED: 
				switch(goal.getState()) {
				case PROPOSED:
					goal.setState(GoalState.DELETED);
					act.setEvent("deleted");
					activityService.activitySaveAndNotify(act);
					break;
	
				case ACCEPTED:
					goal.setState(GoalState.DELETED);
					act.setEvent("deleted");
					activityService.activitySaveAndNotify(act);
					break;				
	
				default:
					break;
				}
	
				break;
	
			default:
				break;
			} 
		}
	}

	@Transactional
	public void goalUpdateNewParent(Long goalId) throws IOException {
		Goal goal = goalDao.get(goalId);
		goalDao.save(goal);

		if(goal.getParentState() == GoalParentState.PROPOSED) {
			/* Create decision */ 
			Decision proposeParent = goal.getProposeParent();

			if(proposeParent != null) {
				/* update goal parent based on create proposeParent */
				Activity act = new Activity();
				act.setCreationDate(new Timestamp(System.currentTimeMillis()));
				act.setProject(goal.getProject());
				act.setGoal(goal);
				act.setType(ActivityType.GOAL);

				if(proposeParent != null) {
					switch(proposeParent.getState()){
					case OPEN: 
						break;
	
					case CLOSED_DENIED : 
						switch(goal.getParentState()) {
						case PROPOSED:
							goal.setParentState(GoalParentState.ACCEPTED);
							act.setEvent(goal.getProposedParent().getGoalTag()+" not accepted as parent");
							activityService.activitySaveAndNotify(act);
							break;
	
						default:
							break;
						}
	
						break;
	
					case CLOSED_ACCEPTED: 
						switch(goal.getParentState()) {
						case PROPOSED:
							goal.setParentState(GoalParentState.ACCEPTED);
							goal.setParent(goal.getProposedParent());
							act.setEvent(goal.getProposedParent().getGoalTag()+" accepted as parent");
							activityService.activitySaveAndNotify(act);
							break;
						default:
							break;
						}
	
					default:
						break;
					} 
				}
			}
		}
	}
	
	@Transactional
	public void goalProposeParent(Long goalId, String parentTag) throws IOException {
		Goal goal = goalDao.get(goalId);
		Goal proposedParent = goalDao.get(parentTag,goal.getProject().getName());

		if(proposedParent != null) {
			if(goal.getParentState() != GoalParentState.PROPOSED) {
				Project project = goal.getProject();
				Decision proposeParent = new Decision();

				proposeParent.setCreationDate(new Timestamp(System.currentTimeMillis()));
				proposeParent.setCreator(userDao.get("collectiveone"));
				proposeParent.setDecisionRealm(decisionRealmDao.getFromGoalId(goal.getId()));
				proposeParent.setDescription("set "+proposedParent.getGoalTag()+" as parent goal");
				proposeParent.setProject(project);
				proposeParent.setGoal(goal);
				proposeParent.setState(DecisionState.IDLE);
				proposeParent.setType(DecisionType.GOAL);
				proposeParent.setAffectedGoal(goal);
				proposeParent.setVerdictHours(36);

				decisionDao.save(proposeParent);

				goal.setProposeParent(proposeParent);
				goal.setProposedParent(proposedParent);
				goal.setParentState(GoalParentState.PROPOSED);

				goalDao.save(goal);

				Activity act = new Activity();
				act.setCreationDate(new Timestamp(System.currentTimeMillis()));
				act.setProject(project);
				act.setGoal(goal);
				act.setType(ActivityType.GOAL);
				act.setEvent(proposedParent.getGoalTag()+" proposed as parent");
				activityService.activitySaveAndNotify(act);
			} else {
			}
		} else {
		}
	}
	
	@Transactional
	public List<String> goalGetParentGoalsTags(Goal goal) {
		/* Add parent goals too */
		List<String> parentGoalsTags = new ArrayList<String>();
		if(goal!= null) {
			List<Goal> parentGoals = goalDao.getAllParents(goal.getId());
			for(Goal parent : parentGoals) { parentGoalsTags.add(parent.getGoalTag()); }
		}
		return parentGoalsTags;
	}
	
	@Transactional
	public GoalWeightsDataDto goalGetWeightsData(String projectName, String goalTag, String username) {
		
		Goal goal = goalDao.get(goalTag, projectName);
		DecisionRealm realm = decisionRealmDao.getFromGoalId(goal.getId());
		
		GoalWeightsDataDto goalWeightsDataDto = new GoalWeightsDataDto();  
		
		if(!username.equals("anonymousUser")) {
			/* if user is logged */
			User user = userDao.get(username);
			
			Voter voter = decisionRealmDao.getVoter(realm.getId(), user.getId());
			
			if(voter != null) {
				/* if logged user is voter in this realm */
				GoalUserWeightsDto userWeightsDto = new GoalUserWeightsDto();
				userWeightsDto.setUsername(username);
				userWeightsDto.setMaxWeight(voter.getMaxWeight());
				userWeightsDto.setActualWeight(voter.getActualWeight());
				
				goalWeightsDataDto.setUserWeightsDto(userWeightsDto);
			}
		}
		
		List<VoterDto> votersDtos = new ArrayList<VoterDto>();
		for(Voter otherVoter : realm.getVoters()) {
			votersDtos.add(otherVoter.toDto());
		}
		
		goalWeightsDataDto.setGoalTag(goalTag);
		goalWeightsDataDto.setProjectName(projectName);
		goalWeightsDataDto.setTotalWeight(decisionRealmDao.getWeightTot(realm.getId()));
		
		goalWeightsDataDto.setVotersDtos(votersDtos);
		
		return goalWeightsDataDto;
	}
	
	@Transactional
	public void goalTouch(GoalTouchDto touchDto) {
		Goal goal = goalDao.get(touchDto.getGoalTag(), touchDto.getProjectName());
		DecisionRealm realm = decisionRealmDao.getFromGoalId(goal.getId());
		User user = userDao.get(touchDto.getUsername());
		Voter voter = decisionRealmDao.getVoter(realm.getId(), user.getId());
		
		if(touchDto.getTouchFlag()) {
			voter.setActualWeight(voter.getMaxWeight());
		} else {
			voter.setActualWeight(0.0);
		}
		
		decisionRealmDao.save(realm);
	}
	
	@Transactional
	public void goalDetach(Long goalId, double initialBudget) throws IOException {
		Goal goal = goalDao.get(goalId);
		
		if(goal.getParent() != null) {
			
			switch(goal.getAttachedState()) {
			
			case ATTACHED:
				goal.setCurrentBudget(0.0);
				goal.setPpsToIncrease(initialBudget);
					
				Decision increaseBudget = new Decision();
				
				increaseBudget.setCreationDate(new Timestamp(System.currentTimeMillis()));
				increaseBudget.setCreator(userDao.get("collectiveone"));
				/* detach and increase budget decisions are taken in the supergoal realm */
				increaseBudget.setDecisionRealm(decisionRealmDao.getFromGoalId(goal.getParent().getId()));
				increaseBudget.setDescription("detach goal +"+goal.getGoalTag()+
						" from +"+goal.getParent().getGoalTag()+" with an initial budget of "+initialBudget+" pps");
				increaseBudget.setProject(goal.getProject());
				increaseBudget.setGoal(goal.getParent());
				increaseBudget.setState(DecisionState.IDLE);
				increaseBudget.setType(DecisionType.GOAL);
				increaseBudget.setAffectedGoal(goal);
				increaseBudget.setVerdictHours(36);
				
				goal.setIncreaseBudget(increaseBudget);
				goal.setAttachedState(GoalAttachState.PROPOSED_DETACH);
			
				goalDao.save(goal);
				decisionDao.save(increaseBudget);
				
				Activity act = new Activity();
				act.setCreationDate(new Timestamp(System.currentTimeMillis()));
				act.setProject(goal.getProject());
				act.setGoal(goal);
				act.setType(ActivityType.GOAL);
				act.setEvent("detach proposed");
				activityService.activitySaveAndNotify(act);
				
				break;
				
			case PROPOSED_DETACH:
			case DETACHED:
			case PROPOSED_REATTACH:
				/* nop */
				break;
				
			}
		}
	}
	
	@Transactional
	public void goalReattach(Long goalId) throws IOException {
		Goal goal = goalDao.get(goalId);
		
		if(goal.getParent() != null) {
			
			switch(goal.getAttachedState()) {
			
			case DETACHED:
				goal.setAttachedState(GoalAttachState.PROPOSED_REATTACH);
				
				Decision reattach = new Decision();
				
				reattach.setCreator(userDao.get("collectiveone"));
				reattach.setCreationDate(new Timestamp(System.currentTimeMillis()));
				reattach.setDescription("reattach goal +"+goal.getGoalTag()+" to "+goal.getParent().getGoalTag());
				reattach.setState(DecisionState.IDLE);
				reattach.setVerdictHours(36);
				reattach.setDecisionRealm(decisionRealmDao.getFromGoalId(goal.getId()));
				reattach.setProject(goal.getProject());
				reattach.setGoal(goal);
				reattach.setType(DecisionType.GOAL);
				reattach.setAffectedGoal(goal);

				goal.setReattach(reattach);
				
				decisionDao.save(reattach);
				goalDao.save(goal);
				
				Activity act = new Activity();
				act.setCreationDate(new Timestamp(System.currentTimeMillis()));
				act.setProject(goal.getProject());
				act.setGoal(goal);
				act.setType(ActivityType.GOAL);
				act.setEvent("reattach proposed");
				activityService.activitySaveAndNotify(act);
				
				break;
				
			case PROPOSED_DETACH:
			case ATTACHED:
			case PROPOSED_REATTACH:
				/* nop */
				break;
				
			}
		}
	}
	
	@Transactional
	public void goalIncreaseBudget(Long goalId, double increaseBudgetPps) throws IOException {
		Goal goal = goalDao.get(goalId);
		
		if(goal.getParent() != null) {
			
			switch(goal.getAttachedState()) {
			
			case DETACHED:
				/* store the pps to increase and create a decision to increase 
				 * clearly the decision realm for the increase is that of the parent
				 * goal */
				goal.setPpsToIncrease(increaseBudgetPps);
				goal.setIncreaseBudgetState(GoalIncreaseBudgetState.PROPOSED);
					
				Decision increaseBudget = new Decision();
				
				increaseBudget.setCreationDate(new Timestamp(System.currentTimeMillis()));
				increaseBudget.setCreator(userDao.get("collectiveone"));
				/* increase budget decisions are taken in the supergoal realm */
				increaseBudget.setDecisionRealm(decisionRealmDao.getFromGoalId(goal.getParent().getId()));
				increaseBudget.setDescription("increase budget of goal +"+goal.getGoalTag()+
						" from "+goal.getCurrentBudget()+" to "+(goal.getCurrentBudget()+increaseBudgetPps)+" pps");
				increaseBudget.setProject(goal.getProject());
				increaseBudget.setGoal(goal.getParent());
				increaseBudget.setState(DecisionState.IDLE);
				increaseBudget.setType(DecisionType.GOAL);
				increaseBudget.setAffectedGoal(goal);
				increaseBudget.setVerdictHours(36);
				
				goal.setIncreaseBudget(increaseBudget);
				
				goalDao.save(goal);
				decisionDao.save(increaseBudget);
				
				Activity act = new Activity();
				act.setCreationDate(new Timestamp(System.currentTimeMillis()));
				act.setProject(goal.getProject());
				act.setGoal(goal);
				act.setType(ActivityType.GOAL);
				act.setEvent("budget increase proposed");
				activityService.activitySaveAndNotify(act);
				
				break;
				
			case PROPOSED_DETACH:
			case ATTACHED:
			case PROPOSED_REATTACH:
				/* nop */
				break;
				
			}
		}
	}
	
	@Transactional
	public void goalUpdateBudget(Long goalId) throws IOException {
		Goal goal = goalDao.get(goalId);
		
		Activity act = new Activity();
		act.setCreationDate(new Timestamp(System.currentTimeMillis()));
		act.setProject(goal.getProject());
		act.setGoal(goal);
		act.setType(ActivityType.GOAL);
		
		switch(goal.getIncreaseBudgetState()) {
		case PROPOSED:
			/* check the status of the increaseBudget decision */
			Decision increaseBudget = goal.getIncreaseBudget();
			if(increaseBudget != null) {
				switch(increaseBudget.getState()) {
				case IDLE:
				case OPEN:
					/* wait for the increase budget decision to close */
					break;
					
				case CLOSED_ACCEPTED:
					/* update the budget of the detached goal from the closest 
					 * parent goal budget and check there is enough pps */
					boolean goWithIncrease = false;
					Goal parent = goalDao.getClosestDetachedParent(goalId);
					
					if(parent == null) {
						/* no parent or grandparent in detached state,
						 * create the pps out of thin air */
						goWithIncrease = true;
					} else {
						/* check parent has enough pps */
						if(parent.getCurrentBudget() >= goal.getPpsToIncrease()) {
							parent.setCurrentBudget(parent.getCurrentBudget() - goal.getPpsToIncrease());
							goWithIncrease = true;
							goalDao.save(parent);
						} else {
							/* no enough pps*/
							goWithIncrease = false;
						}
					}
					
					if(goWithIncrease) {
						/* the goal should be detached*/
						goal.setCurrentBudget(goal.getCurrentBudget() + goal.getPpsToIncrease());
						goalDao.save(goal);
						
						act.setEvent("budget increased");
						activityService.activitySaveAndNotify(act);
					}
					
					/* mark as not proposed anyway, if there was not enough budget
					 * the increase decision has to be taken again */
					goal.setIncreaseBudgetState(GoalIncreaseBudgetState.NOT_PROPOSED);
					
					break;
					
				case CLOSED_DENIED:
				case CLOSED_EXTERNALLY:
					goal.setIncreaseBudgetState(GoalIncreaseBudgetState.NOT_PROPOSED);
					
					act.setEvent("budget increase refused");
					activityService.activitySaveAndNotify(act);
					break;
				}
			}
			
			break;
			
		case NOT_PROPOSED:
			/* nop */
			break;
		}
	}
	
	@Transactional
	public void goalUpdateAttachment(Long goalId) throws IOException {
		Goal goal = goalDao.get(goalId);
		
		Activity act = new Activity();
		act.setCreationDate(new Timestamp(System.currentTimeMillis()));
		act.setProject(goal.getProject());
		act.setGoal(goal);
		act.setType(ActivityType.GOAL);
		
		switch(goal.getAttachedState()) {
		
		case ATTACHED:
		case DETACHED:
			/* nop*/
			break;
			
		case PROPOSED_DETACH:
			
			/* the increaseBudget decision is used both as a detach decision
			 * and as an increase budget decision */
			Decision increaseBudget = goal.getIncreaseBudget();
			if(increaseBudget != null) {
				switch(increaseBudget.getState()) {
				case IDLE:
				case OPEN:
					/* wait for the increase budget decision to close */
					break;
					
				case CLOSED_ACCEPTED:
					/* initialized the budget of the detached goal from the closest 
					 * parent goal budget and check there is enough pps */
					boolean goWithDetachment = false;
					Goal parent = goalDao.getClosestDetachedParent(goalId);
					
					if(parent == null) {
						/* no parent or grandparent in detached state,
						 * create the pps out of thin air */
						goWithDetachment = true;
					} else {
						/* check parent has enough pps */
						if(parent.getCurrentBudget() >= goal.getPpsToIncrease()) {
							parent.setCurrentBudget(parent.getCurrentBudget() - goal.getPpsToIncrease());
							goWithDetachment = true;
							goalDao.save(parent);
						} else {
							/* no enough pps*/
							goWithDetachment = false;
						}
					}
					
					if(goWithDetachment) {
						/* the goal should be detached*/
						goal.setCurrentBudget(goal.getPpsToIncrease());
						goal.setAttachedState(GoalAttachState.DETACHED);
						goalDao.save(goal);
						
						act.setEvent("detached");
						activityService.activitySaveAndNotify(act);
					}
					
					break;
					
				case CLOSED_DENIED:
				case CLOSED_EXTERNALLY:
					act.setEvent("detachment refused");
					activityService.activitySaveAndNotify(act);
					break;
				}
			}
			break;
				
		case PROPOSED_REATTACH:
			
			Decision reattach = goal.getReattach();
			if(reattach != null) {
				switch(reattach.getState()) {
				case IDLE:
				case OPEN:
					break;
					
				case CLOSED_ACCEPTED:
					goal.setAttachedState(GoalAttachState.ATTACHED);
					
					/* remaining budget is returned to parent or grand parent */
					Goal parent = goalDao.getClosestDetachedParent(goalId);
					if(parent != null) {
						parent.setCurrentBudget(parent.getCurrentBudget() + goal.getCurrentBudget());
						goalDao.save(parent);
					}
											
					/* budget is set to zero*/
					goal.setCurrentBudget(0.0);
					
					/* increaseBudget decision is deleted to leave room for 
					 * detachment to reoccur */
					goal.setIncreaseBudget(null);
					goal.setPpsToIncrease(0.0);
					goalDao.save(goal);
					
					/* decision realm is reset to that of the parent */
					Long parentRealmId = decisionRealmDao.getIdFromGoalId(goal.getParent().getId());
					DecisionRealm realm = decisionRealmDao.getFromGoalId(goal.getParent().getId());
					
					decisionRealmService.decisionRealmUpdateToOther(realm, parentRealmId);
					
					break;
					
				case CLOSED_DENIED:
				case CLOSED_EXTERNALLY:
					/* once the decision is closed create a new decision 
					 * to allow the goal to be reattached later */
					goal.setAttachedState(GoalAttachState.DETACHED);
					
					Decision reattachNew = new Decision();
					
					reattachNew.setCreator(userDao.get("collectiveone"));
					reattachNew.setCreationDate(new Timestamp(System.currentTimeMillis()));
					reattachNew.setDescription("reattach goal +"+goal.getGoalTag()+" to "+goal.getParent());
					reattachNew.setState(DecisionState.IDLE);
					reattachNew.setVerdictHours(36);
					reattachNew.setDecisionRealm(decisionRealmDao.getFromGoalId(goal.getId()));
					reattachNew.setFromState(GoalState.PROPOSED.toString());
					reattachNew.setToState(GoalState.ACCEPTED.toString());
					reattachNew.setProject(goal.getProject());
					reattachNew.setGoal(goal);
					reattachNew.setType(DecisionType.GOAL);
					reattachNew.setAffectedGoal(goal);

					goal.setReattach(reattachNew);
					
					decisionDao.save(reattachNew);
					goalDao.save(goal);
					
					break;
					
				}
			}
		}
	}

}
