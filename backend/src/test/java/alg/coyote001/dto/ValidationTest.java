package alg.coyote001.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DTO validation annotations
 */
class ValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCoinDtoValidation() {
        // Valid CoinDto
        CoinDto validCoin = CoinDto.builder()
                .symbol("BTC")
                .name("Bitcoin")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CoinDto>> violations = validator.validate(validCoin);
        assertTrue(violations.isEmpty(), "Valid CoinDto should not have violations");

        // Invalid CoinDto - empty symbol
        CoinDto invalidCoin = CoinDto.builder()
                .symbol("")
                .name("Bitcoin")
                .build();

        violations = validator.validate(invalidCoin);
        assertFalse(violations.isEmpty(), "Invalid CoinDto should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("symbol")));
    }

    @Test
    void testExchangeDtoValidation() {
        // Valid ExchangeDto
        ExchangeDto validExchange = ExchangeDto.builder()
                .name("binance")
                .apiUrl("https://api.binance.com")
                .websocketUrl("wss://stream.binance.com")
                .isActive(true)
                .build();

        Set<ConstraintViolation<ExchangeDto>> violations = validator.validate(validExchange);
        assertTrue(violations.isEmpty(), "Valid ExchangeDto should not have violations");

        // Invalid ExchangeDto - invalid URL
        ExchangeDto invalidExchange = ExchangeDto.builder()
                .name("binance")
                .apiUrl("invalid-url")
                .build();

        violations = validator.validate(invalidExchange);
        assertFalse(violations.isEmpty(), "Invalid ExchangeDto should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("apiUrl")));
    }

    @Test
    void testPriceHistoryDtoValidation() {
        // Valid PriceHistoryDto
        PriceHistoryDto validPrice = PriceHistoryDto.builder()
                .coinSymbol("BTC")
                .coinName("Bitcoin")
                .exchangeName("binance")
                .price(new BigDecimal("50000.00"))
                .volume(new BigDecimal("1000.00"))
                .quoteCurrency("USDT")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<PriceHistoryDto>> violations = validator.validate(validPrice);
        assertTrue(violations.isEmpty(), "Valid PriceHistoryDto should not have violations");

        // Invalid PriceHistoryDto - negative price
        PriceHistoryDto invalidPrice = PriceHistoryDto.builder()
                .coinSymbol("BTC")
                .coinName("Bitcoin")
                .exchangeName("binance")
                .price(new BigDecimal("-100.00"))
                .quoteCurrency("USDT")
                .timestamp(LocalDateTime.now())
                .build();

        violations = validator.validate(invalidPrice);
        assertFalse(violations.isEmpty(), "Invalid PriceHistoryDto should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    void testSettingsDtoValidation() {
        // Valid SettingsDto
        SettingsDto validSettings = SettingsDto.builder()
                .category("trading")
                .settings(java.util.Map.of("key", "value"))
                .updatedBy("admin")
                .build();

        Set<ConstraintViolation<SettingsDto>> violations = validator.validate(validSettings);
        assertTrue(violations.isEmpty(), "Valid SettingsDto should not have violations");

        // Invalid SettingsDto - null settings
        SettingsDto invalidSettings = SettingsDto.builder()
                .category("trading")
                .settings(null)
                .build();

        violations = validator.validate(invalidSettings);
        assertFalse(violations.isEmpty(), "Invalid SettingsDto should have violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("settings")));
    }
} 