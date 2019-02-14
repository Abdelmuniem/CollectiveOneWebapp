package org.collectiveone.modules.contexts;

import java.util.List;
import java.util.UUID;

import org.collectiveone.common.BaseController;
import org.collectiveone.common.dto.GetResult;
import org.collectiveone.common.dto.PostResult;
import org.collectiveone.modules.contexts.dto.CommitDto;
import org.collectiveone.modules.contexts.dto.ContextMetadataDto;
import org.collectiveone.modules.contexts.dto.PerspectiveDto;
import org.collectiveone.modules.contexts.dto.StagedElementDto;
import org.collectiveone.modules.contexts.entities.enums.StageStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/1")
public class ContextController extends BaseController { 
	
	@Autowired
	private ContextOuterService contextService;
	
	
	/**
	 * Creates a new context.
	 * 
	 * URLParams 
	 * - (optional) parentPerspectiveId=[UUID]. If not provided, the private perspective of the user is used.
	 * 
	 * DataParams
	 * 	Required
	 * 	- contextDto 
	 * 		{
	 * 			title: (required max 1024 length)[string],
	 * 			description: (optional)[string]
	 * 		} 
	 * */
	@RequestMapping(path = "/ctx", method = RequestMethod.POST)
	public PostResult createContext(
			@RequestBody ContextMetadataDto contextMetadataDto,
			@RequestParam(name="parentPerspectiveId", defaultValue="") UUID parentPerspectiveId,
			@RequestParam(name="beforePerspectiveId", defaultValue="") UUID beforePerspectiveId,
			@RequestParam(name="afterPerspectiveId", defaultValue="") UUID afterPerspectiveId) {
		
		if (parentPerspectiveId == null) {
			parentPerspectiveId = appUserService.getUserPerspectiveId(getLoggedUserId());
		}
		
		return contextService.createContext(
				contextMetadataDto, 
				parentPerspectiveId, 
				getLoggedUserId(),
				beforePerspectiveId,
				afterPerspectiveId);
	}
	
	/**
	 * Returns the default perspective to the request author on a given context .
	 * 
	 * URLParams
	 *  
	 * - contextId: The id of the context (not of the perspective!) that is requested.
	 * 				The default perspective is retrieved. 
	 * 
	 * DataParams (Optional)
	 * 
	 * - levels: 	Number of levels of subcontext that are retrieved. 
	 * 		     	0: none, 1: first child, and so on
	 * 
	 * - addCards:  If true, the context (and subcontexts if levels > 0) 
	 * 				cards are included. If false, only the context metadata is
	 * 				included  
	 * 
	 * */
	@RequestMapping(path = "/ctx/{contextId}", method = RequestMethod.GET)
	public GetResult<PerspectiveDto> getContext(
			@PathVariable(name="contextId") UUID contextId,
			@RequestParam(name="levels", defaultValue="0") Integer levels,
			@RequestParam(name="addCards", defaultValue="false") Boolean addCards) {
		
		return contextService.getContext(contextId, getLoggedUserId(), levels, addCards);	
	}
	
	/** Set the status of a staged element 
	 * 
	 * URLParams
	 * 
	 * - stagedElementId: The id of the stagedElement
	 * 
	 * Request Parameters
	 * 
	 * - newStatus: new status to be set (PENDING, ADDED, HOLD)
	 *  
	 */
	@RequestMapping(path = "/stagedElement/{stageElementId}/status", method = RequestMethod.PUT)
	public PostResult setStagedElementState(
			@PathVariable(name="stageElementId") UUID stageElementId,
			@RequestParam(name="newStatus", defaultValue="ADD") StageStatus newStatus) {
		
		return contextService.setStagedElementStatus(stageElementId, newStatus, getLoggedUserId());
	} 
	
	/** Get the list of staged elements on a perspective and for a given user 
	 * 
	 * URLParams
	 * 
	 * - perspectiveId: The id of the perspective.
	 * - levels: the number of levels of subperspectives whose staged 
	 *   elements should be aggregated  
	 *  
	 */
	@RequestMapping(path = "/persp/{perspectiveId}/stagedElements", method = RequestMethod.GET)
	public GetResult<List<StagedElementDto>> getStagedElements(
			@PathVariable(name="perspectiveId") UUID perspectiveId,
			@RequestParam(name="levels", defaultValue="0") Integer levels) {
		
		return contextService.getStagedElements(perspectiveId, levels, getLoggedUserId());
	} 
	
	/** commit all ADDED changes of the working commit  
	 * 
	 * URLParams
	 * 
	 * - perspectiveId: The id of the perspective.
	 * - levels: the number of levels of subperspectives whose ADDED staged 
	 *   elements should be added too
	 * - message: an optional message giving context to the commit  
	 */
	@RequestMapping(path = "/persp/{perspectiveId}/commit", method = RequestMethod.PUT)
	public PostResult commitStagedElements(
			@PathVariable(name="perspectiveId") UUID perspectiveId,
			@RequestParam(name="levels", defaultValue="0") Integer levels,
			@RequestBody CommitDto commitDto) {
		
		return contextService.commitWorkingCommit(perspectiveId, commitDto, levels, getLoggedUserId());
	} 
	
}	