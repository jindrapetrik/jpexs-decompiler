package com.docuverse.swt.ole;

import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleFunctionDescription;
import org.eclipse.swt.ole.win32.OleParameterDescription;
import org.eclipse.swt.ole.win32.OlePropertyDescription;
import org.eclipse.swt.ole.win32.Variant;

/**
 * @author donpark
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class OleObject {

	protected OleAutomation oleAutomation;
	protected boolean owned;

	public OleObject(OleAutomation oleAutomation) {
		this(oleAutomation, true);
	}

	public OleObject(OleAutomation oleAutomation, boolean owned) {
		this.oleAutomation = oleAutomation;
		this.owned = owned;
	}

	public void dispose() {
		if (oleAutomation != null) {
			if (owned)
				oleAutomation.dispose();
	
			oleAutomation = null;
		}
	}

	public int getDispID(String dispName) {
		int[] dispIDs = oleAutomation.getIDsOfNames(new String[] { dispName });
		return dispIDs[0];
	}

	// Dispatch ID-based methods
	
	public Variant getVariantProperty(int dispID) {
		return oleAutomation.getProperty(dispID);
	}

	public boolean setVariantProperty(int dispID, Variant propValue) {
		return oleAutomation.setProperty(dispID, propValue);
	}

	public int getIntegerProperty(int dispID) {
		Variant result = oleAutomation.getProperty(dispID);
		return (result != null) ? result.getInt() : -1;
	}

	public boolean setIntegerProperty(int dispID, int value) {
		return oleAutomation.setProperty(dispID, new Variant(value));
	}

	public boolean getBooleanProperty(int dispID) {
		Variant result = oleAutomation.getProperty(dispID);
		return (result != null) ? result.getBoolean() : false;
	}

	public boolean setBooleanProperty(int dispID, boolean value) {
		return oleAutomation.setProperty(dispID, new Variant(value));
	}

	public String getStringProperty(int dispID) {
		Variant result = oleAutomation.getProperty(dispID);
		return (result != null) ? result.getString() : "";
	}

	public boolean setStringProperty(int dispID, String value) {
		return oleAutomation.setProperty(dispID, new Variant(value));
	}

	public float getFloatProperty(int dispID) {
		Variant result = oleAutomation.getProperty(dispID);
		return (result != null) ? result.getFloat() : 0.0f;
	}

	public boolean setFloatProperty(int dispID, float value) {
		return oleAutomation.setProperty(dispID, new Variant(value));
	}

	public OleAutomation getAutomationProperty(int dispID) {
		Variant result = oleAutomation.getProperty(dispID);
		return (result != null) ? result.getAutomation() : null;
	}
	
	public OleObject getObjectProperty(int dispID) {
		Variant result = oleAutomation.getProperty(dispID);
		return (result != null) ? new OleObject(result.getAutomation(), false) : null;
	}
	
	public Variant invoke(int dispID) {
		return oleAutomation.invoke(dispID);
	}

	public void invokeNoReply(int dispID) {
		oleAutomation.invokeNoReply(dispID);
	}

	public Variant invoke(int dispID, Variant[] args) {
		return oleAutomation.invoke(dispID, args);
	}

	public void invokeNoReply(int dispID, Variant[] args) {
		oleAutomation.invokeNoReply(dispID, args);
	}

	// Name-based methods

	public Variant getVariantProperty(String name) {
		return oleAutomation.getProperty(getDispID(name));
	}

	public boolean setVariantProperty(String name, Variant propValue) {
		return oleAutomation.setProperty(getDispID(name), propValue);
	}

	public int getIntegerProperty(String name) {
		Variant result = getVariantProperty(name);
		return (result != null) ? result.getInt() : -1;
	}

	public boolean setIntegerProperty(String name, int value) {
		return setVariantProperty(name, new Variant(value));
	}

	public boolean getBooleanProperty(String name) {
		Variant result = getVariantProperty(name);
		return (result != null) ? result.getBoolean() : false;
	}

	public boolean setBooleanProperty(String name, boolean value) {
		return setVariantProperty(name, new Variant(value));
	}

	public String getStringProperty(String name) {
		Variant result = getVariantProperty(name);
		return (result != null) ? result.getString() : "";
	}

	public boolean setStringProperty(String name, String value) {
		return setVariantProperty(name, new Variant(value));
	}

	public float getFloatProperty(String name) {
		Variant result = oleAutomation.getProperty(getDispID(name));
		return (result != null) ? result.getFloat() : 0.0f;
	}

	public boolean setFloatProperty(String name, float value) {
		return oleAutomation.setProperty(getDispID(name), new Variant(value));
	}

	public OleAutomation getAutomationProperty(String name) {
		Variant result = oleAutomation.getProperty(getDispID(name));
		return (result != null) ? result.getAutomation() : null;
	}

	public OleObject getObjectProperty(String name) {
		Variant result = oleAutomation.getProperty(getDispID(name));
		return (result != null) ? new OleObject(result.getAutomation(), false) : null;
	}

	public Variant invoke(String name) {
		return oleAutomation.invoke(getDispID(name));
	}

	public void invokeNoReply(String name) {
		oleAutomation.invokeNoReply(getDispID(name));
	}

	public Variant invoke(String name, Variant[] args) {
		return oleAutomation.invoke(getDispID(name), args);
	}

	public void invokeNoReply(String name, Variant[] args) {
		oleAutomation.invokeNoReply(getDispID(name), args);
	}
	
	// Common methods
	
	public int getX() {
		return getIntegerProperty("X");
	}

	public boolean setX(int value) {
		return setIntegerProperty("X", value);
	}	
	
	public int getY() {
		return getIntegerProperty("Y");
	}

	public boolean setY(int value) {
		return setIntegerProperty("Y", value);
	}	
	
	public int getWidth() {
		return getIntegerProperty("Width");
	}

	public boolean setWidth(int value) {
		return setIntegerProperty("Width", value);
	}	
	
	public int getHeight() {
		return getIntegerProperty("Height");
	}

	public boolean setHeight(int value) {
		return setIntegerProperty("Height", value);
	}
	
	public void dump() {
		System.out.println("Properties:");
		OlePropertyDescription propDesc;
		for (int i = 0;
			(propDesc = oleAutomation.getPropertyDescription(i)) != null;
			i++) {
			System.out.println(propDesc.name);
		}
		System.out.println();

		System.out.println("Methods:");

		OleFunctionDescription funcDesc;
		for (int i = 0;
			(funcDesc = oleAutomation.getFunctionDescription(i)) != null;
			i++) {
			System.out.print(funcDesc.name);
			System.out.print("(");
			for (int j = 0; j < funcDesc.args.length; j++) {
				OleParameterDescription parmDesc = funcDesc.args[j];
				System.out.print(parmDesc.name);
				System.out.print(", ");
			}
			System.out.println(")");
		}
		System.out.println();
	}
}
