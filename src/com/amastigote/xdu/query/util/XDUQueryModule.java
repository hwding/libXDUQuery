/*
        Copyright 2016 @hwding & @TrafalgarZZZ

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.

            GitHub: https://github.com/hwding/libXDUQuery
            E-mail: m@amastigote.com
*/

package com.amastigote.xdu.query.util;

import java.io.IOException;
import java.util.ArrayList;

public abstract class XDUQueryModule {
    public abstract boolean login(String... params) throws IOException;
    public abstract String getID();
    public abstract ArrayList<String> query(String... params) throws IOException;
    public abstract boolean checkIsLogin(String username) throws IOException;
}
