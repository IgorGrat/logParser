package ua.edg.logparser.parser;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TableRowDTO {
  private LocalDateTime dateTime;
  private String login;
  private String host;
  private int session;
  private String clazz;
  private String method;
  private String param;
  private String serverResponse;

}


