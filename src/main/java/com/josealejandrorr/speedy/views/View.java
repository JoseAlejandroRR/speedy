package com.josealejandrorr.speedy.views;

import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;
import sun.rmi.runtime.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class View {

    public static final String PATH_TEMPLATES = "/views/";

    public static String render(String file, HashMap<String, Object> data)
    {
        //String content = Files.get(file);

        String content = View.loadFile(file);

        content = View.checkIncludes(content);

        content = View.parseExpresion(content, data);

        content = View.parseData(content, data);

        //View.parseExpresion(content, data);

        return content;
    }

    private static String checkIncludes(String content)
    {
        Pattern pInclude = Pattern.compile("\\@include(\\(.*?)\\)");
        Matcher matcher = pInclude.matcher(content);
        String file = "";
        while (matcher.find())
        {
            String in = matcher.group();
            file = in.substring(9, in.length() - 1);
            content = content.replace(in, View.loadFile(file));
            Logger.getLogger().debug("include ",file, View.loadFile(file));
        }
        return content;
    }

    private static String parseData(String content, HashMap<String, Object> data)
    {
        content = View.parseForEach(content, data);

        content = View.parseIfElse(content, data);

        return content;
    }

    private static  String parseIfElse(String content, HashMap<String, Object> data) {
        Pattern pIf = Pattern.compile("\\@if(.*?)@endif");
        Pattern pIfCondition = Pattern.compile("\\@if(\\(.*?)\\)");
        Pattern pIfCondition2 = Pattern.compile("\\@elseif(\\(.*?)\\)");
        Matcher mIf = null;
        Matcher mIfCondition = null;

        String[] conditions = null;

        String[] blocks = null;
        List<String> blockList = new ArrayList<String>();
        String blockIf = null;
        String[] blockElse = null;
        boolean hasElse = false
                ;

        mIf = pIf.matcher(content);

        while(mIf.find())
        {
            blockIf = mIf.group();
            blockIf = blockIf.substring(0, blockIf.length() - 6);
            blocks = blockIf.split("@elseif");
            //Logger.getLogger().debug("COUNT ",String.valueOf(blocks.length));
            /*if (blocks.length > 1) {
                //blocks = blockElse[0].split("@elseif");
                //Logger.getLogger().debug("UNO",blocks[0], blocks[1]);
                blockElse = blocks[ blocks.length - 1 ].split("@else");
                if (blockElse.length > 1) {
                    Logger.getLogger().debug("ELSE ",blockElse[0], blockElse[1]);
                }
            } else {
                Logger.getLogger().debug("ELSIF Vacio");
            }*/
            blockElse = blocks[ blocks.length - 1 ].split("@else");
        }

        if(blockIf ==null && blocks == null) return content;

        if(blocks != null ) {

            for (int i = 0; i < blocks.length ; i++)
            {
                //Logger.getLogger().debug("BLOCKS ", blocks[i]);
                if (i < blocks.length -1 ) {
                    blockList.add(blocks[i]);
                } else {
                    blockList.add(blockElse[0]);
                    if (blockElse.length == 2) {
                        hasElse = true;
                        blockList.add(blockElse[1]);
                    }
                }
            }

        }


        String cp = "";
        conditions = new String[blockList.size()];

        String condition = "";

        String result = null;

        for (int i = 0; i < blockList.size() ; i++)
        {
            if (i > 0) {
                cp = "@if";
            } else {
                cp = "";
            }
            mIfCondition = pIfCondition.matcher(cp + blockList.get(i));
            //Logger.getLogger().debug("SEARCH IN ", cp + blockList.get(i));

            while(mIfCondition.find())
            {
                condition =  mIfCondition.group();
                blockList.set(i, cp + blockList.get(i));
                //conditions[i] = condition.substring(4, condition.length() - 1);
                conditions[i] = condition;
                //Logger.getLogger().debug("nodo ",condition);
            }

            //Logger.getLogger().debug("BLOCKS ", blockList.get(i));
        }

        for(int i = 0; i < conditions.length; i++)
        {
            // Logger.getLogger().debug("Aplicar "+i,conditions[i], blockList.get(i));
            if( conditions[i] != null ){
                //Logger.getLogger().debug("works  ", conditions[i].substring(4, conditions[i].length() - 1));
                if(View.evalCondition(conditions[i].substring(4, conditions[i].length() - 1))){
                    result = blockList.get(i);
                    result = result.replace(conditions[i], "");
                    break;
                }
            }
        }

        if (result == null) {
            if (hasElse) {
                result = blockElse[1];
                //Logger.getLogger().debug("Entra en Esle ", result);
            } else {
                result = "";
            }
        }

        content = content.replace(blockIf + "@endif", result);
        //Logger.getLogger().debug("Entra\n\r", result,"por\n\r", blockIf);

        return content;
    }

    private static boolean evalCondition(String condition)
    {
        try {
            boolean value = Boolean.parseBoolean(condition);
            return value;
        } catch (Exception ex)
        {
            return false;
        }
    }


    private static  String parseForEach(String content, HashMap<String, Object> data)
    {
        Pattern pForeach = Pattern.compile("\\@foreach(.*?)@endforeach");
        Pattern pForeachCondition = Pattern.compile("\\@foreach(\\(.*?)\\)");
        Matcher mForeach = null;
        Matcher mForeachCondition = null;

        for (Map.Entry<String, Object> var : data.entrySet())
        {
            //String key = "$[" + var.getKey() + "]";
            //content = View.parseVars(content, var.getKey(), var.getValue());

            mForeachCondition = pForeachCondition.matcher(content);

            String condition = "";

            String[] conditionVars = new String[3];

            while(mForeachCondition.find())
            {
                condition = mForeachCondition.group().replace("  ", " ").replace("  ", " ");
                condition = condition.substring(9, condition.length() - 1).replace("  "," ");
                Logger.getLogger().debug("confition ", condition);
                conditionVars = condition.split(" as ");
            }

            if (conditionVars[0] != null && conditionVars[1] != null) {
                if (!conditionVars[0].toLowerCase().equals(var.getKey().toLowerCase())) {

                    //Logger.getLogger().debug("Var undefined: " + conditionVars[0], var.getKey().toLowerCase());
                } else {
                    mForeach = pForeach.matcher(content);
                    String node = "";
                    while(mForeach.find())
                    {
                        node = mForeach.group();
                        Logger.getLogger().debug("FOREACH ", node);
                        String htmlSearch = node.substring(node.indexOf(")") + 1, node.length() - 11);
                        Logger.getLogger().debug("KD",htmlSearch);

                        Iterator it = ((ArrayList) var.getValue()).iterator();
                        String[] pointer = conditionVars[1].split(" => ");
                        String kk = null;
                        String row = pointer[0];

                        int index = 0;
                        if (pointer.length == 2 && pointer[0] != null && pointer[1] != null) {
                            kk = pointer[0];
                            row = pointer[1];
                        }


                        String htmlReplacer = "";

                        String kch = "";
                        while (it.hasNext()) {
                            String pp = htmlSearch;

                            HashMap<String, String> obj = (HashMap<String, String>) it.next();
                            Logger.getLogger().debug("CHECK ",kk, row, obj.get("name"));
                            if (kk != null) {
                                pp = View.parseVars(pp, "$k", index);
                                if (!kch.equals(htmlSearch)) {

                                    Logger.getLogger().debug("KK", kch);
                                    htmlReplacer += kch;
                                }
                            }


                            for (Map.Entry<String, String> item : obj.entrySet())
                            {
                                Logger.getLogger().debug("VL2", "$" +row + "." + item.getKey(), item.getValue());
                                pp = View.parseVars(pp, "$" +row + "." + item.getKey(), item.getValue()+"=");
                                Logger.getLogger().debug("VL", pp);
                            }
                            htmlReplacer += pp;

                            Logger.getLogger().debug("POX ",htmlReplacer);
                            index++;

                        }
                        content = content.replace(node, htmlReplacer);
                        //Logger.getLogger().debug("HTML ",content);
                        //Logger.getLogger().debug("htmlReplacer ",htmlReplacer);
                    }
                }
            }





        }

        return content;
    }

    private  static String parseVars(String content, String key, Object obj)
    {
        String keyReplace = "$" + key + "";

        //Logger.getLogger().debug("CHANGE ",key, String.valueOf(obj), String.valueOf(content.indexOf(key)));
        switch (Builder.getClassName(obj))
        {
            case "java.lang.String":
                content = content.replace(key, obj.toString());
                break;

            case "java.lang.Integer":
            case "int":
            case "float":
            case "double":
                content = content.replace(key, String.valueOf(obj));
            case "java.util.HashMap":
        }
        return content;
    }

    private  static String parseExpresion(String content, HashMap<String, Object> data)
    {
        Pattern p =  Pattern.compile("\\{\\{(.+?)\\}\\}");
        Matcher m = p.matcher(content);

        String expresion = "";
        List expresions = new ArrayList();

        while(m.find())
        {
            expresion = m.group();
            expresions.add(expresion);
            content = View.evalExpresion(content, data, expresion);
            //Logger.getLogger().debug("Expresion", expresion);
        }


        return content;
    }

    private static String evalExpresion(String content, HashMap<String, Object> data, String expresion)
    {
        String value = expresion.replace("{{", "").replace( "}}","").trim();
        if(value.contains("$")){
            value = value.replace("$","");
            Logger.getLogger().debug("Reemplzar", expresion, value, String.valueOf(data.get(value)));
            if(data.get(value) != null) {
                content = View.parseVars(content, expresion, data.get(value)+"/");
            }
        } else if(View.isMathExpresion(value))
        {
            View.resolveMath(value);
            //Logger.getLogger().debug("Calculando", expresion, value, String.valueOf(EvaluateString.evaluate(value)));
            //content = View.parseVars(content, expresion, EvaluateString.evaluate(value));
        }

        return content;
    }

    private static String resolveMath(String str)
    {
        str = str.replace(" ","");

        while(View.isMathExpresion(str) || str.contains("(")) {


            String eq = str;
            String eqTemp = str;
            boolean subterm = false;

            if (View.isNumberNegative(eq)) {
                Logger.getLogger().debug("Es Negativo",eq);
                break;
            }

            Logger.getLogger().debug("ROMPE",str);

            if(eq.contains("(")) {
                subterm = true;
                Logger.getLogger().debug("ORIGINAL",str);
                Logger.getLogger().debug("SACAR",String.valueOf(eq.lastIndexOf("(")),String.valueOf(eq.indexOf(")")));
                int init = eq.lastIndexOf("(");
                String endStr = eq.substring(init, eq.length());
                int end = init + endStr.indexOf(")");
                eq = eq.substring(init + 1, end );
                eqTemp = str.substring(init, end +1);
                Logger.getLogger().debug("PORCION",eq);
            }

            Logger.getLogger().debug("REALIZAR",eq);

            while(eq.contains("*") || eq.contains("/") || eq.contains("%"))
            {
                if (View.isNumberNegative(eq)) {
                    Logger.getLogger().debug("Es Negativo",eq);
                    break;
                }

                Pattern p = Pattern.compile("(\\-?[0-9]+\\.?[0-9]*)[\\*\\/](.?)(\\-?[0-9]+\\.?[0-9]*)");
                Matcher m = p.matcher(eq);
                String exp = "";
                Logger.getLogger().debug("ANTES WHBILE",eq);
                while(m.find())
                {
                    exp = m.group();
                    break;
                }
                Logger.getLogger().debug("Resolver",exp);

                String value = View.resolverEquation(exp);

                eq = eq.replace(exp, value);
                eq = View.resolveSigns(eq);
                Logger.getLogger().debug("PUNTO",exp,",",eq);

            }

            while(eq.contains("+") || eq.contains("-"))
            {
                if (View.isNumberNegative(eq)) {
                    Logger.getLogger().debug("Es Negativo",eq);
                    break;
                }

                Pattern p = Pattern.compile("(\\-?[0-9]+\\.?[0-9]*)[\\+\\-](\\-?[0-9]+\\.?[0-9]*)");
                Matcher m = p.matcher(eq);
                String exp = "";
                while(m.find())
                {
                    exp = m.group();
                    break;
                }
                Logger.getLogger().debug("Resolver2",exp, "_"+str);

                String value = View.resolverEquation(exp);

                eq = eq.replace(exp, value);
                eq = View.resolveSigns(eq);

            }

            //eq = "+" + eq;

            Logger.getLogger().debug("RESULTADO",eq, eqTemp);
            /*Logger.getLogger().debug("EVALUO +", str.substring(str.indexOf(eqTemp) -1, str.indexOf(eqTemp) ));
            if (Validator.validateNumber(str.substring(str.indexOf(eqTemp) -1, str.indexOf(eqTemp) ) ) ) {
                Logger.getLogger().debug("PONGO +",eq, eqTemp);
               // str = str.replace(eqTemp, "+"+eq);
            } else {
                str = str.replace(eqTemp, eq);
            }*/
            eq = View.resolveSigns(eq);
            str = str.replace(eqTemp, eq);
            Logger.getLogger().debug("----------------",str);
        }

        Logger.getLogger().debug("ADIOS");

        return str;
    }

    private static String resolveSigns(String exp)
    {
        exp = exp.replace("−","-");
        exp = exp.replace("--","+");
        exp = exp.replace("++","+");
        exp = exp.replace("+-","−");
        exp = exp.replace("-+","-");
        return exp;
    }


    private static String resolverEquation(String exp)
    {


        int a, b;
        String[] numbers;
        String value = "";
        int x = 1;
        int y = 1;
        Logger.getLogger().debug("BEFORE",exp);
        if (exp.substring(0,1).equals("-")) {
            Logger.getLogger().debug("ES NEGATIVOOO");
            x = -1;
            exp = exp.substring(1, exp.length());
        }
        Logger.getLogger().debug("ANTES",exp);
        if(exp.contains("*")) {
            numbers = exp.split("\\*");
            a = Integer.parseInt(numbers[0]) * x;

            if(numbers[1].substring(0, 1).equals("-") || numbers[1].substring(0, 1).equals("−")) {

                y = -1;
                numbers[1] = numbers[1].substring(1, numbers[1].length());
            }
            //Logger.getLogger().debug("MULTIPLICO",String.valueOf(numbers[0]), String.valueOf(numbers[1]),String.valueOf((numbers[1].substring(0, 1))), String.valueOf(numbers[1].substring(1, numbers[1].length())));
            b = Integer.parseInt(numbers[1]) * y;
            value = String.valueOf(a * b);
            //Logger.getLogger().debug("MULTIPLICO",String.valueOf(a), String.valueOf(b),String.valueOf(value));
        } else if(exp.contains("/")) {
            numbers = exp.split("\\/");
            a = Integer.parseInt(numbers[0])* x;

            if(numbers[1].substring(0, 1).equals("-") || numbers[1].substring(0, 1).equals("−")) {
                y = -1;
                numbers[1] = numbers[1].substring(1, numbers[1].length());
            }

            b = Integer.parseInt(numbers[1]) * y;
            value = String.valueOf(a / b);
        } else if(exp.contains("+")) {
            numbers = exp.split("\\+");
            a = Integer.parseInt(numbers[0]) * x;

            if(numbers[1].substring(0, 1).equals("-") || numbers[1].substring(0, 1).equals("−")) {
                y = -1;
                numbers[1] = numbers[1].substring(1, numbers[1].length());
            }

            b = Integer.parseInt(numbers[1]) * y;
            //Logger.getLogger().debug("SUMO",String.valueOf(a), String.valueOf(b));
            value = String.valueOf(a + b);
        } else if(exp.contains("-")) {
            numbers = exp.split("\\-");
            a = Integer.parseInt(numbers[0])* x;

            if(numbers[1].substring(0, 1).equals("-") || numbers[1].substring(0, 1).equals("−")) {
                y = -1;
                numbers[1] = numbers[1].substring(1, numbers[1].length());
            }

            b = Integer.parseInt(numbers[1]) * y;
            value = String.valueOf(a - b);
        }  else if(exp.contains("%")) {
            numbers = exp.split("\\%");
            a = Integer.parseInt(numbers[0])* x;
            if(numbers[1].substring(0, 1).equals("-") || numbers[1].substring(0, 1).equals("−")) {
                y = -1;
                numbers[1] = numbers[1].substring(1, numbers[1].length());
            }

            b = Integer.parseInt(numbers[1]) * y;
            value = String.valueOf(a % b);
        }
        return value;
    }

    private static boolean isNumberNegative(String str)
    {
        try {
            double n = Double.parseDouble(str);
            if (n < 0) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    private static boolean isMathExpresion(String str)
    {
        if (str.contains("+") || str.contains("-") || str.contains("/") || str.contains("*") || str.contains("%")) {
            return true;
        }

        return false;
    }

    private static String loadFile(String file)
    {
        StringBuilder contentBuilder = new StringBuilder();
        file = Application.PATH_RESOURCES + View.PATH_TEMPLATES + file.replace(".sj.html","") + ".sj.html";
        System.out.println("file:"+file);
        String content = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {
            Logger.getLogger().error("Template not found: " + file);
        }
        content = contentBuilder.toString();
        return content;
    }
}
