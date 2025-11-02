package com.example.nodo_final.service.Impl;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.request.CategorySearchReqDTO;
import com.example.nodo_final.dto.request.UpdateCategoryReqDTO;
import com.example.nodo_final.dto.response.*;
import com.example.nodo_final.entity.Category;
import com.example.nodo_final.entity.Resource;
import com.example.nodo_final.enums.Status;
import com.example.nodo_final.exception.DuplicateCodeException;
import com.example.nodo_final.exception.ResourceNotFoundException;
import com.example.nodo_final.mapper.CategoryMapper;
import com.example.nodo_final.mapper.ResourceMapper;
import com.example.nodo_final.repository.CategoryRepository;
import com.example.nodo_final.repository.ResourceRepository;
import com.example.nodo_final.service.CategoryService;
import com.example.nodo_final.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Long createCategory(CategoryRequestDTO dto, List<MultipartFile> files) {
        if (categoryRepository.existsByCategoryCode(dto.getCategoryCode())) {
            throw new DuplicateCodeException("category.code.duplicate");
        }

        Category category = categoryMapper.toEntity(dto);
        category = categoryRepository.save(category);

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

        return category.getId();
    }

    @Override
    public PageResponse<?> getAllCategories(CategorySearchReqDTO dto, Pageable pageable, Locale locale) throws BadRequestException {

        if (dto.getCreatedFrom() != null && dto.getCreatedTo() != null &&
                dto.getCreatedFrom().after(dto.getCreatedTo())) {
            throw new BadRequestException("common.date.range_invalid");
        }

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

        Set<Long> categoryIds = responses.stream()
                .map(CategorySearchResDTO::getId)
                .collect(Collectors.toSet());

        List<Resource> images = resourceRepository.findByCategory_IdInAndStatus(categoryIds, Status.ACTIVE);

        Map<Long, List<Resource>> imageMap = images.stream()
                .collect(Collectors.groupingBy(img -> img.getCategory().getId()));

        for (CategorySearchResDTO categoryDTOs : responses) {
            List<Resource> categoryImages = imageMap.getOrDefault(categoryDTOs.getId(), Collections.emptyList());

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
    @Transactional(rollbackFor = Exception.class)
    public CategoryDetailResDTO updateCategory(Long id, UpdateCategoryReqDTO dto, List<MultipartFile> files, List<Long> deleteIds) {
        Category category = categoryRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("category.notfound"));

        String newCategoryCode = dto.getCategoryCode();

        // Kiểm tra category_code có bị trùng không
        if(newCategoryCode != null && !newCategoryCode.isEmpty()) {
            Optional<Category> optionalCategory = categoryRepository.findByCategoryCode(newCategoryCode);
            if (optionalCategory.isPresent() && !optionalCategory.get().getId().equals(id)) {
                throw new DuplicateCodeException("category.code.duplicate");
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

        List<Resource> activeImages = resourceRepository
                .findByCategoryIdAndStatus(category.getId(), Status.ACTIVE);

        return categoryMapper.toDetailDto(category, activeImages);
    }

    @Override
    @Transactional
    public void softDelete(Long id, Locale locale) {
        Category category = categoryRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("category.notfound"));
        category.setStatus(Status.DELETE);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public StreamingResponseBody exportCategories(CategorySearchReqDTO request) throws BadRequestException {

        if (request.getCreatedFrom() != null && request.getCreatedTo() != null &&
                request.getCreatedFrom().after(request.getCreatedTo())) {
            throw new BadRequestException("common.date.range_invalid");
        }

        return outputStream -> {

            try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {

                Sheet sheet = workbook.createSheet("Categories");

                String[] HEADERS = { "ID", "Tên", "Mã", "Mô tả", "Ngày tạo", "Ngày sửa", "Người tạo", "Người sửa" };
                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < HEADERS.length; col++) {
                    headerRow.createCell(col).setCellValue(HEADERS[col]);
                }

                CellStyle dateCellStyle = workbook.createCellStyle();
                DataFormat dataFormat = workbook.createDataFormat();
                dateCellStyle.setDataFormat(dataFormat.getFormat("dd/MM/yyyy HH:mm:ss"));

                int page = 0;
                final int BATCH_SIZE = 1000;
                int currentRowIndex = 1;

                while (true) {
                    Pageable pageable = PageRequest.of(page, BATCH_SIZE);

                    Page<Object[]> categoryPage = categoryRepository
                            .searchCategoriesForExportPaging(request, pageable); //

                    List<Object[]> categories = categoryPage.getContent();

                    if (categories.isEmpty()) {
                        break;
                    }

                    for (Object[] rowData : categories) {
                        Row row = sheet.createRow(currentRowIndex++);

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

                    page++;
                }

                workbook.write(outputStream);

                workbook.close();
                workbook.dispose();

            } catch (IOException e) {
                throw new RuntimeException("Lỗi streaming file Excel", e);
            }
        };
    }
}
