package com.github.alexishuf.slrpk;

import com.google.common.base.Preconditions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws Throwable {
        Class<?> clazz;
        switch (args[0]) {
            case "update-csv": clazz = UpdateCsv.class; break;
            case "set-field-expr": clazz = SetFieldExpr.class; break;
            case "set-field-rx": clazz = SetFieldRx.class; break;
            case "expr": clazz = RunExpression.class; break;
            default: clazz = null; break;
        }

        Preconditions.checkArgument(clazz != null, "Bad command name: " + args[0]);
        String[] subArgs = new String[args.length-1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);

        try {
            Method main = clazz.getMethod("main", String[].class);
            main.invoke(null, (Object) subArgs);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
