package com.purered.pr1digitalad_aos

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.purered.pr1digitalad.ActionPayload
import com.purered.pr1digitalad.DigitalAd
import com.purered.pr1digitalad.DigitalAdInput

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        var digitalAd: DigitalAd? = null; // Declare a variable to hold the DigitalAd instance

        val clippedCoupons = listOf<Map<String, Any>>(
            mapOf( "couponId" to "70f6af3f-1b6d-452e-b699-d62020b8c7e4", "couponId" to
                    "8ea69008-aa35-4d2b-a075-9012b45e835f" ),
        );

        val key = "pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp3"
        //val key = "pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp8"

        val options: DigitalAdInput = DigitalAdInput(
            domain = "pr1arg-staging.przone.net",
            apiKey = "pgH7QzFHJx4w46fI~5Uzi4RvtTwlEXp8",
            storeKey = "10074",
            viewMode = "",
            cartItems = null, // Pass null for cartItems, as they are not supported in the native apps.
            clippedCoupons = clippedCoupons,
            //environment = "QA",
            payloadJsonString = ""
        ){
                payload: ActionPayload ->
            // Callback handler to handle the actions from the DigitalAd with the payload type ActionPayload

            when (payload.actionName) {
                "onClipCoupon" -> {
                    // Clipped coupons will be accessed from the payload.value
                    println("Performing action for onClipCoupon with payload: ${payload.value}")

                    if (digitalAd != null) {
                        // very important to set the status to success or error and to Show the message needed
                        payload.status = "success";

                        // Update the payload value with the new clipped coupons if
                        // payload.value = listOf<Map<String, Any>>(mapOf( "couponId" to "70f6af3f-1b6d-452e-b699-d62020b8c7e4", "id" to "8ea69008-aa35-4d2b-a075-9012b45e835f"));

                        // Dispatch the payload back to the DigitalAd
                        digitalAd!!.dispatch(payload);
                    }
                }

                "onSelectStore" -> {
                    // Perform action for onUnclipCoupon
                    println("Performing action for onUnclipCoupon")
                }

                else -> {
                    // Default case or handle other cases
                    println("Unknown case: $payload.actionName with object: $payload")
                }
            }
        }

        // Initialise DigitalAd with the required options parameter of type DigitalAdInput
        digitalAd = DigitalAd(this, options)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT )
        digitalAd.updateLayoutParams(layoutParams)

        //Obtain a reference to the Digital Ad view using the `viewRef` property, which can be used to render the ad on the screen.
        val adViewRef = digitalAd.viewRef;
        findViewById<LinearLayout>(R.id.container).addView(adViewRef)

        val btnValidate: View = findViewById(R.id.btnvalidate)
        btnValidate.setOnClickListener { view: View ->
            // to update the clipped coupons dispatch the action with the clipped coupons
            digitalAd.dispatch(ActionPayload( actionName = "clipCoupon", value = clippedCoupons))

            // to update the store dispatch the action with the store key
            digitalAd.dispatch(ActionPayload( actionName = "setStore", value = "10074"))
        }
    }

}