package com.ecnu.adsmls.utils;

import java.util.*;

public class SimulatorConstant {
    public enum SimulatorType {
        CARLA("CARLA"), LGSVL("LGSVL");

        public String value;

        SimulatorType(String value) {
            this.value = value;
        }
    }

    private static final Map<SimulatorType, String[]> weathers = Map.of(
            SimulatorType.CARLA, new String[]{
                    "random",
                    "ClearNoon",
                    "CloudyNoon",
                    "WetNoon",
                    "WetCloudyNoon",
                    "SoftRainNoon",
                    "MidRainyNoon",
                    "HardRainNoon",
                    "ClearSunset",
                    "CloudySunset",
                    "WetSunset",
                    "WetCloudySunset",
                    "SoftRainSunset",
                    "MidRainSunset",
                    "HardRainSunset"
            },
            SimulatorType.LGSVL, new String[]{
                    "random",
                    "clear",
                    "rainy"
            }
    );

    private static final Map<SimulatorType, String[]> models = Map.of(
            SimulatorType.CARLA, new String[]{
                    "random",
                    "vehicle.audi.a2",
                    "vehicle.audi.etron",
                    "vehicle.audi.tt",
                    "vehicle.bmw.grandtourer",
                    "vehicle.carlamotors.carlacola",
                    "vehicle.carlamotors.firetruck",
                    "vehicle.chevrolet.impala",
                    "vehicle.citroen.c3",
                    "vehicle.dodge.charger_2020",
                    "vehicle.dodge.charger_police",
                    "vehicle.dodge.charger_police_2020",
                    "vehicle.ford.ambulance",
                    "vehicle.ford.crown",
                    "vehicle.ford.mustang",
                    "vehicle.jeep.wrangler_rubicon",
                    "vehicle.kawasaki.ninja",
                    "vehicle.lincoln.mkz_2017",
                    "vehicle.lincoln.mkz_2020",
                    "vehicle.mercedes.coupe",
                    "vehicle.mercedes.coupe_2020",
                    "vehicle.mercedes.sprinter",
                    "vehicle.micro.microlino",
                    "vehicle.mini.cooper_s",
                    "vehicle.mini.cooper_s_2021",
                    "vehicle.nissan.micra",
                    "vehicle.nissan.patrol",
                    "vehicle.nissan.patrol_2021",
                    "vehicle.seat.leon",
                    "vehicle.tesla.cybertruck",
                    "vehicle.tesla.model3",
                    "vehicle.toyota.prius",
                    "vehicle.volkswagen.t2",
                    "vehicle.volkswagen.t2_2021"
            },
            SimulatorType.LGSVL, new String[]{"random"});

    public static List<String> getSimulatorTypeList() {
        List<String> simulatorList = new ArrayList<>();
        for(SimulatorType simulatorType: SimulatorType.values()) {
            simulatorList.add(simulatorType.value);
        }
        return simulatorList;
    }

    public static SimulatorType getSimulatorTypeByValue(String simulator) {
        return Arrays.stream(SimulatorType.values())
                .filter(simulatorType -> Objects.equals(simulatorType.value, simulator))
                .findAny()
                .orElse(SimulatorType.CARLA);
    }

    /**
     * 获取蓝图
     */
    public static String[] getModel(SimulatorType simulator) {
        return models.get(simulator);
    }

    /**
     * 设置天气
     */
    public static String[] getWeather(SimulatorType simulator) {
        return weathers.get(simulator);
    }
}
