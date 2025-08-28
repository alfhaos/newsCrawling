package com.news.newscrawling.model.command;

import java.io.IOException;

public interface CommandInterface {
    void execute(String url) throws IOException;
}
