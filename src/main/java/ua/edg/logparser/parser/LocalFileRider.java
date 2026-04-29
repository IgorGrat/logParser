package ua.edg.logparser.parser;

import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ua.edg.logparser.gui.Panel.PATH;

public abstract class LocalFileRider{

  public static final String client_regex = "^(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2}:\\d{2})\\s(.*)\\(host\\s(\\d+\\.\\d+\\.\\d+\\.\\d+);\\ssession\\s(.*)\\)\\s>\\s(\\w+\\.)*(\\w*)\\s>\\s([^\n]*)\n?";
  public static final Pattern CLIENT_PATTERN = Pattern.compile(client_regex);
  public static final String baseFile = "log.txt";
  
  public static final DateTimeFormatter FORMATTER = DateTimeFormatter
  .ofPattern("dd.MM.yyyy HH:mm:ss");

//  protected boolean doAction = true;
  private final LocalDateTime from_this_date;
  private final LocalDateTime to_this_date;

  public LocalFileRider(LocalDateTime from_this_date, LocalDateTime to_this_date){
    this.from_this_date = from_this_date;
    this.to_this_date = to_this_date;
  }

  public void doAction(){
    String prefix = baseFile + ".";
    File folder = new File(PATH);
    File base = new File(folder, baseFile);
    int length = prefix.length();
    File[] files = folder.listFiles((dir, name) -> name.length() > length && name.substring(length).matches("[0-9]*"));
    List<File> fileList = new ArrayList<>();

    if(files != null){
      fileList.addAll(Arrays.stream(files).sorted(Comparator.comparingInt(o -> Integer.parseInt(o.getName()
      .substring(length)))).collect(Collectors.toList()));
    }
    if(base.exists() && base.isFile()){
      fileList.add(base);
    }

    for(int size = fileList.size(), i = 0; i < size; i++){
      File file = fileList.get(i);
      long last_mod_long = file.lastModified();
      if(last_mod_long != 0){
        LocalDateTime lastModified = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(last_mod_long), ZoneId.systemDefault());
        if(!lastModified.isBefore(from_this_date)){
          int previous_file = i - 1;
          LocalDateTime create_file = previous_file >= 0 ?
            LocalDateTime.ofInstant(Instant.ofEpochMilli(
            fileList.get(previous_file).lastModified()), ZoneId.systemDefault()) : from_this_date;
          if(create_file.isAfter(to_this_date)){
            break;
          }
          if(((create_file.isAfter(from_this_date)
            || create_file.isEqual(from_this_date)) && create_file.isBefore(to_this_date))
            || ((lastModified.isAfter(from_this_date)
            || lastModified.isEqual(from_this_date)) && lastModified.isBefore(to_this_date))){
            getContents(file);
          }
        }
      }
    }
  }

  private void getContents(File file){
    try(BufferedReader bufferedReader = new BufferedReader(
    new InputStreamReader(Files.newInputStream(file.toPath(),
      StandardOpenOption.READ), StandardCharsets.UTF_8), 1000000)){
      String string;
//      while((string = bufferedReader.readLine()) != null && doAction){
      while((string = bufferedReader.readLine()) != null){
        if(string.matches(client_regex)){
          Matcher matcher = CLIENT_PATTERN.matcher(string);
          if(!matcher.find()){
            throw new IllegalArgumentException("param string has wrong format");
          }
          transimpex.logParser.TableRowDTO tableRowDTO = new transimpex.logParser.TableRowDTO();
          tableRowDTO.setDateTime(LocalDateTime.parse(matcher.group(1), FORMATTER));
          tableRowDTO.setLogin(matcher.group(2));
          tableRowDTO.setHost(matcher.group(3));
          tableRowDTO.setSession(matcher.group(4).trim().isEmpty() ? 0 :
          Integer.parseInt(matcher.group(4)));
          tableRowDTO.setClazz(matcher.group(5));
          tableRowDTO.setMethod(matcher.group(6));
          tableRowDTO.setParam(matcher.group(7));
          if(tableRowDTO.getDateTime().isBefore(from_this_date)){
            continue;
          }
          if(tableRowDTO.getDateTime().isAfter(to_this_date)){
            break;
          }
          addItemToScope(tableRowDTO);
        }
      }
    }
    catch(FileNotFoundException ex){
      LogManager.getLogger().error("File not found", ex);
    }
    catch(IOException ex){
      LogManager.getLogger().error("IOException", ex);
    }
  }
  protected abstract void addItemToScope(transimpex.logParser.TableRowDTO dTO);
}
