package ua.edg.logparser.parser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalFileRider{
  
  public static void main(String[] args){
    System.out.println("^(\\d{2}\\.\\d{2}\\.\\d{4}р\\.\\s\\d{2}:\\d{2}:\\d{2})\\s(.*)\\(host\\s(\\d+\\.\\d+\\.\\d+\\.\\d+);\\ssession\\s(.*)\\)\\s>\\s(\\w+\\.)*(\\w*)\\s>\\s([^\n]*)\n?"
    .equals("^(\\d{2}\\.\\d{2}\\.\\d{4}р\\.\\s\\d{2}:\\d{2}:\\d{2})\\s(.*)\\(host\\s(\\d+\\.\\d+\\.\\d+\\.\\d+);\\ssession\\s(.*)\\)\\s>\\s(\\w+\\.)*(\\w*)\\s>\\s([^\n]*)\n?"));
  }

//  public static final String CLIENT_REG = "^(\\d{2}\\.\\d{2}\\.\\d{4}р\\.\\s\\d{2}:\\d{2}:\\d{2})\\s(.*)\\(host\\s(\\d+\\.\\d+\\.\\d+\\.\\d+);\\ssession\\s(.*)\\)\\s>\\s(\\w+\\.)*(\\w*)\\s>\\s([^\n]*)\n?";
  public static final String client_regex = "^(\\d{2}\\.\\d{2}\\.\\d{4}р\\.\\s\\d{2}:\\d{2}:\\d{2})\\s(.*)\\(host\\s(\\d+\\.\\d+\\.\\d+\\.\\d+);\\ssession\\s(.*)\\)\\s>\\s(\\w+\\.)*(\\w*)\\s>\\s([^\n]*)\n?";
  public static final Pattern CLIENT_PATTERN = Pattern.compile(client_regex);
  
  public static final DateTimeFormatter FORMATTER = DateTimeFormatter
  .ofPattern("dd.MM.yyyyр. HH:mm:ss");

  private List<String[]> content;
  protected boolean doAction = true;

  public List getContents(File file){
    try(BufferedReader bufferedReader = new BufferedReader(
    new InputStreamReader(Files.newInputStream(file.toPath(), 
    new OpenOption[]{StandardOpenOption.READ}), "utf-8"), 1000000)){
      String string;
      while((string = bufferedReader.readLine()) != null && doAction){
//        if(string.matches("^(\\d{2}\\.\\d{2}\\.\\d{4}р\\.\\s\\d{2}:\\d{2}:\\d{2})\\s(.*)\\(host\\s(\\d+\\.\\d+\\.\\d+\\.\\d+);\\ssession\\s(.*)\\)\\s>\\s(\\w+\\.)*(\\w*)\\s>\\s([^\n]*)\n?")){
        if(string.matches(client_regex)){
          Matcher matcher = CLIENT_PATTERN.matcher(string);
          if(matcher.find() == false){
            throw new IllegalArgumentException("param string has wrong format");
          }
          TableRowDTO tableRowDTO = new TableRowDTO();
          tableRowDTO.dateTime = LocalDateTime.parse(matcher.group(1), FORMATTER);
          tableRowDTO.login = matcher.group(2);
          tableRowDTO.host = matcher.group(3);
          tableRowDTO.session = matcher.group(4).trim().isEmpty() ? 0 : 
          Integer.parseInt(matcher.group(4));
          tableRowDTO.clazz = matcher.group(5);
          tableRowDTO.method = matcher.group(6);
          tableRowDTO.param = matcher.group(7);
          addItemToScope(tableRowDTO);
        }
      }
    }
    catch(FileNotFoundException ex){
      ex.printStackTrace();
    }
    catch(IOException ex){
      ex.printStackTrace();
    }
    return content;
  }
  protected void addItemToScope(TableRowDTO dTO){
    content.add(new String[]{dTO.login});
  }
}
