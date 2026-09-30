/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.upgrade.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the {@link FedoraVersion}
 */
public class FedoraVersionTest {

    @Test
    public void testFromString() {
        for (final var version : FedoraVersion.values()) {
            assertEquals(version, FedoraVersion.fromString(version.getStringValue()));
        }
    }

    @Test
    public void testFromStringInvalid() {
        final var e = assertThrows(IllegalArgumentException.class, () -> FedoraVersion.fromString("3"));
        assertTrue(e.getMessage(), e.getMessage().contains("4.7.5,5+,6+"));
    }
}
