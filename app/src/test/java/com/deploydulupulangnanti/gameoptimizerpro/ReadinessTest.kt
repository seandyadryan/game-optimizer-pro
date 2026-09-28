package com.deploydulupulangnanti.gameoptimizerpro

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadinessTest {
    @Test fun legacyProfilesMigrateWithoutDependingOnCurrentLanguage() {
        assertEquals(GameProfile.BALANCED, GameProfile.fromStored("Seimbang"))
        assertEquals(GameProfile.COMPETITIVE, GameProfile.fromStored("Kompetitif"))
        assertEquals(GameProfile.ENDURANCE, GameProfile.fromStored("Hemat daya"))
    }
    @Test fun stableProfileIdsRoundTrip() {
        GameProfile.entries.forEach { assertEquals(it, GameProfile.fromStored(it.name)) }
    }
    @Test fun unknownProfileUsesSafeDefault() {
        assertEquals(GameProfile.BALANCED, GameProfile.fromStored("unrecognized"))
    }
    @Test fun heatTakesPriorityOverLowBattery() {
        assertEquals(Readiness.HOT, readiness(DeviceSnapshot(battery = 5, temperature = 43f)))
    }
    @Test fun severeThermalStatusWithoutBatterySensorStillWarns() {
        assertEquals(Readiness.HOT, readiness(DeviceSnapshot(thermalStatus = 3)))
    }
    @Test fun lowBatteryBoundary() {
        assertEquals(Readiness.LOW_BATTERY, readiness(DeviceSnapshot(battery = 20)))
    }
    @Test fun powerSavingIsVisible() {
        assertEquals(Readiness.POWER_SAVER, readiness(DeviceSnapshot(battery = 80, powerSaver = true)))
    }
    @Test fun unavailableMemoryIsNotReportedReady() {
        assertEquals(Readiness.LOADING, readiness(DeviceSnapshot()))
    }
    @Test fun healthyDeviceIsReady() {
        assertEquals(Readiness.READY, readiness(DeviceSnapshot(battery = 80, temperature = 30f, totalRam = 100)))
    }
    @Test fun clockRollbackDoesNotCreateNegativeDuration() {
        assertEquals(0L, PlaySession("Game", 120000, 60000, GameProfile.BALANCED).durationMinutes)
    }
    @Test fun elapsedMinutesAreRoundedDown() {
        assertEquals(2L, PlaySession("Game", 1000, 150000, GameProfile.BALANCED).durationMinutes)
    }
}
