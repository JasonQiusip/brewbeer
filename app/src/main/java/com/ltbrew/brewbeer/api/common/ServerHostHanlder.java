package com.ltbrew.brewbeer.api.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.ltbrew.brewbeer.api.model.HostPorts;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ServerHostHanlder
{
  private static final String ROUTE_URL = "http://%s:8001/routes";
  private static final String DN = "5bands.com";
  public static final String BACKUPHOST = "117.28.254.73";
  private static boolean tryedBackupHost = false;
  
  public static ArrayList<String> getIpAddressFromServer()
    throws UnknownHostException
  {
    tryedBackupHost = false;
    ArrayList<String> addrList = new ArrayList();
    
    InetAddress[] inetAddresses = InetAddress.getAllByName("5bands.com");
    for (InetAddress inetAddress : inetAddresses) {
      addrList.add(inetAddress.getHostAddress());
    }
    return addrList;
  }
  
  public static ArrayList<HostPorts> getHostPort(ArrayList<String> addrList)
    throws JSONException
  {
    int size = addrList.size();
    ArrayList<HostPorts> list = new ArrayList();
    for (int i = 0; i < size; i++)
    {
      String addr = (String)addrList.get(i);
      String routUrl = String.format("http://%s:8001/routes", new Object[] { addr });
      HttpResponse result = HttpMethods.httpGet(routUrl, null, null);
      HostPorts hostPorts = handleResult(result);
      if (hostPorts != null)
      {
        hostPorts.getApi().setHost(addr);
        hostPorts.getSso().setHost(addr);
        hostPorts.getFile().setHost(addr);
        hostPorts.getDirect_push().setHost(addr);
        list.add(hostPorts);
      }
    }
    CSSLog.showLog("HostPorts   ", "==============list==============" + list + "  ");
    if ((list.size() == 0) && (!tryedBackupHost))
    {
      tryedBackupHost = true;
      addrList.clear();
      addrList.add("117.28.254.73");
      list = getHostPort(addrList);
    }
    return list;
  }
  
  private static HostPorts handleResult(HttpResponse result)
    throws JSONException
  {
    if (result.isSuccess())
    {
      HostPorts host = null;
      try {
        host = JSON.parseObject(result.getContent(), HostPorts.class);
      }catch (JSONException e){
        e.printStackTrace();
        return null;
      }
      return host;
    }
    return null;
  }
}
