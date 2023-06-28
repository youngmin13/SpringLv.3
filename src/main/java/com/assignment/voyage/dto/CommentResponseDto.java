package com.assignment.voyage.dto;

import com.assignment.voyage.entity.Comment;
import com.assignment.voyage.entity.TimeStamped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommentResponseDto {

    private Long id;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // 에러 상태 반환하기 위한 필드
    private String msg;
    private HttpStatus statusCode;


    public CommentResponseDto(Comment comment){
        this.id = comment.getId();
        this.username = comment.getUser().getUsername();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.modifiedAt = comment.getModifiedAt();
    }

    public CommentResponseDto (ApiResultDto apiResultDto) {
        this.msg = apiResultDto.getMsg();
        this.statusCode = apiResultDto.getStatusCode();
    }
}