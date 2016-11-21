package org.collectiveone.web.controllers.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.collectiveone.model.User;
import org.collectiveone.services.CbtionDtoListRes;
import org.collectiveone.services.DbServicesImp;
import org.collectiveone.services.Filters;
import org.collectiveone.web.dto.CbtionDto;
import org.collectiveone.web.dto.CommentDto;
import org.collectiveone.web.dto.ObjectPromote;
import org.collectiveone.web.dto.ReviewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rest/cbtions")
public class CbtionsController {
	
	@Autowired
	DbServicesImp dbServices;
	
	@RequestMapping(value="/getList", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getList(@RequestBody Filters filters) {
		if(filters.getPage() == 0) filters.setPage(1);
		if(filters.getNperpage() == 0) filters.setNperpage(15);
		
		CbtionDtoListRes cbtionsDtosRes = dbServices.cbtionDtoGetFiltered(filters);
		
		List<CbtionDto> cbtionDtos = cbtionsDtosRes.getCbtionsDtos();
		int[] resSet = cbtionsDtosRes.getResSet();
		
		Map<String,Object> map = new HashMap<>();
		
		map.put("cbtionDtos", cbtionDtos);
		map.put("resSet", resSet);
		
		return map;
	}

	@RequestMapping(value="/get/{id}", method = RequestMethod.POST)
	public @ResponseBody CbtionDto get(@PathVariable Long id) {
		return dbServices.cbtionGetDto(id);
	}
	
	@RequestMapping(value="/promote", method = RequestMethod.POST)
	public @ResponseBody Boolean promote(@RequestBody ObjectPromote cbtionPromote) {
		/* creator is the logged user */
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = dbServices.userGet(auth.getName());
		if(logged != null) {
			dbServices.cbtionPromote(cbtionPromote.getElementId(), logged.getId(), cbtionPromote.getPromoteUp());
		}
		return true;
	}
	
	@RequestMapping(value="/getComment/{commentId}", method = RequestMethod.POST)
	public @ResponseBody CommentDto getComment(@PathVariable Long commentId) {
		CommentDto commentsDto = dbServices.cbtionGetCommentDto(commentId);
		return commentsDto;
	}
	
	@RequestMapping(value="/getComments/{id}", method = RequestMethod.POST)
	public @ResponseBody List<CommentDto> getComments(@PathVariable Long id) {
		List<CommentDto> commentsDtos = dbServices.cbtionGetCommentsDtos(id);
		return commentsDtos;
	}
	
	@RequestMapping(value="/commentNew", method = RequestMethod.POST)
	public @ResponseBody Boolean commentNew(@RequestBody CommentDto commentDto) {
		/* creator is the logged user */
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth.isAuthenticated()) {
			commentDto.setCreatorUsername(auth.getName());
			dbServices.commentCbtionCreate(commentDto);
			return true;
		}
		return false;
	}
	
	@RequestMapping(value="/commentGetReplies/{commentId}", method = RequestMethod.POST)
	public @ResponseBody  List<CommentDto> commentNew(@PathVariable Long commentId) {
		return dbServices.commentGetRepliesDtos(commentId);
	}
	
	@RequestMapping(value="/commentPromote", method = RequestMethod.POST)
	public @ResponseBody Boolean commentPromote(@RequestBody ObjectPromote commentPromote) {
		/* creator is the logged user */
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = dbServices.userGet(auth.getName());
		if(logged != null) {
			dbServices.commentPromote(commentPromote.getElementId(), logged.getId(), commentPromote.getPromoteUp());
		}
		return true;
	}
	
	@RequestMapping(value="/getReviews/{id}", method = RequestMethod.POST)
	public @ResponseBody List<ReviewDto> getReviews(@PathVariable Long id) {
		return dbServices.cbtionGetReviewsDtos(id);
	}
	
	@Secured("ROLE_USER")
	@RequestMapping(value="/edit", method = RequestMethod.POST)
	public @ResponseBody boolean edit(@RequestBody CbtionDto cbtionDto) {
		/* creator is the logged user */
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		cbtionDto.setCreatorUsername(auth.getName()); 
		dbServices.cbtionEdit(cbtionDto);
		return true;
	}
}
