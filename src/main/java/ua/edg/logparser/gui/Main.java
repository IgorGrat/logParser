 package ua.edg.logparser.gui;
 
 import javax.swing.SwingUtilities;
 import ua.edg.conector.ServerTranslator;
 
 public class Main
 {
   public static void main(String[] args) {
     int port = Integer.parseInt(args[0]);
     String path = args[1];
     (new Thread(() -> (new ServerTranslator()).turnOnListener(port))).start();
     SwingUtilities.invokeLater(() -> new Panel(port, path));
   }
 }
