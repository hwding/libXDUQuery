package util;

import java.io.IOException;
import java.util.ArrayList;

public abstract class XDUQueryModule {
    protected abstract boolean login(String... params) throws IOException;
    public abstract String getID();
    public abstract ArrayList<String> query(String... params) throws IOException;
    public abstract boolean checkIsLogin(String username) throws IOException;
}
