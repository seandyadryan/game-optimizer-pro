package com.deploydulupulangnanti.gameoptimizerpro

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadinessTest {
    @Test fun heatTakesPriorityOverLowBattery() {
        assertEquals("Saatnya jeda", readiness(DeviceSnapshot(battery = 5, temperature = 43f)).first)
    }
    @Test fun severeThermalStatusWithoutBatterySensorStillWarns() {
        assertEquals("Saatnya jeda", readiness(DeviceSnapshot(thermalStatus = 3)).first)
    }
    @Test fun lowBatteryBoundary() {
        assertEquals("Daya mulai rendah", readiness(DeviceSnapshot(battery = 20)).first)
    }
    @Test fun powerSavingIsVisible() {
        assertEquals("Mode hemat aktif", readiness(DeviceSnapshot(battery = 80, powerSaver = true)).first)
    }
    @Test fun unavailableMemoryIsNotReportedReady() {
        assertEquals("Memeriksa perangkat", readiness(DeviceSnapshot()).first)
    }
    @Test fun healthyDeviceIsReady() {
        assertEquals("Siap untuk sesi berikutnya", readiness(DeviceSnapshot(battery = 80, temperature = 30f, totalRam = 100)).first)
    }
    @Test fun clockRollbackDoesNotCreateNegativeDuration() {
        assertEquals(0L, PlaySession("Game", 120000, 60000, "Seimbang").durationMinutes)
    }
    @Test fun elapsedMinutesAreRoundedDown() {
        assertEquals(2L, PlaySession("Game", 1000, 150000, "Seimbang").durationMinutes)
    }
}
