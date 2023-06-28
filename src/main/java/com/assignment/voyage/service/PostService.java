package com.assignment.voyage.service;

import com.assignment.voyage.dto.ApiResultDto;
import com.assignment.voyage.dto.PostRequestDto;
import com.assignment.voyage.dto.PostResponseDto;
import com.assignment.voyage.entity.Comment;
import com.assignment.voyage.entity.Post;
import com.assignment.voyage.entity.User;
import com.assignment.voyage.entity.UserRoleEnum;
import com.assignment.voyage.jwt.JwtUtil;
import com.assignment.voyage.repository.PostRepository;
import com.assignment.voyage.repository.UserRepository;
import com.assignment.voyage.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, JwtUtil jwtUtil, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Transactional
    public PostResponseDto createPost(PostRequestDto postRequestDto, HttpServletRequest request) {

        String token = jwtUtil.resolveToken(request);
        Claims claims;

        if (token != null) {
            if (jwtUtil.validateToken(token)) {
                claims = jwtUtil.getUserInfoFromToken(token);
            } else {
                throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
            }

            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            // RequestDto -> Entity
            Post post = new Post(postRequestDto);
            post.addUser(user);

            //DB 저장
            Post savePost = postRepository.save(post);
            // Entity -> ResponseDto
            PostResponseDto postResponseDto = new PostResponseDto(savePost);

            return postResponseDto;
        }
        else throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
    }

    public List<PostResponseDto> getPost() {
        // DB 조회
        return postRepository.findAllByOrderByCreatedAtDesc().stream().map(PostResponseDto::new).toList();
    }

    public PostResponseDto getPostContent(String title) {
        Post post = findPostByTitle(title);
        PostResponseDto postResponseDto = new PostResponseDto(post);
        return postResponseDto;
    }

    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto postRequestDto, HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {

        String token = jwtUtil.resolveToken(request);
        Claims claims;

        PostResponseDto postResponseDto;

        if (token != null) {
            claims = jwtValidationCheck(token);

            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            Post post = findPostById(id);

            if (post.getUser().getUsername().equals(userDetails.getUser().getUsername())) {
                post.update(postRequestDto);
                postResponseDto = new PostResponseDto(post);
                // 수정된 게시글을 반환해야 한다.
                return postResponseDto;
            }
            else if (userDetails.getUser().getRole() == UserRoleEnum.ADMIN) {
                post.update(postRequestDto);
                postResponseDto = new PostResponseDto(post);
                // 수정된 게시글을 반환해야 한다.
                return postResponseDto;
            }
            else return new PostResponseDto (new ApiResultDto("작성자만 삭제/수정할 수 있습니다.", HttpStatus.BAD_REQUEST));
        }
        else throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
    }

    @Transactional
    public ApiResultDto deletePost(String title, HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {

        String token = jwtUtil.resolveToken(request);
        Claims claims;

        if (token != null) {
            claims = jwtValidationCheck(token);

            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            Post post = findPostByTitle(title);

            if (post.getUser().getUsername().equals(userDetails.getUser().getUsername())) {
                postRepository.delete(post);
                return new ApiResultDto("삭제 성공", HttpStatus.OK);
            }
            else if (userDetails.getUser().getRole() == UserRoleEnum.ADMIN) {
                postRepository.delete(post);
                return new ApiResultDto("삭제 성공", HttpStatus.OK);
            }
            else return new ApiResultDto("작성자만 삭제/수정할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }
        else throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
    }

    private Post findPostByTitle(String title) {
        return postRepository.findByTitle(title).orElseThrow(() ->
                new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
        );
    }

    private Post findPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
        );
    }

    public Claims jwtValidationCheck (String token) {
        if (jwtUtil.validateToken(token)) {
            return jwtUtil.getUserInfoFromToken(token);
        } else {
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }
    }
}
