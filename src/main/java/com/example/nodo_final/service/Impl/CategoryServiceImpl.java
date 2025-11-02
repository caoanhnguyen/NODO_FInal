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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
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

    @Override
    @Transactional(readOnly = true)
    public StreamingResponseBody exportCategories(CategorySearchReqDTO request) {

        // 1. Trả về "ống nước"
        return outputStream -> {

            // 2. Dùng SXSSF (Streaming)
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {

                Sheet sheet = workbook.createSheet("Categories");

                // 3. (Tạo Header Row)
                String[] HEADERS = { "ID", "Tên", "Mã", "Mô tả", "Ngày tạo", "Ngày sửa", "Người tạo", "Người sửa" };
                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < HEADERS.length; col++) {
                    headerRow.createCell(col).setCellValue(HEADERS[col]);
                }

                // 4. (Tạo Date Style)
                CellStyle dateCellStyle = workbook.createCellStyle();
                DataFormat dataFormat = workbook.createDataFormat();
                dateCellStyle.setDataFormat(dataFormat.getFormat("dd/MM/yyyy HH:mm:ss"));

                // 5. BẮT ĐẦU VÒNG LẶP (Logic 1000 bản ghi 1 lần)
                int page = 0;
                final int BATCH_SIZE = 1000;
                int currentRowIndex = 1;

                while (true) {
                    Pageable pageable = PageRequest.of(page, BATCH_SIZE);

                    // 6. (QUERY 1 - Lô 1k) Lấy 1000 Category (8 cột)
                    Page<Object[]> categoryPage = categoryRepository
                            .searchCategoriesForExportPaging(request, pageable); //

                    List<Object[]> categories = categoryPage.getContent();

                    if (categories.isEmpty()) {
                        break; // Hết dữ liệu -> Dừng lặp
                    }

                    // 7. (IN-MEMORY) Ghi 1000 dòng này vào Sheet
                    // (Category không cần "query lẻ" N+1, nên dễ hơn Product)
                    for (Object[] rowData : categories) {
                        Row row = sheet.createRow(currentRowIndex++);

                        // Loop qua 8 cột
                        for (int i = 0; i < HEADERS.length; i++) {
                            Cell cell = row.createCell(i);
                            Object value = rowData[i];

                            if (value instanceof Date) {
                                cell.setCellValue((Date) value);
                                cell.setCellStyle(dateCellStyle);
                            } else if (value != null) {
                                cell.setCellValue(value.toString());
                            } else {
                                cell.setCellValue("");
                            }
                        }
                    }

                    page++; // Sang trang tiếp theo
                }

                // 8. Ghi Workbook (file tạm) ra "ống nước"
                workbook.write(outputStream);

                // 9. Dọn dẹp
                workbook.close();
                workbook.dispose(); // Xóa file tạm

            } catch (IOException e) {
                throw new RuntimeException("Lỗi streaming file Excel", e);
            }
        };
    }
}
