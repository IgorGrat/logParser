 package ua.edg.logparser.gui;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
 import javax.swing.*;


 public class Panel{
   
   public static String PATH;
   public static Panel globalLinkPanel;
   private final JLabel label;
   
   public Panel(int port, String path) {
     JDialog owner = new JDialog();
     owner.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
     owner.setTitle("Logs Parser");
     PATH = path;
     globalLinkPanel = this;
     Container container = owner.getContentPane();
     container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
     Dimension dimension = new Dimension(400, 30);
     this.label = new JLabel();
     this.label.setMinimumSize(dimension);
     this.label.setPreferredSize(dimension);
     this.label.setMaximumSize(dimension);
     this.label.setBorder(BorderFactory.createEtchedBorder());
     this.label.setOpaque(true);
     this.label.setBackground(Color.gray);
     this.label.setFont(new Font("Ariel", Font.ITALIC, 14));
     this.label.setHorizontalAlignment(SwingConstants.CENTER);
     
     container.add(this.label);
     JLabel portLabel = new JLabel();
     portLabel.setPreferredSize(dimension);
     portLabel.setMinimumSize(dimension);
     portLabel.setMaximumSize(dimension);
     portLabel.setText("port by default : " + port);
     portLabel.setFont(new Font("Ariel", Font.BOLD | Font.ITALIC, 16));
     portLabel.setHorizontalAlignment(SwingConstants.CENTER);
     portLabel.setBackground(Color.WHITE);
     
     container.add(portLabel);
     owner.pack();
     owner.setVisible(true);
     owner.addWindowListener(new WindowAdapter() {
       @Override
       public void windowClosing(WindowEvent e){
         System.exit(0);
       }
     });
   }
   public void setValue(String text) {
     this.label.setText("last request : " + text);
   }
   public void setColor(Color color) {
     this.label.setBackground(color);
   }
 }