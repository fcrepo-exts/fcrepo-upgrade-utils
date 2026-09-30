/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.upgrade.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.apache.jena.riot.Lang;
import org.junit.Test;

/**
 * Tests the {@link Config}
 */
public class ConfigTest {

    @Test
    public void testNullValuesUseDefaults() {
        final var config = new Config();
        config.setThreads(3);
        config.setThreads(null);
        config.setFedoraUser("user");
        config.setFedoraUser(null);
        config.setFedoraUserAddress("info:user");
        config.setFedoraUserAddress(null);
        config.setSrcRdfLang(Lang.NT);
        config.setSrcRdfLang(null);

        assertEquals(Integer.valueOf(Config.DEFAULT_THREADS), config.getThreads());
        assertEquals(Config.DEFAULT_USER, config.getFedoraUser());
        assertEquals(Config.DEFAULT_USER_ADDRESS, config.getFedoraUserAddress());
        assertEquals(Config.DEFAULT_SRC_RDF_LANG, config.getSrcRdfLang());
        assertEquals(Config.DEFAULT_DIGEST_ALGORITHM, config.getDigestAlgorithm());
    }

    @Test
    public void testThreadsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Config().setThreads(0));
    }

    @Test
    public void testBaseUriRequired() {
        assertThrows(NullPointerException.class, () -> new Config().setBaseUri(null));
    }

    @Test
    public void testSrcRdfExt() {
        final var config = new Config();
        config.setSrcRdfLang(Lang.NT);
        assertEquals("nt", config.getSrcRdfExt());
    }

    @Test
    public void testToString() {
        final var config = new Config();
        config.setSourceVersion(FedoraVersion.V_5);
        config.setTargetVersion(FedoraVersion.V_6);
        config.setS3Bucket("bucket");
        final var str = config.toString();
        assertTrue(str, str.contains("sourceVersion=V_5"));
        assertTrue(str, str.contains("targetVersion=V_6"));
        assertTrue(str, str.contains("s3Bucket=bucket"));
    }
}
