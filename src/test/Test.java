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

package test;

import com.amastigote.xdu.query.conf.Duration;
import com.amastigote.xdu.query.conf.QueryType;
import com.amastigote.xdu.query.conf.Type;
import com.amastigote.xdu.query.module.*;
import org.json.JSONObject;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        try {
            PhysicsExperiment physicsExperiment = new PhysicsExperiment();
            System.out.println(physicsExperiment.checkIsLogin("15130188016"));
            if (physicsExperiment.login("15130188016", "deleted_a")) {
                List<String> stringArrayList_a = physicsExperiment.query();
                System.out.println(stringArrayList_a.size());
                System.out.println(stringArrayList_a);
                System.out.println(physicsExperiment.getID());
            }

            SportsClock sportsClock = new SportsClock();
            if (sportsClock.login("15130188016", "deleted_b")) {
                List<String> stringArrayList_b = sportsClock.query();
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
                List<String> stringArrayList_c = eCard.query("2016-09-10", "2016-10-06");
                System.out.println(stringArrayList_c.size());
                System.out.println(stringArrayList_c);
            }
            boolean tmp_bool = file.delete();
            System.out.println(tmp_bool);

            WaterAndElectricity waterAndElectricity = new WaterAndElectricity();
            if (waterAndElectricity.login("2011022212", "deleted_d")) {
                List<String> stringArrayList_d = waterAndElectricity.query(Type.METER, null);
                List<String> stringArrayList_e = waterAndElectricity.query(Type.PAY, Duration.ONE_MONTH);
                System.out.println(stringArrayList_d.size());
                System.out.println(stringArrayList_d);
                System.out.println(stringArrayList_e.size());
                System.out.println(stringArrayList_e);
            }

            EduSystem eduSystem = new EduSystem();
            if (eduSystem.login("15130188016", "deleted_e")) {
                JSONObject jsonObject_course = eduSystem.query(QueryType.COURSE);
                JSONObject jsonObject_student = eduSystem.query(QueryType.STUDENT);
                JSONObject jsonObject_grades = eduSystem.query(QueryType.GRADES);
                System.out.println(jsonObject_course.toString());
                System.out.println(jsonObject_student.toString());
                System.out.println(jsonObject_grades.toString());

                new ObjectOutputStream(new FileOutputStream("myEduAccount.xduq")).writeObject(eduSystem);
            }

            File file1 = new File("myEduAccount.xduq");
            if (file1.exists()) {
                try {
                    EduSystem eduSystem1 = (EduSystem) new ObjectInputStream(new FileInputStream(file1)).readObject();
                    if (eduSystem1.checkIsLogin(eduSystem1.getID())) {
                        System.out.println("Account session still valid.");
                        System.out.println(eduSystem1.getID());
                        JSONObject jsonObject_course = eduSystem1.query(QueryType.COURSE);
                        JSONObject jsonObject_student = eduSystem1.query(QueryType.STUDENT);
                        JSONObject jsonObject_grades = eduSystem1.query(QueryType.GRADES);
                        System.out.println(jsonObject_course.toString());
                        System.out.println(jsonObject_student.toString());
                        System.out.println(jsonObject_grades.toString());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
