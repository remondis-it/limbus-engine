package com.remondis.limbus.monitoring.influx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.influxdb.dto.Point;

public class InfluxUtils {

  public static void sendData(Point point, InetAddress host, int port) throws IOException {
    sendData(point.lineProtocol(), host, port);
  }

  public static void sendData(String data, InetAddress host, int port) throws IOException {
    try (DatagramSocket socket = new DatagramSocket();) {
      byte[] bytes = data.getBytes();
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, host, port);
      socket.send(packet);
    }
  }
}
