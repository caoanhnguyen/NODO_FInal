package com.example.nodo_final.service.Impl;

import com.example.nodo_final.dto.request.ProductRequestDTO;
import com.example.nodo_final.dto.request.UpdateProductReqDTO;
import com.example.nodo_final.dto.response.ProductResponseDTO;
import com.example.nodo_final.dto.response.ResponseData;
import com.example.nodo_final.entity.Category;
import com.example.nodo_final.entity.Product;
import com.example.nodo_final.entity.ProductCategory;
import com.example.nodo_final.entity.Resource;
import com.example.nodo_final.enums.Status;
import com.example.nodo_final.exception.ResourceNotFoundException;
import com.example.nodo_final.mapper.ProductMapper;
import com.example.nodo_final.repository.CategoryRepository;
import com.example.nodo_final.repository.ProductCategoryRepository;
import com.example.nodo_final.repository.ProductRepository;
import com.example.nodo_final.repository.ResourceRepository;
import com.example.nodo_final.service.FileStorageService;
import com.example.nodo_final.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MessageSource messageSource;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ResourceRepository resourceRepository;
    private final FileStorageService fileStorageService;
    private final ProductCategoryRepository productCategoryRepository;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseData<?> createProduct(ProductRequestDTO dto, List<MultipartFile> files, Locale locale) {
        // Kiểm tra product_code trùng
        if (productRepository.existsByProductCode(dto.getProductCode())) {
            return ResponseData.builder()
                    .status(400)
                    .message(messageSource.getMessage("product.code.duplicate", null, locale))
                    .data(null)
                    .build();
        }

        // Kiểm tra list category_ids có tồn tại không
        List<Category> categories = categoryRepository.findAllByIdInAndStatus(dto.getCategoryIds(), Status.ACTIVE);
        if (categories.size() != dto.getCategoryIds().size()) {
            return ResponseData.builder()
                    .status(400)
                    .message("1 hoac nhiều danh mục không tồn tại")
                    .data(null)
                    .build();
        }

        // Map DTO qua entity
        Product product = productMapper.toEntity(dto);
        productRepository.save(product);

        // Lưu danh mục cho sản phẩm
//        productCategoryRepository.saveAllByProductAndCategoryIn(product, categories);
        List<ProductCategory> productCategories = new ArrayList<>();
        for (Category category : categories) {
            ProductCategory pc = new ProductCategory();
            pc.setProduct(product);
            pc.setCategory(category);
            pc.setStatus(Status.ACTIVE);
            productCategories.add(pc);
        }

        productCategoryRepository.saveAll(productCategories);

        // Lưu file và tài nguyên
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    Resource resource = fileStorageService.save(file);
                    resource.setProduct(product);
                    resourceRepository.save(resource);
                }
            }
        }

        return ResponseData.builder()
                .status(201)
                .message("Product created successfully")
                .data(null)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseData<?> updateProduct(Long id, UpdateProductReqDTO dto, List<MultipartFile> files, Locale locale) {

        // Kiểm tra product có tồn tại không
        Product existingProduct = productRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("product.notfound", null, locale)
                ));

        // Kiểm tra product_code trùng
        if(!validateProductCode(dto.getProductCode(), id)) {
            return ResponseData.builder()
                    .status(400)
                    .message(messageSource.getMessage("product.code.duplicate", null, locale))
                    .data(null)
                    .build();
        }

        // Xử lý cập nhật danh sách category
        handleUpdateCategories(existingProduct, dto.getCategoryIds());

        // Lưu file và tài nguyên
        handleUpdateResources(existingProduct, files, dto.getDeletedIds());

        // Cập nhật thông tin product
        existingProduct = productMapper.updateEntityFromDto(dto, existingProduct);

        // Lưu thay đổi và trả về
        productRepository.save(existingProduct);

        return ResponseData.builder()
                .status(200)
                .message(messageSource.getMessage("product.update.success", null, locale))
                .data(toDto(existingProduct))
                .build();
    }

    @Override
    @Transactional
    public void softDelete(Long id, Locale locale) {
        Product product = productRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("product.notfound", null, locale)
                ));
        product.setStatus(Status.DELETE);
    }


    // Kiểm tra product_code có bị trùng không
    private boolean validateProductCode(String productCode, Long id) {
        if(productCode != null && !productCode.isEmpty()) {
            Optional<Product> product = productRepository.findByProductCode(productCode);
            return product.isEmpty() || product.get().getId().equals(id);
        }
        return true;
    }

    // Xử lý cập nhật danh sách category
    private void handleUpdateCategories(Product product, List<Long> newCategoryIds) {

        Set<Long> newIds = Set.copyOf(newCategoryIds);

        // Danh sách category hiện tại của product trong DB
        Map<Long, ProductCategory> currentCategory = productCategoryRepository.findByProduct_Id((product.getId()))
                .stream()
                .collect(Collectors.toMap(pc -> pc.getCategory().getId(), pc -> pc));

        // Cần 1 list để lưu các ProductCategory mới, 1 list để lưu các cate cần reactive, 1 list để lưu các cate cần soft delete
        List<ProductCategory> reactive = new ArrayList<>();
        List<ProductCategory> deactive = new ArrayList<>();
        Set<Long> toInsert = new HashSet<>(Set.copyOf(newIds));

        // Duyệt qua các category hiện tại
        for (Map.Entry<Long, ProductCategory> entry : currentCategory.entrySet()) {
            Long categoryId = entry.getKey();
            ProductCategory productCategory = entry.getValue();

            if (newIds.contains(categoryId)) {
                // Nếu category hiện tại có trong danh sách mới và đang bị xóa mềm, thì cần reactive
                if (productCategory.getStatus() == Status.DELETE) {
                    productCategory.setStatus(Status.ACTIVE);
                    reactive.add(productCategory);
                }
                // Đã xử lý, không cần insert nữa
                toInsert.remove(categoryId);
            } else {
                // Nếu category hiện tại không có trong danh sách mới, cần xóa mềm
                if (productCategory.getStatus() == Status.ACTIVE) {
                    productCategory.setStatus(Status.DELETE);
                    deactive.add(productCategory);
                }
            }
        }

        // Đến đây đã lấy được các cate cần reactive, deactive và các cate cần insert mới

        // Insert mới
        if(!toInsert.isEmpty()) {
            // Validate các categoryId có tồn tại không
            List<Category> categoriesToAdd = categoryRepository.findAllByIdInAndStatus(new ArrayList<>(toInsert), Status.ACTIVE);

            // Nếu số lượng category trả về không đúng bằng số lượng cần thêm, có nghĩa là có category không tồn tại
            if(categoriesToAdd.size() != toInsert.size()) {
                throw new ResourceNotFoundException("1 hoac nhieu danh muc khong ton tai");
            }

            List<ProductCategory> toInsertList = categoriesToAdd.stream()
                    .map(category -> {
                        ProductCategory pc = new ProductCategory();
                        pc.setProduct(product);
                        pc.setCategory(category);
                        pc.setStatus(Status.ACTIVE);
                        return pc;
                    })
                    .toList();

            // Lưu các category mới
            productCategoryRepository.saveAll(toInsertList);
        }

        // Reactive các category
        productCategoryRepository.saveAll(reactive);
        // Deactive các category
        productCategoryRepository.saveAll(deactive);
        productCategoryRepository.flush();
    }

    // Xử lý lưu ảnh mới và xóa ảnh theo deletedIds
    private void handleUpdateResources(Product product, List<MultipartFile> files, List<Long> deletedIds) {
        // Xóa ảnh theo deletedIds
        if (deletedIds != null && !deletedIds.isEmpty()) {
            List<Resource> resources = resourceRepository.findResourcesForDeleteByProductId(deletedIds, product.getId(), Status.ACTIVE);
            for(Resource resource : resources) {
                resource.setStatus(Status.DELETE);
            }

            resourceRepository.saveAll(resources);
        }

        // Lưu file và tài nguyên mới
        if(files != null && !files.isEmpty()) {
            for(MultipartFile file : files) {
                if(file != null && !file.isEmpty()) {
                    Resource resource = fileStorageService.save(file);
                    resource.setProduct(product);
                    resourceRepository.save(resource);
                }
            }
        }
        resourceRepository.flush();
    }

    // Map sang ProductResponseDTO
    private ProductResponseDTO toDto(Product product) {
        // Lấy danh sách category của product
        List<Category> categories = productCategoryRepository.findLinksByProductIdAndStatus(product.getId(), Status.ACTIVE)
                .stream()
                .map(ProductCategory::getCategory)
                .collect(Collectors.toList());

        List<Resource> images = resourceRepository
                .findByProductAndStatus(product, Status.ACTIVE);

        return productMapper.toDto(product, categories, images);
    }
}
