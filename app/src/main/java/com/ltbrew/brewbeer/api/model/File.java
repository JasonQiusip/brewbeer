package com.ltbrew.brewbeer.api.model;

import java.util.ArrayList;

public class File
{
  ArrayList<Integer> ports;
  int ttl;
  String host;
  
  public ArrayList<Integer> getPorts()
  {
    return this.ports;
  }
  
  public void setPorts(ArrayList<Integer> ports)
  {
    this.ports = ports;
  }
  
  public int getTtl()
  {
    return this.ttl;
  }
  
  public void setTtl(int ttl)
  {
    this.ttl = ttl;
  }
  
  public String getHost()
  {
    return this.host;
  }
  
  public String toString()
  {
    return "File{host='" + this.host + '\'' + ", ports=" + this.ports + ", ttl=" + this.ttl + '}';
  }
  
  public void setHost(String host)
  {
    this.host = host;
  }
}
