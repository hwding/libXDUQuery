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

package com.amastigote.xdu.query.test;

import com.amastigote.xdu.query.module.ECard;
import com.amastigote.xdu.query.module.PhysicsExperiment;
import com.amastigote.xdu.query.module.SportsClock;
import com.amastigote.xdu.query.module.WaterAndElectricity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        try {
            PhysicsExperiment physicsExperiment = new PhysicsExperiment();
            System.out.println(physicsExperiment.checkIsLogin("15130188016"));
            if (physicsExperiment.login("15130188016", "deleted_a")) {
                ArrayList<String> stringArrayList_a = physicsExperiment.query();
                System.out.println(stringArrayList_a.size());
                System.out.println(stringArrayList_a);
                System.out.println(physicsExperiment.getID());
            }

            SportsClock sportsClock = new SportsClock();
            if (sportsClock.login("15130188016", "deleted_b")) {
                ArrayList<String> stringArrayList_b = sportsClock.query();
                System.out.println(stringArrayList_b.size());
                System.out.println(stringArrayList_b);
                System.out.println(sportsClock.getID());
            }

            ECard eCard = new ECard();
            File file = new File("temp_captcha.jpeg");
            eCard.getCaptcha(file);
            System.out.print("Captcha generated in PATH, enter: ");
            Scanner scanner = new Scanner(System.in);
            String captcha = scanner.nextLine();
            if (eCard.login("15130188016", "deleted_c", captcha)) {
                ArrayList<String> stringArrayList_c = eCard.query("2016-09-10", "2016-10-06");
                System.out.println(stringArrayList_c.size());
                System.out.println(stringArrayList_c);
            }
            boolean tmp_bool = file.delete();
            System.out.println(tmp_bool);

            WaterAndElectricity waterAndElectricity = new WaterAndElectricity();
            if (waterAndElectricity.login("2011022212","deleted_d")) {
                ArrayList<String> stringArrayList_d = waterAndElectricity.query(WaterAndElectricity.METER);
                ArrayList<String> stringArrayList_e = waterAndElectricity.query(WaterAndElectricity.PAY, WaterAndElectricity.ONE_MONTH);
                System.out.println(stringArrayList_d.size());
                System.out.println(stringArrayList_d);
                System.out.println(stringArrayList_e.size());
                System.out.println(stringArrayList_e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
