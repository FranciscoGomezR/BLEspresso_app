package com.example.blespresso.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.example.blespresso.data.BoilerDataResult
import com.example.blespresso.data.BoilerSetpoint
import com.example.blespresso.data.CharDataClass
import com.example.blespresso.data.ConnectionState
import com.example.blespresso.data.RemoteControlReceiveManager
import com.example.blespresso.utilities.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


// Tutorial 1 -> Youtube: https://www.youtube.com/watch?v=qyG-SDfYNBE&t=422s&ab_channel=KApps
// vide 1-> Time-Stamp: 25:40

@SuppressLint("MissingPermission")

//this class_Manager takes two constructor arguments
class RemoteControlBLEReceiveManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context  //for the Gradle context
): RemoteControlReceiveManager {

    private val DEVICE_NAME = "BLEspresso"
    private val CONTROL_SERVICE_UUID =
        "00001400-00b0-4240-ba50-05ca45bf8abc"

    private val BLE_CHAR_BLESPRESSO_STATUS__UUID =
        "00001401-00b0-4240-ba50-05ca45bf8abc"
    private val BOILER_WATER_TEMP_CHARACTERISTICS_UUID =
        "00001402-00b0-4240-ba50-05ca45bf8abc"
    private val BOILER_TEMP_SETPOINT_CHARACTERISTICS_UUID =
        "00001403-00b0-4240-ba50-05ca45bf8abc"

    // MCU GATT (ble_cus.h): 0x1403 active setpoint (READ + NOTIFY, set by BREW/STEAM switches),
    // 0x1404 brew preset, 0x1405 steam preset,
    // 0x1406 pre-inf pwr(3B), 0x1407 pre-inf time(3B), 0x1408 inf pwr(4B), 0x1409 inf time(3B),
    // 0x140A declining pwr(4B), 0x140B declining time(3B).
    private val BREW_TEMP_PRESET_CHARACTERISTICS_UUID =
        "00001404-00b0-4240-ba50-05ca45bf8abc"
    private val STEAM_TEMP_PRESET_CHARACTERISTICS_UUID =
        "00001405-00b0-4240-ba50-05ca45bf8abc"

    private val BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID =     //Default value = 35.0%         → 3 digits
        "00001406-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID =     //Default value = 5.0 seconds   → 3 digits
        "00001407-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_INFUSION_POWER_UUID =         //Default value = 99.0%         → 4 digits
        "00001408-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_INFUSION_TIME__UUID =         //Default value = 5.0 seconds   → 3 digits
        "00001409-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_DECLINING_PR_POWER_UUID =     //Default value = 80.0%         → 4 digits
        "0000140a-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_DECLINING_PR_TIME__UUID =     //Default value = 10.0 seconds  → 3 digits
        "0000140b-00b0-4240-ba50-05ca45bf8abc"

    private val CONTROL_PID_SERVICE_UUID =
        "00001500-00b0-4240-ba50-05ca45bf8abc"

    private val BLE_CHAR_PID_P_TERM_UUID =
        "00001501-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_I_TERM_UUID =
        "00001502-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_I_MAX_TERM_UUID =
        "00001503-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_D_TERM_UUID =
        "00001504-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_P_BOOST_UUID =
        "00001505-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_I_BOOST_UUID =
        "00001506-00b0-4240-ba50-05ca45bf8abc"

    // Sequential GATT read queue. Read front-to-back; one bad/failed char is logged and skipped,
    // it never aborts the chain. Completion signal fires once the queue drains.
    // The front element stays in the queue while its read is in flight / being retried, and is
    // only popped once its callback fires (or it's permanently given up on) - this is what lets a
    // BUSY dispatch be retried without losing the char's place in line.
    private val readQueue = ArrayDeque<BluetoothGattCharacteristic>()
    private var isReadingQueue: Boolean = false
    private var currentReadRetryCount: Int = 0
    private val MAX_READ_RETRIES = 5
    private val READ_RETRY_DELAY_MSECS = 150L
    private val MAX_WRITE_RETRIES = 5
    private val WRITE_RETRY_DELAY_MSECS = 150L

    // CCCD (0x2902) enable queue. Notifications must be re-enabled on every connection (firmware
    // has no bonding). Android allows one GATT op at a time, so CCCD writes are chained through
    // onDescriptorWrite instead of fired back-to-back (a second write would be silently dropped).
    // Firmware TODO #24: add BLE_CHAR_BLESPRESSO_STATUS__UUID (0x1401) to this list.
    private val CCCD_NOTIFY_UUIDS = listOf(
        BOILER_WATER_TEMP_CHARACTERISTICS_UUID,     // 0x1402 water temp
        BOILER_TEMP_SETPOINT_CHARACTERISTICS_UUID   // 0x1403 active setpoint
    )
    private val cccdQueue = ArrayDeque<String>()
    private var currentCccdRetryCount: Int = 0

    private enum class ActiveReadOperation { NONE, MACHINE_CONFIG, PID_CONFIG }
    private var activeReadOperation = ActiveReadOperation.NONE

    // Android's GATT stack allows only one outstanding operation at a time. If a second read
    // operation (machine config vs PID config) is requested while one is already in flight, it is
    // held here and started automatically once the current one drains - instead of clobbering the
    // in-flight queue.
    private data class PendingReadRequest(
        val op: ActiveReadOperation,
        val serviceUuid: String,
        val charUuids: List<String>
    )
    private var pendingReadRequest: PendingReadRequest? = null

    private val _machineConfigReadComplete = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val machineConfigReadComplete: SharedFlow<Unit> = _machineConfigReadComplete.asSharedFlow()

    private val _pidConfigReadComplete = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val pidConfigReadComplete: SharedFlow<Unit> = _pidConfigReadComplete.asSharedFlow()

    override val data: MutableSharedFlow<Resource<BoilerDataResult>> = MutableSharedFlow()
    override val dataToWrite: MutableSharedFlow<Resource<BoilerDataResult>> = MutableSharedFlow()
    override val dataToRead: MutableSharedFlow<Resource<BoilerSetpoint>> = MutableSharedFlow()
    override val dataBrewCfgPreInfusionPwr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfgPreInfusionTmr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfg___InfusionPwr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfg___InfusionTmr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfgDeclining__Pwr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfgDeclining__Tmr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    // flows for brew (0x1404) / steam (0x1405) temperature presets
    override val dataBrewTempPreset: MutableSharedFlow<Resource<BoilerSetpoint>> = MutableSharedFlow()
    override val dataSteamTempPreset: MutableSharedFlow<Resource<BoilerSetpoint>> = MutableSharedFlow()

    override val dataPIDcfgPterm:       MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgIterm:       MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgImaxterm:    MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgDterm:       MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgPboost:      MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgIboost:      MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        //SCAN_MODE_LOW_LATENCY) //for scanning a short period of time
        .build()

    //This is the main instance for the connection
    private var gatt: BluetoothGatt? = null
    //This is a flag
    private var isScanning = false
    //this is to collect data by  the viewModel, then emit data to our flow then to the UI
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    //this is variable is and object than inherits from ScanCallback()
    private val scanCallback = object : ScanCallback()
    {
        //Callback when a BLE advertisement has been found.
        override fun onScanResult(callbackType: Int, result: ScanResult?)
        {
            //result.device.address //48bit address in HEX
            //look for specific address / devices
            if (result?.device?.name == DEVICE_NAME)
            {
                val blespressoName = result.device.name
                val blespressoAddress = result.device.address
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Scanning\n for\n $blespressoName Machine"))
                    //text = " Scanning\n for\n Espresso Machine",
                    //data.emit(Resource.Loading(message = "Device Address: $droneBLEaddress")
                }
                Log.d("BluetoothGatt","Device name = $blespressoName")
                //if bluetooth is currently scanning
                if (isScanning)
                {
                    //result.device.connectGatt(context,false,gattCallback,BluetoothDevice.TRANSPORT_LE)
                    result.device.connectGatt(context, false, gattCallback)
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }
        }
    }

    private var currentConnectionAttempt = 1
    private var MAXIMUM_CONNECTION_ATTEMPT = 3
    //this is variable is and object than inherits from BluetoothGattCallback()
    private val gattCallback = object : BluetoothGattCallback()
    {
        //Callback indicating when GATT client has connected/disconnected to/from a remote GATT server.
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int)
        {
            Log.d("BluetoothGatt","Connection State Change -> $status")
            //if the connect operation = GATT operation completed successfully
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                //if  profile is in connected state
                if (newState == BluetoothProfile.STATE_CONNECTED)
                {
                    Log.d("BluetoothGatt","device connected")
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Discovering\n Services..."))
                    }
                    //Discovers services offered by a remote device as well as their characteristics and descriptors.
                    gatt.discoverServices()
                    //now the main instance (line 59) is equal the just discovered GATT
                    this@RemoteControlBLEReceiveManager.gatt = gatt

                    // Emit something that your UI uses to navigate (even a dummy BoilerDataResult)
                    //--------------------------------------------------------------------------------------------------
                    /*
                    coroutineScope.launch {
                        data.emit(Resource.Success(BoilerDataResult(0f, ConnectionState.Connected)))
                    }
                     */
                    //--------------------------------------------------------------------------------------------------
                }//also check if The profile is in disconnected state
                else if (newState == BluetoothProfile.STATE_DISCONNECTED)
                {
                    Log.d("BluetoothGatt","device disconnected")
                    coroutineScope.launch {
                        //if state is disconnected, write default values to var
                        data.emit(Resource.Loading(message = "Device Disconnected"))
                    }
                    gatt.close()
                }
            } else {
                //if GAT is not SUCCESS
                gatt.close()
                currentConnectionAttempt += 1
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Attempting to Connect $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPT"))
                }
                if (currentConnectionAttempt <= MAXIMUM_CONNECTION_ATTEMPT) {
                    startReceiving()
                } else {
                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not connect to Device"))
                    }
                }
            }
        }
        //Callback invoked when the list of remote services, characteristics and descriptors for the remote device have been updated,
        // ie new services have been discovered.
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int)
        {
            with(gatt) {
                Log.d("BluetoothGatt","print device GATT table")
                printGattTable()
                Log.d("BluetoothGatt","GATT table printed")
                gatt.requestMtu(517)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            if (findCharacteristic(CONTROL_SERVICE_UUID, BOILER_WATER_TEMP_CHARACTERISTICS_UUID) == null)
            {
                coroutineScope.launch {
                    data.emit(Resource.Error(errorMessage = "Could not find boiler water temp characteristic"))
                }
                return
            }
            cccdQueue.clear()
            cccdQueue.addAll(CCCD_NOTIFY_UUIDS)
            currentCccdRetryCount = 0
            enableNextNotification()
        }

        // CCCD write finished -> enable the next notification in the queue.
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            Log.d("BLE_Notification", "CCCD write uuid=${descriptor.characteristic.uuid} status=$status")
            if (descriptor.uuid == UUID.fromString(CCCD_DESCRIPTOR_UUID)) {
                enableNextNotification()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            var rxtemperature: Float
            var rxString: String
            var rxPower: Float
            var rxTime: Float
            var rxPIDparam: Float
            var rxPowerValue : CharDataClass = CharDataClass(0.0f, ConnectionState.Uninitialized)
            var rxTimeValue : CharDataClass = CharDataClass(0.0f, ConnectionState.Uninitialized)
            var rxPIDValue : CharDataClass = CharDataClass(0.0f, ConnectionState.Uninitialized)

            Log.d("BLERxManager_BREW_CFG", "Read uuid: ${characteristic.uuid} status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w("BLERxManager_BREW_CFG", "Read FAILED status=$status uuid=${characteristic.uuid} - skipping")
                if (isReadingQueue) {
                    readQueue.removeFirstOrNull()
                    currentReadRetryCount = 0
                    readNextInQueue()
                }
                return
            }
            try {
            with(characteristic)
            {
                when(uuid)
                {
                    // 0x1403 active boiler setpoint -> boilerTemperatureProgressIndicator
                    UUID.fromString(BOILER_TEMP_SETPOINT_CHARACTERISTICS_UUID)->{
                        rxtemperature = parseTemp4(value)
                        coroutineScope.launch {
                            dataToRead.emit(Resource.Success(data = BoilerSetpoint(rxtemperature, ConnectionState.Connected)))
                        }
                        Log.d("BLEReceiverManagerRX", "Boiler temp SETPOINT: $rxtemperature")
                    }
                    // 0x1404 brew temperature preset
                    UUID.fromString(BREW_TEMP_PRESET_CHARACTERISTICS_UUID)->{
                        rxtemperature = parseTemp4(value)
                        coroutineScope.launch {
                            dataBrewTempPreset.emit(Resource.Success(data = BoilerSetpoint(rxtemperature, ConnectionState.Connected)))
                        }
                        Log.d("BLEReceiverManagerRX", "Brew temp PRESET: $rxtemperature")
                    }
                    // 0x1405 steam temperature preset
                    UUID.fromString(STEAM_TEMP_PRESET_CHARACTERISTICS_UUID)->{
                        rxtemperature = parseTemp4(value)
                        coroutineScope.launch {
                            dataSteamTempPreset.emit(Resource.Success(data = BoilerSetpoint(rxtemperature, ConnectionState.Connected)))
                        }
                        Log.d("BLEReceiverManagerRX", "Steam TEMPERATURE: $rxtemperature")
                    }
                    //Catch BREW configuration data
                    UUID.fromString(BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_BREW_CFG", "Read Brew PreInfusion Power char from device: $rxString")
                        rxPower = (rxString[0].code - 48) * 10.0f
                        rxPower += (rxString[1].code - 48) * 1.0f
                        rxPower += (rxString[2].code - 48) * 0.1f
                        rxPowerValue = CharDataClass( rxPower,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataBrewCfgPreInfusionPwr.emit(Resource.Success(data= rxPowerValue))
                        }
                        Log.d("BLERxManager_BREW_CFG", "Sending: $rxPower to screen")
                    }
                    UUID.fromString(BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_BREW_CFG", "Read Brew PreInfusion time char from device: $rxString")
                        rxTime = (rxString[0].code - 48) * 10.0f
                        rxTime += (rxString[1].code - 48) * 1.0f
                        rxTime += (rxString[2].code - 48) * 0.1f
                        rxTimeValue = CharDataClass( rxTime,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataBrewCfgPreInfusionTmr.emit(Resource.Success(data= rxTimeValue))
                        }
                        Log.d("BLERxManager_BREW_CFG", "Sending: $rxTime to screen")
                    }
                    UUID.fromString(BLE_CHAR_BREW_INFUSION_POWER_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_BREW_CFG", "Read Brew Infusion Power char from device: $rxString")
                        rxPower = (rxString[0].code - 48) * 100.0f
                        rxPower += (rxString[1].code - 48) * 10.0f
                        rxPower += (rxString[2].code - 48) * 1.0f
                        rxPower += (rxString[3].code - 48) * 0.1f
                        rxPowerValue = CharDataClass( rxPower,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataBrewCfg___InfusionPwr.emit(Resource.Success(data= rxPowerValue))
                        }
                        Log.d("BLERxManager_BREW_CFG", "Sending: $rxPower to screen")
                    }
                    UUID.fromString(BLE_CHAR_BREW_INFUSION_TIME__UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_BREW_CFG", "Read Brew Infusion time char from device: $rxString")
                        rxTime = (rxString[0].code - 48) * 10.0f
                        rxTime += (rxString[1].code - 48) * 1.0f
                        rxTime += (rxString[2].code - 48) * 0.1f
                        rxTimeValue = CharDataClass( rxTime,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataBrewCfg___InfusionTmr.emit(Resource.Success(data= rxTimeValue))
                        }
                        Log.d("BLERxManager_BREW_CFG", "Sending: $rxTime to screen")
                    }
                    UUID.fromString(BLE_CHAR_BREW_DECLINING_PR_POWER_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_BREW_CFG", "Read Brew Declining Pressure Power char from device: $rxString")
                        rxPower = (rxString[0].code - 48) * 100.0f
                        rxPower += (rxString[1].code - 48) * 10.0f
                        rxPower += (rxString[2].code - 48) * 1.0f
                        rxPower += (rxString[3].code - 48) * 0.1f
                        rxPowerValue = CharDataClass( rxPower,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataBrewCfgDeclining__Pwr.emit(Resource.Success(data= rxPowerValue))
                        }
                        Log.d("BLERxManager_BREW_CFG", "Sending: $rxPower to screen")
                    }
                    UUID.fromString(BLE_CHAR_BREW_DECLINING_PR_TIME__UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_BREW_CFG", "Read Brew Declining Pressure time char from device: $rxString")
                        rxTime = (rxString[0].code - 48) * 10.0f
                        rxTime += (rxString[1].code - 48) * 1.0f
                        rxTime += (rxString[2].code - 48) * 0.1f
                        rxTimeValue = CharDataClass( rxTime,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataBrewCfgDeclining__Tmr.emit(Resource.Success(data= rxTimeValue))
                        }
                        Log.d("BLERxManager_BREW_CFG", "Sending: $rxTime to screen")
                    }
                    //Catch PID configuration data
                    //-------------------------------------------------------------------------------------------------
                    UUID.fromString(BLE_CHAR_PID_P_TERM_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID Proportional term from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 100.0f
                        rxPIDparam += (rxString[1].code - 48) * 10.0f
                        rxPIDparam += (rxString[2].code - 48) * 1.0f
                        rxPIDparam += (rxString[3].code - 48) * 0.1f
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgPterm.emit(Resource.Success(data= rxPIDValue))
                        }
                        Log.d("BLERxManager_PID_CFG", "Sending: ${rxPIDValue.charData} to screen")
                    }
                    UUID.fromString(BLE_CHAR_PID_I_TERM_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID Integral term from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 10.0f
                        rxPIDparam += (rxString[1].code - 48) * 1.0f
                        rxPIDparam += (rxString[2].code - 48) * 0.1f
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgIterm.emit(Resource.Success(data= rxPIDValue))
                        }
                        Log.d("BLERxManager_PID_CFG", "Sending: $rxPIDparam to screen")
                    }
                    UUID.fromString(BLE_CHAR_PID_I_MAX_TERM_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID Integral MaxValue term from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 100.0f
                        rxPIDparam += (rxString[1].code - 48) * 10.0f
                        rxPIDparam += (rxString[2].code - 48) * 1.0f
                        rxPIDparam += (rxString[3].code - 48) * 0.1f
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgImaxterm.emit(Resource.Success(data= rxPIDValue))
                        }
                        Log.d("BLERxManager_PID_CFG", "Sending: $rxPIDparam to screen")
                    }
                    UUID.fromString(BLE_CHAR_PID_D_TERM_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID Derivative term from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 10.0f
                        rxPIDparam += (rxString[1].code - 48) * 1.0f
                        rxPIDparam += (rxString[2].code - 48) * 0.1f
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgDterm.emit(Resource.Success(data= rxPIDValue))
                        }
                        Log.d("BLERxManager_PID_CFG", "Sending: $rxPIDparam to screen")
                    }
                    UUID.fromString(BLE_CHAR_PID_P_BOOST_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID Phase-1 P-gain boost from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 10.0f
                        rxPIDparam += (rxString[1].code - 48) * 1.0f
                        rxPIDparam += (rxString[2].code - 48) * 0.1f
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgPboost.emit(Resource.Success(data= rxPIDValue))
                        }
                        Log.d("BLERxManager_PID_CFG", "Sending: $rxPIDparam to screen")
                    }
                    UUID.fromString(BLE_CHAR_PID_I_BOOST_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID Phase-1 I-gain boost from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 10.0f
                        rxPIDparam += (rxString[1].code - 48) * 1.0f
                        rxPIDparam += (rxString[2].code - 48) * 0.1f
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgIboost.emit(Resource.Success(data= rxPIDValue))
                        }
                        Log.d("BLERxManager_PID_CFG", "Sending: $rxPIDparam to screen")
                    }
                    else -> Unit
                }
            }
            } catch (e: Exception) {
                Log.e("BLERxManager_BREW_CFG", "Parse error uuid=${characteristic.uuid}: ${e.message}")
            }
            if (isReadingQueue) {
                readQueue.removeFirstOrNull()
                currentReadRetryCount = 0
                readNextInQueue()
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                with(characteristic) {
                    when (uuid) {
                        UUID.fromString(BREW_TEMP_PRESET_CHARACTERISTICS_UUID) ->
                            Log.d("BLE_ManagerTX", "Brew Temperature Preset Write Successful")
                        UUID.fromString(STEAM_TEMP_PRESET_CHARACTERISTICS_UUID) ->
                            Log.d("BLE_ManagerTX", "Steam Temperature Preset Write Successful")
                        UUID.fromString(BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID) ->
                            Log.d("BLE_ManagerTX", "BREW -PRE_INFUSION_POWER-> Write Successful")
                        UUID.fromString(BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID) ->
                            Log.d("BLE_ManagerTX", "BREW -PRE_INFUSION_TIME-> Write Successful")
                        UUID.fromString(BLE_CHAR_BREW_INFUSION_POWER_UUID) ->
                            Log.d("BLE_ManagerTX", "BREW -INFUSION_POWER-> Write Successful")
                        UUID.fromString(BLE_CHAR_BREW_INFUSION_TIME__UUID) ->
                            Log.d("BLE_ManagerTX", "BREW -INFUSION_TIME-> Write Successful")
                        UUID.fromString(BLE_CHAR_BREW_DECLINING_PR_POWER_UUID) ->
                            Log.d("BLE_ManagerTX", "BREW -DECLINING_PR_POWER-> Write Successful")
                        UUID.fromString(BLE_CHAR_BREW_DECLINING_PR_TIME__UUID) ->
                            Log.d("BLE_ManagerTX", "BREW -DECLINING_PR_TIME-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_P_TERM_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -P_TERM-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_I_TERM_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -I_TERM-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_I_MAX_TERM_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -I_MAX_TERM-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_D_TERM_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -D_TERM-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_P_BOOST_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -P_BOOST-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_I_BOOST_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -I_BOOST-> Write Successful")
                        else -> Unit
                    }
                }
            } else {
                Log.d("BLE_ManagerTX", "Write FAILED status=$status uuid=${characteristic.uuid}")
            }
        }

        // vide 1-> Time-Stamp: 58:20
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic)
            {
                when(uuid){
                    UUID.fromString(BOILER_WATER_TEMP_CHARACTERISTICS_UUID)->{
                        val temperature = parseTemp4(value)
                        Log.d("BLEReceiverManager2", "$temperature")
                        val waterTemp = BoilerDataResult(
                            temperature,
                            ConnectionState.Connected)
                        //write  data read from BLe device to "data" value
                        //data is define in RemoteControlReceiveManaher.kt
                        coroutineScope.launch {
                            data.emit(Resource.Success(data = waterTemp))
                        }
                    }
                    // 0x1403 active boiler setpoint: firmware notifies on BREW/STEAM switch change
                    UUID.fromString(BOILER_TEMP_SETPOINT_CHARACTERISTICS_UUID)->{
                        val setpoint = parseTemp4(value)
                        Log.d("BLEReceiverManager2", "Boiler temp SETPOINT notify: $setpoint")
                        coroutineScope.launch {
                            dataToRead.emit(Resource.Success(data = BoilerSetpoint(setpoint, ConnectionState.Connected)))
                        }
                    }
                    UUID.fromString(BLE_CHAR_BLESPRESSO_STATUS__UUID)->{
                        //Data format received from BLE device
                        Log.d("BLEReceiverManager2", "$value")
                    }
                    else -> Unit
                }
            }
        }
    }

    private enum class WriteDispatchResult { DISPATCHED, NOT_CONNECTED, BUSY }

    /** Attempts to dispatch a write. BUSY means the GATT stack has another op in flight -
     *  transient, worth retrying (same hazard as the read side: Android allows only one
     *  outstanding GATT operation at a time). */
    private fun writeCharacteristicsDevice(characteristic: BluetoothGattCharacteristic, payload: ByteArray): WriteDispatchResult
    {
        val g = gatt
        if (g == null) {
            Log.w("BLEReceiverManager2", "writeCharacteristicsDevice: not connected")
            return WriteDispatchResult.NOT_CONNECTED
        }
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        characteristic.value = payload
        // Convert the payload to a hex string and print it
        val hexString = payload.joinToString(" ") { it.toString(16).padStart(2, '0') }
        println("Characteristic UUID: ${characteristic.uuid}")
        println("Payload (hex): $hexString")

        return if (g.writeCharacteristic(characteristic)) WriteDispatchResult.DISPATCHED else WriteDispatchResult.BUSY
    }

    /** Dispatches a write, retrying on a transient BUSY result instead of silently dropping it
     *  (mirrors the read queue's BUSY retry). Calls [onDone] once the write is dispatched or
     *  retries are exhausted, so a caller can safely chain a follow-up write after this one has
     *  left the stack instead of firing both at once. */
    private fun writeWithRetry(
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray,
        retryCount: Int = 0,
        onDone: () -> Unit = {}
    ) {
        when (writeCharacteristicsDevice(characteristic, payload)) {
            WriteDispatchResult.DISPATCHED, WriteDispatchResult.NOT_CONNECTED -> onDone()
            WriteDispatchResult.BUSY -> {
                if (retryCount < MAX_WRITE_RETRIES) {
                    Log.w("BLEReceiverManager2", "GATT busy, retry ${retryCount + 1}/$MAX_WRITE_RETRIES for write ${characteristic.uuid}")
                    coroutineScope.launch {
                        delay(WRITE_RETRY_DELAY_MSECS)
                        writeWithRetry(characteristic, payload, retryCount + 1, onDone)
                    }
                } else {
                    Log.e("BLEReceiverManager2", "GATT busy, giving up on write ${characteristic.uuid} after $MAX_WRITE_RETRIES retries")
                    onDone()
                }
            }
        }
    }

    // TLDR: Helper that encodes a Float to a fixed-width ASCII digit string and writes it
    //       to the specified GATT characteristic via the given service and char UUIDs. Retries on
    //       a transient GATT-busy dispatch instead of silently dropping the write; onDone lets a
    //       caller chain a follow-up write after this one leaves the stack.
    private fun writeCharacteristic(serviceUuid: String, charUuid: String, value: Float, digits: Int, onDone: () -> Unit = {}) {
        val sb = StringBuilder()
        dataConvertionFloatToCharacteristic(value, sb, digits)
        val characteristicToWr = findCharacteristic(serviceUuid, charUuid)
        if (characteristicToWr != null) {
            writeWithRetry(characteristicToWr, sb.toString().toByteArray(), onDone = onDone)
        } else {
            Log.w("BLEReceiverManager2", "char not found on device, skipping write: $charUuid")
            onDone()
        }
    }

    private enum class ReadDispatchResult { DISPATCHED, NOT_READABLE, BUSY, NOT_CONNECTED }

    /** Attempts to dispatch a read. BUSY means the GATT stack has another op in flight - transient,
     *  worth retrying. NOT_READABLE/NOT_CONNECTED are permanent for this characteristic. */
    private fun readCharacteristicsDevice(characteristic: BluetoothGattCharacteristic): ReadDispatchResult
    {
        val g = gatt
        if (g == null) {
            Log.w("BLEReceiverManager2", "readCharacteristicsDevice: not connected")
            return ReadDispatchResult.NOT_CONNECTED
        }
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ == 0) {
            Log.w("BLEReceiverManager2", "Characteristic ${characteristic.uuid} does not support reading.")
            return ReadDispatchResult.NOT_READABLE
        }
        return if (g.readCharacteristic(characteristic)) ReadDispatchResult.DISPATCHED else ReadDispatchResult.BUSY
    }

    /** Drives the sequential read queue. The front element is only popped once its callback fires
     *  (see onCharacteristicRead) or it's permanently given up on here; a BUSY dispatch is retried
     *  in place after a short delay instead of being treated as a failure. On drain, emits the
     *  matching completion signal, then starts any pending read request that arrived meanwhile. */
    private fun readNextInQueue() {
        val next = readQueue.firstOrNull()
        if (next == null) {
            isReadingQueue = false
            currentReadRetryCount = 0
            val op = activeReadOperation
            activeReadOperation = ActiveReadOperation.NONE
            Log.d("BLEReceiverManager2", "Read queue complete ($op)")
            coroutineScope.launch {
                when (op) {
                    ActiveReadOperation.MACHINE_CONFIG -> _machineConfigReadComplete.emit(Unit)
                    ActiveReadOperation.PID_CONFIG     -> _pidConfigReadComplete.emit(Unit)
                    ActiveReadOperation.NONE           -> Unit
                }
            }
            val pending = pendingReadRequest
            if (pending != null) {
                pendingReadRequest = null
                Log.d("BLEReceiverManager2", "Starting pending read request (${pending.op})")
                startReadQueue(pending.op, pending.serviceUuid, pending.charUuids)
            }
            return
        }
        when (readCharacteristicsDevice(next)) {
            ReadDispatchResult.DISPATCHED -> {
                currentReadRetryCount = 0
                Log.d("BLEReceiverManager2", "Read Next CHAR: ${next.uuid}")
                // wait for onCharacteristicRead callback to pop this element and advance
            }
            ReadDispatchResult.BUSY -> {
                currentReadRetryCount++
                if (currentReadRetryCount <= MAX_READ_RETRIES) {
                    Log.w("BLEReceiverManager2", "GATT busy, retry $currentReadRetryCount/$MAX_READ_RETRIES for ${next.uuid}")
                    coroutineScope.launch {
                        delay(READ_RETRY_DELAY_MSECS)
                        readNextInQueue()
                    }
                } else {
                    Log.e("BLEReceiverManager2", "GATT busy, giving up on ${next.uuid} after $MAX_READ_RETRIES retries")
                    readQueue.removeFirstOrNull()
                    currentReadRetryCount = 0
                    readNextInQueue()
                }
            }
            ReadDispatchResult.NOT_READABLE, ReadDispatchResult.NOT_CONNECTED -> {
                Log.w("BLEReceiverManager2", "permanently skipping ${next.uuid}")
                readQueue.removeFirstOrNull()
                currentReadRetryCount = 0
                readNextInQueue()
            }
        }
    }

    /** Builds the queue from whatever characteristics actually exist and starts reading. If a read
     *  operation is already in flight, holds this request as pending instead of clobbering it. */
    private fun startReadQueue(op: ActiveReadOperation, serviceUuid: String, charUuids: List<String>) {
        if (isReadingQueue) {
            Log.w("BLEReceiverManager2", "Read op ($op) requested while (${activeReadOperation}) in flight - queued as pending")
            pendingReadRequest = PendingReadRequest(op, serviceUuid, charUuids)
            return
        }
        activeReadOperation = op
        readQueue.clear()
        for (u in charUuids) {
            val c = findCharacteristic(serviceUuid, u)
            if (c != null) readQueue.add(c)
            else Log.w("BLEReceiverManager2", "char not found on device, skipping: $u")
        }
        isReadingQueue = true
        currentReadRetryCount = 0
        readNextInQueue()
    }

    /** Pops the next char from cccdQueue and enables its notification. Advanced by
     *  onDescriptorWrite; a BUSY CCCD write is retried in place (same backoff as reads/writes).
     *  A char that is missing or has no CCCD is skipped. */
    private fun enableNextNotification() {
        val charUuid = cccdQueue.firstOrNull() ?: return
        val characteristic = findCharacteristic(CONTROL_SERVICE_UUID, charUuid)
        when (if (characteristic != null) enableNotification(characteristic) else CccdDispatchResult.SKIPPED) {
            CccdDispatchResult.DISPATCHED -> {
                cccdQueue.removeFirstOrNull()
                currentCccdRetryCount = 0
                // wait for onDescriptorWrite to advance
            }
            CccdDispatchResult.BUSY -> {
                currentCccdRetryCount++
                if (currentCccdRetryCount <= MAX_WRITE_RETRIES) {
                    Log.w("BLE_Notification", "GATT busy, CCCD retry $currentCccdRetryCount/$MAX_WRITE_RETRIES for $charUuid")
                    coroutineScope.launch {
                        delay(WRITE_RETRY_DELAY_MSECS)
                        enableNextNotification()
                    }
                } else {
                    Log.e("BLE_Notification", "GATT busy, giving up CCCD for $charUuid")
                    cccdQueue.removeFirstOrNull()
                    currentCccdRetryCount = 0
                    enableNextNotification()
                }
            }
            CccdDispatchResult.SKIPPED -> {
                Log.w("BLE_Notification", "No notifiable char/CCCD, skipping $charUuid")
                cccdQueue.removeFirstOrNull()
                currentCccdRetryCount = 0
                enableNextNotification()
            }
        }
    }

    private enum class CccdDispatchResult { DISPATCHED, BUSY, SKIPPED }

    //video1 - timestamp: 51:05
    private fun enableNotification(characteristic: BluetoothGattCharacteristic): CccdDispatchResult
    {
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return CccdDispatchResult.SKIPPED
        }
        val cccdDescriptor = characteristic.getDescriptor(cccdUuid) ?: return CccdDispatchResult.SKIPPED
        if (gatt?.setCharacteristicNotification(characteristic, true) != true) {
            Log.d("BLEReceiverManager", "Set characteristics notification failed")
            return CccdDispatchResult.SKIPPED
        }
        return if (writeDescriptor(cccdDescriptor, payload)) CccdDispatchResult.DISPATCHED else CccdDispatchResult.BUSY
    }

    /** Over-the-air CCCD write. false = not connected or GATT busy. */
    private fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray): Boolean {
        val g = gatt ?: return false
        descriptor.value = payload
        return g.writeDescriptor(descriptor)
    }

    /** Parses a 4-byte ASCII temperature "DDDd" (last digit = tenth) e.g. "0935" -> 93.5. */
    private fun parseTemp4(bytes: ByteArray): Float =
        (bytes[0] - 48) * 100.0f + (bytes[1] - 48) * 10.0f + (bytes[2] - 48) * 1.0f + (bytes[3] - 48) * 0.1f

    private fun findCharacteristic(serviceUUID: String, characteristicsUUID: String): BluetoothGattCharacteristic? {
        return gatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find {
                characteristics -> characteristics.uuid.toString() == characteristicsUUID
        }
    }



    override fun rxReadBoilerTempSetpoint()
    {
        val characteristictoRead = findCharacteristic(CONTROL_SERVICE_UUID, BOILER_TEMP_SETPOINT_CHARACTERISTICS_UUID)
        if (characteristictoRead != null)
        {
            Log.d("BLE_rxTargetTemp", "About to READ Boiler Temp Setpoint")
            readCharacteristicsDevice(characteristictoRead)
        }
    }

    override fun rxReadBrewTempPreset() {
        val characteristictoRead = findCharacteristic(CONTROL_SERVICE_UUID, BREW_TEMP_PRESET_CHARACTERISTICS_UUID)
        if (characteristictoRead != null) {
            Log.d("BLE_rxBrewTemp", "About to READ Brew Temp Preset")
            readCharacteristicsDevice(characteristictoRead)
        }
    }

    override fun rxReadSteamTempPreset() {
        val characteristictoRead = findCharacteristic(CONTROL_SERVICE_UUID, STEAM_TEMP_PRESET_CHARACTERISTICS_UUID)
        if (characteristictoRead != null) {
            Log.d("BLE_rxSteamTemp", "About to READ Steam Temp Preset")
            readCharacteristicsDevice(characteristictoRead)
        }
    }

    // TLDR: Writes the brew temperature preset 0x1404 (4-digit ASCII format, e.g. 93.5°C → "0935").
    //       0x1403 (active setpoint) is READ + NOTIFY only: firmware sets it from the presets.
    override fun txNewBrewTempPreset(newTarget: Float) = writeCharacteristic(CONTROL_SERVICE_UUID, BREW_TEMP_PRESET_CHARACTERISTICS_UUID, newTarget, 4)
    // TLDR: Writes the steam temperature preset 0x1405 (4-digit ASCII format, e.g. 140.0°C → "1400").
    override fun txNewSteamTempPreset(newTarget: Float) = writeCharacteristic(CONTROL_SERVICE_UUID, STEAM_TEMP_PRESET_CHARACTERISTICS_UUID, newTarget, 4)

    // TLDR: Writes pre-infusion pump power % to device (3-digit format, e.g. 35.0% → "350").
    override fun txWritePreInfusionPwr(value: Float) = writeCharacteristic(CONTROL_SERVICE_UUID, BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID,     value, 3)
    // TLDR: Writes pre-infusion phase duration in seconds to device (3-digit format, e.g. 5.0s → "050").
    override fun txWritePreInfusionTmr(value: Float) = writeCharacteristic(CONTROL_SERVICE_UUID, BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID,     value, 3)
    // TLDR: Writes full infusion pump power % to device (4-digit format, e.g. 99.0% → "0990").
    override fun txWriteInfusionPwr(value: Float)    = writeCharacteristic(CONTROL_SERVICE_UUID, BLE_CHAR_BREW_INFUSION_POWER_UUID,         value, 4)
    // TLDR: Writes full infusion phase duration in seconds to device (3-digit format, e.g. 5.0s → "050").
    override fun txWriteInfusionTmr(value: Float)    = writeCharacteristic(CONTROL_SERVICE_UUID, BLE_CHAR_BREW_INFUSION_TIME__UUID,         value, 3)
    // TLDR: Writes declining pressure pump power % to device (4-digit format, e.g. 80.0% → "0800").
    override fun txWriteDecliningPwr(value: Float)   = writeCharacteristic(CONTROL_SERVICE_UUID, BLE_CHAR_BREW_DECLINING_PR_POWER_UUID,     value, 4)
    // TLDR: Writes declining pressure phase duration in seconds to device (3-digit format, e.g. 10.0s → "100").
    override fun txWriteDecliningTmr(value: Float)   = writeCharacteristic(CONTROL_SERVICE_UUID, BLE_CHAR_BREW_DECLINING_PR_TIME__UUID,     value, 3)


    // TLDR: Writes PID Proportional term to device (4-digit format, e.g. 12.5 → "0125").
    override fun txWritePidPTerm(value: Float)  = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_P_TERM_UUID,        value, 4)
    // TLDR: Writes PID Integral term to device (3-digit format, e.g. 1.5 → "015").
    override fun txWritePidITerm(value: Float)  = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_I_TERM_UUID,        value, 3)
    // TLDR: Writes PID Integral max clamp value to device (4-digit format, e.g. 50.0 → "0500").
    override fun txWritePidIMax(value: Float)   = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_I_MAX_TERM_UUID,    value, 4)
    // TLDR: Writes PID Derivative term to device (3-digit format, e.g. 2.5 → "025").
    override fun txWritePidDTerm(value: Float)  = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_D_TERM_UUID,        value, 3)
    // TLDR: Writes PID phase-1 proportional-gain boost multiplier to device (3-digit format, e.g. 1.0 → "010").
    override fun txWritePidPBoost(value: Float) = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_P_BOOST_UUID,       value, 3)
    // TLDR: Writes PID phase-1 integral-gain boost multiplier to device (3-digit format, e.g. 6.5 → "065").
    override fun txWritePidIBoost(value: Float) = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_I_BOOST_UUID,       value, 3)

    // TLDR: Reads all 9 machine-configuration UUIDs in a single GATT-safe sequential queue:
    //       boiler temp setpoint → brew preset → steam preset → 6 brew params.
    //       Queue is processed tail-to-head via onCharacteristicRead callbacks.
    override suspend fun rxReadMachineConfiguration() {
        Log.d("BLE_rxMachineCfg", "About to READ machine configuration characteristics")
        startReadQueue(
            ActiveReadOperation.MACHINE_CONFIG,
            CONTROL_SERVICE_UUID,
            listOf(
                BOILER_TEMP_SETPOINT_CHARACTERISTICS_UUID, // 0x1403 active setpoint
                BREW_TEMP_PRESET_CHARACTERISTICS_UUID,     // 0x1404 brew preset
                STEAM_TEMP_PRESET_CHARACTERISTICS_UUID,    // 0x1405 steam preset
                BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID,    // 0x1406
                BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID,    // 0x1407
                BLE_CHAR_BREW_INFUSION_POWER_UUID,        // 0x1408
                BLE_CHAR_BREW_INFUSION_TIME__UUID,        // 0x1409
                BLE_CHAR_BREW_DECLINING_PR_POWER_UUID,    // 0x140A
                BLE_CHAR_BREW_DECLINING_PR_TIME__UUID     // 0x140B
            )
        )
    }

    override suspend fun rxReadPIDConfiguration(){
        Log.d("BLE_rxTargetTemp", "About to READ PID configuration")
        startReadQueue(
            ActiveReadOperation.PID_CONFIG,
            CONTROL_PID_SERVICE_UUID,
            listOf(
                BLE_CHAR_PID_P_TERM_UUID,
                BLE_CHAR_PID_I_TERM_UUID,
                BLE_CHAR_PID_I_MAX_TERM_UUID,
                BLE_CHAR_PID_D_TERM_UUID,
                BLE_CHAR_PID_P_BOOST_UUID,
                BLE_CHAR_PID_I_BOOST_UUID
            )
        )
    }



    override fun startConnection() {
        coroutineScope.launch{
            data.emit(Resource.Loading(message = "CONNECT \nTO ESPRESSO \nMACHINE"))
        }
        isScanning = true
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    //
    override fun startReceiving() {
        coroutineScope.launch{
            data.emit(Resource.Loading(message = "CONNECT \nTO ESPRESSO \nMACHINE"))
        }
        isScanning = true
        bleScanner.startScan(null, scanSettings, scanCallback)

    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }
    // vide 1-> Time-Stamp: 53:00
    // No CCCD disable writes: firmware (no bonding) resets every CCCD on disconnect.
    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        cccdQueue.clear()
        gatt?.close()
    }

    private fun dataConvertionFloatToCharacteristic(data: Float, charTxData: StringBuilder, txStringDigits: Int)
    {
        var newTxData: Int
        when(txStringDigits){
            4 ->{
                newTxData = (data*10f).toInt()
                when
                {
                    newTxData > 999 ->
                    {
                        charTxData.append(newTxData.toString())
                    }
                    newTxData in 100..999 ->
                    {
                        charTxData.append("0")
                        charTxData.append(newTxData.toString())
                    }
                    newTxData in 10..99 ->
                    {
                        charTxData.append("00")
                        charTxData.append(newTxData.toString())
                    }
                    newTxData in 0..9 ->
                    {
                        charTxData.append("000")
                        charTxData.append(newTxData.toString())
                    }
                }
            }
            3 ->{
                newTxData = (data*10f).toInt()
                when (newTxData) {
                    in 100..999 -> {
                        charTxData.append(newTxData.toString())
                    }
                    in 10..99 -> {
                        charTxData.append("0")
                        charTxData.append(newTxData.toString())
                    }
                    in 0..9 -> {
                        charTxData.append("00")
                        charTxData.append(newTxData.toString())
                    }
                    else -> {charTxData.append("000")}
                }
            }
            2 ->{ }
            1 ->{
                if ( data >= 1.0f)
                {
                    charTxData.append("1")
                }else{
                    charTxData.append("0")
                }
            }
            else -> Unit
        }
    }

}