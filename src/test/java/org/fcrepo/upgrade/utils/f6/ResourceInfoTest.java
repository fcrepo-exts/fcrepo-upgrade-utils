/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.upgrade.utils.f6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests {@link ResourceInfo} and {@link ResourceInfoLogger}
 */
public class ResourceInfoTest {

    private static final Path OUTER = Paths.get("export", "rest");

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private static ResourceInfo container() {
        return ResourceInfo.container("info:fedora", "info:fedora/a", "info:fedora/ag", OUTER, "a");
    }

    @Test
    public void testEquals() {
        final var info = container();
        assertEquals(info, info);
        assertEquals(container(), info);
        assertEquals(container().hashCode(), info.hashCode());
        assertNotEquals(info, null);
        assertNotEquals(info, "info:fedora/a");
    }

    @Test
    public void testNotEqualWhenAnyFieldDiffers() {
        final var info = container();
        assertNotEquals(info, ResourceInfo.container("info:fedora/x", "info:fedora/a", "info:fedora/ag", OUTER, "a"));
        assertNotEquals(info, ResourceInfo.container("info:fedora", "info:fedora/b", "info:fedora/ag", OUTER, "a"));
        assertNotEquals(info, ResourceInfo.container("info:fedora", "info:fedora/a", null, OUTER, "a"));
        assertNotEquals(info, ResourceInfo.container("info:fedora", "info:fedora/a", "info:fedora/ag", OUTER, "b"));
        assertNotEquals(info, ResourceInfo.container("info:fedora", "info:fedora/a", "info:fedora/ag",
                Paths.get("other"), "a"));
        assertNotEquals(info, ResourceInfo.binary("info:fedora", "info:fedora/a", "info:fedora/ag", OUTER, "a"));
        assertNotEquals(info, ResourceInfo.externalBinary("info:fedora", "info:fedora/a", "info:fedora/ag",
                OUTER, "a"));
    }

    @Test
    public void testParseLog() throws IOException {
        final var mapper = new ObjectMapper();
        final var infos = List.of(container(),
                ResourceInfo.binary("info:fedora/a", "info:fedora/a/b", null, OUTER.resolve("a"), "b"));
        final var log = temp.newFile().toPath();
        Files.writeString(log, mapper.writeValueAsString(infos.get(0)) + System.lineSeparator()
                + mapper.writeValueAsString(infos.get(1)) + System.lineSeparator());

        assertEquals(infos, new ResourceInfoLogger().parseLog(log));
    }

    @Test
    public void testParseMissingLog() {
        final var missing = temp.getRoot().toPath().resolve("missing.log");
        assertThrows(UncheckedIOException.class, () -> new ResourceInfoLogger().parseLog(missing));
    }
}
