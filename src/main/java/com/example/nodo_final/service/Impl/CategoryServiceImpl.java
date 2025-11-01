package com.example.nodo_final.service.Impl;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.request.CategorySearchReqDTO;
import com.example.nodo_final.dto.response.*;
import com.example.nodo_final.entity.Category;
import com.example.nodo_final.entity.Resource;
import com.example.nodo_final.enums.Status;
import com.example.nodo_final.exception.ResourceNotFoundException;
import com.example.nodo_final.mapper.CategoryMapper;
import com.example.nodo_final.mapper.ResourceMapper;
import com.example.nodo_final.repository.CategoryRepository;
import com.example.nodo_final.repository.ResourceRepository;
import com.example.nodo_final.service.CategoryService;
import com.example.nodo_final.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;
    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;


    @Override
    @Transactional
    public ResponseData<?> createCategory(CategoryRequestDTO dto, List<MultipartFile> files, Locale locale) {
        if (categoryRepository.existsByCategoryCode(dto.getCategoryCode())) {
            return ResponseData.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(messageSource.getMessage("category.code.duplicate", null, locale))
                    .data(null)
                    .build();
        }

        Category category = categoryMapper.toEntity(dto);
        category = categoryRepository.save(category);

        // Save files
        if(files != null && !files.isEmpty()) {
            for(MultipartFile file : files) {
                if(file != null && !file.isEmpty()) {
                    Resource resource = fileStorageService.save(file);
                    resource.setCategory(category);
                }
            }
        }

        return ResponseData.builder()
                .status(HttpStatus.CREATED.value())
                .message("Category created successfully")
                .data("id: " + category.getId())
                .build();
    }

    @Override
    public PageResponse<?> getAllCategories(CategorySearchReqDTO dto, Pageable pageable, Locale locale) {
        Page<Object[]> page = categoryRepository.searchCategories(dto, pageable);
        List<Object[]> results = page.getContent();

        if(results.isEmpty()) {
            return PageResponse.builder()
                    .data(Collections.emptyList())
                    .currentPage(page.getNumber())
                    .pageSize(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .build();
        }


//        // Map kết quả sang CategorySearchResDTO
        List<CategorySearchResDTO> responses = new ArrayList<>();
        for (Object[] row : results) {
            CategorySearchResDTO response = new CategorySearchResDTO();
            response.setId(((Number) row[0]).longValue());
            response.setName((String) row[1]);
            response.setCategoryCode((String) row[2]);
            response.setDescription((String) row[3]);
            response.setCreatedDate((Date) row[4]);
            response.setModifiedDate((Date) row[5]);
            responses.add(response);
        }

        // 3. CHUẨN BỊ CHỐNG N+1
        Set<Long> categoryIds = responses.stream()
                .map(CategorySearchResDTO::getId)
                .collect(Collectors.toSet());

        // 4. (QUERY 2 - "Query Lẻ") Lấy TẤT CẢ ảnh
        List<Resource> images = resourceRepository.findByCategory_IdInAndStatus(categoryIds, Status.ACTIVE);

        // 5. (IN-MEMORY) Group ảnh theo categoryId (Rất nhanh)
        Map<Long, List<Resource>> imageMap = images.stream()
                .collect(Collectors.groupingBy(img -> img.getCategory().getId()));

        // 6. (IN-MEMORY) Map ảnh vào DTO ("Stitch" - May vá)
        for (CategorySearchResDTO categoryDTOs : responses) {
            List<Resource> categoryImages = imageMap.getOrDefault(categoryDTOs.getId(), Collections.emptyList());

            // Dùng mapper (Giả sử bạn có ResourceMapper)
            categoryDTOs.setImages(
                    categoryImages.stream()
                            .map(resourceMapper::toResponseDto)
                            .collect(Collectors.toList())
            );
        }

        return PageResponse.builder()
                .data(responses)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public ResponseData<?> updateCategory(Long id, CategoryRequestDTO dto, List<MultipartFile> files, List<Long> deleteIds, Locale locale) {
        Category category = categoryRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String newCategoryCode = dto.getCategoryCode();

        // Kiểm tra category_code có bị trùng không
        if(newCategoryCode != null && !newCategoryCode.isEmpty()) {
            Optional<Category> optionalCategory = categoryRepository.findByCategoryCode(newCategoryCode);
            if (optionalCategory.isPresent() && !optionalCategory.get().getId().equals(id)) {
                return ResponseData.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(messageSource.getMessage("category.code.duplicate", null, locale))
                        .data(null)
                        .build();
            }
        }

        // Update thông tin category
        categoryMapper.updateEntityFromDto(dto, category);

        // Xóa ảnh theo deleteIds
        if (deleteIds != null && !deleteIds.isEmpty()) {
            List<Resource> resources = resourceRepository.findResourcesForDelete(deleteIds, category.getId(), Status.ACTIVE);
            for(Resource resource : resources) {
                resource.setStatus(Status.DELETE);
            }

            resourceRepository.saveAll(resources);
        }

        // Save files
        if(files != null && !files.isEmpty()) {
            for(MultipartFile file : files) {
                if(file != null && !file.isEmpty()) {
                    Resource resource = fileStorageService.save(file);
                    resource.setCategory(category);
                    resourceRepository.save(resource);
                }
            }
        }

        categoryRepository.save(category);

        CategoryDetailResDTO detail = categoryMapper.toDetailDto(category);

        List<ResourceResponseDTO> activeImages = detail.getImages().stream()
                .filter(img -> img.getStatus().equals(Status.ACTIVE.name()))
                .toList();
        detail.setImages(activeImages);

        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message(messageSource.getMessage("category.update.success", null, locale))
                .data(detail)
                .build();
    }

    @Override
    @Transactional
    public ResponseData<?> softDelete(Long id, Locale locale) {
        Category category = categoryRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setStatus(Status.DELETE);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message(messageSource.getMessage("category.delete.success", null, locale))
                .data(null)
                .build();
    }
}
