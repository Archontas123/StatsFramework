package io.tavuc.skillsystem.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockitoAnnotations;

public abstract class UnitTest {
    
    @BeforeEach
    public void baseSetUp() {
        MockitoAnnotations.openMocks(this);
    }
}