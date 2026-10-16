package com.gigverse.app

import com.gigverse.app.config.EnvironmentMode
import com.gigverse.app.config.ProductionConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionConfigTest {

    @Test
    fun testEnvironmentModeSwitching() {
        ProductionConfig.setEnvironmentMode(EnvironmentMode.PRODUCTION)
        assertEquals(EnvironmentMode.PRODUCTION, ProductionConfig.currentEnvironment)
        assertTrue(ProductionConfig.isProductionMode())

        ProductionConfig.setEnvironmentMode(EnvironmentMode.STAGING)
        assertEquals(EnvironmentMode.STAGING, ProductionConfig.currentEnvironment)

        ProductionConfig.setEnvironmentMode(EnvironmentMode.TESTING)
        assertEquals(EnvironmentMode.TESTING, ProductionConfig.currentEnvironment)
    }

    @Test
    fun testProductionTelemetry() {
        ProductionConfig.setEnvironmentMode(EnvironmentMode.PRODUCTION)
        val telemetry = ProductionConfig.getTelemetry()

        assertNotNull(telemetry)
        assertEquals(EnvironmentMode.PRODUCTION, telemetry.environment)
        assertEquals("v1.1.1-PROD", telemetry.releaseVersion)
        assertTrue(telemetry.isHttpsEnforced)
        assertTrue(telemetry.isSslPinningActive)
        assertEquals("SQLCipher 256-bit AES", telemetry.databaseEncryption)
    }
}
