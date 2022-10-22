package com.lukefz.dragaliafound.steps

import com.android.apksig.ApkSigner
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

class StepSign(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        manager.updateStep(R.string.activity_patcher_step_signing)
        manager.onMessage("Loading keystore...")
        val keystoreInstance = KeyStore.getInstance("PKCS12")

        storage.keystorePath.inputStream().use {
            keystoreInstance.load(it, "dragaliafound".toCharArray())
        }

        val privateKey = keystoreInstance.getKey("dragaliafound", "dragaliafound".toCharArray()) as PrivateKey
        val certificate = keystoreInstance.getCertificate("dragaliafound") as X509Certificate
        val config = ApkSigner.SignerConfig.Builder("DragaliPatch", privateKey, listOf(certificate)).build()
        val signer = ApkSigner
            .Builder(listOf(config))
            .setV2SigningEnabled(true)
            .setInputApk(storage.alignedApk)
            .setOutputApk(storage.signedApk)
            .setDebuggableApkPermitted(true)
            .setCreatedBy("DragaliPatch")
            .build()

        manager.onMessage("Loaded keystore!")

        manager.onMessage("Signing...")
        signer.sign()
        manager.onMessage("Finished signing!")
    }
}