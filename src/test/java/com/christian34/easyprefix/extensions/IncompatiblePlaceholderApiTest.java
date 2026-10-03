package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.PluginTestBase;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * A plugin named PlaceholderAPI without the real api behind it, like an incompatible version would behave.
 *
 * @author Christian34
 */
class IncompatiblePlaceholderApiTest extends PluginTestBase {

    @Override
    protected void beforePluginLoad() {
        MockBukkit.createMockPlugin("PlaceholderAPI");
    }

    @Test
    void easyPrefixStillWorks() {
        assertTrue(plugin.isEnabled());
        assertFalse(plugin.getExpansionManager().isUsingPapi());
        assertEquals("default", plugin.setPlaceholders(user(addPlayer("Steve")), "%ep_user_group%"));
    }

}
