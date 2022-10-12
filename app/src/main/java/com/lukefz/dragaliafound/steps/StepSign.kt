package com.lukefz.dragaliafound.steps

import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil
import kellinwood.security.zipsigner.ZipSigner
import kellinwood.security.zipsigner.optional.KeyStoreFileManager
import java.security.PrivateKey
import java.security.cert.X509Certificate

class StepSign(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        manager.updateStep(R.string.activity_patcher_step_signing)
        manager.onMessage("Loading keystore...")
        val keystore = KeyStoreFileManager.loadKeyStore(storage.keystorePath.path, "dragaliafound".toCharArray())
        manager.onMessage("Loaded keystore!")

        val signer = ZipSigner()

        var previousPercent = 0
        var previousMessage = ""

        signer.addProgressListener {
            if (it.percentDone % 10 == 0) {
                if (previousPercent == 0 || previousPercent != it.percentDone) {
                    previousPercent = it.percentDone
                    manager.addProgress((it.percentDone - previousPercent) / 100f)
                    if (previousMessage != it.message) {
                        previousMessage = it.message
                        manager.onMessage(it.message)
                    }
                }
            }
        }

        signer.issueLoadingCertAndKeysProgressEvent()
        val signCert = keystore.getCertificate("dragaliafound") as X509Certificate
        val signKey = keystore.getKey("dragaliafound", "dragaliafound".toCharArray()) as PrivateKey

        signer.setKeys("dragaliafound", signCert, signKey, "SHA256withRSA", null)

        manager.onMessage("Signing...")
        signer.signZip(storage.unsignedApk.toString(), storage.signedApk.toString())
        manager.onMessage("Finished signing!")
    }
}