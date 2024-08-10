/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package macromedia.asc.util;

import java.math.*;

public class Decimal128Context {

	private MathContext bdctx;
	public int status;
	
	public Decimal128Context() { 
		status = 0;
		try {
			bdctx = new MathContext(34, RoundingMode.HALF_EVEN);
		} catch (NoClassDefFoundError e) {
			bdctx = null;	// we're not going to be using decimal stuff on 1.4
		}
	}
	
	public Decimal128Context(int precision, RoundingMode rounding) {

		status = 0;
		try {
			bdctx = new MathContext(precision, rounding);
		} catch (NoClassDefFoundError e) {
			bdctx = null;	// we're not going to be using decimal stuff on 1.4
		}
	}
	
	
	public int getPrecision() {
		return bdctx.getPrecision();
	}
	
	public RoundingMode getRoundingMode() {
		return bdctx.getRoundingMode();
	}
	
	protected MathContext mathCtx() { // should be visible only to Decimal128
		return bdctx;
	}
	/* Should these be immutable like MathContext?  It's hard to have the status field
	 * reflect error history if that is the case
	 */
	
	public void setPrecision (int precision) {
		if (precision < 1 || precision > 34) {
			throw new IllegalArgumentException("Precision must be between 1 and 34");
		}
		if (bdctx.getPrecision() != precision) {
			bdctx = new MathContext(precision, bdctx.getRoundingMode());
		}
	}
	
	public void setRoundingMode(RoundingMode mode) {
		if (bdctx.getRoundingMode() != mode) {
			bdctx = new MathContext(bdctx.getPrecision(), mode);
		}
	}
	
	private static RoundingMode defaultMode() {
		RoundingMode result;
		try {
			result = RoundingMode.HALF_EVEN;
		} catch (NoClassDefFoundError e) {
			result = null;	// we're not going to be using decimal stuff on 1.4
		}
		return result;
	}
	
	public static final Decimal128Context DECIMAL128 = new Decimal128Context(34, defaultMode());
	
	  /* Trap-enabler and Status flags (exceptional conditions), and their names */
	  // Top byte is reserved for internal use
	static final int DEC_Conversion_syntax = 0x00000001;
	static final int DEC_Division_by_zero = 0x00000002;
	static final int DEC_Division_impossible = 0x00000004;
	static final int DEC_Division_undefined = 0x00000008;
	static final int DEC_Insufficient_storage = 0x00000010;
	static final int DEC_Inexact = 0x00000020;
	static final int DEC_Invalid_context = 0x00000040;
	static final int DEC_Invalid_operation = 0x00000080;
	static final int DEC_Overflow = 0x00000200;
	static final int DEC_Clamped = 0x00000400;
	static final int DEC_Rounded = 0x00000800;
	static final int DEC_Subnormal = 0x00001000;
	static final int DEC_Underflow = 0x00002000;
	static final int DEC_sNaN = 0x40000000;

	  // name strings for the exceptional conditions

	static final String DEC_Condition_CS = "Conversion syntax";
	static final String DEC_Condition_DZ = "Division by zero";
	static final String DEC_Condition_DI = "Division impossible";
	static final String DEC_Condition_DU = "Division undefined";
	static final String DEC_Condition_IE = "Inexact";
	static final String DEC_Condition_IS = "Insufficient storage";
	static final String DEC_Condition_IC = "Invalid context";
	static final String DEC_Condition_IO = "Invalid operation";
	static final String DEC_Condition_OV = "Overflow";
	static final String DEC_Condition_PA = "Clamped";
	static final String DEC_Condition_RO = "Rounded";
	static final String DEC_Condition_SU = "Subnormal";
	static final String DEC_Condition_UN = "Underflow";
	static final String DEC_Condition_ZE = "No status";
	static final String DEC_Condition_MU = "Multiple status";
	static final int DEC_Condition_Length = 21;		// length of the longest string,
														// including terminator
}
