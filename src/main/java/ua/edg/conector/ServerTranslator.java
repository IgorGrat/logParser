package ua.edg.conector;
 
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
 
public class ServerTranslator{
   
  public void turnOnListener(int port){
    if(port == 0){
      throw new IllegalArgumentException("port is invalid");
    } 
    try(ServerSocket ss = new ServerSocket(port)){
      while(true){
        new ClientsThread(ss.accept()).start();
      }
    } 
    catch (BindException e){
      LogManager.getLogger().error("port is busy");
    }
    catch (IOException e) {
      LogManager.getLogger().error("server error");
    }
  }
}