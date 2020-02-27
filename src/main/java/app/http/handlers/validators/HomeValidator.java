package app.http.handlers.validators;

import com.josealejandrorr.speedy.contracts.http.IRequestValidator;
import com.josealejandrorr.speedy.utils.RuleValidator;

import java.util.HashMap;

public class HomeValidator implements IRequestValidator {


    @Override
    public HashMap<String, RuleValidator> getRulesPost() {
        HashMap<String, RuleValidator> rules = new HashMap<String, RuleValidator>();

        rules.put("email", new RuleValidator("email"));
        //rules.put("user_id", new RuleValidator("number"));
        return rules;
    }

    @Override
    public HashMap<String, RuleValidator> getRulesGet() {

        HashMap<String, RuleValidator> rules = new HashMap<String, RuleValidator>();

        //rules.put("email", new RuleValidator("email"));
        rules.put("user_id", new RuleValidator("number"));

        return rules;
    }
}