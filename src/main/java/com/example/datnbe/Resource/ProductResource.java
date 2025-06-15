package com.example.datnbe.Resource;

import com.example.datnbe.Entity.Criteria.ProductCriteria;
import com.example.datnbe.Entity.DTO.ProductCategoryStatisticDTO;
import com.example.datnbe.Entity.DTO.ProductsDTO;
import com.example.datnbe.Service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductResource {

    @Autowired
    private ProductService productService;

    @PostMapping("/search-product")
    public ResponseEntity<List<ProductsDTO>> searchProduct(@RequestBody ProductCriteria criteria, Pageable pageable) {
        Page<ProductsDTO> page = productService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/get-all-product")
    public ResponseEntity<List<ProductsDTO>> getAllProduct() {
        return ResponseEntity.ok(productService.getAllProduct());
    }

    @PostMapping("/get-product-by-id")
    public ResponseEntity<ProductsDTO> getProductById(@RequestParam String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/create-product")
    public ResponseEntity<ProductsDTO> createProduct(@RequestBody ProductsDTO dto) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @PostMapping("/update-product")
    public ResponseEntity<ProductsDTO> updateProduct(@RequestBody ProductsDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(dto));
    }

    @PostMapping("/update-product-with-image")
    public ResponseEntity<ProductsDTO> updateProductWithImage(
            @RequestParam("product") String productJson,
            @RequestParam(value = "imageUrl", required = false) MultipartFile imageUrl,
            @RequestParam(value = "imageDetail", required = false) List<MultipartFile> imageDetailList
    ) throws IOException {

        ProductsDTO dto = new ObjectMapper().readValue(productJson, ProductsDTO.class);

        String uploadDir = "D:/DATN/Image/";

        if (dto.getDeletedImages() != null) {
            for (String fileName : dto.getDeletedImages()) {
                File fileToDelete = new File(uploadDir + fileName);
                if (fileToDelete.exists()) fileToDelete.delete();
            }
        }

        if (imageUrl != null) {
            String imageUrlPath = saveFile(uploadDir, imageUrl, dto.getCode() + "-M");
            dto.setImageUrl(imageUrlPath);
        }

        if (imageDetailList != null && !imageDetailList.isEmpty()) {
            StringBuilder detailPaths = new StringBuilder();
            for (int i = 0; i < imageDetailList.size(); i++) {
                String detailPath = saveFile(uploadDir, imageDetailList.get(i), dto.getCode() + "-" + (i + 1));
                if (detailPaths.length() > 0) detailPaths.append(",");
                detailPaths.append(detailPath);
            }
            dto.setImageDetail(detailPaths.toString());
        }

        return ResponseEntity.ok(productService.updateProduct(dto));
    }

    @DeleteMapping("/delete-product")
    public ResponseEntity<Void> deleteProduct(@RequestParam String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("code") String code,
            @RequestParam("imageUrl") MultipartFile imageUrl,
            @RequestParam("imageDetail") List<MultipartFile> imageDetails) throws IOException {

        String uploadDir = "D:/DATN/Image/";

        // Đổi tên ảnh chính thành {code}-M
        String urlPath = saveFile(uploadDir, imageUrl, code + "-M");

        // Đổi tên các ảnh chi tiết thành {code}-1, {code}-2, ...
        StringBuilder imageDetailPaths = new StringBuilder();
        for (int i = 0; i < imageDetails.size(); i++) {
            MultipartFile file = imageDetails.get(i);
            String fileName = code + "-" + (i + 1);
            String path = saveFile(uploadDir, file, fileName);
            if (imageDetailPaths.length() > 0) {
                imageDetailPaths.append(",");
            }
            imageDetailPaths.append(path);
        }

        Map<String, String> result = new HashMap<>();
        result.put("imageUrlPath", urlPath);
        result.put("imageDetailPath", imageDetailPaths.toString());

        return ResponseEntity.ok(result);
    }

    public String saveFile(String uploadDir, MultipartFile file, String customName) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = uploadDir + customName + extension;
        File saveFile = new File(fileName);
        file.transferTo(saveFile);
        return fileName;
    }

    @GetMapping("/get-image")
    public ResponseEntity<byte[]> getImage(@RequestParam String imagePath) throws IOException {
        String decodedPath = URLDecoder.decode(imagePath, StandardCharsets.UTF_8);

        File file = new File(decodedPath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = Files.readAllBytes(Paths.get(decodedPath));
        String contentType = Files.probeContentType(Paths.get(decodedPath));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(imageBytes);
    }

    @GetMapping("/statistic-by-category")
    public ResponseEntity<List<ProductCategoryStatisticDTO>> getStatisticByCategory() {
        return ResponseEntity.ok(productService.getProductStatisticWithCategoryName());
    }

    @GetMapping("/get-all-products-info")
    public ResponseEntity<List<ProductsDTO>> getAllProductInfo() {
        return ResponseEntity.ok(productService.getAllProductInfo());
    }

}
