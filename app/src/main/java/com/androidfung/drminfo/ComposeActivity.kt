package com.androidfung.drminfo

import android.media.MediaCodecList
import android.os.Build
import android.os.Build.VERSION.MEDIA_PERFORMANCE_CLASS
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.hardware.display.DisplayManagerCompat
import com.androidfung.drminfo.Util.convertHdrTypeToStringRes
import com.androidfung.drminfo.ui.theme.DrmInfoAndroidTheme
import java.text.DecimalFormat

//val fontFamily = FontFamily(
//    Font(R.font.open_sans, FontWeight.Light),
//    Font(R.font.open_sans, FontWeight.Normal),
//    Font(R.font.open_sans, FontWeight.Normal, FontStyle.Italic),
//    Font(R.font.open_sans, FontWeight.Medium),
//    Font(R.font.open_sans, FontWeight.Bold)
//)

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DrmInfoAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
//                    Greeting("Android")
                    Scaffold(
                        topBar = {
                            TopAppBar {
                                title = getString(R.string.app_name)
                            }
                        }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            DeviceInfo()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DrmInfoAndroidTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceInfo() {
    val spaceBetweenRows = 8.dp

    Card(elevation = 4.dp) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(Build.MANUFACTURER + " " + Build.MODEL, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(spaceBetweenRows))


            Column {
                // Brand, Hardware, SoC Manufacturer, SoC Model,
                // Build, Build Type, Fingerprint,
                // SDK Level, Release or Codename, Supported ABI
                // Supported Hardware Decoder, Supported HDR Types, Supported Refresh Rate

                Text(stringResource(R.string.text_brand), style = MaterialTheme.typography.caption)
                Text(Build.BRAND, style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.height(spaceBetweenRows))

                Text(
                    stringResource(R.string.text_hardware),
                    style = MaterialTheme.typography.caption
                )
                Text(Build.HARDWARE, style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.height(spaceBetweenRows))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Text(
                        stringResource(R.string.text_soc_manufacturer),
                        style = MaterialTheme.typography.caption
                    )
                    Text(Build.SOC_MANUFACTURER, style = MaterialTheme.typography.body2)
                    Spacer(modifier = Modifier.height(spaceBetweenRows))

                    Text(
                        stringResource(R.string.text_soc_model),
                        style = MaterialTheme.typography.caption
                    )
                    Text(Build.SOC_MODEL, style = MaterialTheme.typography.body2)
                    Spacer(modifier = Modifier.height(spaceBetweenRows))
                }

                Text(
                    stringResource(R.string.text_display),
                    style = MaterialTheme.typography.caption
                )
                Text(Build.DISPLAY, style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.height(spaceBetweenRows))

                Text(
                    stringResource(R.string.text_fingerprint),
                    style = MaterialTheme.typography.caption
                )
                Text(Build.FINGERPRINT, style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.height(spaceBetweenRows))

                Text(
                    stringResource(R.string.text_sdk_level),
                    style = MaterialTheme.typography.caption
                )
                Text(Build.VERSION.SDK_INT.toString(), style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.height(spaceBetweenRows))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Text(
                        stringResource(R.string.text_release_or_codename),
                        style = MaterialTheme.typography.caption
                    )
                    Text(Build.VERSION.RELEASE_OR_CODENAME, style = MaterialTheme.typography.body2)
                    Spacer(modifier = Modifier.height(spaceBetweenRows))
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Text(
                        stringResource(R.string.text_media_performance_class),
                        style = MaterialTheme.typography.caption
                    )
                    Text(MEDIA_PERFORMANCE_CLASS.toString(), style = MaterialTheme.typography.body2)
                    Spacer(modifier = Modifier.height(spaceBetweenRows))
                }

                Text(
                    stringResource(R.string.text_abi),
                    style = MaterialTheme.typography.caption
                )
                Text(Build.SUPPORTED_ABIS.joinToString(), style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.height(spaceBetweenRows))

                // Codec Related
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val hardwareAcceleratedType =
                        MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                            .filter { codec -> codec.isHardwareAccelerated && !codec.isEncoder }
                            .flatMap { codec -> codec.supportedTypes.asIterable() }
                            .toSortedSet()


                    Text(
                        stringResource(R.string.text_hw_decode_type),
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        hardwareAcceleratedType.joinToString(),
                        style = MaterialTheme.typography.body2
                    )
                    Spacer(modifier = Modifier.height(spaceBetweenRows))
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val displayManager = DisplayManagerCompat.getInstance(LocalContext.current)
                    displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.also { display ->
                        val supportedRefreshRates =
                            display.supportedModes.map { mode -> mode.refreshRate }.toSortedSet()
                                .map(DecimalFormat("0.##")::format)

                        Text(
                            stringResource(R.string.text_supported_refresh_rate),
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            supportedRefreshRates.joinToString(),
                            style = MaterialTheme.typography.body2
                        )
                        Spacer(modifier = Modifier.height(spaceBetweenRows))


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val supportedHdrType = display.hdrCapabilities.supportedHdrTypes
                            if (supportedHdrType.isNotEmpty()) {

                                val types = supportedHdrType.map {
                                    stringResource(
                                        convertHdrTypeToStringRes(it)
                                    )
                                }.sorted()
//                                val types = supportedHdrType.map(Util::convertHdrTypeToString).sorted()

                                Text(
                                    stringResource(R.string.text_supported_hdr_types),
                                    style = MaterialTheme.typography.caption
                                )
                                Text(types.joinToString(), style = MaterialTheme.typography.body2)
                                Spacer(modifier = Modifier.height(spaceBetweenRows))
                            }
                        }
                    }


//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                    map["securityPatch"] = Build.VERSION.SECURITY_PATCH
//
//                    // Display Related
//                    val displayManager = DisplayManagerCompat.getInstance(ComposeActivity.this)
//                    displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.also { display ->
//                        val supportedRefreshRate = display.supportedModes.map { mode -> mode.refreshRate }.toSortedSet()
//                        map["supportedRefreshRate"] = supportedRefreshRate.map(DecimalFormat("0.##")::format)
//
//                        Text(
//                            stringResource(R.string.text_supported_hdr_types),
//                            style = MaterialTheme.typography.caption
//                        )
//                        Text(Build.DISPLAY, style = MaterialTheme.typography.body2)
//                        Spacer(modifier = Modifier.height(spaceBetweenRows))
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            val supportedHdrType = display.hdrCapabilities.supportedHdrTypes
//                            if (supportedHdrType.isNotEmpty()) {
//                                map["supportedHdrType"] = supportedHdrType.map(this::convertHdrTypeToString).sorted()
//
//                                Text(
//                                    stringResource(R.string.text_supported_refresh_rate),
//                                    style = MaterialTheme.typography.caption
//                                )
//                                Text(Build.FINGERPRINT, style = MaterialTheme.typography.body2)
//                                Spacer(modifier = Modifier.height(spaceBetweenRows))
//                            }
//
//                        }
//                    }
//                }


                }

            }
        }
    }
}