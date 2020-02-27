package com.josealejandrorr.speedy.contracts.http;

import com.josealejandrorr.speedy.utils.RuleValidator;

import java.util.HashMap;

public interface IRequestValidator {

    public HashMap<String, RuleValidator> getRulesPost();

    public HashMap<String, RuleValidator> getRulesGet();

}