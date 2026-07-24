package ua.edg.logparser.parser;

import org.apache.logging.log4j.LogManager;
import transimpex.logParser.TableRowDTO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.edg.logparser.gui.Panel.PATH;

public abstract class LocalFileRider{

	public static final String client_regex = "^(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2}:\\d{2})\\s(.*)\\(host\\s(\\d+\\.\\d+\\.\\d+\\.\\d+);\\ssession\\s(.*)\\)\\s>\\s(\\w+\\.)*(\\w*)\\s>\\s([^\n]*)\n?";
	public static final String server_regex = "^(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2}:\\d{2}\\sServer\\(process=\\d+\\smainBuilder=\\d+\\sdeliberateBuilder=\\d+\\suserMarket=(\\d+)\\)>\\s([^\n]*)\n?)";
	public static final Pattern CLIENT_PATTERN = Pattern.compile(client_regex);
	public static final Pattern SERVER_PATTERN = Pattern.compile(server_regex);

	public static final String baseFile = "log.txt";

	public static final DateTimeFormatter FORMATTER = DateTimeFormatter
			.ofPattern("dd.MM.yyyy HH:mm:ss");

	private final LocalDateTime from_this_date;
	private final LocalDateTime to_this_date;

	public LocalFileRider(LocalDateTime from_this_date, LocalDateTime to_this_date){
		this.from_this_date = from_this_date.minusDays(1);
		this.to_this_date = to_this_date.plusDays(1);
	}

	public void doAction(){
		String prefix = baseFile + ".dd.MM.yyyy.";
    File folder = new File(PATH);
		File base = new File(folder, baseFile);
		int length = prefix.length();
		File[] files = folder.listFiles((dir, name) -> name.length() > length && name.substring(length).matches("[0-9]*"));
		List<File> fileList = new ArrayList<>();

		if(files != null){
			fileList.addAll(Arrays.stream(files).sorted(Comparator.comparingInt(o -> Integer.parseInt(o.getName()
					.substring(length)))).toList());
		}
		if(base.exists() && base.isFile()){
			fileList.add(base);
		}

		for(File file : fileList){
			String[] parts = file.getName().split("\\.");
			String dateStr = parts[4] + "-" + parts[3] + "-" + parts[2];
			LocalDate lastFileRowDate = LocalDate.parse(dateStr);
			if(lastFileRowDate.isAfter(from_this_date.toLocalDate()) && lastFileRowDate.isBefore(to_this_date.toLocalDate()) ||
					lastFileRowDate.equals(from_this_date.toLocalDate()) || lastFileRowDate.equals(to_this_date.toLocalDate())){
				getContents(file);
			}
		}
	}

	private void getContents(File file){
		try(BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(Files.newInputStream(file.toPath(),
						StandardOpenOption.READ), StandardCharsets.UTF_8), 1000000)){
			String string;
			Map<Integer, transimpex.logParser.TableRowDTO> tableRowDTOs = new HashMap<>();
			while((string = bufferedReader.readLine()) != null){
				if(string.matches(client_regex)){
					TableRowDTO tableRowDTO = getTableRowDTO(string);
					tableRowDTOs.put(tableRowDTO.getSession(), tableRowDTO);
				}
				else if(string.matches(server_regex)){
					Matcher matcher = SERVER_PATTERN.matcher(string);
					if(!matcher.find()){
						throw new IllegalArgumentException("param string has wrong format");
					}

					int session = Integer.parseInt(matcher.group(2));
					TableRowDTO one = tableRowDTOs.remove(session);
					if(one != null) one.setServerResponse(string);
					addItemToScope(one);
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

	private static TableRowDTO getTableRowDTO(String string){
		Matcher matcher = CLIENT_PATTERN.matcher(string);
		if(!matcher.find()){
			throw new IllegalArgumentException("param string has wrong format");
		}
		TableRowDTO tableRowDTO = new TableRowDTO();
		tableRowDTO.setDateTime(LocalDateTime.parse(matcher.group(1), FORMATTER));
		tableRowDTO.setLogin(matcher.group(2));
		tableRowDTO.setHost(matcher.group(3));
		tableRowDTO.setSession(matcher.group(4).trim().isEmpty() ? 0 :
				Integer.parseInt(matcher.group(4)));
		tableRowDTO.setClazz(matcher.group(5));
		tableRowDTO.setMethod(matcher.group(6));
		tableRowDTO.setParam(matcher.group(7));
		return tableRowDTO;
	}

	protected abstract void addItemToScope(transimpex.logParser.TableRowDTO dTO);
}
