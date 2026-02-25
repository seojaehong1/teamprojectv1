package com.du.adminservice.controller;

import com.du.adminservice.model.Product;
import com.du.adminservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public AdminProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 상품 목록 조회 (페이징, 검색, 필터)
    @GetMapping
    public ResponseEntity<?> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        try {
            // URL 디코딩 처리 (한글 검색어 지원)
            String decodedKeyword = null;
            if (keyword != null && !keyword.trim().isEmpty()) {
                decodedKeyword = URLDecoder.decode(keyword.trim(), StandardCharsets.UTF_8);
            }

            System.out.println("[AdminProductController] keyword (원본): " + keyword + ", (디코딩): " + decodedKeyword);

            // 페이지 크기 제한 (최대 50)
            if (size > 50) size = 50;
            if (size < 1) size = 10;

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Product> productPage;

            if (category != null && !category.trim().isEmpty()) {
                if (decodedKeyword != null && !decodedKeyword.isEmpty()) {
                    productPage = productRepository.searchProductsByCategory(category, decodedKeyword, pageable);
                } else {
                    productPage = productRepository.findByCategory(category, pageable);
                }
            } else {
                if (decodedKeyword != null && !decodedKeyword.isEmpty()) {
                    productPage = productRepository.searchProducts(decodedKeyword, pageable);
                } else {
                    productPage = productRepository.findAll(pageable);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("products", productPage.getContent());
            response.put("currentPage", productPage.getNumber());
            response.put("totalPages", productPage.getTotalPages());
            response.put("totalItems", productPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("상품 목록 조회 중 오류가 발생했습니다.");
        }
    }

    // 단일 상품 조회
    @GetMapping("/{menuCode}")
    public ResponseEntity<?> getProductByMenuCode(@PathVariable String menuCode) {
        try {
            Product product = productRepository.findById(menuCode)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

            return ResponseEntity.ok(product);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("상품 조회 중 오류가 발생했습니다.");
        }
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestParam String menuCode,
            @RequestParam String menuName,
            @RequestParam BigDecimal basePrice,
            @RequestParam String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile image) {
        try {
            // 상품 코드 중복 확인
            if (productRepository.existsById(menuCode)) {
                return ResponseEntity.badRequest().body("이미 존재하는 상품 코드입니다.");
            }

            // 유효성 검사
            if (menuCode == null || menuCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("상품 코드를 입력해주세요.");
            }

            if (menuName == null || menuName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("상품명을 입력해주세요.");
            }

            String productName = menuName.trim();
            if (productName.length() < 2 || productName.length() > 100) {
                return ResponseEntity.badRequest().body("상품명은 2~100자 이내로 입력해주세요.");
            }

            if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("가격은 0보다 커야 합니다.");
            }

            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("카테고리를 입력해주세요.");
            }

            // 설명 길이 검증
            if (description != null && description.trim().length() > 500) {
                return ResponseEntity.badRequest().body("설명은 500자 이내로 입력해주세요.");
            }

            // 이미지 검증
            if (image != null && !image.isEmpty()) {
                // 파일 크기 검증 (5MB)
                if (image.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body("이미지 파일은 5MB 이하만 업로드 가능합니다.");
                }

                // 파일 형식 검증
                String contentType = image.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") &&
                    !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
                    return ResponseEntity.badRequest().body("이미지는 jpg, png, webp 형식만 업로드 가능합니다.");
                }
            }

            Product product = new Product();
            product.setMenuCode(menuCode.trim());
            product.setMenuName(productName);
            product.setBasePrice(basePrice);
            product.setCategory(category.trim());
            product.setDescription(description != null ? description.trim() : null);
            product.setIsAvailable(true);

            // 이미지 업로드 처리
            if (image != null && !image.isEmpty()) {
                String imageUrl = saveImage(image);
                product.setImageUrl(imageUrl);
            }

            productRepository.save(product);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "상품이 등록되었습니다.");
            response.put("menuCode", product.getMenuCode());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("상품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 상품 수정
    @PutMapping("/{menuCode}")
    public ResponseEntity<?> updateProduct(
            @PathVariable String menuCode,
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false) BigDecimal basePrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) MultipartFile image) {
        try {
            Product product = productRepository.findById(menuCode)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

            if (menuName != null && !menuName.trim().isEmpty()) {
                String productName = menuName.trim();
                if (productName.length() < 2 || productName.length() > 100) {
                    return ResponseEntity.badRequest().body("상품명은 2~100자 이내로 입력해주세요.");
                }
                product.setMenuName(productName);
            }

            if (basePrice != null) {
                if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest().body("가격은 0보다 커야 합니다.");
                }
                product.setBasePrice(basePrice);
            }

            if (category != null && !category.trim().isEmpty()) {
                product.setCategory(category.trim());
            }

            if (description != null) {
                if (description.trim().length() > 500) {
                    return ResponseEntity.badRequest().body("설명은 500자 이내로 입력해주세요.");
                }
                product.setDescription(description.trim());
            }

            if (isAvailable != null) {
                product.setIsAvailable(isAvailable);
            }

            // 이미지 검증 및 업로드 처리
            if (image != null && !image.isEmpty()) {
                // 파일 크기 검증 (5MB)
                if (image.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body("이미지 파일은 5MB 이하만 업로드 가능합니다.");
                }

                // 파일 형식 검증
                String contentType = image.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") &&
                    !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
                    return ResponseEntity.badRequest().body("이미지는 jpg, png, webp 형식만 업로드 가능합니다.");
                }

                // 기존 이미지 삭제
                if (product.getImageUrl() != null) {
                    deleteImage(product.getImageUrl());
                }
                String imageUrl = saveImage(image);
                product.setImageUrl(imageUrl);
            }

            productRepository.save(product);

            Map<String, String> response = new HashMap<>();
            response.put("message", "상품이 수정되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("상품 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 상품 삭제
    @DeleteMapping("/{menuCode}")
    public ResponseEntity<?> deleteProduct(@PathVariable String menuCode) {
        try {
            Product product = productRepository.findById(menuCode)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

            // 이미지 삭제
            if (product.getImageUrl() != null) {
                deleteImage(product.getImageUrl());
            }

            productRepository.delete(product);

            Map<String, String> response = new HashMap<>();
            response.put("message", "상품이 삭제되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("상품 삭제 중 오류가 발생했습니다.");
        }
    }

    // 이미지 저장 메서드
    private String saveImage(MultipartFile file) throws IOException {
        // 업로드 디렉토리 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 파일명 생성 (UUID + 원본 파일 확장자)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString() + extension;

        // 파일 저장
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        // 저장된 파일 경로 반환
        return "/uploads/products/" + filename;
    }

    // 이미지 삭제 메서드
    private void deleteImage(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.startsWith("/uploads/products/")) {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir, filename);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // 이미지 삭제 실패는 무시
        }
    }
}
