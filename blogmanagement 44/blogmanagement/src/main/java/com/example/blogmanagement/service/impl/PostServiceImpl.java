package com.example.blogmanagement.service.impl;

import com.example.blogmanagement.dto.PagedResponse;
import com.example.blogmanagement.dto.PostRequestDto;
import com.example.blogmanagement.dto.PostResponseDto;
import com.example.blogmanagement.document.Post;
import com.example.blogmanagement.exception.ResourceNotFoundException;
import com.example.blogmanagement.exception.BadRequestException;
import com.example.blogmanagement.repository.CommentRepository;
import com.example.blogmanagement.repository.PostRepository;
import com.example.blogmanagement.repository.UserRepository;
import com.example.blogmanagement.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PostService}. Handles creation, retrieval,
 * updating, deletion and listing of blog posts stored in MongoDB. Also
 * coordinates cascade deletion of comments and validates author existence.
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public PostResponseDto createPost(PostRequestDto request) {
        // Ensure the author exists in the relational database
        userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(request.getAuthorId())
                .build();
        Post saved = postRepository.save(post);
        return mapToResponse(saved);
    }

    @Override
    public PostResponseDto getPost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return mapToResponse(post);
    }

    @Override
    public PostResponseDto updatePost(String postId, PostRequestDto request) {
        Post existing = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!existing.getAuthorId().equals(request.getAuthorId())) {
            throw new BadRequestException("Author of a post cannot be changed");
        }
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        Post updated = postRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    public void deletePost(String postId) {
        Post existing = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        commentRepository.deleteByPostId(postId);

        postRepository.delete(existing);
    }

    @Override
    public PagedResponse<PostResponseDto> listPosts(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postsPage;
        if (search != null && !search.trim().isEmpty()) {
            postsPage = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(search, search, pageable);
        } else {
            postsPage = postRepository.findAll(pageable);
        }
        List<PostResponseDto> content = postsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        boolean last = postsPage.getNumber() >= postsPage.getTotalPages() - 1;
        return new PagedResponse<>(
                content,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                last);
    }


    private PostResponseDto mapToResponse(Post post) {

        String authorUsername = userRepository.findById(post.getAuthorId())
                .map(u -> u.getUsername())
                .orElse(null);
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(authorUsername)
                .createdAt(post.getCreatedAt())
                .build();
    }
}