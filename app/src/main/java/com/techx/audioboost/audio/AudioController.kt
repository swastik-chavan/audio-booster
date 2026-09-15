package com.techx.audioboost.audio

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AudioDeviceListener {
    fun onDeviceChanged()
}

interface AudioDeviceMonitor {
    fun start()
    fun stop()
    fun getCurrentDevice(): Pair<String, DeviceType>
}

@RequiresApi(Build.VERSION_CODES.M)
class Api23DeviceMonitor(
    private val audioManager: AudioManager,
    private val listener: AudioDeviceListener
) : AudioDeviceMonitor {

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            listener.onDeviceChanged()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            listener.onDeviceChanged()
        }
    }

    override fun start() {
        try {
            audioManager.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
        } catch (t: Throwable) {
        }
    }

    override fun stop() {
        try {
            audioManager.unregisterAudioDeviceCallback(deviceCallback)
        } catch (t: Throwable) {
        }
    }

    override fun getCurrentDevice(): Pair<String, DeviceType> {
        try {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            var bestType = DeviceType.SPEAKER
            var bestName = "Phone Speaker"

            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_HEARING_AID,
                    31, 32 -> {
                        return Pair("Bluetooth Audio", DeviceType.BLUETOOTH)
                    }
                    AudioDeviceInfo.TYPE_USB_DEVICE,
                    AudioDeviceInfo.TYPE_USB_HEADSET,
                    AudioDeviceInfo.TYPE_USB_ACCESSORY -> {
                        bestType = DeviceType.USB
                        bestName = "USB Audio"
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_LINE_ANALOG,
                    AudioDeviceInfo.TYPE_LINE_DIGITAL -> {
                        if (bestType != DeviceType.USB) {
                            bestType = DeviceType.WIRED_HEADSET
                            bestName = "Wired Headphones"
                        }
                    }
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                        if (bestType == DeviceType.SPEAKER) {
                            bestType = DeviceType.SPEAKER
                            bestName = "Phone Speaker"
                        }
                    }
                }
            }
            return Pair(bestName, bestType)
        } catch (t: Throwable) {
            return Pair("Phone Speaker", DeviceType.SPEAKER)
        }
    }
}

class Api21DeviceMonitor(
    private val audioManager: AudioManager,
    private val listener: AudioDeviceListener
) : AudioDeviceMonitor {

    override fun start() {
    }

    override fun stop() {
    }

    @Suppress("DEPRECATION")
    override fun getCurrentDevice(): Pair<String, DeviceType> {
        return try {
            if (audioManager.isBluetoothA2dpOn) {
                Pair("Bluetooth Audio", DeviceType.BLUETOOTH)
            } else if (audioManager.isWiredHeadsetOn) {
                Pair("Wired Headphones", DeviceType.WIRED_HEADSET)
            } else {
                Pair("Phone Speaker", DeviceType.SPEAKER)
            }
        } catch (t: Throwable) {
            Pair("Phone Speaker", DeviceType.SPEAKER)
        }
    }
}

class AudioController(
    private val context: Context,
    private val onDeviceTransition: ((Pair<String, DeviceType>) -> Unit)? = null
) {

    private var audioManager: AudioManager? = null
    private var maxVolume = 15

    private val _volumeFlow = MutableStateFlow(0)
    val volumeFlow: StateFlow<Int> = _volumeFlow.asStateFlow()

    private val _deviceFlow = MutableStateFlow(Pair("Phone Speaker", DeviceType.SPEAKER))
    val deviceFlow: StateFlow<Pair<String, DeviceType>> = _deviceFlow.asStateFlow()

    private var deviceMonitor: AudioDeviceMonitor? = null

    private val audioRoutingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "android.media.VOLUME_CHANGED_ACTION" -> {
                    try {
                        val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                        if (streamType == AudioManager.STREAM_MUSIC) {
                            _volumeFlow.value = getMediaVolumePercent()
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                updateDeviceState()
                            }
                        }
                    } catch (t: Throwable) {
                    }
                }
                AudioManager.ACTION_AUDIO_BECOMING_NOISY,
                Intent.ACTION_HEADSET_PLUG,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    updateDeviceState()
                }
            }
        }
    }

    init {
        try {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15

            val listener = object : AudioDeviceListener {
                override fun onDeviceChanged() {
                    updateDeviceState()
                }
            }

            audioManager?.let { am ->
                deviceMonitor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Api23DeviceMonitor(am, listener)
                } else {
                    Api21DeviceMonitor(am, listener)
                }
                deviceMonitor?.start()
            }

            val filter = IntentFilter().apply {
                addAction("android.media.VOLUME_CHANGED_ACTION")
                addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                addAction(Intent.ACTION_HEADSET_PLUG)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(audioRoutingReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(audioRoutingReceiver, filter)
            }

            _volumeFlow.value = getMediaVolumePercent()
            updateDeviceState()

        } catch (t: Throwable) {
        }
    }

    private fun updateDeviceState() {
        try {
            val device = deviceMonitor?.getCurrentDevice() ?: Pair("Phone Speaker", DeviceType.SPEAKER)
            val previous = _deviceFlow.value
            _deviceFlow.value = device
            if (previous != device) {
                onDeviceTransition?.invoke(device)
            }
        } catch (t: Throwable) {
        }
    }

    fun getMediaVolumePercent(): Int {
        val am = audioManager ?: return 0
        return try {
            val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            (current * 100) / maxVolume
        } catch (t: Throwable) {
            0
        }
    }

    fun setMediaVolumePercent(percent: Int) {
        val am = audioManager ?: return
        try {
            val targetVolume = (percent * maxVolume) / 100
            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            _volumeFlow.value = percent
        } catch (t: Throwable) {
        }
    }

    fun getCurrentDevice(): Pair<String, DeviceType> {
        return _deviceFlow.value
    }

    fun release() {
        try {
            context.unregisterReceiver(audioRoutingReceiver)
        } catch (t: Throwable) {
        }
        try {
            deviceMonitor?.stop()
        } catch (t: Throwable) {
        }
    }
}
