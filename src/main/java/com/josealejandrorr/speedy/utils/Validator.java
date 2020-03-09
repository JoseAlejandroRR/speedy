package com.josealejandrorr.speedy.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Validator {

    private static HashMap<String, String> messages;
    private static HashMap<String, String> errors;

    public static final String NUMBER = "number";
    public static final String STRING = "string";
    public static final String EMAIL = "email";
    public static final String DATE = "date";
    public static final String BOOLEAN = "boolean";
    public static final String NAME = "name";

    public static final String DATE_FORMAT_DEFAULT = "dd/MM/yyyy";
    private static final String MESSAGE_FIELD_NUMBER_INVALID = "field $[field] is not a Number valid";
    private static final String MESSAGE_FIELD_STRING_INVALID = "field $[field] is not a String valid";
    private static final String MESSAGE_FIELD_EMAIL_INVALID = "field $[field] is not a Email valid";
    private static final String MESSAGE_FIELD_DATE_INVALID = "field $[field] is not a Date valid";
    private static final String MESSAGE_FIELD_MISSING = "field $[field] should be exits";


    public static HashMap<String, String> array(HashMap<String, RuleValidator> rules, HashMap<String, Object> array)
    {
        errors = new HashMap<String, String>();
        return Validator.array(rules, array, new HashMap<>());
    }

    public static HashMap<String, String> array(HashMap<String, RuleValidator> rules, HashMap<String, Object> array, HashMap<String, String> messages)
    {
        Validator.messages = messages;
        return Validator.validateParams(rules, array);
    }

    private static HashMap<String, String> validateParams(HashMap<String, RuleValidator> rules, HashMap<String, Object> array)
    {
        for(Map.Entry<String, RuleValidator> item : rules.entrySet())
        {
            RuleValidator rule = (RuleValidator) item.getValue();
            System.out.println(item.getKey()+"= "+rule.type);
            String value = (String) array.get(item.getKey());
            if (value != null) {
                if (value.trim().length() == 0) {
                    if (rule.required) {
                        Validator.checkDataTypeElement(item.getKey(), rule, value);
                    }
                } else {
                    Validator.checkDataTypeElement(item.getKey(), rule, value);
                }
            } else {
                if (!rule.nullable) {
                    Validator.errors.put(item.getKey(), Validator.MESSAGE_FIELD_MISSING);
                }
            }
        }

        return Validator.parseFieldErrors(Validator.errors);
    }

    private static HashMap<String, String> parseFieldErrors(HashMap<String, String> messages) {
        for(Map.Entry<String, String> node : messages.entrySet())
        {
            String text = node.getValue();
            text = text.replace("$[field]", node.getKey());
            node.setValue(text);
        }
        return messages;
    }

    private static void checkDataTypeElement(String key, RuleValidator rule, String value) {
        switch (rule.type)
        {
            case Validator.STRING:
                if (!Validator.validateString(value)) {
                    String msg = Validator.MESSAGE_FIELD_STRING_INVALID;
                    //f()
                    Validator.errors.put(key, msg);
                }
                break;
            case Validator.NUMBER:
                if (!Validator.validateNumber(value)) {
                    String msg =  Validator.MESSAGE_FIELD_NUMBER_INVALID;;
                    Validator.errors.put(key, msg);
                }
                break;
            case Validator.EMAIL:
                if (!Validator.validateEmail(value)) {
                    String msg =  Validator.MESSAGE_FIELD_EMAIL_INVALID;
                    Validator.errors.put(key, msg);
                }
                break;
            case Validator.DATE:
                if (!Validator.validateDate(value)) {
                    String msg =  Validator.MESSAGE_FIELD_DATE_INVALID;;
                    Validator.errors.put(key, msg);
                }
                break;
        }
    }

    public static boolean validateString(String str)
    {
        try {
            if(str.trim().length()>0){
                return true;
            }
        } catch (Exception e)
        {
        }
        return false;
    }

    public static boolean validateNumber(String str)
    {
        try {
            Double.parseDouble(str);
            return  true;
        } catch (Exception e)
        {
        }
        return false;
    }

    public static boolean validateEmail(String str)
    {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(str);
        return m.matches();
    }

    public static boolean validateDate(String str)
    {
        return Validator.validateDate(str, Validator.DATE_FORMAT_DEFAULT);
    }

    public static boolean validateDate(String str, String patter)
    {
        try {
            SimpleDateFormat format = new SimpleDateFormat(patter);
            format.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

