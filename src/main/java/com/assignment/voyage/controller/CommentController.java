package com.assignment.voyage.controller;

import com.assignment.voyage.dto.ApiResultDto;
import com.assignment.voyage.dto.CommentRequestDto;
import com.assignment.voyage.dto.CommentResponseDto;
import com.assignment.voyage.dto.PostRequestDto;
import com.assignment.voyage.security.UserDetailsImpl;
import com.assignment.voyage.service.CommentService;
import com.assignment.voyage.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class CommentController {
    private final CommentService commentService;

    public CommentController (CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 생성
    // 해당하는 post의 댓글을 생성하는 기능
    @PostMapping("/{id}/comments")
    public CommentResponseDto createComments (@PathVariable Long id, @RequestBody CommentRequestDto requestDto, HttpServletRequest request) {
        return commentService.createComments(id, requestDto, request);
    }

    // 댓글 수정
    // 해당하는 post의 댓글을 수정하는 기능
    @PutMapping("/{post_id}/{comment_id}")
    public CommentResponseDto updateComments (@PathVariable Long post_id, @PathVariable Long comment_id, @RequestBody CommentRequestDto requestDto, HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.updateComments(post_id, comment_id, requestDto, request, userDetails);
    }

    // 댓글 삭제
    // 해당하는 Post의 댓글을 삭제하는 기능
    @DeleteMapping("/{postId}/{commentId}")
    public ApiResultDto deleteComment(@PathVariable Long postId, @PathVariable Long commentId, HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.deleteComment(postId, commentId, request, userDetails);
    }
}
