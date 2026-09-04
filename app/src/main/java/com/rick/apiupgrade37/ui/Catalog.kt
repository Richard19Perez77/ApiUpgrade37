package com.rick.apiupgrade37.ui

data class CatalogItem(
    val route: String,
    val title: String,
    val api37: String
)

val catalogItems: List<CatalogItem> = listOf(
    CatalogItem("adaptive_detail", "Adaptive layouts & windowing", "Large-screen resizability, bubbles, desktop PiP, NavigationSuiteScaffold"),
    CatalogItem("contacts", "System contacts picker", "ContactsPickerSessionContract — no READ_CONTACTS"),
    CatalogItem("eyedropper", "System eyedropper", "Intent.ACTION_OPEN_EYE_DROPPER"),
    CatalogItem("localnet", "Local network permission", "ACCESS_LOCAL_NETWORK runtime prompt"),
    CatalogItem("photos", "Photo picker aspect ratio", "PhotoPickerUiCustomizationParams portrait 9:16"),
    CatalogItem("aapm", "Advanced Protection Mode", "AdvancedProtectionManager"),
    CatalogItem("ech", "ECH + Certificate Transparency", "domainEncryption + CT default-on"),
    CatalogItem("profiling", "ProfilingManager triggers", "OOM, cold start, anomaly, CPU kill"),
    CatalogItem("jobs", "JobScheduler debug stats", "getPendingJobReasonStats"),
    CatalogItem("alarms", "Allow-while-idle alarm listener", "setExactAndAllowWhileIdle(OnAlarmListener)"),
    CatalogItem("memory", "Memory limiter + exit info", "MemoryLimiter:AnonSwap"),
    CatalogItem("live", "Live Update semantic colors", "SEMANTIC_STYLE_* + promoted ongoing"),
    CatalogItem("metrics", "MetricStyle notifications", "Notification.MetricStyle"),
    CatalogItem("camera", "Camera & media (API 37)", "INFO_DEVICE_TYPE, RAW14, VVC, CQ encode"),
    CatalogItem("hearing", "BLE hearing aids", "TYPE_BLE_HEARING_AID + STREAM_ASSISTANT"),
    CatalogItem("uwb", "UWB downlink TDoA", "RangingManager DL-TDoA"),
    CatalogItem("appfn", "AppFunctions", "Register tools for on-device agents"),
    CatalogItem("ime", "CJKV IME accessibility", "TEXT_CHANGE_TYPE_* on AccessibilityEvent"),
    CatalogItem("behaviors_detail", "Target-37 behavior changes", "MessageQueue, static final, DCL, SMS OTP, NPU")
)
