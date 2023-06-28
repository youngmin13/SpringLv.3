package com.assignment.voyage.service;

import com.assignment.voyage.dto.*;
import com.assignment.voyage.entity.Comment;
import com.assignment.voyage.entity.Post;
import com.assignment.voyage.entity.User;
import com.assignment.voyage.entity.UserRoleEnum;
import com.assignment.voyage.jwt.JwtUtil;
import com.assignment.voyage.repository.CommentRepository;
import com.assignment.voyage.repository.PostRepository;
import com.assignment.voyage.repository.UserRepository;
import com.assignment.voyage.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    public CommentService(PostRepository postRepository, CommentRepository commentRepository, JwtUtil jwtUtil, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }


    @Transactional
    public CommentResponseDto createComments(Long id, CommentRequestDto requestDto, HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        Claims claims;

        if (token != null) {
            claims = jwtValidationCheck(token);

            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            Post post = postRepository.findById(id).orElseThrow(() ->
                    new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
            );

            Comment comment = new Comment(requestDto, user, post);

            post.getCommentList().add(comment);

            commentRepository.save(comment);
            postRepository.save(post);

            return new CommentResponseDto(comment);
        }
        else throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
    }

    @Transactional
    public CommentResponseDto updateComments(Long postId, Long commentId, CommentRequestDto requestDto, HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String token = jwtUtil.resolveToken(request);
        Claims claims;

        CommentResponseDto responseDto;

        if (token != null) {

            claims = jwtValidationCheck(token);

            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            Post post = postRepository.findById(postId).orElseThrow(() ->
                    new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
            );

            Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                    new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
            );

            if (userDetails.getUser().getUsername().equals(comment.getUser().getUsername()) || userDetails.getUser().getRole() == UserRoleEnum.ADMIN) {
                comment.update(requestDto);
                responseDto = new CommentResponseDto(comment);
                // 수정된 게시글을 반환해야 한다.
                return responseDto;
            }
            else return new CommentResponseDto (new ApiResultDto("작성자만 삭제/수정할 수 있습니다.", HttpStatus.BAD_REQUEST));
        }
        else throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
    }

    public ApiResultDto deleteComment(Long postId, Long commentId, HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String token = jwtUtil.resolveToken(request);
        Claims claims;

        if (token != null) {
            claims = jwtValidationCheck(token);

            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            Post post = postRepository.findById(postId).orElseThrow(() ->
                    new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
            );

            Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                    new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
            );

            if (comment.getUser().getUsername().equals(userDetails.getUser().getUsername()) || userDetails.getUser().getRole() == UserRoleEnum.ADMIN) {
                commentRepository.delete(comment);
                return new ApiResultDto("삭제 성공", HttpStatus.OK);
            }
            else return new ApiResultDto("작성자만 삭제/수정할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }
        else throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
    }

    public Claims jwtValidationCheck (String token) {
        if (jwtUtil.validateToken(token)) {
            return jwtUtil.getUserInfoFromToken(token);
        } else {
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }
    }
}
