package com.example.blogmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {

    @Schema(description = "Unique identifier of the post")
    private String id;

    @Schema(description = "Title of the post")
    private String title;

    @Schema(description = "Content of the post")
    private String content;

    @Schema(description = "Username of the author")
    private String authorUsername;

    @Schema(description = "Timestamp when the post was created")
    private LocalDateTime createdAt;
    // In a real‑world API we omit the updatedAt field from responses to reduce noise.
}