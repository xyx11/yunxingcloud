package com.yunxingcloud.order.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validDTOShouldPass() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Test Product");
        dto.setPrice(10000L);
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void blankNameShouldFail() {
        ProductDTO dto = new ProductDTO();
        dto.setName("");
        dto.setPrice(10000L);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void nullPriceShouldFail() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Test");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void negativePriceShouldFail() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Test");
        dto.setPrice(-1L);
        assertFalse(validator.validate(dto).isEmpty());
    }
}
