package com.example.attestationtest

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        setContentView(textView)

        val result = generateKeyAndAttest()
        textView.text = result
    }

    private fun generateKeyAndAttest(): String {
        return try {
            val alias = "test_key"

            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )

            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setAttestationChallenge("hello_attestation".toByteArray())
                .build()

            keyPairGenerator.initialize(spec)
            keyPairGenerator.generateKeyPair()

            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val certChain = keyStore.getCertificateChain(alias)

            val sb = StringBuilder()
            sb.append("Attestation cert chain:\n\n")

            certChain.forEachIndexed { index, cert ->
                sb.append("Cert $index:\n")
                sb.append(Base64.encodeToString(cert.encoded, Base64.DEFAULT))
                sb.append("\n\n")
            }

            sb.toString()

        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
