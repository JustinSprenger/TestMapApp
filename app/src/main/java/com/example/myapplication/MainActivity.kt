package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this)

        setContent {
            MapScreen()
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context).apply {
        getMapAsync { mapboxMap ->
                // Load a default style (MapLibre GL supports many map styles)
                mapboxMap.setStyle("https://api.maptiler.com/maps/basic-v2/style.json?key=6By9yJsNZKiBFWjzUqUy") {
                    // You can add more customization or layers here
                    //it.addSource()
                    it.addLayer(FillExtrusionLayer("3d-buildings", "building").apply {
                        setProperties(
                            visibility(Property.VISIBLE),
                            fillExtrusionColor("rgba(200, 200, 250, 0.8)"),
                            fillExtrusionHeight(Expression.get("height")),
                            fillExtrusionBase(0f),
                            fillExtrusionOpacity(1f)
                        )
                    })
                }
                // Set a default camera position (New York City in this example)
                mapboxMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(52.5128, 13.4060),
                        10.0)
                )
                mapboxMap.moveCamera(CameraUpdateFactory.bearingTo(-17.6))
                mapboxMap.moveCamera(CameraUpdateFactory.tiltTo(45.0))
                mapboxMap
            }
        }
    }

    val lifecycleObserver = rememberMapViewLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapViewLifecycleObserver(mapView: MapView): LifecycleObserver {
    return remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
    }
}


@Composable
fun MapScreen() {
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView }
    )
}