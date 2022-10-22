package com.lukefz.dragaliafound.utils

object Constants {
    const val PACKAGE_NAME = "com.nintendo.zaga"
    const val PATCHED_PACKAGE_NAME = "dev.dragaliafound.prod.game"
    const val APK_EXTENSION = ".apk"
    const val SUPPORTED_PACKAGE_VERSION = 173

    const val URL_MAX_LENGTH = 40
    const val DEFAULT_CUSTOM_URL = "https://prod.dragaliafound.lukefz.xyz"

    var currentCustomUrl = DEFAULT_CUSTOM_URL

    object Arm64Constants {
        const val RET = 0xd65f03c0
        const val NETWORK_PACK = 0x2e56cb0L
        const val NETWORK_UNPACK = 0x2e573ccL
        const val URL_OFFSET = 0x5cbb255L
    }

    object Arm32Constants {
        const val RET = 0xe1a0f00e
        const val NETWORK_PACK = 0x2452a4cL
        const val NETWORK_UNPACK = 0x24532acL
        const val URL_OFFSET = 0x497b8d9L
    }

    const val BAAS_URL_LOCATION = "assets/npf.json"
    const val DEFAULT_BAAS_URL = "48cc81cdb8de30e061928f56e9bd4b4d.baas.nintendo.com"
    const val DEFAULT_ACCOUNTS_URL = "accounts.nintendo.com"

    const val GITHUB_URL = "https://github.com/LukeFZ/DragaliPatch"
    const val PROJECT_EARTH_GITHUB_URL = "https://github.com/Project-Earth-Team/PatcherApp"
    const val PATCH_DOWNLOAD_URL = "https://github.com/DragaliaLostRevival/DragaliPatch-Patches/archive/main.zip"

    const val AAPT = "aapt"
    const val ZIPALIGN = "zipalign"
    const val FRAMEWORK = "framework"
    const val KEYSTORE = "dragaliafound.p12"
    const val PATCHING_DIR = "temp_patching"
    const val PATCH_DOWNLOAD_DIR = "patch"
    const val COLOR_DIR = "colorprofiles"
    const val MANIFEST = "AndroidManifest.xml"
    const val APKTOOL = "apktool.yml"
    const val TEMP_PREFIX = "temp_"
    const val UNSIGNED_SUFFIX = "_unsigned"
    const val ALIGNED_SUFFIX = "_aligned"
}