package io.tavuc.skillsystem.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.tavuc.skillsystem.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class MockBukkitTest {
    
    protected ServerMock server;
    protected Main plugin;
    
    @BeforeEach
    public void baseSetUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Main.class);
    }
    
    @AfterEach
    public void baseTearDown() {
        MockBukkit.unmock();
    }
}