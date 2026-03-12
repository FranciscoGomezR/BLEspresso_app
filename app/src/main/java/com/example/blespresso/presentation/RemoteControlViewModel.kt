package com.example.blespresso.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blespresso.data.ConnectionState
import com.example.blespresso.data.RemoteControlReceiveManager
import com.example.blespresso.utilities.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// video 1-> Time-Stamp: 1:07:00
@HiltViewModel
class  RemoteControlViewModel @Inject constructor(
    private val remoteControlReceiveManager: RemoteControlReceiveManager
) :ViewModel() {
    var initialazingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var boilerWaterTemp by mutableStateOf(0f)
        private set

    var boilerSetpointTemp by mutableStateOf(0f)
        public set

    // new value for steam temperature received from BLE
    var steamTempSetpoint by mutableStateOf(0f)
        public set

    var brewConfigData by mutableStateOf(C_BrewConfigData())
        public set

    var txNewBrewConfigData by mutableStateOf(C_BrewConfigData())
        public set

    var pIDConfigData by mutableStateOf(C_PIDConfigData())
        public set


    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)

    var hasAutoLoadedBrewConfig by mutableStateOf(false)

    // video 1-> Time-Stamp: 1:08:30
    private fun subscribeToChanges() {
        viewModelScope.launch {
            remoteControlReceiveManager.data.collect { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        boilerWaterTemp = result.data.waterTemperature
                    }

                    is Resource.Loading -> {
                        initialazingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
        /*
        viewModelScope.launch {
            remoteControlReceiveManager.dataToRead.collect  { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        boilerSetpointTemp = result.data.targetTemperature
                    }
                }
            }
        }
         */
    }

    private fun subscribeToReadOperation() {
        viewModelScope.launch {
            remoteControlReceiveManager.dataToRead.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        boilerSetpointTemp = result.data.targetTemperature
                    }
                }
            }
        }
        // collect steam setpoint from BLE characteristic
        viewModelScope.launch {
            remoteControlReceiveManager.dataSteamSetpoint.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        steamTempSetpoint = result.data.targetTemperature
                    }
                }
            }
        }

        //ViewScope → Collect data from char: BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataBrewCfgPreInfusionPwr.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        brewConfigData.PreInfussionPwr = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataBrewCfgPreInfusionTmr.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        brewConfigData.PreInfussionTmr = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_BREW_INFUSION_POWER_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataBrewCfg___InfusionPwr.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        brewConfigData.InfussionPwr = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_BREW_INFUSION_TIME__UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataBrewCfg___InfusionTmr.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        brewConfigData.InfussionTmr = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_BREW_DECLINING_PR_POWER_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataBrewCfgDeclining__Pwr.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        brewConfigData.DecliningPwr = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_BREW_DECLINING_PR_TIME__UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataBrewCfgDeclining__Tmr.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        Log.d(
                            "RemoteCtrlViewModel",
                            "View model (collectLatest) dataBrewCfgDeclining__Tmr: ${result.data.charData} "
                        )
                        connectionState = result.data.connectionState
                        brewConfigData.DecliningTmr = result.data.charData
                    }
                }
            }
        }

        //ViewScope → Collect data from char: BLE_CHAR_PID_P_TERM_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataPIDcfgPterm.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        Log.d(
                            "RemoveCtrlViewModel",
                            "View model (collectLatest) dataPIDcfgPterm: ${result.data.charData} "
                        )
                        connectionState = result.data.connectionState
                        pIDConfigData.cfgPterm = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_PID_I_TERM_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataPIDcfgIterm.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        pIDConfigData.cfgIterm = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_PID_I_MAX_TERM_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataPIDcfgImaxterm.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        pIDConfigData.cfgImaxterm = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_PID_I_TERM_WINDUP_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataPIDcfgWindup.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        pIDConfigData.cfgWindup = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_PID_D_TERM_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataPIDcfgDterm.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        pIDConfigData.cfgDterm = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_PID_D_TERM_LPF_UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataPIDcfgDlpfterm.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        pIDConfigData.cfgDlpfterm = result.data.charData
                    }
                }
            }
        }
        //ViewScope → Collect data from char: BLE_CHAR_PID_GAIN___UUID
        viewModelScope.launch {
            remoteControlReceiveManager.dataPIDcfgGainterm.collectLatest { result ->
                when (result) {
                    //Video1 -> TimeStamp: 1:09:30
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        pIDConfigData.cfgGainterm = result.data.charData
                    }
                }
            }
        }

    }

    //public function -> thi one can be call when al permission are granted from user
    //then we can init. connection
    // video 1-> Time-Stamp: 1:11:30
    fun initiazeConnection() {
        errorMessage = null
        subscribeToChanges()
        subscribeToReadOperation()
        //remoteControlReceiveManager.startReceiving()
        remoteControlReceiveManager.startConnection()
    }

    fun sendCommandToDrone() {
        //Log.d("RemScrViewModel","Slide Bar status: $throttle")
        //Log.d("RemScrViewModel","Roll status: $rollAngle")
        //Log.d("RemScrViewModel","Pitch status: $pitchAngle")
        //Log.d("RemScrViewModel","Pitch status: $holdStabilizer")
        //remoteControlReceiveManager.sendCommandtoDrone(throttle,rollAngle,pitchAngle,false)
    }

    fun txNewBoilerTargetTemp(value: Float) {
        boilerSetpointTemp = value
        remoteControlReceiveManager.txNewBoilerTargetTemp(value)
    }

    fun txNewSteamTempSetpoint(value: Float) {
        steamTempSetpoint = value
        remoteControlReceiveManager.txNewSteamTempSetpoint(value)
    }

    fun rxNewBoilerTargetTemp() {
        remoteControlReceiveManager.rxReadBoilerTargetTemp()
    }

    // request updated steam setpoint from BLE device
    fun rxReadSteamTempSetpoint() {
        remoteControlReceiveManager.rxReadSteamTempSetpoint()
    }

    fun txWritePreInfusionPwr(value: Float) = remoteControlReceiveManager.txWritePreInfusionPwr(value)
    fun txWritePreInfusionTmr(value: Float) = remoteControlReceiveManager.txWritePreInfusionTmr(value)
    fun txWriteInfusionPwr(value: Float)    = remoteControlReceiveManager.txWriteInfusionPwr(value)
    fun txWriteInfusionTmr(value: Float)    = remoteControlReceiveManager.txWriteInfusionTmr(value)
    fun txWriteDecliningPwr(value: Float)   = remoteControlReceiveManager.txWriteDecliningPwr(value)
    fun txWriteDecliningTmr(value: Float)   = remoteControlReceiveManager.txWriteDecliningTmr(value)

    fun txWritePidPTerm(value: Float)  = remoteControlReceiveManager.txWritePidPTerm(value)
    fun txWritePidITerm(value: Float)  = remoteControlReceiveManager.txWritePidITerm(value)
    fun txWritePidIMax(value: Float)   = remoteControlReceiveManager.txWritePidIMax(value)
    fun txWritePidWindup(value: Float) = remoteControlReceiveManager.txWritePidWindup(value)
    fun txWritePidDTerm(value: Float)  = remoteControlReceiveManager.txWritePidDTerm(value)
    fun txWritePidDLpf(value: Float)   = remoteControlReceiveManager.txWritePidDLpf(value)
    fun txWritePidGain(value: Float)   = remoteControlReceiveManager.txWritePidGain(value)

    fun rxReadMachineConfiguration() {
        viewModelScope.launch {
            remoteControlReceiveManager.rxReadMachineConfiguration()
        }
    }
    fun rxReadPIDConfiguration() {
        viewModelScope.launch {
            remoteControlReceiveManager.rxReadPIDConfiguration()
        }
    }

    fun reconnect() {
        remoteControlReceiveManager.reconnect()
    }

    fun disconnect() {
        remoteControlReceiveManager.disconnect()
    }

    // video 1-> Time-Stamp: 1:12:30
    override fun onCleared() {
        super.onCleared()
        //had to comment this line to prevent GATT to close connection when chaning screen
        //remoteControlReceiveManager.closeConnection()
    }
}
