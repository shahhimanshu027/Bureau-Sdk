package com.bureau.services

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bureau.`interface`.UrlFilterInterface
import com.bureau.models.Domains
import com.bureau.models.callFilter.request.UrlFilterRequest
import com.bureau.network.APIClient
import com.bureau.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Abhin.
 */
class ASUrl : AccessibilityService() {

    private var isApiCall = false
    private var preferenceManager: PreferenceManager? = null

    companion object {
        var mUrlFilterInterface: UrlFilterInterface? = null

        fun initCallbacks(listener: UrlFilterInterface) {
            mUrlFilterInterface = listener
        }

        /** @return a list of supported browser configs
         * This list could be instead obtained from remote server to support future browser updates without updating an app
         */
        private val supportedBrowsers: List<SupportedBrowserConfig>
            get() {
                val browsers: MutableList<SupportedBrowserConfig> = ArrayList()
                browsers.add(
                    SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar")
                )
                browsers.add(
                    SupportedBrowserConfig(
                        "org.mozilla.firefox",
                        "org.mozilla.firefox:id/url_bar_title"
                    )
                )
                return browsers
            }
    }

    override fun onServiceConnected() {
        Log.e("TAG", "OnServiceConnected")
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        preferenceManager =
            PreferenceManager(this.getSharedPreferences(MY_PREFERENCE, MODE_PRIVATE))
        val source = event.source ?: return
        val packageName = source.packageName.toString()
        var browserConfig: SupportedBrowserConfig? = null
        for (supportedConfig in supportedBrowsers) {
            if (supportedConfig.packageName == packageName) {
                browserConfig = supportedConfig
            }
        }
        //this is not supported browser, so exit
        if (browserConfig == null) {
            return
        }
        val browserList = Arrays.asList(*browserConfig.packageName.split(",\\s*").toTypedArray())
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (!browserList.contains(packageName)) {
                return
            }
        }
        if (browserList.contains(packageName)) {
            try {
                // App opened is a browser.
                // Parse urls in browser.
                if (AccessibilityEvent.eventTypeToString(event.eventType).contains("WINDOW")) {
                    val nodeInfo = event.source
                    getUrlsFromViews(nodeInfo, browserConfig)
                }
            } catch (ex: StackOverflowError) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Method to loop through all the views and try to find a URL.
     *
     * @param info
     * @param browserConfig
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getUrlsFromViews(info: AccessibilityNodeInfo, browserConfig: SupportedBrowserConfig) {
        val nodes = info.findAccessibilityNodeInfosByViewId(browserConfig.addressBarId)
        try {
            if (nodes == null) return
            if (nodes.isNotEmpty()) {
                val addressBarNodeInfo = nodes[0]
                var url: String? = null
                if (addressBarNodeInfo.text != null) {
                    url = addressBarNodeInfo.text.toString()
                }
                addressBarNodeInfo.recycle()
                Log.e("TAG", url!!)
                if (!isApiCall) {
                    isApiCall = true
                    if (!preferenceManager!!.getValue(PREF_STORED_DOMAIN_LIST, "")
                            .isNullOrEmpty()
                    ) {
                        val storedDomainList: ArrayList<Domains> = convertObjectFromString(
                            preferenceManager?.getValue(
                                PREF_STORED_DOMAIN_LIST,
                                ""
                            ).toString()
                        )
                        if (!storedDomainList.isNullOrEmpty()) {
                            if (storedDomainList.map { it.domain_name }
                                    .contains(getHostName(url))) {
                                val domainIsValidOrNot = storedDomainList.find {
                                    it.domain_name == getHostName(url)
                                }?.is_valid
                                if (domainIsValidOrNot == false) {
                                    Toast.makeText(
                                        this@ASUrl,
                                        "Found in local list : Url is UnSafeUrl",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    mUrlFilterInterface?.unSafeUrlWarning(url.toString(),"UnSafeUrl")
                                }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    apiCallForUrlFiltering(url.toString())
                                }

                            }
                        }
                        isApiCall = false
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            apiCallForUrlFiltering(url.toString())
                        }
                        isApiCall = false
                    }
                }

            }
        } catch (ex: StackOverflowError) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onInterrupt() {}
    class SupportedBrowserConfig(var packageName: String, var addressBarId: String)

    private suspend fun apiCallForUrlFiltering(url: String) {
        try {
            val apiCall = APIClient(this@ASUrl).getClient()
                .urlFilterApi(UrlFilterRequest(getHostName(url)))
            if (apiCall.isSuccessful) {
                var list = ArrayList<Domains>()
                if (apiCall.body()?.warn != null && apiCall.body()?.warn!!) {
                    Toast.makeText(this@ASUrl, "unSafeUrl : ${getHostName(url)}", Toast.LENGTH_LONG)
                        .show()
                    mUrlFilterInterface?.unSafeUrlWarning("url","unSafeUrl")
                    if (!preferenceManager?.getValue(PREF_STORED_DOMAIN_LIST, "").isNullOrEmpty()) {
                        list = convertObjectFromString(
                            preferenceManager?.getValue(
                                PREF_STORED_DOMAIN_LIST,
                                ""
                            ).toString()
                        )
                        if (list.size < 100) {
                            list.add(Domains(getHostName(url), is_valid = false))
                        } else {
                            list.removeAt(0)
                            list.add(list.size - 1, Domains(getHostName(url), is_valid = false))
                        }
                        val set: Set<Domains> = HashSet(list)
                        list.clear()
                        list.addAll(set)
                        preferenceManager?.setValue(PREF_STORED_DOMAIN_LIST, Gson().toJson(list))
                    } else {
                        list.add(Domains(getHostName(url), is_valid = false))
                        preferenceManager?.setValue(
                            PREF_STORED_DOMAIN_LIST,
                            Gson().toJson(list)
                        )
                    }
                } else {
                    if (!preferenceManager?.getValue(PREF_STORED_DOMAIN_LIST, "").isNullOrEmpty()) {
                        list = convertObjectFromString(
                            preferenceManager?.getValue(
                                PREF_STORED_DOMAIN_LIST,
                                ""
                            ).toString()
                        )
                        if (list.size < 100) {
                            list.add(Domains(getHostName(url), is_valid = true))
                        } else {
                            list.removeAt(0)
                            list.add(list.size - 1, Domains(getHostName(url), is_valid = true))
                        }
                        val set: Set<Domains> = HashSet(list)
                        list.clear()
                        list.addAll(set)
                        preferenceManager?.setValue(PREF_STORED_DOMAIN_LIST, Gson().toJson(list))
                    } else {
                        list.add(Domains(getHostName(url), is_valid = true))
                        preferenceManager?.setValue(PREF_STORED_DOMAIN_LIST, Gson().toJson(list))
                    }
                }
            } else {
                Toast.makeText(this@ASUrl, "ApI Failure --> ${apiCall.body()}", Toast.LENGTH_LONG)
                    .show()
            }
            isApiCall = false
        } catch (e: Exception) {
            isApiCall = false
            e.printStackTrace()
        }
    }
}