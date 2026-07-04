package co.edu.escuelaing.alphaeci.matching_service.entrypoints.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateCategoryRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateCategoryWithTagsRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateTagRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.UpdateCategoryRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.UpdateTagRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.CategoryResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.CategoryWithTagsResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.TagResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.mapper.CategoryAppMapper;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.CategoryUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.entrypoints.advice.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories - Management", description = "APIs to manage categories and tags used by the recommendation engine and client UI.")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryUseCasePort categoryUseCase;
    private final CategoryAppMapper categoryAppMapper;


    // -------------------- CATEGORIES --------------------

    @PostMapping
    @Operation(summary = "Create a category",
               description = "Creates a new category resource. Use when introducing a new interest category used to group tags. Returns the created category with its generated UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class),
                examples = @ExampleObject(value = "{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Sports\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid payload or category already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid category name.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Unexpected error while creating category.\",\"status\":500,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryAppMapper.toResponse(categoryUseCase.createCategory(request.getName())));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID",
               description = "Retrieves a single category by its UUID. Returns 200 with the category details when found.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class),
                examples = @ExampleObject(value = "{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Sports\"}"))),
        @ApiResponse(responseCode = "404", description = "Not found: category does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Category not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid UUID format",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid UUID format.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<CategoryResponse> getCategory(
            @Parameter(description = "ID of the category") @PathVariable UUID categoryId) {
        return ResponseEntity.ok(categoryAppMapper.toResponse(categoryUseCase.getCategory(categoryId)));
    }

    @GetMapping("/all")
    @Operation(summary = "List all categories",
               description = "Returns a list with all categories available in the system. Useful for dropdowns or client configuration.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of categories retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class),
                examples = @ExampleObject(value = "[{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Sports\"}]"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Server error retrieving categories.\",\"status\":500,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryAppMapper.toResponseCategoryList(categoryUseCase.getAllCategories()));
    }

    @PatchMapping("/{categoryId}")
    @Operation(summary = "Update category",
               description = "Updates the name of an existing category. Returns the updated category on success.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class),
                examples = @ExampleObject(value = "{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Updated name\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid payload or name conflict",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Category name already exists.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Not found: category does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Category not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID of the category") @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(
                categoryAppMapper.toResponse(categoryUseCase.updateCategory(categoryId, request.getName())));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category",
               description = "Deletes a category by its UUID. Use with caution: consumers may rely on existing categories.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Not found: category does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Category not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID of the category") @PathVariable UUID categoryId) {
        categoryUseCase.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }


    // -------------------- TAGS --------------------

    @PostMapping("/tags")
    @Operation(summary = "Create a tag",
               description = "Creates a new tag associated to a category. Required to enrich user interests used by the matching engine.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tag created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagResponse.class),
                examples = @ExampleObject(value = "{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Basketball\",\"categoryId\":\"00000000-0000-0000-0000-000000000000\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid payload or tag exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid tag data.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryAppMapper.toResponse(categoryUseCase.createTag(request.getName(), request.getCategoryId())));
    }

    @GetMapping("/tags/{tagId}")
    @Operation(summary = "Get tag by ID",
               description = "Retrieves a tag by its UUID. Returns tag id, name and parent category id.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagResponse.class),
                examples = @ExampleObject(value = "{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Basketball\",\"categoryId\":\"00000000-0000-0000-0000-000000000000\"}"))),
        @ApiResponse(responseCode = "404", description = "Not found: tag does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Tag not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<TagResponse> getTag(
            @Parameter(description = "ID of the tag") @PathVariable UUID tagId) {
        return ResponseEntity.ok(categoryAppMapper.toResponse(categoryUseCase.getTag(tagId)));
    }

    @GetMapping("/tags/all")
    @Operation(summary = "List all tags",
               description = "Returns a list with all tags across categories. Useful for client filters and tag selection UI.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of tags retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagResponse.class),
                examples = @ExampleObject(value = "[{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Basketball\",\"categoryId\":\"00000000-0000-0000-0000-000000000000\"}]"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Server error retrieving tags.\",\"status\":500,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(categoryAppMapper.toResponseTagList(categoryUseCase.getAllTags()));
    }

    @PatchMapping("/tags/{tagId}")
    @Operation(summary = "Update tag",
               description = "Update the name of an existing tag. Returns the updated tag on success.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagResponse.class),
                examples = @ExampleObject(value = "{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Updated Tag\",\"categoryId\":\"00000000-0000-0000-0000-000000000000\"}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid payload or name conflict",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Tag name already exists.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Not found: tag does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Tag not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<TagResponse> updateTag(
            @Parameter(description = "ID of the tag") @PathVariable UUID tagId,
            @Valid @RequestBody UpdateTagRequest request) {
        return ResponseEntity.ok(
                categoryAppMapper.toResponse(categoryUseCase.updateTag(tagId, request.getName())));
    }

    @DeleteMapping("/tags/{tagId}")
    @Operation(summary = "Delete tag",
               description = "Deletes a tag by its UUID. Use carefully if tags are referenced by profiles.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Not found: tag does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Tag not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "ID of the tag") @PathVariable UUID tagId) {
        categoryUseCase.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}/tags")
    @Operation(summary = "List tags by category",
               description = "Returns tags that belong to a given category UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of tags retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagResponse.class),
                examples = @ExampleObject(value = "[{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Basketball\",\"categoryId\":\"00000000-0000-0000-0000-000000000000\"}]"))),
        @ApiResponse(responseCode = "404", description = "Not found: category does not exist",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Category not found.\",\"status\":404,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<TagResponse>> getTagsByCategoryId(
            @Parameter(description = "ID of the category") @PathVariable UUID categoryId) {
        return ResponseEntity.ok(categoryAppMapper.toResponseTagList(categoryUseCase.getTagsByCategoryId(categoryId)));
    }

    @GetMapping("/categories-with-tags")
    @Operation(summary = "List categories with tags",
               description = "Returns all categories including their associated tags. Useful for rich UI displays where tags are shown grouped by category.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categories with tags retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryWithTagsResponse.class),
                examples = @ExampleObject(value = "[{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Sports\",\"tags\":[{\"id\":\"00000000-0000-0000-0000-000000000001\",\"name\":\"Basketball\",\"categoryId\":\"00000000-0000-0000-0000-000000000000\"}]}]"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Server error retrieving categories with tags.\",\"status\":500,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<List<CategoryWithTagsResponse>> getAllCategoriesWithTags() {
        return ResponseEntity.ok(categoryAppMapper.toResponseCategoryWithTags(categoryUseCase.getAllCategoriesWithTags()));
    }


    // -------------------- OTHER ENDPOINTS --------------------

    @PostMapping("/with-tags")
    @Operation(summary = "Create category with tags",
               description = "Creates a new category and associated tags in a single operation. Returns the created structure with tag ids.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category with tags created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryWithTagsResponse.class),
                examples = @ExampleObject(value = "{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Sports\",\"tags\":[{\"id\":\"00000000-0000-0000-0000-000000000001\",\"name\":\"Basketball\",\"categoryId\":\"00000000-0000-0000-0000-000000000000\"}]}"))),
        @ApiResponse(responseCode = "400", description = "Bad request: invalid payload or duplicate names",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\":\"Invalid request data.\",\"status\":400,\"timestamp\":\"2026-05-22T12:00:00\"}")))
    })
    public ResponseEntity<CategoryWithTagsResponse> createCategoryWithTags(@Valid @RequestBody CreateCategoryWithTagsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryAppMapper.toSingleResponseCategoryWithTags(categoryUseCase.createCategoryWithTags(request.getName(), request.getTagNames())));
    }

}
