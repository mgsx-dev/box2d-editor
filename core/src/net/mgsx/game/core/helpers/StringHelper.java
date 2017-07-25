package net.mgsx.game.core.helpers;

public class StringHelper {

	/**
	 * Convert a camel case string to underscored capitalized string.
	 * eg. thisIsStandardCamelCaseString is converted to THIS_IS_STANDARD_CAMEL_CASE_STRING
	 * @param camelCase a camel case string : only letters without consecutive uppercase letters.
	 * @return the transformed string or the same if not camel case.
	 */
	public static String camelCaseToUnderScoreUpperCase(String camelCase)
	{
		String result = "";
		boolean prevUpperCase = false;
		for(int i=0 ; i<camelCase.length() ; i++){
			char c = camelCase.charAt(i);
			if(!Character.isLetter(c)) return camelCase;
			if(Character.isUpperCase(c)){
				if(prevUpperCase) return camelCase;
				result += "_" + c;
				prevUpperCase = true;
			}else{
				result += Character.toUpperCase(c);
				prevUpperCase = false;
			}
		}
		return result;
	}
}
