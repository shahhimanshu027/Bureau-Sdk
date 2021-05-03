package com.bureau.receivers.call

import android.content.Context
import com.bureau.utils.startCallFilteringService
import java.util.*

/**
 * Created by Abhin.
 * Call Receiver used to get call backs from the PhoneCallReceiver()
 */
class CallReceiver : PhoneCallReceiver() {


    //This will triggered when incoming call is started
    override fun onIncomingCallStarted(context: Context?, number: String?, start: Date?) {
        context?.let {
            // Starting the service to get the valid or invalid number
            startCallFilteringService(context = it, number = number)
        }
    }

    // This will triggered when outgoing call is started
    override fun onOutgoingCallStarted(context: Context?, number: String?, start: Date?) {
    }

    // This will triggered when incoming call is ended
    override fun onIncomingCallEnded(context: Context?, number: String?, start: Date?, end: Date?) {

    }

    // This will triggered when outgoing call is ended
    override fun onOutgoingCallEnded(context: Context?, number: String?, start: Date?, end: Date?) {
    }

    // This will triggered when call is missed
    override fun onMissedCall(context: Context?, number: String?, missed: Date?) {
    }
}