package net.mentalarray.doozie.Internal.RPC

import java.net.InetAddress

/**
 * Created by kdivincenzo on 2/6/15.
 */
trait TcpDetails {

  protected final val portMin = 4096
  protected final val portMax = 65536
  protected final val loopback = InetAddress.getByName(null)

}
