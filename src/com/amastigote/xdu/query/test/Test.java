package com.amastigote.xdu.query.test;

import com.amastigote.xdu.query.module.ECard;
import com.amastigote.xdu.query.module.PhysicalExperiment;
import com.amastigote.xdu.query.module.SportsClock;
import com.amastigote.xdu.query.module.WaterAndElectricity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        try {
            PhysicalExperiment physicalExperiment = new PhysicalExperiment();
            System.out.println(physicalExperiment.checkIsLogin("15130188016"));
            if (physicalExperiment.login("15130188016", "deleted_a")) {
                ArrayList<String> stringArrayList_a = physicalExperiment.query();
                System.out.println(stringArrayList_a.size());
                System.out.println(stringArrayList_a);
                System.out.println(physicalExperiment.getID());
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
            Scanner scanner = new Scanner(System.in);
            String captcha = scanner.nextLine();
            if (eCard.login("15130188016", "deleted_c", captcha)) {
                ArrayList<String> stringArrayList_c = eCard.query("2016-09-10", "2016-10-06");
                System.out.println(stringArrayList_c.size());
                System.out.println(stringArrayList_c);
            }
            file.delete();

            WaterAndElectricity waterAndElectricity = new WaterAndElectricity();
            System.out.println(waterAndElectricity.login("2011011212", "deleted_d"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
