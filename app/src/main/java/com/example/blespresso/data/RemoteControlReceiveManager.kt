package com.example.blespresso.data

import com.example.blespresso.presentation.C_BrewConfigData
import com.example.blespresso.presentation.C_PIDConfigData
import com.example.blespresso.utilities.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
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
    val dataToRead:MutableSharedFlow<Resource<BoilerSetpoint>>
    val dataBrewCfgPreInfusionPwr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfgPreInfusionTmr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfg___InfusionPwr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfg___InfusionTmr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfgDeclining__Pwr:MutableSharedFlow<Resource<CharDataClass>>
    val dataBrewCfgDeclining__Tmr:MutableSharedFlow<Resource<CharDataClass>>

    // new characteristic for steam temperature setpoint
    val dataSteamSetpoint: MutableSharedFlow<Resource<BoilerSetpoint>>

    val dataPIDcfgPterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgIterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgImaxterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgWindup:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgDterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgDlpfterm:MutableSharedFlow<Resource<CharDataClass>>
    val dataPIDcfgGainterm:MutableSharedFlow<Resource<CharDataClass>>

    fun reconnect()

    fun disconnect()

    fun startConnection()

    fun txNewBoilerTargetTemp(newTarget: Float)

    fun rxReadBoilerTargetTemp()

    // read steam temperature setpoint from BLE
    fun rxReadSteamTempSetpoint()

    fun txNewSteamTempSetpoint(newTarget: Float)

    fun txfcnWriteBrewConfiguration(newTxData: C_BrewConfigData)
    fun txfcnWritePIDConfiguration(newTxData: C_PIDConfigData)

    suspend fun rxReadBrewConfiguration()
    suspend fun rxReadPIDConfiguration()

    fun startReceiving()
    fun closeConnection()
}