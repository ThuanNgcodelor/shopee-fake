package com.example.stockservice.controller;

import com.example.stockservice.dto.SizeDto;
import com.example.stockservice.model.Size;
import com.example.stockservice.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/stock/size")
@RequiredArgsConstructor
public class SizeController {
    private final SizeRepository sizeRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/getSizeById/{sizeId}")
    ResponseEntity<SizeDto> getSizeById(@PathVariable String sizeId) {
        Size size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new RuntimeException("Size not found for ID: " + sizeId));
        
        SizeDto sizeDto = modelMapper.map(size, SizeDto.class);
        return ResponseEntity.status(HttpStatus.OK).body(sizeDto);
    }
}

