package net.mentalarray.doozie.Internal

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}
import java.net.{InetAddress, ServerSocket, Socket}

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding

/**
 * Created by bwilson on 1/9/15. Stold from http://www.scala-sbt.org/0.12.2/sxr/IPC.scala.html
 */

object IPC
{
  private val portMin = 4096
  private val portMax = 65536
  private val loopback = InetAddress.getByName(null)

  def client[T](port: Int)(f: IPC => T): T =
    ipc(new Socket(loopback, port))(f)

  def pullServer[T](f: Server => T): T =
  {
    val server = makeServer
    try { f(new Server(server)) }
    finally { server.close() }
  }

  def serverAction[T](socket: ServerSocket)(f: Server => T) : T = {
    f(new Server(socket))
  }

  def makeServer: ServerSocket =
  {
    val random = new java.util.Random
    def nextPort = random.nextInt(portMax - portMin + 1) + portMin
    def createServer(attempts: Int): ServerSocket =
      if(attempts > 0)
        try { new ServerSocket(nextPort, 1, loopback) }
        catch { case _: Exception => createServer(attempts - 1) }
      else
        sys.error("Could not connect to socket: maximum attempts exceeded")
    createServer(10)
  }
  def server[T](socket: ServerSocket)(f : IPC => Option[T]) : T = serverImpl(socket, f)
  def server[T](f: IPC => Option[T]): T = serverImpl(makeServer, f)
  def server[T](port: Int)(f: IPC => Option[T]): T =
    serverImpl(new ServerSocket(port, 1, loopback), f)
  private def serverImpl[T](server: ServerSocket, f: IPC => Option[T]): T =
  {
    def listen(): T =
    {
      ipc(server.accept())(f) match
      {
        case Some(done) => done
        case None => listen()
      }
    }

    try { listen() }
    finally { server.close() }
  }
  private def ipc[T](s: Socket)(f: IPC => T): T =
    try { f(new IPC(s)) }
    finally { s.close() }

  final class Server private[IPC](s: ServerSocket) extends NotNull
  {
    def port = s.getLocalPort
    def close() = s.close()
    def connection[T](f: IPC => T): T = IPC.ipc(s.accept())(f)
  }
}
final class IPC private(s: Socket) extends NotNull
{
  def port = s.getLocalPort
  private val in = new BufferedReader(new InputStreamReader(s.getInputStream))
  private val out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream))

  private def toBase64(s: String) = BaseEncoding.base64.encode(s.getBytes(Charsets.UTF_8))
  private def fromBase64(data: Array[Char]) = new String(BaseEncoding.base64.decode(data), Charsets.UTF_8)

  def send(s: String) = {
    val encodedData = toBase64(s)
    // write the data length
    out.write(encodedData.length)
    // write the data
    out.write(encodedData)
    // flush the stream
    out.flush()
  }
  def receive: String = {
    // read the length
    val length = in.read()
    // create the buffer
    val buffer = new Array[Char](length)
    // read the data
    val data = in.read(buffer)
    // check the length
    assert(data == length)
    // convert to a string
    fromBase64(buffer)
  }
}