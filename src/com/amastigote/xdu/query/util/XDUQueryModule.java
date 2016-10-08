package com.amastigote.xdu.query.util;

import java.io.IOException;
import java.util.ArrayList;

public abstract class XDUQueryModule {
    public abstract boolean login(String... params) throws IOException;
    public abstract String getID();
    public abstract ArrayList<String> query(String... params) throws IOException;
    public abstract boolean checkIsLogin(String username) throws IOException;
}
