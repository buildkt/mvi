package com.buildkt.showcase

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.buildkt.showcase.presentation.ADDRESS_FLOW_ROUTE
import com.buildkt.showcase.presentation.addressFlowNavigation
import com.buildkt.showcase.presentation.theme.ExtendedMaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent { ExtendedMaterialTheme { MainScreen() } }
    }
}

@Composable
private fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val activity = LocalActivity.current as Activity
    val entryPoint = EntryPointAccessors.fromActivity(
        activity = activity,
        entryPoint = PlaygroundAppEntryPoint::class.java
    )

    NavHost(
        navController = navController,
        startDestination = ADDRESS_FLOW_ROUTE,
        modifier = modifier,
        builder = {
            addressFlowNavigation(navController, addressRepository = entryPoint.getAddressRepository())
        }
    )
}
