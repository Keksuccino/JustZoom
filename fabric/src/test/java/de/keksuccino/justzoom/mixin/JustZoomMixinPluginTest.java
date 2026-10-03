package de.keksuccino.justzoom.mixin;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JustZoomMixinPluginTest {

    private static final String PLUGIN_CLASS = JustZoomMixinPlugin.class.getName();
    private static final String REFMAP = "justzoom.refmap.json";

    @Test
    void releaseArtifactSelectsItsBundledReferenceMap() throws Exception {
        try (URLClassLoader loader = artifactClassLoader(false)) {
            var plugin = loader.loadClass(PLUGIN_CLASS).getConstructor().newInstance();
            String referenceMap = (String) plugin.getClass().getMethod("getRefMapperConfig").invoke(plugin);
            assertNotNull(referenceMap);
            try (var stream = loader.getResourceAsStream(referenceMap)) {
                assertNotNull(stream);
                var config = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                assertFalse(config.getAsJsonObject("mappings").entrySet().isEmpty());
            }
        }
    }

    @Test
    void loadersWithoutAReferenceMapKeepNativeNames() throws Exception {
        try (URLClassLoader loader = artifactClassLoader(true)) {
            var plugin = loader.loadClass(PLUGIN_CLASS).getConstructor().newInstance();
            assertNull(plugin.getClass().getMethod("getRefMapperConfig").invoke(plugin));
        }
    }

    private static URLClassLoader artifactClassLoader(boolean hideReferenceMap) throws Exception {
        URL artifact = Path.of(System.getProperty("justzoom.releaseJar")).toUri().toURL();
        // Isolate the production plugin and its resources from Loom's development classpath.
        // NeoForge uses native names and does not bundle the Fabric/Forge reference map.
        return new URLClassLoader(new URL[]{artifact}, JustZoomMixinPluginTest.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!PLUGIN_CLASS.equals(name)) return super.loadClass(name, resolve);
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) loaded = findClass(name);
                if (resolve) resolveClass(loaded);
                return loaded;
            }

            @Override
            public URL getResource(String name) {
                if (REFMAP.equals(name)) return hideReferenceMap ? null : findResource(name);
                return super.getResource(name);
            }
        };
    }

}
