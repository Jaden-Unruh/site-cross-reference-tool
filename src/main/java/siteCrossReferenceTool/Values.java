package siteCrossReferenceTool;

import java.util.ResourceBundle;

public class Values {
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("siteCrossReferenceTool/values");
	
	public static int getValue(String key) {
		return Integer.parseInt(RESOURCE_BUNDLE.getString(key));
	}
}
