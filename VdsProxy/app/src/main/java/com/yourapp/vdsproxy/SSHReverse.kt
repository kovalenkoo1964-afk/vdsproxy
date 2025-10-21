package com.yourapp.vdsproxy

import android.content.Context
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.forwarding.RemotePortForwarder
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import java.io.File

data class AppConfig(
  val host: String,
  val port: Int,
  val user: String,
  val privateKeyPem: String,
  val localSocksPort: Int = 1080,
  val remotePortOnVds: Int = 11011
)

object SSHReverse {
  private var ssh: SSHClient? = null
  private var forwardOpened = false

  fun ensure(ctx: Context, cfg: AppConfig) {
    if (ssh?.isConnected == true && forwardOpened) return
    close()

    val client = SSHClient()
    client.addHostKeyVerifier(PromiscuousVerifier())
    client.connect(cfg.host, cfg.port)

    val keyFile = writePemToAppFile(ctx, cfg.privateKeyPem)
    val kp: KeyProvider = client.loadKeys(keyFile.absolutePath)

    client.authPublickey(cfg.user, kp)

    client.newRemotePortForwarder().bind(
      RemotePortForwarder.Forward("127.0.0.1", cfg.remotePortOnVds, "127.0.0.1", cfg.localSocksPort)
    )

    ssh = client
    forwardOpened = true
  }

  private fun writePemToAppFile(ctx: Context, pem: String): File {
    val f = File(ctx.filesDir, "id_vds.pem")
    val normalized = if (pem.endsWith("\n")) pem else pem + "\n"
    f.writeText(normalized.trim())
    try { f.setReadable(false, true) } catch (_: Throwable) {}
    try { f.setWritable(true, true) } catch (_: Throwable) {}
    return f
  }

  fun close() {
    try { ssh?.disconnect() } catch (_: Throwable) {}
    try { ssh?.close() } catch (_: Throwable) {}
    ssh = null
    forwardOpened = false
  }
}
