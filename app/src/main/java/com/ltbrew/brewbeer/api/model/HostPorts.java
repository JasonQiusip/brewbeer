package com.ltbrew.brewbeer.api.model;

public class HostPorts
{
  API api;
  SSO sso;
  File file;
  Direct_push direct_push;
  
  public API getApi()
  {
    return this.api;
  }
  
  public void setApi(API api)
  {
    this.api = api;
  }
  
  public File getFile()
  {
    return this.file;
  }
  
  public void setFile(File file)
  {
    this.file = file;
  }
  
  public SSO getSso()
  {
    return this.sso;
  }
  
  public void setSso(SSO sso)
  {
    this.sso = sso;
  }
  
  public Direct_push getDirect_push()
  {
    return this.direct_push;
  }
  
  public void setDirect_push(Direct_push direct_push)
  {
    this.direct_push = direct_push;
  }
  
  public String toString()
  {
    return "Hosts{api=" + this.api + ", sso=" + this.sso + ", file=" + this.file + ", directPush=" + this.direct_push + '}';
  }
}
