package org.collectiveone.modules.initiatives.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.collectiveone.common.dto.GetResult;
import org.collectiveone.common.dto.PostResult;
import org.collectiveone.modules.decisions.enums.DecisionMakerRole;
import org.collectiveone.modules.decisions.enums.DecisionVerdict;
import org.collectiveone.modules.decisions.services.DecisionService;
import org.collectiveone.modules.initiatives.InitiativeRelationshipType;
import org.collectiveone.modules.initiatives.dto.InitiativeDto;
import org.collectiveone.modules.initiatives.dto.MemberDto;
import org.collectiveone.modules.initiatives.dto.NewInitiativeDto;
import org.collectiveone.modules.initiatives.model.Initiative;
import org.collectiveone.modules.initiatives.model.InitiativeRelationship;
import org.collectiveone.modules.initiatives.model.Member;
import org.collectiveone.modules.initiatives.repositories.InitiativeRelationshipRepositoryIf;
import org.collectiveone.modules.initiatives.repositories.InitiativeRepositoryIf;
import org.collectiveone.modules.initiatives.repositories.MemberRepositoryIf;
import org.collectiveone.modules.tokens.dto.AssetsDto;
import org.collectiveone.modules.tokens.dto.TokenHolderType;
import org.collectiveone.modules.tokens.dto.TransferDto;
import org.collectiveone.modules.tokens.model.InitiativeTransfer;
import org.collectiveone.modules.tokens.model.TokenType;
import org.collectiveone.modules.tokens.repositories.InitiativeTransferRepositoryIf;
import org.collectiveone.modules.tokens.services.TokenService;
import org.collectiveone.modules.users.model.AppUser;
import org.collectiveone.modules.users.repositories.AppUserRepositoryIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InitiativeService {
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private DecisionService decisionService;
	
	
	@Autowired
	private AppUserRepositoryIf appUserRepository;
	
	@Autowired
	private InitiativeRepositoryIf initiativeRepository;
	
	@Autowired
	private InitiativeRelationshipRepositoryIf initiativeRelationshipRepository;
	
	@Autowired
	private InitiativeTransferRepositoryIf initiativeTransferRepository;
	
	@Autowired
	private MemberRepositoryIf memberRepository;
	
	
	
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
		List<Initiative> superInitiatives = initiativeRepository.findSuperInitiativesOfMember(user.getC1Id());
		
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
}
