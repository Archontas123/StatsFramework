package io.tavuc.skillsystem.test.config;

import io.tavuc.skillsystem.config.FormulaConfig;
import io.tavuc.skillsystem.config.FormulaType;
import io.tavuc.skillsystem.test.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class FormulaConfigTest extends UnitTest {
    
    @Test
    public void testLinearFormula() {
        FormulaConfig formula = new FormulaConfig("test", FormulaType.LINEAR, 10.0, 2.0, 0.0);
        
        assertEquals(10.0, formula.calculate(0), 0.001);
        assertEquals(20.0, formula.calculate(5), 0.001);
        assertEquals(30.0, formula.calculate(10), 0.001);
    }
    
    @Test
    public void testDiminishingFormula() {
        FormulaConfig formula = new FormulaConfig("test", FormulaType.DIMINISHING, 10.0, 1.0, 0.1);
        
        assertEquals(10.0, formula.calculate(0), 0.001);
        
        double value = formula.calculate(10);
        assertTrue(value > 10.0);
        assertTrue(value < 20.0);
        
        double value2 = formula.calculate(20);
        assertTrue(value2 > value);
    }
    
    @Test
    public void testSteppedFormula() {
        FormulaConfig formula = new FormulaConfig("test", FormulaType.STEPPED, 10.0, 5.0, 0.0, 3.0);
        
        assertEquals(10.0, formula.calculate(0), 0.001);
        assertEquals(10.0, formula.calculate(2), 0.001);
        assertEquals(15.0, formula.calculate(3), 0.001);
        assertEquals(15.0, formula.calculate(5), 0.001);
        assertEquals(20.0, formula.calculate(6), 0.001);
    }
    
    @Test
    public void testChanceFormula() {
        FormulaConfig formula = new FormulaConfig("test", FormulaType.CHANCE, 5.0, 2.0, 0.0);
        
        assertEquals(5.0, formula.calculate(0), 0.001);
        assertEquals(25.0, formula.calculate(10), 0.001);
        assertEquals(85.0, formula.calculate(40), 0.001);
        assertEquals(100.0, formula.calculate(60), 0.001);
        assertEquals(100.0, formula.calculate(100), 0.001);
    }
    
    @ParameterizedTest
    @CsvSource({
        "LINEAR, 10.0, 2.0, 0.0, 30.0",
        "DIMINISHING, 10.0, 2.0, 0.05, 26.27",
        "STEPPED, 10.0, 5.0, 0.0, 25.0",
        "CHANCE, 5.0, 2.0, 0.0, 45.0"
    })
    public void testVariousFormulas(FormulaType type, double baseValue, double scaleValue, 
                                   double factor, double expected) {
        FormulaConfig formula = new FormulaConfig("test", type, baseValue, scaleValue, factor);
        assertEquals(expected, formula.calculate(10), 0.01);
    }
}