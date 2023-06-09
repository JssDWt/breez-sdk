package com.breezsdk

import breez_sdk.*
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import java.io.File

class BreezSDKModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var breezServices: BlockingBreezServices? = null

    companion object {
        var TAG = "RNBreezSDK"
    }

    override fun getName(): String {
        return TAG
    }

    @Throws(SdkException::class)
    fun getBreezServices(): BlockingBreezServices {
        if (this.breezServices != null) {
            return this.breezServices!!
        }

        throw SdkException.Exception("BreezServices not initialized")
    }

    @ReactMethod
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Int) {}

    @ReactMethod
    fun mnemonicToSeed(mnemonic: String, promise: Promise) {
        try {
            var seed = mnemonicToSeed(mnemonic)
            promise.resolve(readableArrayOf(seed))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling mnemonicToSeed", e)
        }
    }

    @ReactMethod
    fun parseInput(input: String, promise: Promise) {
        try {
            var inputType = parseInput(input)
            promise.resolve(readableMapOf(inputType))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling parseInput", e)
        }
    }

    @ReactMethod
    fun parseInvoice(invoice: String, promise: Promise) {
        try {
            var lnInvoice = parseInvoice(invoice)
            promise.resolve(readableMapOf(lnInvoice))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling parseInvoice", e)
        }
    }

    @ReactMethod
    fun registerNode(network: String, seed: ReadableArray, registerCredentials: ReadableMap, inviteCode: String, promise: Promise) {
        try {
            var registerCreds = asGreenlightCredentials(registerCredentials)
            var optionalInviteCode = inviteCode.takeUnless { it.isEmpty() }
            var creds = registerNode(asNetwork(network), asUByteList(seed), registerCreds, optionalInviteCode)
            promise.resolve(readableMapOf(creds))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling registerNode", e)
        }
    }

    @ReactMethod
    fun recoverNode(network: String, seed: ReadableArray, promise: Promise) {
        try {
            var creds = recoverNode(asNetwork(network), asUByteList(seed))
            promise.resolve(readableMapOf(creds))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling recoverNode", e)
        }
    }

    @ReactMethod
    fun startLogStream(promise: Promise) {
        try {
            var emitter = reactApplicationContext
                    .getJSModule(RCTDeviceEventEmitter::class.java)

            setLogStream(BreezSDKLogStream(emitter))
            promise.resolve(readableMapOf("status" to "ok"))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling setLogStream", e)
        }
    }

    @ReactMethod
    fun defaultConfig(envType: String, promise: Promise) {
        try {
            var workingDir = File(reactApplicationContext.filesDir.toString() + "/breezSdk")

            if (!workingDir.exists()) {
                workingDir.mkdirs()
            }

            var config = defaultConfig(asEnvironmentType(envType))
            config.workingDir = workingDir.absolutePath

            promise.resolve(readableMapOf(config))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling defaultConfig", e)
        }
    }

    @ReactMethod
    fun initServices(config: ReadableMap, deviceKey: ReadableArray, deviceCert: ReadableArray, seed: ReadableArray, promise: Promise) {
        if (this.breezServices != null) {
            promise.reject(TAG, "BreezServices already initialized")
        }

        var configData = asConfig(config)

        if (configData == null) {
            promise.reject(TAG, "Invalid config")
        } else {
            var emitter = reactApplicationContext.getJSModule(RCTDeviceEventEmitter::class.java)
            var creds = GreenlightCredentials(asUByteList(deviceKey), asUByteList(deviceCert))

            try {
                this.breezServices = initServices(configData, asUByteList(seed), creds, BreezSDKListener(emitter))
                promise.resolve(readableMapOf("status" to "ok"))
            } catch (e: SdkException) {
                e.printStackTrace()
                promise.reject(TAG, e.message ?: "Error calling initServices", e)
            }
        }
    }

    @ReactMethod
    fun start(promise: Promise) {
        try {
            getBreezServices().start()
            promise.resolve(readableMapOf("status" to "ok"))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling start", e)
        }
    }

    @ReactMethod
    fun sync(promise: Promise) {
        try {
            getBreezServices().sync()
            promise.resolve(readableMapOf("status" to "ok"))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling sync", e)
        }
    }

    @ReactMethod
    fun stop(promise: Promise) {
        try {
            getBreezServices().stop()
            promise.resolve(readableMapOf("status" to "ok"))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling stop", e)
        }
    }

    @ReactMethod
    fun sendPayment(bolt11: String, amountSats: Double, promise: Promise) {
        try {
            var optionalAmountSats = amountSats.takeUnless { it == 0.0 }
            var payment = getBreezServices().sendPayment(bolt11, optionalAmountSats?.toULong())
            promise.resolve(readableMapOf(payment))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling sendPayment", e)
        }
    }

    @ReactMethod
    fun sendSpontaneousPayment(nodeId: String, amountSats: Double, promise: Promise) {
        try {
            var payment = getBreezServices().sendSpontaneousPayment(nodeId, amountSats.toULong())
            promise.resolve(readableMapOf(payment))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling sendSpontaneousPayment", e)
        }
    }

    @ReactMethod
    fun receivePayment(amountSats: Double, description: String, promise: Promise) {
        try {
            var payment = getBreezServices().receivePayment(amountSats.toULong(), description)
            promise.resolve(readableMapOf(payment))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling receivePayment", e)
        }
    }

    @ReactMethod
    fun lnurlAuth(reqData: ReadableMap, promise: Promise) {
        var lnUrlAuthRequestData = asLnUrlAuthRequestData(reqData)

        if (lnUrlAuthRequestData == null) {
            promise.reject(TAG, "Invalid reqData")
        } else {
            try {
                var lnUrlCallbackStatus = getBreezServices().lnurlAuth(lnUrlAuthRequestData)
                promise.resolve(readableMapOf(lnUrlCallbackStatus))
            } catch (e: SdkException) {
                e.printStackTrace()
                promise.reject(TAG, e.message ?: "Error calling lnurlAuth", e)
            }
        }
    }

    @ReactMethod
    fun payLnurl(reqData: ReadableMap, amountSats: Double, comment: String, promise: Promise) {
        var lnUrlPayRequestData = asLnUrlPayRequestData(reqData)

        if (lnUrlPayRequestData == null) {
            promise.reject(TAG, "Invalid reqData")
        } else {
            try {
                var optionalComment = comment.takeUnless { it.isEmpty() }
                var lnUrlPayResult = getBreezServices().payLnurl(lnUrlPayRequestData, amountSats.toULong(), optionalComment)
                promise.resolve(readableMapOf(lnUrlPayResult))
            } catch (e: SdkException) {
                e.printStackTrace()
                promise.reject(TAG, e.message ?: "Error calling payLnurl", e)
            }
        }
    }

    @ReactMethod
    fun withdrawLnurl(reqData: ReadableMap, amountSats: Double, description: String, promise: Promise) {
        var lnUrlWithdrawRequestData = asLnUrlWithdrawRequestData(reqData)

        if (lnUrlWithdrawRequestData == null) {
            promise.reject(TAG, "Invalid reqData")
        } else {
            try {
                var optionalDescription = description.takeUnless { it.isEmpty() }
                var lnUrlCallbackStatus = getBreezServices().withdrawLnurl(lnUrlWithdrawRequestData, amountSats.toULong(), optionalDescription)
                promise.resolve(readableMapOf(lnUrlCallbackStatus))
            } catch (e: SdkException) {
                e.printStackTrace()
                promise.reject(TAG, e.message ?: "Error calling withdrawLnurl", e)
            }
        }
    }

    @ReactMethod
    fun nodeInfo(promise: Promise) {
        try {
            getBreezServices().nodeInfo()?.let {nodeState->
                promise.resolve(readableMapOf(nodeState))
            } ?: run {
                promise.reject(TAG, "No available node info")
            }
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling nodeInfo", e)
        }
    }

    @ReactMethod
    fun listPayments(filter: String, fromTimestamp: Double, toTimestamp: Double, promise: Promise) {
        try {
            var optionalFromTimestamp = fromTimestamp.takeUnless { it == 0.0 }
            var optionalToTimestamp = toTimestamp.takeUnless { it == 0.0 }
            var payments = getBreezServices().listPayments(asPaymentTypeFilter(filter), optionalFromTimestamp?.toLong(), optionalToTimestamp?.toLong())
            promise.resolve(readableArrayOf(payments))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling listPayments", e)
        }
    }

    @ReactMethod
    fun sweep(toAddress: String, feeRateSatsPerVbyte: Double, promise: Promise) {
        try {
            getBreezServices().sweep(toAddress, feeRateSatsPerVbyte.toULong())
            promise.resolve(readableMapOf("status" to "ok"))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling sweep", e)
        }
    }

    @ReactMethod
    fun fetchFiatRates(promise: Promise) {
        try {
            var rates = getBreezServices().fetchFiatRates()
            promise.resolve(readableArrayOf(rates))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling fetchFiatRates", e)
        }
    }

    @ReactMethod
    fun listFiatCurrencies(promise: Promise) {
        try {
            var fiatCurrencies = getBreezServices().listFiatCurrencies()
            promise.resolve(readableArrayOf(fiatCurrencies))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling listFiatCurrencies", e)
        }
    }

    @ReactMethod
    fun listLsps(promise: Promise) {
        try {
            var lsps = getBreezServices().listLsps()
            promise.resolve(readableArrayOf(lsps))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling listLsps", e)
        }
    }

    @ReactMethod
    fun connectLsp(lspId: String, promise: Promise) {
        try {
            getBreezServices().connectLsp(lspId)
            promise.resolve(readableMapOf("status" to "ok"))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling connectLsp", e)
        }
    }

    @ReactMethod
    fun fetchLspInfo(lspId: String, promise: Promise) {
        try {
            getBreezServices().fetchLspInfo(lspId)?.let {lspInformation->
                promise.resolve(readableMapOf(lspInformation))
            } ?: run {
                promise.reject(TAG, "No available lsp info")
            }
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling fetchLspInfo", e)
        }
    }

    @ReactMethod
    fun lspId(promise: Promise) {
        try {
            getBreezServices().lspId()?.let {lspId->
                promise.resolve(lspId)
            } ?: run {
                promise.reject(TAG, "No available lsp id")
            }
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling lspId", e)
        }
    }

    @ReactMethod
    fun closeLspChannels(promise: Promise) {
        try {
            getBreezServices().closeLspChannels()
            promise.resolve(readableMapOf("status" to "ok"))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling closeLspChannels", e)
        }
    }

    @ReactMethod
    fun receiveOnchain(promise: Promise) {
        try {
            var swapInfo = getBreezServices().receiveOnchain()
            promise.resolve(readableMapOf(swapInfo))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling receiveOnchain", e)
        }
    }

    @ReactMethod
    fun inProgressSwap(promise: Promise) {
        try {
            getBreezServices().inProgressSwap()?.let {swapInfo->
                promise.resolve(readableMapOf(swapInfo))
            } ?: run {
                promise.reject(TAG, "No available in progress swap")
            }
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling inProgressSwap", e)
        }
    }

    @ReactMethod
    fun listRefundables(promise: Promise) {
        try {
            var swapInfos = getBreezServices().listRefundables()
            promise.resolve(readableArrayOf(swapInfos))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling listRefundables", e)
        }
    }

    @ReactMethod
    fun refund(swapAddress: String, toAddress: String, satPerVbyte: Double, promise: Promise) {
        try {
            var result = getBreezServices().refund(swapAddress, toAddress, satPerVbyte.toUInt())
            promise.resolve(result)
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling refund", e)
        }
    }

    @ReactMethod
    fun executeDevCommand(command: String, promise: Promise) {
        try {
            var result = getBreezServices().executeDevCommand(command)
            promise.resolve(result)
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling executeDevCommand", e)
        }
    }

    @ReactMethod
    fun recommendedFees(promise: Promise) {
        try {
            var fees = getBreezServices().recommendedFees()
            promise.resolve(readableMapOf(fees))
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling recommendedFees", e)
        }
    }

    @ReactMethod
    fun buyBitcoin(provider: String, promise: Promise) {
        try {
            var result = getBreezServices().buyBitcoin(provider)
            promise.resolve(result)
        } catch (e: SdkException) {
            e.printStackTrace()
            promise.reject(TAG, e.message ?: "Error calling buyBitcoin", e)
        }
    }
}
