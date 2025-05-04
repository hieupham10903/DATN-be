package com.example.datnbe.Resource;

import com.example.datnbe.Entity.Criteria.ProductCriteria;
import com.example.datnbe.Entity.DTO.ProductsDTO;
import com.example.datnbe.Service.ProductService;
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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

    @DeleteMapping("/delete-product")
    public ResponseEntity<Void> deleteProduct(@RequestParam String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("imageUrl") MultipartFile imageUrl,
            @RequestParam("imageDetail") List<MultipartFile> imageDetails) throws IOException {

        String uploadDir = "D:/DATN/Image/";

        String urlPath = saveFile(uploadDir, imageUrl);

        StringBuilder imageDetailPaths = new StringBuilder();
        for (MultipartFile file : imageDetails) {
            String path = saveFile(uploadDir, file);
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

    private String saveFile(String dir, MultipartFile file) throws IOException {
        String filePath = dir + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        return filePath;
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
}
