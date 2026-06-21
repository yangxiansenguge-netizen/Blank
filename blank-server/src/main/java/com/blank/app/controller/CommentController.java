package com.blank.app.controller;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{postcardId}")
    public ApiResponse<List<Map<String, Object>>> getComments(
            @PathVariable Integer postcardId,
            @CurrentUser(required = false) JwtUserDetails user) {
        Integer userId = user != null ? user.getId() : null;
        return ApiResponse.success(commentService.getComments(postcardId, userId));
    }

    @PostMapping("/{postcardId}")
    public ApiResponse<Map<String, Object>> addComment(
            @CurrentUser JwtUserDetails user,
            @PathVariable Integer postcardId,
            @RequestBody Map<String, String> body) {
        return ApiResponse.success(commentService.addComment(postcardId, user.getId(), body.get("content")), "评论成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@CurrentUser JwtUserDetails user,
                                            @PathVariable Integer id) {
        commentService.deleteComment(id, user.getId());
        return ApiResponse.success(null, "删除成功");
    }

    @PutMapping("/{id}/pin")
    public ApiResponse<Map<String, Object>> togglePin(@CurrentUser JwtUserDetails user,
                                                       @PathVariable Integer id) {
        return ApiResponse.success(commentService.togglePin(id, user.getId()));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> likeComment(@CurrentUser JwtUserDetails user,
                                                         @PathVariable Integer id) {
        return ApiResponse.success(commentService.likeComment(id, user.getId()));
    }
}
