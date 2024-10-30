package com.example.fuelcalculator.ui.common

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.fuelcalculator.R

object BottomNavHelper {
    private const val ACTIVE_COLOR = "#6B00FF"
    private const val INACTIVE_COLOR = "#666666"

    fun setActiveNavItem(
        navHome:LinearLayout,
        navLocate:LinearLayout,
        navVehicles:LinearLayout,
        navCalculator:LinearLayout,
        activeItemId: Int
    ){
        //Set all to inactive
        setInactive(navHome, R.id.ivNavHome, R.id.tvNavHome)
        setInactive(navLocate, R.id.ivNavLocate, R.id.tvNavLocate)
        setInactive(navVehicles, R.id.ivNavVehicles, R.id.tvNavVehicles)
        setInactive(navCalculator, R.id.ivNavCalculator, R.id.tvNavCalculator)

        //Set active state based on selection
        when(activeItemId){
            R.id.navHome -> setActive(navHome, R.id.ivNavHome, R.id.tvNavHome)
            R.id.navLocate -> setActive(navLocate, R.id.ivNavLocate, R.id.tvNavLocate)
            R.id.navVehicles -> setActive(navVehicles, R.id.ivNavVehicles, R.id.tvNavVehicles)
            R.id.navCalculator -> setActive(navCalculator, R.id.ivNavCalculator, R.id.tvNavCalculator)
        }
    }

    //Function to set element as active in navbar
    private fun setActive(navItem:LinearLayout, iconId:Int, textId:Int){
        navItem.findViewById<ImageView>(iconId)?.setColorFilter(
            android.graphics.Color.parseColor(ACTIVE_COLOR)
        )
        navItem.findViewById<TextView>(textId)?.setTextColor(
            android.graphics.Color.parseColor(ACTIVE_COLOR)
        )
    }

    //Function to set element as inactive in navbar
    private fun setInactive(navItem:LinearLayout, iconId:Int, textId:Int){
        navItem.findViewById<ImageView>(iconId)?.setColorFilter(
            android.graphics.Color.parseColor(INACTIVE_COLOR)
        )
        navItem.findViewById<TextView>(textId)?.setTextColor(
            android.graphics.Color.parseColor(INACTIVE_COLOR)
        )
    }

}