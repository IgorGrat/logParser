 package ua.edg.logparser.gui;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 
 
 public class Panel{
   
   public static String PATH;
   public static Panel globalLinkPanel;
   private final JDialog owner;
   private final JLabel label;
   
   public Panel(int port, String path) {
     this.owner = new JDialog();
     this.owner.setDefaultCloseOperation(2);
     this.owner.setTitle("Парсер логов");
     PATH = path;
     globalLinkPanel = this;
     Container container = this.owner.getContentPane();
     container.setLayout(new BoxLayout(container, 1));
     Dimension dimension = new Dimension(400, 30);
     this.label = new JLabel();
     this.label.setMinimumSize(dimension);
     this.label.setPreferredSize(dimension);
     this.label.setMaximumSize(dimension);
     this.label.setBorder(BorderFactory.createEtchedBorder());
     this.label.setOpaque(true);
     this.label.setBackground(Color.gray);
     this.label.setFont(new Font("Ariel", 2, 14));
     this.label.setHorizontalAlignment(0);
     
     container.add(this.label);
     JLabel portLabel = new JLabel();
     portLabel.setPreferredSize(dimension);
     portLabel.setMinimumSize(dimension);
     portLabel.setMaximumSize(dimension);
     portLabel.setText("Порт по умолчанию : " + String.valueOf(port));
     portLabel.setFont(new Font("Ariel", 3, 16));
     portLabel.setHorizontalAlignment(0);
     portLabel.setBackground(Color.WHITE);
     
     container.add(portLabel);
     this.owner.pack();
     this.owner.setVisible(true);
     this.owner.addWindowListener(new WindowAdapter() {
       @Override
       public void windowClosing(WindowEvent e){
         System.exit(0);
       }
     });
   }
   public void setValue(String text) {
     this.label.setText("Последний запрос : " + text);
   }
   public void setColor(Color color) {
     this.label.setBackground(color);
   }
 }