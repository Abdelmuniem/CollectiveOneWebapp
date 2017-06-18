package org.collectiveone.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.collectiveone.model.basic.AppUser;
import org.collectiveone.model.basic.Assignation;
import org.collectiveone.model.basic.Initiative;
import org.collectiveone.model.basic.TokenType;
import org.collectiveone.model.enums.AssignationState;
import org.collectiveone.model.enums.AssignationType;
import org.collectiveone.model.enums.DecisionMakerRole;
import org.collectiveone.model.enums.DecisionVerdict;
import org.collectiveone.model.enums.EvaluationGradeState;
import org.collectiveone.model.enums.EvaluationGradeType;
import org.collectiveone.model.enums.EvaluatorState;
import org.collectiveone.model.enums.InitiativeRelationshipType;
import org.collectiveone.model.enums.ReceiverState;
import org.collectiveone.model.enums.TokenHolderType;
import org.collectiveone.model.support.Bill;
import org.collectiveone.model.support.EvaluationGrade;
import org.collectiveone.model.support.Evaluator;
import org.collectiveone.model.support.InitiativeRelationship;
import org.collectiveone.model.support.InitiativeTransfer;
import org.collectiveone.model.support.Member;
import org.collectiveone.model.support.MemberTransfer;
import org.collectiveone.model.support.Receiver;
import org.collectiveone.repositories.AppUserRepositoryIf;
import org.collectiveone.repositories.AssignationRepositoryIf;
import org.collectiveone.repositories.BillRepositoryIf;
import org.collectiveone.repositories.EvaluationGradeRepositoryIf;
import org.collectiveone.repositories.EvaluatorRepositoryIf;
import org.collectiveone.repositories.InitiativeRelationshipRepositoryIf;
import org.collectiveone.repositories.InitiativeRepositoryIf;
import org.collectiveone.repositories.InitiativeTransferRepositoryIf;
import org.collectiveone.repositories.MemberRepositoryIf;
import org.collectiveone.repositories.MemberTransferRepositoryIf;
import org.collectiveone.repositories.ReceiverRepositoryIf;
import org.collectiveone.web.dto.AssetsDto;
import org.collectiveone.web.dto.AssignationDto;
import org.collectiveone.web.dto.BillDto;
import org.collectiveone.web.dto.EvaluationDto;
import org.collectiveone.web.dto.EvaluationGradeDto;
import org.collectiveone.web.dto.EvaluatorDto;
import org.collectiveone.web.dto.GetResult;
import org.collectiveone.web.dto.InitiativeDto;
import org.collectiveone.web.dto.MemberDto;
import org.collectiveone.web.dto.NewInitiativeDto;
import org.collectiveone.web.dto.PostResult;
import org.collectiveone.web.dto.ReceiverDto;
import org.collectiveone.web.dto.TransferDto;
import org.collectiveone.web.evaluationlogic.PeerReviewedAssignation;
import org.collectiveone.web.evaluationlogic.PeerReviewedAssignationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InitiativeService {
	
	private long DAYS_TO_MS = 24L*60L*60L*1000L;
	
	@Autowired
	AppUserService appUserService;
	
	@Autowired
	TokenService tokenService;
	
	@Autowired
	DecisionService decisionService;
	
	
	@Autowired
	AppUserRepositoryIf appUserRepository;
	
	@Autowired
	InitiativeRepositoryIf initiativeRepository;
	
	@Autowired
	InitiativeRelationshipRepositoryIf initiativeRelationshipRepository;
	
	@Autowired
	InitiativeTransferRepositoryIf initiativeTransferRepository;
	
	@Autowired
	MemberTransferRepositoryIf memberTransferRepository;
	
	@Autowired
	AssignationRepositoryIf assignationRepository;
	
	@Autowired
	BillRepositoryIf billRepository;
	
	@Autowired
	ReceiverRepositoryIf receiverRepository;
	
	@Autowired
	EvaluatorRepositoryIf evaluatorRepository;
	
	@Autowired
	EvaluationGradeRepositoryIf evaluationGradeRepository;
	
	@Autowired
	MemberRepositoryIf memberRepository;
	
	@Transactional
	public PostResult init(UUID c1Id, NewInitiativeDto initiativeDto) {
		
		AppUser creator = appUserRepository.findByC1Id(c1Id);
		
		if (creator == null) {
			return new PostResult("error", "creator not found",  "");
		}
			
		/* Authorization is needed if it is a subinitiative */
		DecisionVerdict canCreate = null;
		Initiative parent = null; 
		
		if (!initiativeDto.getAsSubinitiative()) {
			canCreate = DecisionVerdict.APPROVED;
		} else {
			parent = initiativeRepository.findById(UUID.fromString(initiativeDto.getParentInitiativeId()));
			canCreate = decisionService.createSubInitiative(parent.getId(), creator.getC1Id());
		}
		
		if (canCreate == DecisionVerdict.DENIED) {
			return new PostResult("error", "creation not aproved",  "");
		}
		
		Initiative initiative = new Initiative();
		
		/* Basic properties*/
		initiative.setName(initiativeDto.getName());
		initiative.setDriver(initiativeDto.getDriver());
		initiative.setCreator(creator);
		initiative.setCreationDate(new Timestamp(System.currentTimeMillis()));
		initiative.setEnabled(true);
		
		initiative = initiativeRepository.save(initiative);
		
		/* List of Members */
		for (MemberDto memberDto : initiativeDto.getMembers()) {
			AppUser memberUser = appUserRepository.findByC1Id(UUID.fromString(memberDto.getUser().getC1Id()));
			
			Member member = new Member();
			member.setInitiative(initiative);
			member.setUser(memberUser);
			member = memberRepository.save(member);
			initiative.getMembers().add(member);
			
			if (memberDto.getRole().equals(DecisionMakerRole.ADMIN.toString())) {
				decisionService.addDecisionMaker(initiative.getId(), memberUser.getC1Id(), DecisionMakerRole.ADMIN);
			}
		}
		
		
		if (!initiativeDto.getAsSubinitiative()) {
			/* if is not a sub-initiative, then create a token for this initiative */
			TokenType token = tokenService.create(initiativeDto.getOwnTokens().getAssetName(), "T");
			initiative.setTokenType(token);
			initiative = initiativeRepository.save(initiative);
			tokenService.mintToHolder(token.getId(), initiative.getId(), initiativeDto.getOwnTokens().getOwnedByThisHolder(), TokenHolderType.INITIATIVE);
			
			return new PostResult("success", "initiative created and tokens created", initiative.getId().toString());
			
		} else {
			/* if it is a sub-initiative, then link to parent initiative */
			InitiativeRelationship relationship = new InitiativeRelationship();
			relationship.setInitiative(initiative);
			relationship.setType(InitiativeRelationshipType.IS_DETACHED_SUB);
			relationship.setOfInitiative(parent);
			
			relationship = initiativeRelationshipRepository.save(relationship);
			
			/* and transfer parent assets to child */
			for (TransferDto thisTransfer : initiativeDto.getOtherAssetsTransfers()) {
				TokenType token = tokenService.getTokenType(UUID.fromString(thisTransfer.getAssetId()));
				
				tokenService.transfer(token.getId(), parent.getId(), initiative.getId(), thisTransfer.getValue(), TokenHolderType.INITIATIVE);
				
				/* upper layer keeping track of who transfered what to whom */
				InitiativeTransfer transfer = new InitiativeTransfer();
				transfer.setRelationship(relationship);
				transfer.setTokenType(token);
				transfer.setValue(thisTransfer.getValue());
				
				transfer = initiativeTransferRepository.save(transfer);
				relationship.getTokensTransfers().add(transfer);
			}
			
			initiative.getRelationships().add(relationship);
			initiativeRepository.save(initiative);
		}
			
		return new PostResult("success", "sub initiative created and tokens transferred",  initiative.getId().toString());
	}
	
	
	/** Get key data from an initiative: id, name and driver but no
	 * data from its assets */
	@Transactional
	public InitiativeDto getLight(UUID id) {
		return  initiativeRepository.findById(id).toDto();
	}
	
	/** Get the light data by calling getLight, and then add data
	 * about the assets held by an initiative */
	@Transactional
	public InitiativeDto getWithOwnAssets(UUID id) {
		Initiative initiative = initiativeRepository.findById(id); 
		InitiativeDto initiativeDto = getLight(initiative.getId());

		/* set own tokens data */
		if(initiative.getTokenType() != null) {
			AssetsDto ownTokens = tokenService.getTokensOfHolderDto(initiative.getTokenType().getId(), initiative.getId());
			ownTokens.setHolderName(initiative.getName());
			initiativeDto.setOwnTokens(ownTokens);
		}
		
		/* set other assets data */
		List<TokenType> otherTokens = null;
		if(initiative.getTokenType() != null) {
			otherTokens = tokenService.getTokenTypesHeldByOtherThan(initiative.getId(), initiative.getTokenType().getId());
		} else {
			otherTokens = tokenService.getTokenTypesHeldBy(initiative.getId());
		}
		
		for (TokenType otherToken : otherTokens) {
			AssetsDto otherAsset = tokenService.getTokensOfHolderDto(otherToken.getId(), initiative.getId());
			otherAsset.setHolderName(initiative.getName());
			initiativeDto.getOtherAssets().add(otherAsset);
		}
		
		return initiativeDto;
	}
	
	@Transactional
	public GetResult<List<InitiativeDto>> getOfUser(UUID userC1Id) {
		/* return the initiatives of a user but inserting initiatives that are 
		 * sub-initiatives as sub-elements */
		
		AppUser user = appUserRepository.findByC1Id(userC1Id);
		List<Initiative> superInitiatives = initiativeRepository.findSuperInitiativesOfContributor(user.getC1Id());
		
		/* get all initiatives in which the user is a contributor */
		List<InitiativeDto> initiativesDtos = new ArrayList<InitiativeDto>();
		for (Initiative initiative : superInitiatives) {
			InitiativeDto dto = initiative.toDto();
			
			/* look for the full sub-initiative tree of each super initiative */
			List<InitiativeDto> subInitiatives = getSubinitiativesTree(initiative.getId());
			dto.setSubInitiatives(subInitiatives);
			
			initiativesDtos.add(dto);
		}
		
		return new GetResult<List<InitiativeDto>>("success", "initiatives retrieved", initiativesDtos);
	}
	
	@Transactional
	public List<InitiativeDto> getSubinitiativesTree(UUID initiativeId) {
		Initiative initiative = initiativeRepository.findById(initiativeId); 
		List<Initiative> subIniatiatives = initiativeRepository.findInitiativesWithRelationship(initiative.getId(), InitiativeRelationshipType.IS_DETACHED_SUB);
		
		List<InitiativeDto> subinitiativeDtos = new ArrayList<InitiativeDto>();
		
		for (Initiative subinitiative : subIniatiatives) {
			InitiativeDto subinitiativeDto = subinitiative.toDto();
			
			/* recursively call this for its own sub-initiatives */
			List<InitiativeDto> subsubIniatiativesDto = getSubinitiativesTree(subinitiative.getId());
			subinitiativeDto.setSubInitiatives(subsubIniatiativesDto);
			
			subinitiativeDtos.add(subinitiativeDto);
		}
		
		return subinitiativeDtos;
	}
	
	public List<MemberDto> getMembers(UUID initiativeId) {
		Initiative initiative = initiativeRepository.findById(initiativeId);
		
		List<MemberDto> members = new ArrayList<MemberDto>();
		for (Member member : initiative.getMembers()) {
			MemberDto memberDto = new MemberDto();
			
			memberDto.setId(member.getId().toString());
			memberDto.setInitiativeId(initiative.getId().toString());
			memberDto.setUser(member.getUser().toDto());
		}
		
		return members;
	}
	
	@Transactional
	public GetResult<List<InitiativeDto>> searchBy(String q) {
		List<Initiative> initiatives = initiativeRepository.searchBy(q);
		List<InitiativeDto> initiativesDtos = new ArrayList<InitiativeDto>();
		
		for(Initiative initiative : initiatives) {
			initiativesDtos.add(initiative.toDto());
		}
		
		return new GetResult<List<InitiativeDto>>("succes", "initiatives returned", initiativesDtos);
		
	}
	
	@Transactional
	public PostResult makeDirectAssignation(UUID initiativeId, AssignationDto assignationDto) {
		Initiative initiative = initiativeRepository.findById(initiativeId);
		
		Assignation assignation = new Assignation();	
		
		assignation.setType(AssignationType.valueOf(assignationDto.getType()));
		assignation.setMotive(assignationDto.getMotive());
		assignation.setNotes(assignationDto.getNotes());
		assignation.setInitiative(initiative);
		assignation.setState(AssignationState.DONE);
		assignation = assignationRepository.save(assignation);
		
		for(BillDto bill : assignationDto.getAssets()) {
			for(ReceiverDto receiver : assignationDto.getReceivers()) {
				TransferDto transfer = new TransferDto();
				
				transfer.setAssetId(bill.getAssetId());
				transfer.setAssetName(bill.getAssetName());
				transfer.setSenderId(initiative.getId().toString());
				transfer.setReceiverId(receiver.getUser().getC1Id());
				
				/* each asset type is distributed among the receviers using
				 * the same percentage*/
				transfer.setValue(bill.getValue() * (receiver.getPercent() / 100.0));
				
				transferFromInitiativeToUser(initiative.getId(), transfer);
			}
		}
		return new PostResult("success", "success", "");
	}
	
	@Transactional
	public PostResult transferFromInitiativeToUser(UUID initiativeId, UUID receiverId, UUID assetId, double value) {
		AppUser receiver = appUserRepository.findByC1Id(receiverId);
		TokenType tokenType = tokenService.getTokenType(assetId);
		
		tokenService.transfer(
				tokenType.getId(), 
				initiativeId, 
				receiver.getC1Id(), 
				value, 
				TokenHolderType.USER);
		
		/* register the transfer to the contributor  */
		Member member = getOrAddMember(initiativeId, receiver.getC1Id());
		
		MemberTransfer thisTransfer = new MemberTransfer();
		thisTransfer.setMember(member);
		thisTransfer.setTokenType(tokenType);
		thisTransfer.setValue(value);
		
		memberTransferRepository.save(thisTransfer);
		member.getTokensTransfers().add(thisTransfer);
		
		memberRepository.save(member);
	
		return new PostResult("success", "assets transferred successfully", "");
	}
	
	@Transactional
	public Member getOrAddMember(UUID initiativeId, UUID userId) {
		Member member = memberRepository.findByInitiative_IdAndUser_C1Id(initiativeId, userId);
		
		if(member == null) {
			Initiative initiative = initiativeRepository.findById(initiativeId);
			AppUser user = appUserRepository.findByC1Id(userId);
			
			member = new Member();
			member.setInitiative(initiative);
			member.setUser(user);
			
			member = memberRepository.save(member);
		}
		
		return member;
	}

	@Transactional
	public PostResult transferFromInitiativeToUser(UUID initiativeId, TransferDto transfer) {
		return transferFromInitiativeToUser(initiativeId, UUID.fromString(transfer.getReceiverId()), UUID.fromString(transfer.getAssetId()), transfer.getValue());
	}
	
	public PostResult createAssignation(UUID initaitiveId, AssignationDto assignationDto) {
		Initiative initiative = initiativeRepository.findById(initaitiveId);
	
		Assignation assignation = new Assignation();
		
		assignation.setType(AssignationType.valueOf(assignationDto.getType()));
		assignation.setMotive(assignationDto.getMotive());
		assignation.setNotes(assignationDto.getNotes());
		assignation.setInitiative(initiative);
		assignation.setState(AssignationState.OPEN);
		assignation.setMinClosureDate(new Timestamp(System.currentTimeMillis()));
		assignation.setMaxClosureDate(new Timestamp(System.currentTimeMillis() + 7L*DAYS_TO_MS));
		assignation = assignationRepository.save(assignation);
		
		for(ReceiverDto receiverDto : assignationDto.getReceivers()) {
			Receiver receiver = new Receiver();
			
			receiver.setUser(appUserRepository.findByC1Id(UUID.fromString(receiverDto.getUser().getC1Id())));
			receiver.setAssignation(assignation);
			receiver.setAssignedPercent(receiverDto.getPercent());
			receiver.setState(ReceiverState.PENDING);
			receiver = receiverRepository.save(receiver);
			
			assignation.getReceivers().add(receiver);
		}
		
		if(assignation.getType() == AssignationType.PEER_REVIEWED) {
			for(EvaluatorDto evaluatorDto : assignationDto.getEvaluators()) {
				Evaluator evaluator = new Evaluator();
				
				evaluator.setUser(appUserRepository.findByC1Id(UUID.fromString(evaluatorDto.getUser().getC1Id())));
				evaluator.setAssignation(assignation);
				evaluator.setState(EvaluatorState.PENDING);
				/* Weight logic TBD */
				evaluator.setWeight(1.0);
				evaluator = evaluatorRepository.save(evaluator);
				
				/* Create the grades of all evaluators already */
				for(Receiver receiver : assignation.getReceivers()) {
					EvaluationGrade grade = new EvaluationGrade();
					
					grade.setAssignation(assignation);
					grade.setEvaluator(evaluator);
					grade.setReceiver(receiver);
					grade.setPercent(0.0);
					grade.setType(EvaluationGradeType.SET);
					grade.setState(EvaluationGradeState.PENDING);
					grade = evaluationGradeRepository.save(grade);
					
					evaluator.getGrades().add(grade);
				}
				
				assignation.getEvaluators().add(evaluator);
			}
		}
		
		for(BillDto billDto : assignationDto.getAssets()) {
			TokenType tokenType = tokenService.getTokenType(UUID.fromString(billDto.getAssetId()));
			Bill bill = new Bill();
			
			bill.setAssignation(assignation);
			bill.setTokenType(tokenType);
			bill.setValue(billDto.getValue());
			bill = billRepository.save(bill);
			
			assignation.getBills().add(bill);
		}
		
		return new PostResult("success", "success", "");
	}
	
	/** Get the distribution of an asset starting from a given initiative
	 * and including the tokens transferred to its sub-initiatives and members */
	@Transactional
	public AssetsDto getTokenDistribution(UUID tokenId, UUID initiativeId) {

		Initiative initiative = initiativeRepository.findById(initiativeId); 
		AssetsDto assetDto = tokenService.getTokensOfHolderDto(tokenId, initiative.getId());
		assetDto.setHolderName(initiative.getName());
		
		assetDto.setTransferredToSubinitiatives(getTransferredToSubinitiatives(tokenId, initiative.getId()));
		assetDto.setTransferredToUsers(getTransferredToUsers(tokenId, initiative.getId()));
		
		return assetDto;
	}
	
	/** Get the tokens transferred from one initiative into its sub-initiatives */
	@Transactional
	public List<TransferDto> getTransferredToSubinitiatives(UUID tokenId, UUID initiativeId) {
		Initiative initiative = initiativeRepository.findById(initiativeId); 
		TokenType token = tokenService.getTokenType(tokenId);

		/* get of sub-initiatives */
		List<InitiativeRelationship> subinitiativesRelationships = 
				initiativeRelationshipRepository.findByOfInitiativeIdAndType(initiative.getId(), InitiativeRelationshipType.IS_DETACHED_SUB);
		
		List<TransferDto> transferredToSubinitiatives = new ArrayList<TransferDto>();
		
		for (InitiativeRelationship relationship : subinitiativesRelationships) {
			/* get all transfers of a given token made from and to these initiatives */
			Double totalTransferred = initiativeTransferRepository.getTotalTransferred(tokenId, relationship.getId());
			
			TransferDto dto = new TransferDto();
			
			dto.setAssetId(token.getId().toString());
			dto.setAssetName(token.getName());
			dto.setSenderId(relationship.getOfInitiative().getId().toString());
			dto.setSenderName(relationship.getOfInitiative().getName());
			dto.setReceiverId(relationship.getInitiative().getId().toString());
			dto.setReceiverName(relationship.getInitiative().getName());
			dto.setValue(totalTransferred);
			
			transferredToSubinitiatives.add(dto);
		}
		
		return transferredToSubinitiatives;
	}
	
	/** Get the tokens transferred from one initiative to its members */
	@Transactional
	public List<TransferDto> getTransferredToUsers(UUID tokenId, UUID initiativeId) {
		Initiative initiative = initiativeRepository.findById(initiativeId); 
		TokenType token = tokenService.getTokenType(tokenId);

		List<TransferDto> transferredToUsers = new ArrayList<TransferDto>();
		for (Member member : initiative.getMembers()) {
			/* get all transfers of a given token made from an initiative to a contributor */
			double totalTransferred = memberTransferRepository.getTotalTransferred(tokenId, member.getId());
			
			TransferDto dto = new TransferDto();
			
			dto.setAssetId(token.getId().toString());
			dto.setAssetName(token.getName());
			dto.setSenderId(initiative.getId().toString());
			dto.setSenderName(initiative.getName());
			dto.setReceiverId(member.getUser().getC1Id().toString());
			dto.setReceiverName(member.getUser().getNickname());
			dto.setValue(totalTransferred);
			
			transferredToUsers.add(dto);
		}
		
		return transferredToUsers;
	}

	@Transactional
	public AssignationDto getAssignation(UUID initiativeId, UUID assignationId, UUID evaluatorAppUserId) {
		Assignation assignation = assignationRepository.findById(assignationId);
		AssignationDto assignationDto = assignation.toDto();
		
		/* add the evaluations of logged user */
		Evaluator evaluator = evaluatorRepository.findByAssignationIdAndUser_C1Id(assignation.getId(), evaluatorAppUserId);
		
		EvaluationDto evaluation = new EvaluationDto();
		evaluation.setId(evaluator.getGrades().toString());
		evaluation.setEvaluationState(evaluator.getState().toString());
		
		for (EvaluationGrade grade : evaluator.getGrades()) {
			evaluation.getEvaluationGrades().add(grade.toDto());
		}
		
		assignationDto.setThisEvaluation(evaluation);
		return assignationDto;
	}
	
	@Transactional
	public GetResult<List<AssignationDto>> getAssignations(UUID initiativeId, UUID evaluatorAppUserId) {
		List<Assignation> assignations = assignationRepository.findByInitiativeId(initiativeId);
		List<AssignationDto> assignationsDtos = new ArrayList<AssignationDto>();
		
		for(Assignation assignation : assignations) {
			assignationsDtos.add(getAssignation(initiativeId, assignation.getId(), evaluatorAppUserId));
		}
		
		return new GetResult<List<AssignationDto>>("success", "assignations found", assignationsDtos);
	}
	
	/** Non-transactional to evaluate and update assignation state in different transactions */
	public PostResult evaluateAndUpdateAssignation(UUID evaluatorAppUserId, UUID assignationId, EvaluationDto evaluationDto) {
		PostResult result = evaluateAssignation(evaluatorAppUserId, assignationId, evaluationDto);
		updateAssignationState(assignationId);
		
		return result;
	}
	
	@Transactional
	private PostResult evaluateAssignation(UUID evaluatorUserId, UUID assignationId, EvaluationDto evaluationsDto) {
		
		Assignation assignation = assignationRepository.findById(assignationId);
		Evaluator evaluator = evaluatorRepository.findByAssignationIdAndUser_C1Id(assignation.getId(), evaluatorUserId);
		
		for(EvaluationGradeDto evaluationGradeDto : evaluationsDto.getEvaluationGrades()) {
			UUID receiverUserId = UUID.fromString(evaluationGradeDto.getReceiverUser().getC1Id());
			EvaluationGrade grade = evaluationGradeRepository.findByAssignation_IdAndReceiver_User_C1IdAndEvaluator_User_C1Id(assignation.getId(), receiverUserId, evaluatorUserId);
					
			grade.setPercent(evaluationGradeDto.getPercent());
			grade.setType(EvaluationGradeType.valueOf(evaluationGradeDto.getType()));
			grade.setState(EvaluationGradeState.DONE);
			
			evaluationGradeRepository.save(grade);			
		}
		
		evaluator.setState(EvaluatorState.DONE);
		evaluatorRepository.save(evaluator);
		
		return new PostResult("success", "evaluation saved", evaluator.getId().toString());
	}

	@Transactional
	public void updateAssignationState(UUID assignationId) {
		
		Assignation assignation = assignationRepository.findById(assignationId);
		
		PeerReviewedAssignation peerReviewedAssignation = new PeerReviewedAssignation();
		peerReviewedAssignation.setAssignation(assignation);
		
		peerReviewedAssignation.updateState();
		
		if(peerReviewedAssignation.getState() == PeerReviewedAssignationState.CLOSED) {
			/* percents already updated by peerReviewAssignation,
			 * so just change state, transfer funds and save */
			
			/* transfer the assets to the receivers */
			for(Bill bill : assignation.getBills()) {
				for(Receiver receiver : assignation.getReceivers()) {
					double value = bill.getValue() * receiver.getAssignedPercent() / 100.0;
					transferFromInitiativeToUser(assignation.getInitiative().getId(), receiver.getUser().getC1Id(), bill.getTokenType().getId(), value);
					receiver.setState(ReceiverState.RECEIVED);
				}
			}
			
			assignation.setState(AssignationState.DONE);
			assignationRepository.save(assignation);
		}
	}
	
}
