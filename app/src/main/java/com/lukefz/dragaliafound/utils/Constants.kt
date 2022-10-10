package com.lukefz.dragaliafound.utils

object Constants {
    const val PACKAGE_NAME = "com.nintendo.zaga"
    const val PATCHED_PACKAGE_NAME = "dev.dragaliafound.prod.game"
    const val APK_EXTENSION = ".apk"
    const val SUPPORTED_PACKAGE_VERSION = 173

    const val URL_MAX_LENGTH = 40
    const val URL_OFFSET_ARM64 = 0x5cbb255
    const val DEFAULT_CUSTOM_URL = "prod.dragaliafound.lukefz.xyz"

    const val ARM64_RET = 0xd65f03c0
    const val NETWORK_PACK_OFFSET_ARM64 = 0x2e56cb0
    const val NETWORK_UNPACK_OFFSET_ARM64 = 0x2e573cc

    const val AAPT = "aapt"
    const val FRAMEWORK = "framework"
    const val KEYSTORE = "dragaliafound.jks"
    const val PATCHING_DIR = "temp_patching"
}