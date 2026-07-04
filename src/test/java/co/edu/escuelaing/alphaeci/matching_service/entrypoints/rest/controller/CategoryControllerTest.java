package co.edu.escuelaing.alphaeci.matching_service.entrypoints.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateCategoryRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.request.CreateTagRequest;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.CategoryResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.CategoryWithTagsResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.dto.response.TagResponse;
import co.edu.escuelaing.alphaeci.matching_service.application.mapper.CategoryAppMapper;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.in.CategoryUseCasePort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;
import co.edu.escuelaing.alphaeci.matching_service.entrypoints.advice.GlobalExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryUseCasePort categoryUseCase;

    @Mock
    private CategoryAppMapper categoryAppMapper;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID categoryId;
    private UUID tagId;
    private Category category;
    private Tag tag;
    private CategoryResponse categoryResponse;
    private TagResponse tagResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        categoryId = UUID.randomUUID();
        tagId = UUID.randomUUID();
        category = new Category("Technology");
        category.setId(categoryId);
        tag = new Tag("Java", categoryId);
        tag.setId(tagId);

        categoryResponse = new CategoryResponse(categoryId, "technology");
        tagResponse = new TagResponse(tagId, "java", categoryId);
    }

    // ======================== CATEGORY ENDPOINTS ========================

    @Test
    void createCategory_success_returns201() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Technology");

        when(categoryUseCase.createCategory("Technology")).thenReturn(category);
        when(categoryAppMapper.toResponse(category)).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("technology"));
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_alreadyExists_returns400() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Technology");

        when(categoryUseCase.createCategory("Technology"))
                .thenThrow(new InvalidInputException("Category already exists"));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCategory_success_returns200() throws Exception {
        when(categoryUseCase.getCategory(categoryId)).thenReturn(category);
        when(categoryAppMapper.toResponse(category)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/categories/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()));
    }

    @Test
    void getCategory_notFound_returns404() throws Exception {
        when(categoryUseCase.getCategory(categoryId))
                .thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(get("/api/v1/categories/{categoryId}", categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCategories_returns200() throws Exception {
        when(categoryUseCase.getAllCategories()).thenReturn(List.of(category));
        when(categoryAppMapper.toResponseCategoryList(List.of(category)))
                .thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/v1/categories/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()));
    }

    @Test
    void deleteCategory_success_returns204() throws Exception {
        doNothing().when(categoryUseCase).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_notFound_returns404() throws Exception {
        doThrow(new NotFoundException("Category not found"))
                .when(categoryUseCase).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryId))
                .andExpect(status().isNotFound());
    }

    // ======================== TAG ENDPOINTS ========================

    @Test
    void createTag_success_returns201() throws Exception {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Java");
        request.setCategoryId(categoryId);

        when(categoryUseCase.createTag("Java", categoryId)).thenReturn(tag);
        when(categoryAppMapper.toResponse(tag)).thenReturn(tagResponse);

        mockMvc.perform(post("/api/v1/categories/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tagId.toString()));
    }

    @Test
    void createTag_blankName_returns400() throws Exception {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("");
        request.setCategoryId(categoryId);

        mockMvc.perform(post("/api/v1/categories/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTag_nullCategoryId_returns400() throws Exception {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Java");
        request.setCategoryId(null);

        mockMvc.perform(post("/api/v1/categories/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTag_success_returns200() throws Exception {
        when(categoryUseCase.getTag(tagId)).thenReturn(tag);
        when(categoryAppMapper.toResponse(tag)).thenReturn(tagResponse);

        mockMvc.perform(get("/api/v1/categories/tags/{tagId}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tagId.toString()));
    }

    @Test
    void getTag_notFound_returns404() throws Exception {
        when(categoryUseCase.getTag(tagId))
                .thenThrow(new NotFoundException("Tag not found"));

        mockMvc.perform(get("/api/v1/categories/tags/{tagId}", tagId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTagsByCategoryId_success_returns200() throws Exception {
        when(categoryUseCase.getTagsByCategoryId(categoryId)).thenReturn(List.of(tag));
        when(categoryAppMapper.toResponseTagList(List.of(tag))).thenReturn(List.of(tagResponse));

        mockMvc.perform(get("/api/v1/categories/{categoryId}/tags", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tagId.toString()));
    }

    @Test
    void deleteTag_success_returns204() throws Exception {
        doNothing().when(categoryUseCase).deleteTag(tagId);

        mockMvc.perform(delete("/api/v1/categories/tags/{tagId}", tagId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTag_notFound_returns404() throws Exception {
        doThrow(new NotFoundException("Tag not found"))
                .when(categoryUseCase).deleteTag(tagId);

        mockMvc.perform(delete("/api/v1/categories/tags/{tagId}", tagId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTags_returns200() throws Exception {
        when(categoryUseCase.getAllTags()).thenReturn(List.of(tag));
        when(categoryAppMapper.toResponseTagList(List.of(tag))).thenReturn(List.of(tagResponse));

        mockMvc.perform(get("/api/v1/categories/tags/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tagId.toString()));
    }

    @Test
    void getAllCategoriesWithTags_returns200() throws Exception {
        Map<Category, List<Tag>> map = new LinkedHashMap<>();
        map.put(category, List.of(tag));
        CategoryWithTagsResponse response = new CategoryWithTagsResponse(categoryId, "technology", List.of(tagResponse));

        when(categoryUseCase.getAllCategoriesWithTags()).thenReturn(map);
        when(categoryAppMapper.toResponseCategoryWithTags(map)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/categories/categories-with-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()));
    }
}
