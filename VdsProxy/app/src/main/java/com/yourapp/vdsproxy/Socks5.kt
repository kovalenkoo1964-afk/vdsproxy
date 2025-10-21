package com.yourapp.vdsproxy

import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

object Socks5 {
  @Volatile private var server: ServerSocket? = null
  private var acceptJob: Job? = null

  fun ensure(port: Int = 1080) {
    if (server?.isClosed != false) {
      server = ServerSocket(port, 128, java.net.InetAddress.getByName("127.0.0.1"))
      acceptJob = CoroutineScope(Dispatchers.IO).launch {
        while (isActive) {
          val c = server!!.accept()
          launch { handle(c) }
        }
      }
    }
  }

  fun stop() {
    acceptJob?.cancel()
    server?.close()
    server = null
  }

  private fun readFully(s: Socket, n: Int): ByteArray {
    val b = ByteArray(n); var o=0
    val ins = s.getInputStream()
    while (o<n) { val r = ins.read(b, o, n-o); if (r<=0) throw Exception("EOF"); o+=r }
    return b
  }

  private fun handle(c: Socket) {
    try {
      val ins = c.getInputStream(); val out = c.getOutputStream()
      if (ins.read()!=0x05) throw Exception("Not SOCKS5")
      val nMethods = ins.read(); readFully(c, nMethods)     // ignore methods
      out.write(byteArrayOf(0x05, 0x00)); out.flush()       // no auth

      val h = readFully(c,4) // ver, cmd, rsv, atyp
      if (h[0].toInt()!=0x05 || h[1].toInt()!=0x01) throw Exception("Only CONNECT")
      val atyp = h[3].toInt()
      val host = when (atyp) {
        0x01 -> { val a=readFully(c,4); "${a[0].toInt() and 0xff}.${a[1].toInt() and 0xff}.${a[2].toInt() and 0xff}.${a[3].toInt() and 0xff}" }
        0x03 -> { val l = ins.read(); String(readFully(c,l), StandardCharsets.US_ASCII) }
        0x04 -> { val a=readFully(c,16); java.net.Inet6Address.getByAddress(a).hostAddress }
        else -> throw Exception("ATYP")
      }
      val pb = readFully(c,2)
      val port = ((pb[0].toInt() and 0xff) shl 8) or (pb[1].toInt() and 0xff)

      val r = Socket(host, port); r.tcpNoDelay = true
      out.write(byteArrayOf(0x05,0x00,0x00,0x01,0,0,0,0,0,0)); out.flush()

      val j1 = CoroutineScope(Dispatchers.IO).launch { c.getInputStream().copyTo(r.getOutputStream()) }
      val j2 = CoroutineScope(Dispatchers.IO).launch { r.getInputStream().copyTo(c.getOutputStream()) }
      runBlocking { j1.join(); j2.join() }
      r.close(); c.close()
    } catch (_: Throwable) { try { c.close() } catch (_: Throwable) {} }
  }
}
