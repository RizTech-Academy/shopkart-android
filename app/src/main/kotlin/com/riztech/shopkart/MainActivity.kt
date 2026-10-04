package com.riztech.shopkart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.riztech.shopkart.designsystem.theme.ShopKartTheme
import com.riztech.shopkart.navigation.ShopKartNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ShopKartTheme {
                ShopKartNavHost()
            }
        }
    }
}
