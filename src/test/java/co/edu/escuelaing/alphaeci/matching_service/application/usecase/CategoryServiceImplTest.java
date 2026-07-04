package co.edu.escuelaing.alphaeci.matching_service.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;
import co.edu.escuelaing.alphaeci.matching_service.domain.ports.out.CategoryRepositoryPort;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Category;
import co.edu.escuelaing.alphaeci.matching_service.domain.valueobjects.Tag;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID categoryId;
    private UUID tagId;
    private Category category;
    private Tag tag;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        tagId = UUID.randomUUID();
        category = new Category("Technology");
        category.setId(categoryId);
        tag = new Tag("Java", categoryId);
        tag.setId(tagId);
    }

    // ======================== CATEGORY TESTS ========================

    @Test
    void createCategory_success() {
        when(categoryRepository.existsByName("technology")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.createCategory("Technology");

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_alreadyExists_throwsInvalidInputException() {
        when(categoryRepository.existsByName("technology")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory("Technology"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Category already exists");
    }

    @Test
    void getCategory_success() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategory(categoryId);

        assertThat(result).isEqualTo(category);
    }

    @Test
    void getCategory_notFound_throwsNotFoundException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void getAllCategories_returnsList() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(category);
    }

    @Test
    void updateCategory_success_sameNormalizedName() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // same name — no existsByName check
        Category result = categoryService.updateCategory(categoryId, "Technology");

        assertThat(result).isNotNull();
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_success_differentName() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("science")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.updateCategory(categoryId, "Science");

        assertThat(result).isNotNull();
        verify(categoryRepository).existsByName("science");
    }

    @Test
    void updateCategory_categoryNotFound_throwsNotFoundException() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, "NewName"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateCategory_newNameAlreadyExists_throwsInvalidInputException() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("science")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, "Science"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void deleteCategory_success() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCategory_notFound_throwsNotFoundException() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllTags_returnsList() {
        when(categoryRepository.findAllTags()).thenReturn(List.of(tag));

        List<Tag> result = categoryService.getAllTags();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllCategoriesWithTags_returnsMap() {
        Map<Category, List<Tag>> map = new HashMap<>();
        map.put(category, List.of(tag));
        when(categoryRepository.findAllCategoriesWithTags()).thenReturn(map);

        Map<Category, List<Tag>> result = categoryService.getAllCategoriesWithTags();

        assertThat(result).hasSize(1);
        assertThat(result.get(category)).contains(tag);
    }

    // ======================== TAG TESTS ========================

    @Test
    void createTag_success() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.existsTagByName("java")).thenReturn(false);
        when(categoryRepository.save(any(Tag.class))).thenReturn(tag);

        Tag result = categoryService.createTag("Java", categoryId);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Tag.class));
    }

    @Test
    void createTag_categoryNotFound_throwsNotFoundException() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.createTag("Java", categoryId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createTag_tagAlreadyExists_throwsInvalidInputException() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.existsTagByName("java")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createTag("Java", categoryId))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Tag already exists");
    }

    @Test
    void getTag_success() {
        when(categoryRepository.findTagById(tagId)).thenReturn(Optional.of(tag));

        Tag result = categoryService.getTag(tagId);

        assertThat(result).isEqualTo(tag);
    }

    @Test
    void getTag_notFound_throwsNotFoundException() {
        when(categoryRepository.findTagById(tagId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getTag(tagId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Tag not found");
    }

    @Test
    void getTagsByCategoryId_success() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findTagsByCategoryId(categoryId)).thenReturn(List.of(tag));

        List<Tag> result = categoryService.getTagsByCategoryId(categoryId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTagsByCategoryId_categoryNotFound_throwsNotFoundException() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.getTagsByCategoryId(categoryId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateTag_success_sameName() {
        when(categoryRepository.existsTagById(tagId)).thenReturn(true);
        when(categoryRepository.findTagById(tagId)).thenReturn(Optional.of(tag));
        when(categoryRepository.save(any(Tag.class))).thenReturn(tag);

        Tag result = categoryService.updateTag(tagId, "Java");

        assertThat(result).isNotNull();
        verify(categoryRepository).save(tag);
    }

    @Test
    void updateTag_success_differentName() {
        when(categoryRepository.existsTagById(tagId)).thenReturn(true);
        when(categoryRepository.findTagById(tagId)).thenReturn(Optional.of(tag));
        when(categoryRepository.existsTagByName("python")).thenReturn(false);
        when(categoryRepository.save(any(Tag.class))).thenReturn(tag);

        Tag result = categoryService.updateTag(tagId, "Python");

        assertThat(result).isNotNull();
        verify(categoryRepository).existsTagByName("python");
    }

    @Test
    void updateTag_notFound_throwsNotFoundException() {
        when(categoryRepository.existsTagById(tagId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.updateTag(tagId, "Python"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateTag_newNameAlreadyExists_throwsInvalidInputException() {
        when(categoryRepository.existsTagById(tagId)).thenReturn(true);
        when(categoryRepository.findTagById(tagId)).thenReturn(Optional.of(tag));
        when(categoryRepository.existsTagByName("python")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateTag(tagId, "Python"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void deleteTag_success() {
        when(categoryRepository.existsTagById(tagId)).thenReturn(true);

        categoryService.deleteTag(tagId);

        verify(categoryRepository).deleteTagById(tagId);
    }

    @Test
    void deleteTag_notFound_throwsNotFoundException() {
        when(categoryRepository.existsTagById(tagId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteTag(tagId))
                .isInstanceOf(NotFoundException.class);
    }
}
