package br.com.tx.storageInterface.Utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.api.client.util.ArrayMap;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class ClassUtils<E> {

	public Map<String, String> validClass(E classe) {

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<E>> violations = validator.validate(classe);

		var resultMap = new ArrayMap<String, String>();
		if (!violations.isEmpty()) {
			var logList = new ArrayList<String>();
			for (ConstraintViolation<E> violation : violations) {
				logList.add(violation.getPropertyPath() + ": " + violation.getMessage());
			}
			resultMap.add("STATUS", "NOK");
			resultMap.add("MESSAGE", String.join("\n", logList));
			return resultMap;
		} else {
			resultMap.add("STATUS", "OK");
			resultMap.add("MESSAGE", "");
			return resultMap;
		}
	}

}
