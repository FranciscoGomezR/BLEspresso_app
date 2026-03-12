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
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private val SET_NEW_BOILER_TEMP_CHARACTERISTICS_UUID =
        "00001403-00b0-4240-ba50-05ca45bf8abc"

    // new steam setpoint characteristic (ends in 140A)
    private val STEAM_TEMP_SETPOINT_CHARACTERISTICS_UUID =
        "0000140a-00b0-4240-ba50-05ca45bf8abc"

    private val BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID =     //Default value = 35.0%         → 3 digits
        "00001404-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID =     //Default value = 5.0 seconds   → 3 digits
        "00001405-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_INFUSION_POWER_UUID =         //Default value = 99.0%         → 4 digits
        "00001406-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_INFUSION_TIME__UUID =         //Default value = 5.0 seconds   → 3 digits
        "00001407-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_DECLINING_PR_POWER_UUID =     //Default value = 80.0%         → 4 digits
        "00001408-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_BREW_DECLINING_PR_TIME__UUID =     //Default value = 10.0 seconds  → 3 digits
        "00001409-00b0-4240-ba50-05ca45bf8abc"

    private val CONTROL_PID_SERVICE_UUID =
        "00001500-00b0-4240-ba50-05ca45bf8abc"

    private val BLE_CHAR_PID_P_TERM_UUID =
        "00001501-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_I_TERM_UUID =
        "00001502-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_I_MAX_TERM_UUID =
        "00001503-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_I_TERM_WINDUP_UUID =
        "00001504-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_D_TERM_UUID =
        "00001505-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_D_TERM_LPF_UUID =
        "00001506-00b0-4240-ba50-05ca45bf8abc"
    private val BLE_CHAR_PID_GAIN___UUID =
        "00001507-00b0-4240-ba50-05ca45bf8abc"

    private var characteristicToReadQueue = ArrayList<Any>()
    private var characteristicToReadQueueIndex: Int = 0
    private var characteristicToReadFLAG: Boolean = true

    override val data: MutableSharedFlow<Resource<BoilerDataResult>> = MutableSharedFlow()
    override val dataToWrite: MutableSharedFlow<Resource<BoilerDataResult>> = MutableSharedFlow()
    override val dataToRead: MutableSharedFlow<Resource<BoilerSetpoint>> = MutableSharedFlow()
    override val dataBrewCfgPreInfusionPwr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfgPreInfusionTmr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfg___InfusionPwr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfg___InfusionTmr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfgDeclining__Pwr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataBrewCfgDeclining__Tmr: MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    // flow for new steam setpoint value
    override val dataSteamSetpoint: MutableSharedFlow<Resource<BoilerSetpoint>> = MutableSharedFlow()

    override val dataPIDcfgPterm:       MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgIterm:       MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgImaxterm:    MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgWindup:      MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgDterm:       MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgDlpfterm:    MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()
    override val dataPIDcfgGainterm:    MutableSharedFlow<Resource<CharDataClass>> = MutableSharedFlow()

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
            val characteristic = findCharacteristic(CONTROL_SERVICE_UUID,BOILER_WATER_TEMP_CHARACTERISTICS_UUID)
            if(characteristic == null)
            {
                coroutineScope.launch {
                    data.emit(Resource.Error(errorMessage = "Could not find boiler water temp characteristic"))
                }
                return
            }
            enableNotification(characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            var rxString: String
            var rxPower: Float
            var rxTime: Float
            var rxPIDparam: Float
            var rxPowerValue : CharDataClass = CharDataClass(0.0f, ConnectionState.Uninitialized)
            var rxTimeValue : CharDataClass = CharDataClass(0.0f, ConnectionState.Uninitialized)
            var rxPIDValue : CharDataClass = CharDataClass(0.0f, ConnectionState.Uninitialized)

            Log.d("BLERxManager_BREW_CFG", "Read uuid: ${characteristic.uuid}")
            with(characteristic)
            {
                when(uuid)
                {
                    UUID.fromString(SET_NEW_BOILER_TEMP_CHARACTERISTICS_UUID)->{
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLEReceiverManager2", "I have read: $rxString")
                        //Data format received from Bbyte is inLE device
                        var rxtemperature = (rxString[0].code - 48) * 100.0f
                        rxtemperature += (rxString[1].code - 48) * 10.0f
                        rxtemperature += (rxString[2].code - 48) * 1.0f
                        rxtemperature += (rxString[3].code - 48) * 0.1f
                        //write  data read from BLe device to "data" value
                        //data is define in RemoteControlReceiveManager.kt
                        val targetTemp = BoilerSetpoint(
                            rxtemperature,
                            ConnectionState.Connected)
                        //write  data read from BLe device to "data" value
                        //data is define in RemoteControlReceiveManaher.kt
                        coroutineScope.launch {
                            dataToRead.emit(Resource.Success(data = targetTemp))
                            //data.emit(Resource.Success(data = targetTemp))
                        }
                        Log.d("BLEReceiverManagerRX", "Target TEMPERATURE: $rxtemperature")
                    }
                    UUID.fromString(STEAM_TEMP_SETPOINT_CHARACTERISTICS_UUID)->{
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLEReceiverManager2", "I have read steam setpoint: $rxString")
                        // parse same way as boiler setpoint
                        var rxtemperature = (rxString[0].code - 48) * 100.0f
                        rxtemperature += (rxString[1].code - 48) * 10.0f
                        rxtemperature += (rxString[2].code - 48) * 1.0f
                        rxtemperature += (rxString[3].code - 48) * 0.1f
                        val steamTemp = BoilerSetpoint(
                            rxtemperature,
                            ConnectionState.Connected)
                        coroutineScope.launch {
                            dataSteamSetpoint.emit(Resource.Success(data = steamTemp))
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
                    UUID.fromString(BLE_CHAR_PID_I_TERM_WINDUP_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID Integral windup from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 1.0f
                        rxPIDparam += 0
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgWindup.emit(Resource.Success(data= rxPIDValue))
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
                    UUID.fromString(BLE_CHAR_PID_D_TERM_LPF_UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID LowPass Filter term from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 100.0f
                        rxPIDparam += (rxString[1].code - 48) * 10.0f
                        rxPIDparam += (rxString[2].code - 48) * 1.0f
                        rxPIDparam += (rxString[3].code - 48) * 0.1f
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgDlpfterm.emit(Resource.Success(data= rxPIDValue))
                        }
                        Log.d("BLERxManager_PID_CFG", "Sending: $rxPIDparam to screen")
                    }
                    UUID.fromString(BLE_CHAR_PID_GAIN___UUID)-> {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        rxString = characteristic?.getStringValue(0).toString()
                        Log.d("BLERxManager_PID_CFG", "Read PID Gain term from device: $rxString")
                        rxPIDparam = (rxString[0].code - 48) * 100.0f
                        rxPIDparam += (rxString[1].code - 48) * 10.0f
                        rxPIDparam += (rxString[2].code - 48) * 1.0f
                        rxPIDparam += (rxString[3].code - 48) * 0.1f
                        rxPIDValue = CharDataClass( rxPIDparam,ConnectionState.Connected)
                        coroutineScope.launch {
                            dataPIDcfgGainterm.emit(Resource.Success(data= rxPIDValue))
                        }
                        Log.d("BLERxManager_PID_CFG", "Sending: $rxPIDparam to screen")
                    }
                    else -> Unit
                }
            }
            println(characteristicToReadFLAG)
            if(characteristicToReadFLAG)
            {
                println(characteristicToReadQueue.size)
                characteristicToReadQueue.remove(characteristicToReadQueue.get(characteristicToReadQueueIndex))
                println(characteristicToReadQueue.size)
                if(characteristicToReadQueue.size >= 0)
                {
                    characteristicToReadQueueIndex--
                    if (characteristicToReadQueueIndex == -1) {
                        Log.d("BLEReceiverManager2", "Complete")
                        characteristicToReadFLAG = false
                    }
                    else {
                        Log.d("BLEReceiverManager2", "Read Next CHAR")
                        readCharacteristicsDevice( characteristicToReadQueue.get(characteristicToReadQueueIndex) as BluetoothGattCharacteristic)
                    }
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                with(characteristic) {
                    when (uuid) {
                        UUID.fromString(SET_NEW_BOILER_TEMP_CHARACTERISTICS_UUID) ->
                            Log.d("BLE_ManagerTX", "Boiler Target Temperature Write Successful")
                        UUID.fromString(STEAM_TEMP_SETPOINT_CHARACTERISTICS_UUID) ->
                            Log.d("BLE_ManagerTX", "Steam Temperature Setpoint Write Successful")
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
                        UUID.fromString(BLE_CHAR_PID_I_TERM_WINDUP_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -I_TERM_WINDUP-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_D_TERM_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -D_TERM-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_D_TERM_LPF_UUID) ->
                            Log.d("BLE_ManagerTX", "PID -D_TERM_LPF-> Write Successful")
                        UUID.fromString(BLE_CHAR_PID_GAIN___UUID) ->
                            Log.d("BLE_ManagerTX", "PID -PID_GAIN-> Write Successful")
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
                        //Data format received from BLE device
                        var temperature = (value[0].toInt() - 48) * 100.0f
                        temperature += (value[1].toInt() - 48) * 10.0f
                        temperature += (value[2].toInt() - 48) * 1.0f
                        temperature += (value[3].toInt() - 48) * 0.1f
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
                    UUID.fromString(BLE_CHAR_BLESPRESSO_STATUS__UUID)->{
                        //Data format received from BLE device
                        Log.d("BLEReceiverManager2", "$value")
                    }
                    else -> Unit
                }
            }
        }
    }

    // TLDR: Helper that encodes a Float to a fixed-width ASCII digit string and writes it
    //       to the specified GATT characteristic via the given service and char UUIDs.
    private fun writeCharacteristic(serviceUuid: String, charUuid: String, value: Float, digits: Int) {
        val sb = StringBuilder()
        dataConvertionFloatToCharacteristic(value, sb, digits)
        var characteristicToWr = findCharacteristic(serviceUuid, charUuid)
        if(characteristicToWr != null) {
            writeCharacteristicsDevice(characteristicToWr, sb.toString().toByteArray())
        }
    }

    private fun writeCharacteristicsDevice(characteristic: BluetoothGattCharacteristic, payload: ByteArray)
    {
        gatt?.let {
            gatt ->
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            characteristic.value = payload
            // Convert the payload to a hex string and print it
            val hexString = payload.joinToString(" ") { it.toString(16).padStart(2, '0') }
            println("Characteristic UUID: ${characteristic.uuid}")
            println("Payload (hex): $hexString")

            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
    }

    private fun readCharacteristicsDevice(characteristic: BluetoothGattCharacteristic)
    {
        //Log.d("BLEReceiverManager2", "readCharacteristicsDevice call")
        gatt?.let { gatt ->
            // Check if the characteristic supports reading
            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                gatt.readCharacteristic(characteristic)
            } else {
                error("Characteristic ${characteristic.uuid} does not support reading.")
            }
        } ?: error("Not connected to a BLE device!")
        //Log.d("BLEReceiverManager2", "readCharacteristicsDevice call Finish")
    }

    //video1 - timestamp: 51:05
    private fun enableNotification(characteristic: BluetoothGattCharacteristic)
    {
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        Log.d("BLE_Notification", "$cccdUuid")
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if (gatt?.setCharacteristicNotification(characteristic, true) == false) {
                Log.d("BLEReceiverManager", "Set characteristics notification failed")
                return
            }
            writeDescriptor(cccdDescriptor, payload)
        }
    }

    private fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        gatt?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to a BLE device!")
    }

    private fun findCharacteristic(serviceUUID: String, characteristicsUUID: String): BluetoothGattCharacteristic? {
        return gatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find {
                characteristics -> characteristics.uuid.toString() == characteristicsUUID
        }
    }



    override fun rxReadBoilerTargetTemp()
    {
        var characteristictoRead = findCharacteristic(CONTROL_SERVICE_UUID,SET_NEW_BOILER_TEMP_CHARACTERISTICS_UUID)
        if (characteristictoRead != null)
        {
            Log.d("BLE_rxTargetTemp", "About to READ Target Temp")
            readCharacteristicsDevice(characteristictoRead)
        }
    }

    override fun rxReadSteamTempSetpoint() {
        var characteristictoRead = findCharacteristic(CONTROL_SERVICE_UUID, STEAM_TEMP_SETPOINT_CHARACTERISTICS_UUID)
        if (characteristictoRead != null) {
            Log.d("BLE_rxSteamTemp", "About to READ Steam Setpoint")
            readCharacteristicsDevice(characteristictoRead)
        }
    }

    // TLDR: Writes the new boiler target temperature to the device (4-digit ASCII format, e.g. 93.5°C → "0935").
    override fun txNewBoilerTargetTemp(newTarget: Float) = writeCharacteristic(CONTROL_SERVICE_UUID, SET_NEW_BOILER_TEMP_CHARACTERISTICS_UUID, newTarget, 4)
    // TLDR: Writes the new steam temperature setpoint to the device (4-digit ASCII format, e.g. 140.0°C → "1400").
    override fun txNewSteamTempSetpoint(newTarget: Float) = writeCharacteristic(CONTROL_SERVICE_UUID, STEAM_TEMP_SETPOINT_CHARACTERISTICS_UUID, newTarget, 4)

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
    // TLDR: Writes PID anti-windup flag to device (1-digit format: "1" if ≥1.0, "0" otherwise).
    override fun txWritePidWindup(value: Float) = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_I_TERM_WINDUP_UUID, value, 1)
    // TLDR: Writes PID Derivative term to device (3-digit format, e.g. 2.5 → "025").
    override fun txWritePidDTerm(value: Float)  = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_D_TERM_UUID,        value, 3)
    // TLDR: Writes PID Derivative low-pass filter cutoff to device (4-digit format, e.g. 10.0 → "0100").
    override fun txWritePidDLpf(value: Float)   = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_D_TERM_LPF_UUID,    value, 4)
    // TLDR: Writes overall PID controller gain multiplier to device (4-digit format, e.g. 1.0 → "0010").
    override fun txWritePidGain(value: Float)   = writeCharacteristic(CONTROL_PID_SERVICE_UUID, BLE_CHAR_PID_GAIN___UUID,        value, 4)

    // TLDR: Reads all 8 machine-configuration UUIDs in a single GATT-safe sequential queue:
    //       boiler target temp → steam setpoint → 6 brew params (pre-infusion, infusion, declining).
    //       Queue is processed tail-to-head via onCharacteristicRead callbacks.
    override suspend fun rxReadMachineConfiguration() {
        characteristicToReadFLAG = true
        characteristicToReadQueue.clear()
        // Items are added with the last-to-read at index 0; queue is consumed from the tail.
        val uuidsToRead = listOf(
            BLE_CHAR_BREW_DECLINING_PR_TIME__UUID,    // idx 0 – read last
            BLE_CHAR_BREW_DECLINING_PR_POWER_UUID,    // idx 1
            BLE_CHAR_BREW_INFUSION_TIME__UUID,        // idx 2
            BLE_CHAR_BREW_INFUSION_POWER_UUID,        // idx 3
            BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID,    // idx 4
            BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID,    // idx 5
            STEAM_TEMP_SETPOINT_CHARACTERISTICS_UUID, // idx 6
            SET_NEW_BOILER_TEMP_CHARACTERISTICS_UUID  // idx 7 – read first
        )
        for (uuid in uuidsToRead) {
            val characteristic = findCharacteristic(CONTROL_SERVICE_UUID, uuid)
            if (characteristic != null) {
                println(characteristic)
                characteristicToReadQueue.add(characteristic)
                println(characteristicToReadQueue.size)
            }
        }
        Log.d("BLE_rxMachineCfg", "About to READ all 8 machine configuration characteristics")
        characteristicToReadQueueIndex = 7
        readCharacteristicsDevice(characteristicToReadQueue.get(characteristicToReadQueueIndex) as BluetoothGattCharacteristic)
    }

    override suspend fun rxReadPIDConfiguration(){
        characteristicToReadFLAG = true
        characteristicToReadQueue.clear()
        val characteristicsPIDToRead = listOf(
            BLE_CHAR_PID_GAIN___UUID,
            BLE_CHAR_PID_D_TERM_LPF_UUID,
            BLE_CHAR_PID_D_TERM_UUID,
            BLE_CHAR_PID_I_TERM_WINDUP_UUID,
            BLE_CHAR_PID_I_MAX_TERM_UUID,
            BLE_CHAR_PID_I_TERM_UUID,
            BLE_CHAR_PID_P_TERM_UUID
        )
        for (characteristicUuid in characteristicsPIDToRead) {
            val charToRead = findCharacteristic(CONTROL_PID_SERVICE_UUID, characteristicUuid )
            //println(charToRead)
            if (charToRead != null) {
                println(charToRead)
                characteristicToReadQueue.add(charToRead as BluetoothGattCharacteristic)
                println(characteristicToReadQueue.size)
            }
        }

        Log.d("BLE_rxTargetTemp", "About to READ PID configuration")
        characteristicToReadQueueIndex = 6
        readCharacteristicsDevice( characteristicToReadQueue.get(characteristicToReadQueueIndex) as BluetoothGattCharacteristic)
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
    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        val characteristic = findCharacteristic(CONTROL_SERVICE_UUID,SET_NEW_BOILER_TEMP_CHARACTERISTICS_UUID)
        if (characteristic != null)
        {
            disconnectCharacteristic(characteristic)
        }
        gatt?.close()
    }
    // vide 1-> Time-Stamp: 56:00
    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic)
    {
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let {cccdDescriptor ->
            if (gatt?.setCharacteristicNotification(characteristic, false) == false)
            {
                Log.d("TempHumidReceiveManager","Set characteristics notifications failed!")
                return
            }
            writeDescriptor(cccdDescriptor,BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
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
/*
    override suspend fun rxReadBrewConfiguration(){
        characteristicToReadFLAG = true
        characteristicToReadQueue.clear()
        val characteristicsToRead = listOf(
            BLE_CHAR_BREW_DECLINING_PR_TIME__UUID,
            BLE_CHAR_BREW_DECLINING_PR_POWER_UUID,
            BLE_CHAR_BREW_INFUSION_TIME__UUID,
            BLE_CHAR_BREW_INFUSION_POWER_UUID,
            BLE_CHAR_BREW_PRE_INFUSION_TIME__UUID,
            BLE_CHAR_BREW_PRE_INFUSION_POWER_UUID
        )
        for (characteristicUuid in characteristicsToRead) {
            val characteristicToRead = findCharacteristic(CONTROL_SERVICE_UUID, characteristicUuid)
            println(characteristicToRead)
            if (characteristicToRead != null) {
                //println(characteristicUuid)
                characteristicToReadQueue.add(characteristicToRead)
            }
        }
        Log.d("BLE_rxTargetTemp", "About to READ brew configuration")
        characteristicToReadQueueIndex = 5
        readCharacteristicsDevice( characteristicToReadQueue.get(characteristicToReadQueueIndex) as BluetoothGattCharacteristic)
    }
*/