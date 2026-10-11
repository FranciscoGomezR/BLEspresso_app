package com.example.blespresso.data

import com.example.blespresso.utilities.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
// Tutorial 1 -> Youtube: https://www.youtube.com/watch?v=qyG-SDfYNBE&t=422s&ab_channel=KApps
// Time-Stamp: 24:00


//the reason to create this interface is because  we can inject this interface into our VIEW_MODULATOR
// So the viewModel -> is just depended of abstraction & not concretion
//This is useful ti inject test-cases

interface RemoteControlReceiveManager
{
    //Our ReceiveManager need to realize the concrete implementation of:
    val data:MutableSharedFlow<Resource<BoilerDataResult>>
    val dataToWrite:MutableSharedFlow<Resource<BoilerDataResult>>
    // 0x1403 active boiler setpoint (READ at sync + NOTIFY on BREW/STEAM switch change)
    val dataToRead:MutableSharedFlow<Resource<BoilerSetpoint>>
    // 0x1401 machine status {mode, state} (READ at sync + NOTIFY on change)
    val dataMachineStatus:MutableSharedFlow<Resource<MachineStatus>>
    val dataBrewCfgPreInfusionPwr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfgPreInfusionTmr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfg___InfusionPwr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfg___InfusionTmr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfgDeclining__Pwr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfgDeclining__Tmr:MutableSharedFlow<Resource<CharDataClass>>

    // brew (0x1404) / steam (0x1405) temperature presets
    val dataBrewTempPreset: MutableSharedFlow<Resource<BoilerSetpoint>>
    val dataSteamTempPreset: MutableSharedFlow<Resource<BoilerSetpoint>>

    val dataPIDcfgPterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgIterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgImaxterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgDterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgPboost:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgIboost:MutableSharedFlow<Resource<CharDataClass>>

    fun reconnect()

    fun disconnect()

    fun startConnection()

    fun rxReadBoilerTempSetpoint()
    fun rxReadBrewTempPreset()
    fun rxReadSteamTempPreset()

    // Individual MAchine characteristic writes (0x1403 setpoint is read-only)
    fun txNewBrewTempPreset(newTarget: Float)
    fun txNewSteamTempPreset(newTarget: Float)
    fun txWritePreInfusionPwr(value: Float)
    fun txWritePreInfusionTmr(value: Float)
    fun txWriteInfusionPwr(value: Float)
    fun txWriteInfusionTmr(value: Float)
    fun txWriteDecliningPwr(value: Float)
    fun txWriteDecliningTmr(value: Float)

    // Individual PID characteristic writes
    fun txWritePidPTerm(value: Float)
    fun txWritePidITerm(value: Float)
    fun txWritePidIMax(value: Float)
    fun txWritePidDTerm(value: Float)
    fun txWritePidPBoost(value: Float)
    fun txWritePidIBoost(value: Float)

    suspend fun rxReadPIDConfiguration()
    // Reads the machine config UUIDs (active setpoint 0x1403, brew/steam presets 0x1404/0x1405, 6 brew params
    // 0x1406-0x140B, machine status 0x1401) in a single GATT-safe sequential queue. A failed/short char is skipped, not
    // fatal; machineConfigReadComplete fires once the queue drains.
    suspend fun rxReadMachineConfiguration()

    // Emits Unit when the machine-config read queue drains completely.
    val machineConfigReadComplete: SharedFlow<Unit>
    // Emits Unit when the PID-config read queue drains completely.
    val pidConfigReadComplete: SharedFlow<Unit>

    fun startReceiving()
    fun closeConnection()
}