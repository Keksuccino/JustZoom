package de.keksuccino.justzoom.util.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigSliderTest {

    @Test
    void integerRangeUsesTheCompleteRangeAndConfiguredSteps() {
        assertEquals(1, ConfigSlider.sliderValueToInteger(-1.0D, 1, 401, 4, 101));
        assertEquals(5, ConfigSlider.sliderValueToInteger(0.01D, 1, 401, 4, 101));
        assertEquals(101, ConfigSlider.sliderValueToInteger(0.25D, 1, 401, 4, 101));
        assertEquals(401, ConfigSlider.sliderValueToInteger(2.0D, 1, 401, 4, 101));
        assertEquals(101, ConfigSlider.sliderValueToInteger(Double.NaN, 1, 401, 4, 101));
    }

    @Test
    void integerRangeNormalizesStoredAndContinuousValues() {
        assertEquals(0.0D, ConfigSlider.integerToSliderValue(-10, 1, 401, 4, 101));
        assertEquals(0.25D, ConfigSlider.integerToSliderValue(102, 1, 401, 4, 101));
        assertEquals(1.0D, ConfigSlider.integerToSliderValue(900, 1, 401, 4, 101));
        assertEquals(0.01D, ConfigSlider.snapIntegerSliderValue(0.014D, 1, 401, 4, 101));
        assertEquals(0.02D, ConfigSlider.snapIntegerSliderValue(0.015D, 1, 401, 4, 101));
        assertEquals(Integer.MAX_VALUE, ConfigSlider.sliderValueToInteger(1.0D, Integer.MIN_VALUE, Integer.MAX_VALUE, 3, 0));
    }

    @Test
    void integerRangeRejectsInvalidDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> ConfigSlider.sliderValueToInteger(0.5D, 0, 10, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfigSlider.sliderValueToInteger(0.5D, 10, 10, 1, 10));
        assertThrows(IllegalArgumentException.class, () -> ConfigSlider.sliderValueToInteger(0.5D, 0, 10, 3, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfigSlider.sliderValueToInteger(0.5D, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 0));
    }

    @Test
    void floatRangeUsesTheCompleteRangeInConfiguredSteps() {
        assertEquals(0.0F, ConfigSlider.sliderValueToFloat(-1.0D, 0.0F, 5.0F, 0.05F, 0.3F));
        assertEquals(0.05F, ConfigSlider.sliderValueToFloat(0.01D, 0.0F, 5.0F, 0.05F, 0.3F));
        assertEquals(0.45F, ConfigSlider.sliderValueToFloat(0.09D, 0.0F, 5.0F, 0.05F, 0.3F));
        assertEquals(5.0F, ConfigSlider.sliderValueToFloat(2.0D, 0.0F, 5.0F, 0.05F, 0.3F));
        assertEquals(0.3F, ConfigSlider.sliderValueToFloat(Double.NaN, 0.0F, 5.0F, 0.05F, 0.3F));
    }

    @Test
    void floatRangeNormalizesStoredAndContinuousValues() {
        assertEquals(0.09D, ConfigSlider.floatToSliderValue(0.45F, 0.0F, 5.0F, 0.05F, 0.3F), 0.00000001D);
        assertEquals(0.06D, ConfigSlider.floatToSliderValue(Float.NaN, 0.0F, 5.0F, 0.05F, 0.3F), 0.00000001D);
        assertEquals(0.01D, ConfigSlider.snapFloatSliderValue(0.014D, 0.0F, 5.0F, 0.05F, 0.3F), 0.00000001D);
        assertEquals(0.02D, ConfigSlider.snapFloatSliderValue(0.015D, 0.0F, 5.0F, 0.05F, 0.3F), 0.00000001D);
    }

    @Test
    void floatRangeRejectsInvalidDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> ConfigSlider.sliderValueToFloat(0.5D, 0.0F, 1.0F, 0.3F, 0.0F));
        assertThrows(IllegalArgumentException.class, () -> ConfigSlider.sliderValueToFloat(0.5D, Float.NaN, 1.0F, 0.1F, 0.0F));
        assertThrows(IllegalArgumentException.class, () -> ConfigSlider.sliderValueToFloat(0.5D, 0.0F, Float.MAX_VALUE, Float.MIN_VALUE, 1.0F));
    }

}
