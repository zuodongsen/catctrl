package com.zds.carctrl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpScan {
    public IpScan() {
        this.localIp = getLocalIPAddress();
    }
    public String getLocalIp() {
        return this.localIp;
    }

    public void scan() {
        String ipPreFix = this.localIp.substring(0, this.localIp.lastIndexOf(".") + 1);
        System.out.println(ipPreFix);
        for(int loop = 0; loop < 255; loop++){
            String ipPeer = ipPreFix + String.valueOf(loop);
            System.out.println(ipPeer);
        }
    }

    private String getLocalIPAddress() {
        try {
            for(Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for(Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();){
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if(!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)){
                        System.out.println(inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "null";
    }

    private String localIp;
}
