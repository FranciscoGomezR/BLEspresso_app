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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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

    // TLDR: Replaced the boolean mutableState flag pattern (passRxvalfromBLEtoVar / passTempValModeltoVar) that composables
    //       flipped to trigger a LaunchedEffect with a delay(). SharedFlow lets the ViewModel push a "data ready" event
    //       directly to any collector; replay=1 ensures a late subscriber (e.g. after config change) still receives the last event.
    private val _pidConfigLoaded = MutableSharedFlow<Unit>(replay = 1)
    val pidConfigLoaded = _pidConfigLoaded.asSharedFlow()

    private val _machineConfigLoaded = MutableSharedFlow<Unit>(replay = 1)
    val machineConfigLoaded = _machineConfigLoaded.asSharedFlow()

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
        // TLDR: Replaced delay(1500L) with a proper completion signal. The BLE manager now emits
        //       on machineConfigReadComplete when its read queue drains (index hits -1), so we
        //       subscribe before firing the read to avoid any race, then await the signal.
        viewModelScope.launch {
            val completed = async { remoteControlReceiveManager.machineConfigReadComplete.first() }
            remoteControlReceiveManager.rxReadMachineConfiguration()
            completed.await()
            _machineConfigLoaded.emit(Unit)
        }
    }
    fun rxReadPIDConfiguration() {
        // TLDR: Same pattern as rxReadMachineConfiguration(). Replaced delay(1500L) with
        //       pidConfigReadComplete signal emitted by the BLE manager when its queue drains.
        viewModelScope.launch {
            val completed = async { remoteControlReceiveManager.pidConfigReadComplete.first() }
            remoteControlReceiveManager.rxReadPIDConfiguration()
            completed.await()
            _pidConfigLoaded.emit(Unit)
        }
    }

    // TLDR: Replaced the 40-line onDoubleTap block in ControllerParameterScreen.kt that iterated every PID field,
    //       checked modified && isNotBlank(), called the matching txWritePid*() function, reset the flag, and
    //       set anyModified. Moved here so the UI only passes current values; the ViewModel decides what to write.
    //       A null argument means "not modified" — skip that field. Returns true if at least one write was issued
    //       so the composable can show the appropriate toast.
    fun writePIDConfig(
        pTerm: Float?, iTerm: Float?, iMax: Float?, windup: Float?,
        dTerm: Float?, dLpf: Float?, gain: Float?
    ): Boolean {
        var anyModified = false
        pTerm?.let  { txWritePidPTerm(it);  Log.d("RemoteCtrlViewModel", "P Term: $it -> Saved");   anyModified = true }
        iTerm?.let  { txWritePidITerm(it);  Log.d("RemoteCtrlViewModel", "I Term: $it -> Saved");   anyModified = true }
        iMax?.let   { txWritePidIMax(it);   Log.d("RemoteCtrlViewModel", "I Max: $it -> Saved");    anyModified = true }
        windup?.let { txWritePidWindup(it); Log.d("RemoteCtrlViewModel", "I Windup: $it -> Saved"); anyModified = true }
        dTerm?.let  { txWritePidDTerm(it);  Log.d("RemoteCtrlViewModel", "D Term: $it -> Saved");   anyModified = true }
        dLpf?.let   { txWritePidDLpf(it);   Log.d("RemoteCtrlViewModel", "D LPF: $it -> Saved");    anyModified = true }
        gain?.let   { txWritePidGain(it);   Log.d("RemoteCtrlViewModel", "Gain: $it -> Saved");     anyModified = true }
        return anyModified
    }

    // TLDR: Replaced the 50-line onDoubleTap block in RemoteControlScreen.kt that did the same check-write-reset
    //       loop for 8 machine-config fields. Same contract as writePIDConfig: pass null for fields that were not
    //       modified, non-null for fields to write. Logging is also centralised here instead of being spread
    //       across the composable.
    fun writeMachineConfig(
        boilerTemp: Float?, steamTemp: Float?,
        preInfPwr: Float?, preInfTmr: Float?,
        infPwr: Float?,    infTmr: Float?,
        declPwr: Float?,   declTmr: Float?
    ): Boolean {
        var anyModified = false
        boilerTemp?.let { txNewBoilerTargetTemp(it);  Log.d("RemoteCtrlViewModel", "Brew Temp: $it -> Saved");        anyModified = true }
        steamTemp?.let  { txNewSteamTempSetpoint(it); Log.d("RemoteCtrlViewModel", "Steam Temp: $it -> Saved");       anyModified = true }
        preInfPwr?.let  { txWritePreInfusionPwr(it);  Log.d("RemoteCtrlViewModel", "Pre-Inf Pwr: $it -> Saved");      anyModified = true }
        preInfTmr?.let  { txWritePreInfusionTmr(it);  Log.d("RemoteCtrlViewModel", "Pre-Inf Tmr: $it -> Saved");      anyModified = true }
        infPwr?.let     { txWriteInfusionPwr(it);     Log.d("RemoteCtrlViewModel", "Infusion Pwr: $it -> Saved");      anyModified = true }
        infTmr?.let     { txWriteInfusionTmr(it);     Log.d("RemoteCtrlViewModel", "Infusion Tmr: $it -> Saved");      anyModified = true }
        declPwr?.let    { txWriteDecliningPwr(it);    Log.d("RemoteCtrlViewModel", "Declining Pwr: $it -> Saved");     anyModified = true }
        declTmr?.let    { txWriteDecliningTmr(it);    Log.d("RemoteCtrlViewModel", "Declining Tmr: $it -> Saved");     anyModified = true }
        return anyModified
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

    // TLDR: Replaced hardcoded magic numbers sprinkled across CTextBox.maxInValue/minInValue assignments in both
    //       composable functions. Centralising bounds here means changing a parameter range only requires editing
    //       one constant; both screens reference it automatically via RemoteControlViewModel.CONSTANT_NAME.
    companion object {
        // PID controller parameter bounds
        const val PID_P_MIN = 0f;       const val PID_P_MAX = 100f
        const val PID_I_MIN = 0f;       const val PID_I_MAX = 20f
        const val PID_ILIMIT_MIN = 0f;  const val PID_ILIMIT_MAX = 500f
        const val PID_WINDUP_MIN = 0f;  const val PID_WINDUP_MAX = 1f
        const val PID_D_MIN = 0f;       const val PID_D_MAX = 10f
        const val PID_LPF_MIN = 5f;     const val PID_LPF_MAX = 250f
        const val PID_GAIN_MIN = 0f;    const val PID_GAIN_MAX = 100f
        // Machine configuration bounds
        const val BOILER_TEMP_MIN = 0f;   const val BOILER_TEMP_MAX = 150f
        const val STEAM_TEMP_MIN = 100f;  const val STEAM_TEMP_MAX = 145f
        const val PRE_INF_PWR_MIN = 0f;   const val PRE_INF_PWR_MAX = 50f
        const val PRE_INF_TMR_MIN = 0f;   const val PRE_INF_TMR_MAX = 15f
        const val INF_PWR_MIN = 0f;       const val INF_PWR_MAX = 100f
        const val INF_TMR_MIN = 1f;       const val INF_TMR_MAX = 15f
        const val DECL_PWR_MIN = 70f;     const val DECL_PWR_MAX = 100f
        const val DECL_TMR_MIN = 0f;      const val DECL_TMR_MAX = 15f
    }
}
