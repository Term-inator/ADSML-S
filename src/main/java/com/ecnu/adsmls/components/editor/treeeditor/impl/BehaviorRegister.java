package com.ecnu.adsmls.components.editor.treeeditor.impl;

import com.ecnu.adsmls.utils.FunctionRegister;
import javafx.util.Pair;

import java.util.*;

public class BehaviorRegister extends FunctionRegister {
    // 行为名，参数名，参数类型，参数必要性
    private static LinkedHashMap<String, List<FunctionParam>> behaviorFunctions = new LinkedHashMap<>();

    // 初始化内置 behavior 及其参数
    static {
        // 匀速
        register("Keep", new ArrayList<>(List.of(
                new FunctionParam("duration", DataType.INT, Necessity.OPTIONAL))
        ));

        // TODO Acc Dec 加速度非负
        register("Accelerate", new ArrayList<>(List.of(
                new FunctionParam("acceleration", DataType.DOUBLE, Necessity.REQUIRED),
                new FunctionParam("target speed", DataType.DOUBLE, Necessity.REQUIRED),
                new FunctionParam("duration", DataType.INT, Necessity.OPTIONAL))
        ));

        register("Decelerate", new ArrayList<>(List.of(
                new FunctionParam("acceleration", DataType.DOUBLE, Necessity.REQUIRED),
                new FunctionParam("target speed", DataType.DOUBLE, Necessity.REQUIRED),
                new FunctionParam("duration", DataType.INT, Necessity.OPTIONAL))
        ));

        register("ChangeLeft", new ArrayList<>(List.of(
                new FunctionParam("acceleration", DataType.DOUBLE, Necessity.OPTIONAL),
                new FunctionParam("target speed", DataType.DOUBLE, Necessity.OPTIONAL)
        )));
        register("ChangeRight", new ArrayList<>(List.of(
                new FunctionParam("acceleration", DataType.DOUBLE, Necessity.OPTIONAL),
                new FunctionParam("target speed", DataType.DOUBLE, Necessity.OPTIONAL)
        )));

        register("TurnLeft", new ArrayList<>(List.of(
                new FunctionParam("acceleration", DataType.DOUBLE, Necessity.REQUIRED),
                new FunctionParam("target speed", DataType.DOUBLE, Necessity.REQUIRED)
        )));
        register("TurnRight",new ArrayList<>(List.of(
                new FunctionParam("acceleration", DataType.DOUBLE, Necessity.REQUIRED),
                new FunctionParam("target speed", DataType.DOUBLE, Necessity.REQUIRED)
        )));

        // 静止且什么都不做
        register("Idle", new ArrayList<>(List.of(
                new FunctionParam("duration", DataType.DOUBLE, Necessity.OPTIONAL)
        )));
    }

    public static void register(String behaviorName, List<FunctionParam> params) {
        if(behaviorFunctions.containsKey(behaviorName)) {
            return;
        }
        behaviorFunctions.put(behaviorName, params);
    }

    public static List<String> getBehaviorNames() {
        return new ArrayList<>(behaviorFunctions.keySet());
    }

    public static List<FunctionParam> getParams(String behaviorName) {
        return behaviorFunctions.get(behaviorName);
    }
}
