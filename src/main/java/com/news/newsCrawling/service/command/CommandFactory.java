package com.news.newsCrawling.service.command;

import com.news.newsCrawling.model.contants.COMMAND_SITE_TYPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandFactory {

    private final Map<String, CommandInterface> commandMap = new HashMap<>();

    @Autowired
    public CommandFactory(List<CommandInterface> commandList) {
        for (CommandInterface command : commandList) {
            String name = command.getClass().getName();
            if(name.equals(DaumCommand.class.getName())) {
                commandMap.put(COMMAND_SITE_TYPE.DAUM.getValue(), command);
            } else if(name.equals(DaumRecursiveCommand.class.getName())) {
                commandMap.put(COMMAND_SITE_TYPE.RECURSIVE_DAUM.getValue(), command);
            }
        }
    }

    public CommandInterface getCommand(String type) {
        return commandMap.get(type);
    }
}
