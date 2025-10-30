package com.example.stockservice.controller;

import com.example.stockservice.dto.ProductDto;
import com.example.stockservice.jwt.JwtUtil;
import com.example.stockservice.model.Product;
import com.example.stockservice.model.Size;
import com.example.stockservice.request.product.DecreaseStockRequest;
import com.example.stockservice.request.product.IncreaseStockRequest;
import com.example.stockservice.request.product.ProductCreateRequest;
import com.example.stockservice.request.product.ProductUpdateRequest;
import com.example.stockservice.service.product.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequestMapping("/v1/stock/product")
@RequiredArgsConstructor
@RestController
public class ProductController {
    private final ProductService productService;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/decreaseStock")
    public ResponseEntity<ProductDto> decreaseStock(@Valid @RequestBody DecreaseStockRequest request) {
        productService.decreaseStockBySize(request.getSizeId(), request.getQuantity());
        
        // Get product by finding the size first
        Product product = productService.findProductBySizeId(request.getSizeId());
        
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        ProductDto productDto = toDto(product);
        return ResponseEntity.status(HttpStatus.OK).body(productDto);
    }

    @PostMapping("/increaseStock")
    public ResponseEntity<ProductDto> increaseStock(@Valid @RequestBody IncreaseStockRequest request) {
        productService.increaseStockBySize(request.getSizeId(), request.getQuantity());
        
        // Get product by finding the size first
        Product product = productService.findProductBySizeId(request.getSizeId());
        
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        ProductDto productDto = toDto(product);
        return ResponseEntity.status(HttpStatus.OK).body(productDto);
    }

    //    {
    //        "name": "Điện thoại iPhone 15 Pro",
    //            "description": "Điện thoại cao cấp với chip A17 Bionic, màn hình 6.1 inch Super Retina XDR. Camera 48MP với zoom quang học 3x. Pin lithium-ion với MagSafe.",
    //            "price": 28990000,
    //            "originalPrice": 32990000,
    //            "discountPercent": 12.1,
    //            "categoryId": "1",
    //            "sizes": [
    //        {
    //            "name": "128GB",
    //                "description": "Bộ nhớ 128GB",
    //                "stock": 50,
    //                "priceModifier": 0
    //        },
    //        {
    //            "name": "256GB",
    //                "description": "Bộ nhớ 256GB",
    //                "stock": 30,
    //                "priceModifier": 4000000
    //        },
    //        {
    //            "name": "512GB",
    //                "description": "Bộ nhớ 512GB",
    //                "stock": 20,
    //                "priceModifier": 8000000
    //        }
    //  ]
    //    }
    @PostMapping("/create")
    ResponseEntity<ProductDto> createProduct(@Valid @RequestPart("request") ProductCreateRequest request,
                                            @RequestPart(value = "file", required = false) MultipartFile files,
                                            HttpServletRequest httpServletRequest) {
        String userId = jwtUtil.ExtractUserId(httpServletRequest);
        request.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDto(productService.createProduct(request, files)));
    }

    @PutMapping("/update")
    ResponseEntity<ProductDto> updateProduct(@Valid @RequestPart("request") ProductUpdateRequest request,
                                            @RequestPart(value = "file", required = false) MultipartFile files,
                                            HttpServletRequest httpServletRequest) {
        String userId = jwtUtil.ExtractUserId(httpServletRequest);
        request.setUserId(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(toDto(productService.updateProduct(request, files)));
    }

    @DeleteMapping("/deleteProductById/{id}")
    ResponseEntity<ProductDto> deleteProductById(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/getProductById/{id}")
    ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(toDto(productService.getProductById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "6") Integer pageSize) {

        Page<Product> products = productService.searchProductByKeyword(keyword, pageNo, pageSize);
        Page<ProductDto> dtoPage = products.map(this::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/listPage")
    ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "6") Integer pageSize) {
        Page<ProductDto> products = productService.getAllProducts(pageNo, pageSize).map(this::toDto);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("/list")
    ResponseEntity<List<ProductDto>> getAllProduct(){
        return ResponseEntity.ok(productService.getAllProducts().stream()
                .map(this::toDto).toList());
    }

    @GetMapping("/listPageShopOwner")
    ResponseEntity<Page<ProductDto>> getAllProductsByShopOwner(
            HttpServletRequest httpServletRequest,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "6") Integer pageSize) {

        String userId = jwtUtil.ExtractUserId(httpServletRequest);
        Page<ProductDto> products = productService.getProductsByUserIdWithPaging(userId, pageNo, pageSize)
                .map(this::toDto);

        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("/searchShopOwner")
    public ResponseEntity<Page<ProductDto>> searchProductsByShopOwner(
            HttpServletRequest httpServletRequest,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "6") Integer pageSize) {

        String userId = jwtUtil.ExtractUserId(httpServletRequest);
        Page<Product> products = productService.searchProductsByUserId(userId, keyword, pageNo, pageSize);
        Page<ProductDto> dtoPage = products.map(this::toDto);

        return ResponseEntity.ok(dtoPage);
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = modelMapper.map(product, ProductDto.class);
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
            dto.setCategoryId(product.getCategory().getId());
        }
        dto.setCreatedAt(product.getCreatedTimestamp());
        int total = 0;
        if (product.getSizes() != null) {
            for (Size s : product.getSizes()) {
                total += (s.getStock() != 0 ? s.getStock() : 0);
            }
        }
        dto.setTotalStock(total);
        return dto;
    }
}

