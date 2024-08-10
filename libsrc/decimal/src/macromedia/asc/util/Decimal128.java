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
import java.lang.ArithmeticException;

public class Decimal128 {

	private static final byte DEC_NAN = 0x01;
	private static final byte DEC_SNAN = 0x02;
	private static final byte DEC_INF = 0x04;
	private static final byte DEC_NEG = 0x08;
	
	private static final byte DEC_SPECIAL = (DEC_NAN | DEC_SNAN | DEC_INF);
	
	private byte flags;
	private BigDecimal value;
	
	public static final int MAX_PRECISION = 34;
	
	public static final BigDecimal BigDecimalZERO = new BigDecimal("0");
	
	public static final Decimal128 ZERO = new Decimal128(BigDecimalZERO, (byte)0);
	public static final Decimal128 NEGZERO = new Decimal128(BigDecimalZERO, DEC_NEG);
	public static final Decimal128 ONE = new Decimal128(new BigDecimal("1"), (byte)0);
	public static final Decimal128 NEG1 = new Decimal128(new BigDecimal("-1"), DEC_NEG);
	public static final Decimal128 NaN = new Decimal128(BigDecimalZERO, DEC_NAN);
	public static final Decimal128 INFINITY = new Decimal128(BigDecimalZERO, DEC_INF);
	public static final Decimal128 NEGINFINITY = new Decimal128(BigDecimalZERO, (byte)(DEC_INF | DEC_NEG));
	
	/* private constructors used internally */
	private Decimal128() {
		flags = 0;
		value = BigDecimalZERO; // or maybe leave null
	}
	
	private Decimal128(BigDecimal val, byte flagval) {
		value = val;
		flags = flagval;
	}
	
	/* public constructors */
	public Decimal128(String str) {
		// parse string that's either NaN, SNaN, (+/-)INF[INITY], or valid BigDecimal
		flags = 0;
		parseDecimalNumber(str.toCharArray(), Decimal128Context.DECIMAL128);
	}
	
	public Decimal128(String str, Decimal128Context ctx) {
		// parse string that's either NaN, SNaN, (+/-)INF[INITY], or valid BigDecimal
		flags = 0;
		parseDecimalNumber(str.toCharArray(), ctx);
	}
	
	public Decimal128(char in[]) {
		// same as string
		flags = 0;
		parseDecimalNumber(in, Decimal128Context.DECIMAL128);
	}
	
	public Decimal128(char in[], Decimal128Context ctx) {
		// same as string
		flags = 0;
		parseDecimalNumber(in, ctx);
	}
	

	private void parseDecimalNumber(char in[], Decimal128Context ctx) {
		boolean seenS = false;
		value = BigDecimalZERO; // until proven otherwise
		preparse: do {			
			for (int i = 0; i < in.length; i++) {
				char c = in[i];
				if (Character.isDigit(c) || c == '.') {
					if (seenS)
						break; // invalid input
					else
						break preparse; // parse as BigDecimal
				}
				if (c == '+') {
					if (seenS)
						break; // invalid input
					continue; // NaN and infinity can have signs
				}
				if (c == '-') {
					if (seenS)
						break; // invalid input
					flags = DEC_NEG;
					continue;
				}
				if ((c == 'S') || (c == 's')) {
					if (seenS)
						break; // invalid input
					seenS = true;
					continue;
				}
				if (c == 'N') {
					if (((i+2) < in.length) && (in[i+1] == 'a') && (in[i+2] == 'N')) {
						flags |= (seenS)? DEC_SNAN : DEC_NAN;
						if ((i+3) < in.length) {
							// additional characters after NAN.
							for (int j = i+3; j < in.length; j++) {
								if (!Character.isDigit(in[j])) {
									throw new IllegalArgumentException("invalid NaN payload");
								}
							}
							value = new BigDecimal(in, i+3, (in.length - (i+3)), ctx.mathCtx());
							// we know scale = 0 since we checked for only digits
							if (value.precision() > 33)
								value = BigDecimalZERO;
						}
						return;
					}
					else break;
				}
				if (c == 'I') {
					if (seenS)
						break;
					if (((i+8) == in.length) && (in[i+1] == 'n') && (in[i+2] == 'f') 
								&& (in[i+3] == 'i') && (in[i+4] == 'n')
								&& (in[i+5] == 'i') && (in[i+6] == 't') && (in[i+7] == 'y')) {
						flags |= DEC_INF;
						return; // nothing more to do here
					}
					// temporarily allow short infinity literals to run Mike's testcases
					if (((i+3) == in.length) && (in[i+1] == 'n') && (in[i+2] == 'f')) {
						flags |= DEC_INF;
						return; // nothing more to do here
					}
					break; // invalid input
				}
			}
			// only get here if input is invalid
			throw  new IllegalArgumentException("invalid decimal literal");
		} while (false); // dummy loop labeled preparse
		
		// we got here because we saw something that looks like the beginning of a number
		value = new BigDecimal(in, ctx.mathCtx());
		this.ClampOverflow(ctx);
	}
	
	public Decimal128(int val) {
		flags = (val >= 0)?0 : DEC_NEG;
		value = new BigDecimal(val, MathContext.DECIMAL128);
	}

	public Decimal128(int val, Decimal128Context ctx) {
		flags = (val >= 0)?0 : DEC_NEG;
		value = new BigDecimal(val, ctx.mathCtx());
	}
	
	public Decimal128(double val) {
		flags = (val >= 0)?0 : DEC_NEG;
		value = BigDecimalZERO; // until proven otherwise
		if (Double.isNaN(val)) {
			flags |= DEC_NAN;
		}
		else if (Double.isInfinite(val)) {
			flags |= DEC_INF; 	// sign already set above.
		}
		else
			value = new BigDecimal(val, MathContext.DECIMAL128);
	}

	public Decimal128(double val, Decimal128Context ctx) {
		flags = (val >= 0)?0 : DEC_NEG;
		value = BigDecimalZERO; // until proven otherwise
		if (Double.isNaN(val)) {
			flags |= DEC_NAN;
		}
		else if (Double.isInfinite(val)) {
			flags |= DEC_INF; 	// sign already set above.
		}
		else
			value = new BigDecimal(val, ctx.mathCtx());
	}
	
	public Decimal128(long val) {
		flags = (val >= 0)?0 : DEC_NEG;
		value = new BigDecimal(val, MathContext.DECIMAL128);
	}

	public Decimal128(long val, Decimal128Context ctx) {
		flags = (val >= 0)?0 : DEC_NEG;
		value = new BigDecimal(val, ctx.mathCtx());
	}
	
	/* operations to/from IEEE representation */
	
	private static final int mask[] = {0x000, 0x001, 0x003, 0x007, 0x00F, 
		0x01F, 0x03F, 0x07F, 0x0FF, 0x1FF, 0x3FF}; // can't declare static inside IEEERep
	
	private class IEEERep {
		// class to manipulate the representation of a IEEE 128 bit floating point
		//  currently generates format specified by http://www2.hursley.ibm.com/decimal/decbits.html
		
		int data[] = {0, 0, 0, 0}; // data[0] is msb, containing sign, combo field, etc.
		// data is initialized to 0, methods below OR in values taking advantage of this fact.
		
		boolean negative=false, qNaN=false, sNaN=false, infinite=false, finalized=false;
		int exponent;
		short topDigit;
		
		int currentDatum, currentShift, totalDigits;
		
		/* *****************************************************************************
		 * Routines for taking apart IEEE representation 
		 *******************************************************************************/
		IEEERep() { // use this constructor to build a representation
			topDigit = 0;
			// initialize state for writing unit
			currentDatum = 3;
			currentShift = 0;
			totalDigits = 0;
		}
		
		void setNeg() {
			negative = true;
		}
		void setNaN(boolean signalling) {
			if (signalling)
				sNaN = true;
			else qNaN = true;
		}
		void setInfinite() {
			infinite = true;
		}
		
		void setExponent(int exp) { // exp is biased exponent
			exponent = exp;			
		}
		
		void addUnit (short unit) {
			// add 3 more digits to the number.  Digits are added in groups of 3, least significant
			// first, the value passed to this routine is a number between 0 and 999.  If 33 digits
			// have already been added, it is an error if unit > 9.
			
			if (totalDigits == 34)
				throw new IllegalArgumentException("trying to add more than 34 digits to IEEE128");
			if (totalDigits == 33) {
				if (unit > 9)
					throw new IllegalArgumentException("trying to add more than 34 digits to IEEE128");
				topDigit = unit;
				totalDigits++; 
				return;
			}
			totalDigits += 3;
			int dpd = DenselyPackedDecimal.Bin2DPD(unit);
			if (currentShift <= 22) {
				// dpd fits into currentDatum completely
				data[currentDatum] |= dpd << currentShift;
				currentShift += 10;
			}
			else {
				// bits overlap next data word
				int onThis = 32 - currentShift; // will be between 0 and 9
				if (onThis != 0)
					data[currentDatum] |= dpd << currentShift;
				data[currentDatum-1] |= (dpd >>> onThis);
				currentDatum--;
				currentShift = 10 - onThis;
			}
		}
		
		byte[] getRep() {
			if (!finalized) {
				int combo = 0;
				if (negative)
					data[0] |= 0x80000000;
				if (infinite)
					combo = 0x1E;
				else if (sNaN || qNaN) {
					combo = 0x1F;
					if (sNaN)
						exponent = 0x800; // high order bit on in continuation field
					else
						exponent = 0;
				}
				else {
					// finite number
					if (topDigit < 8) 
						combo = topDigit | ((exponent >>> 9) & 0x18);
					else 
						combo = 0x18 | (((exponent >>> 12) & 0x3) << 1) | (topDigit - 8);
				}
				data[0] |= (combo <<26) | ((exponent & 0xFFF) << 14);
				finalized = true;
			}
			byte[] result = new byte[16];
			int ndx = 0;
			for (int i = 0; i < 16; i+= 4) {
				int datum = data[ndx++];
				result[i]   = (byte)(datum >>> 24);
				result[i+1] = (byte)((datum >>> 16) & 0xFF);
				result[i+2] = (byte)((datum >>> 8) & 0xFF);
				result[i+3] = (byte)(datum & 0xFF);
			}
			return result;
		}
		
		/* *****************************************************************************
		 * Routines for taking apart IEEE representation 
		 *******************************************************************************/
		
		int currentBit;
		
		IEEERep(byte[] rep) { // use this constructor to take apart a representation
			int ndx = 0;
			for (int i = 0; i < 16; i+= 4) {
				data[ndx++] = ((rep[i] & 0xFF)<<24) + ((rep[i+1] & 0xFF)<<16) + 
				((rep[i+2] & 0xFF) <<8) + (rep[i+3] & 0xFF);
			}
			if ((data[0] & 0x80000000) != 0)
				negative = true;
			int combo = (data[0] >> 26) & 0x1F;
			if (combo == 0x1E)
				infinite = true;
			else if (combo == 0x1F) {
				if ((data[0] & 0x02000000) != 0)
					sNaN = true;
				else qNaN = true;
			}
			else {
				// finite number
				int expmsb;
				if ((combo & 0x18) == 0x18) {
					expmsb = (combo >> 1) & 0x3;
					topDigit = (short)(8 + (combo & 0x01));
				}
				else {
					expmsb = combo >>> 3;
					topDigit = (short)(combo & 0x7);
				}
				exponent = ((data[0] >>> 14) & 0xFFF) | (expmsb << 12);
			}
			// initialize state for reading digits
			currentDatum = -1;
		}
		short getUnit() {
			// returns bases 1000 units from most significant to least
			short result = 0;
			if (currentDatum == -1) {
				currentDatum = 0;
				currentBit = 14;
				return topDigit;
			}
			else if (currentDatum > 3) {
				throw new IllegalArgumentException("trying to read more than 34 digits from IEEE128");
			}
			else {
				if (currentBit >= 10) {
					// all bits are in the current Datum
					result = (short)((data[currentDatum] >>> (currentBit - 10)) & 0x3FF);
					currentBit -= 10;
				}
				else {
					// bits overlap into next Datum
					int overlap = 10 - currentBit;
					if (currentBit != 0) {
						result = (short)((data[currentDatum] & mask[currentBit]) << overlap);
					}
					result |= (short)((data[++currentDatum] >>> (32-overlap)));
					currentBit = 32- overlap;
				}
			}
			return DenselyPackedDecimal.DPD2Bin(result);
		}
		
		boolean isInfinite() {
			return infinite;
		}
		
		boolean isNaN() {
			return (qNaN | sNaN);
		}
		
		boolean isSNaN() {
			return (sNaN);
		}
		
		boolean isNegative() {
			return negative;
		}
		
		int getExponent() {
			return exponent;
		}
		
	} // IEEERep class
	
	private static final int DECIMAL128_Pmax = 34;
	private static final int DECIMAL128_Emax = 6144;
	private static final int DECIMAL128_Emin = -6143;
	private static final int DECIMAL128_Bias = 6176;
	  // highest biased exponent (Elimit-1)
	private static final int DECIMAL128_Ehigh = (DECIMAL128_Emax+DECIMAL128_Bias-DECIMAL128_Pmax+1);
	
	public byte[] toIEEE(Decimal128Context ctx) {
		// returns 16 bytes of IEEE decimal128, big endian
		int exp; // holds biased exponent
		short dval;
		IEEERep ieee = new IEEERep();
		if ((flags & DEC_NEG) != 0) 
			ieee.setNeg();
		
		do { // dummy loop to allow break
			if ((flags & DEC_SPECIAL) == 0) {
				char[] coefficient = value.abs().unscaledValue().toString().toCharArray();
				int exponent = -value.scale(); // it appears backward from decNumber
				int digits = coefficient.length;
				int ae = exponent + digits - 1;
				if ((digits > DECIMAL128_Pmax)
					|| (ae > DECIMAL128_Emax)
					|| (ae < DECIMAL128_Emin)) {
					// possibly out of range.  Do things to get it in range
					Decimal128 n2 = this.add(Decimal128.ZERO, Decimal128Context.DECIMAL128);
					if ((n2.flags & DEC_INF) != 0) {
						ieee.setInfinite();
						break;
					}
					coefficient = n2.value.abs().unscaledValue().toString().toCharArray();
					exponent = -n2.value.scale();
					digits = coefficient.length;
				}
				if (value.compareTo(BigDecimalZERO) == 0) {
					if (exponent < -DECIMAL128_Bias) {
						exp = 0;
						ctx.status |= Decimal128Context.DEC_Clamped;
					}
					else {
						exp = exponent + DECIMAL128_Bias;	// bias exponent
						if (exp > DECIMAL128_Ehigh) {
							exp = DECIMAL128_Ehigh;
							ctx.status |= Decimal128Context.DEC_Clamped;
						}
					}
					ieee.setExponent(exp);
				}
				else { // nonzero
					exp = exponent + DECIMAL128_Bias;
					if (exp > DECIMAL128_Ehigh) {
						// We want to limit exponent to Ehigh, even if we add trailing
						// 0's to the coefficient
						int shift = exp - DECIMAL128_Ehigh;
						int k;
						exp = DECIMAL128_Ehigh;
						ctx.status |= Decimal128Context.DEC_Clamped;
						char newCoef[] = new char[digits + shift];
						for (k = 0; k < digits; k++) {
							newCoef[k] = coefficient[k];
						}
						for (k = digits; k < (digits + shift); k++) {
							newCoef[k] = '0';
						}
						coefficient = newCoef;
						digits += shift;
					}
					ieee.setExponent(exp);
					for (int i = digits-1; i >= 0; i -= 3) {
						switch (i) {
						case 0:
							dval = (short)(coefficient[0] - '0');
							break;
						case 1:
							dval = (short)(10 * (coefficient[0] - '0') + (coefficient[1] - '0'));
							break;
						default:
							dval = (short)(100 * (coefficient[i-2] - '0') 
									+ 10 * (coefficient[i-1] - '0') + (coefficient[i] - '0'));
						}
						ieee.addUnit(dval);
					}
				}
			}
			else {
				if ((flags & DEC_INF) != 0) {
					ieee.setInfinite();
				}
				else {
					ieee.setNaN((flags & DEC_SNAN) != 0);
					if (value != BigDecimalZERO) {
						char[] payload = value.abs().unscaledValue().toString().toCharArray();
						int digits = payload.length;
						ieee.setExponent(0);
						for (int i = digits-1; i >= 0; i -= 3) {
							switch (i) {
							case 0:
								dval = (short)(payload[0] - '0');
								break;
							case 1:
								dval = (short)(10 * (payload[0] - '0') + (payload[1] - '0'));
								break;
							default:
								dval = (short)(100 * (payload[i-2] - '0') 
										+ 10 * (payload[i-1] - '0') + (payload[i] - '0'));
							}
							ieee.addUnit(dval);
						}
					}
				}
			}
		} while (false);
		
		return ieee.getRep();
	}
	
	public byte[] toIEEE() {
		return toIEEE(Decimal128Context.DECIMAL128);
	}
	/*
        JPEXS
	public ByteList toByteList(ByteList result) {
		byte rep[] = toIEEE();
		if (result == null)
			result = new ByteList(16);
		result.set(rep, 16);
		return result;
	}*/
	
    /* constructor to go from IEEE representation */	
	
	/*
        JPEXS
        public Decimal128(ByteList rep) {
		byte reparray[] = rep.toByteArray();
		initFromIEEE(reparray);
	}*/
	
	public Decimal128(byte[] rep) {
		initFromIEEE(rep);
	}
	
	private void initFromIEEE(byte[] rep) {
		// must be 16 bytes holding an IEEE decimal128, big endian
		if (rep.length != 16)
			throw new IllegalArgumentException("Decimal128 needs 16 bytes for representation");
		IEEERep ieee = new IEEERep(rep);
		if (ieee.isNegative())
			flags = DEC_NEG;
		else
			flags = 0;
		if (ieee.isNaN()) {
			value = BigDecimalZERO; // some day support fancier NaNs
			if (ieee.isSNaN())
				flags |= DEC_SNAN;
			else
				flags |= DEC_NAN;
			// and fall through to parse payload
		}
		else if (ieee.isInfinite()) {
			value = BigDecimalZERO; // so it's not uninitialized
			flags |= DEC_INF;
			return;
		}
		byte thousand[] = {0x03, (byte)0xE8};
		// Why isn't there a public constructor for BigInteger that takes an int or long or something?
		BigInteger THOUSAND = new BigInteger(thousand);
		BigInteger coefficient = BigInteger.ZERO;
		short unit;
		boolean leadingZeroes = true;
		for (int i = 0; i < 12; i++) {
			unit = ieee.getUnit();
			if (!leadingZeroes || (unit != 0 )) {
				if (leadingZeroes) {
					leadingZeroes = false;
				}
				else {
					coefficient = THOUSAND.multiply(coefficient);
				}
				byte unitBytes[] = {(byte)(unit >>> 8), (byte)(unit & 0xFF)};
				BigInteger bigUnit = new BigInteger(unitBytes);
				coefficient = coefficient.add(bigUnit);
			}
		}
		int scale = (ieee.isNaN())? 0 : (-(ieee.getExponent() - DECIMAL128_Bias));
		value = new BigDecimal(coefficient, scale);
		if ((flags & (DEC_NEG | DEC_NAN | DEC_SNAN)) == DEC_NEG) {
			value = value.negate(MathContext.DECIMAL128); // don't negate payload
		}
	}
	
    /* to other numeric types */
	public int intValue() {
		if ((this.flags & DEC_SPECIAL) != 0) {
			throw new ArithmeticException("no valid integer value");
		}
		return this.value.intValue();
	}
	
	public long longValue() {
		if ((this.flags & DEC_SPECIAL) != 0) {
			throw new ArithmeticException("no valid long value");
		}
		return this.value.longValue();
	}
	
	public double doubleValue() {
		if ((flags & (DEC_NAN | DEC_SNAN)) != 0) 
			return Double.NaN;
		if ((flags & DEC_INF) != 0) {
			if ((flags & DEC_NEG) != 0)
				return Double.NEGATIVE_INFINITY;
			else
				return Double.POSITIVE_INFINITY;
		}
		return this.value.doubleValue();
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (((flags & DEC_SPECIAL) != 0) && ((flags & DEC_NEG) != 0))
			result.append('-');
		if ((flags & (DEC_NAN | DEC_SNAN)) != 0) {
			if ((flags & DEC_SNAN) != 0) {
				result.append("S");
			}
			result.append("NaN");
			if (value.compareTo(BigDecimalZERO) != 0) {
				result.append(value.toString());
			}
		}
		else if ((flags & DEC_INF) != 0) {
			result.append("Infinity");
		}
		else {
			// a number, but BigDecimal doesn't have -0
			if (((flags & DEC_NEG) != 0) && (value.compareTo(BigDecimalZERO) == 0)) {
				result.append('-');
			}
			result.append(value.toString());
		}
		return result.toString();
	}
	
	public String toEngineeringString() {
		if ((flags & (DEC_NAN | DEC_SNAN)) != 0) {
			return "NaN"; // what about number after NaN
		}
		else if ((flags & DEC_INF) != 0) {
			return "Infinity";
		}
		else {
			return value.toEngineeringString();
		}
	}
	
	public boolean isNaN() {
		return ((flags & (DEC_NAN | DEC_SNAN)) != 0);
	}
	
	public boolean isQNaN() {
		return ((flags & DEC_NAN) != 0);
	}
	
	public boolean isSNaN() {
		return ((flags & DEC_SNAN) != 0);
	}

	public boolean isFinite() {
		return ((flags & DEC_INF) == 0);
	}

	public boolean isNegative() {
		return ((flags & DEC_NEG) != 0);
	}
	
	/* helper functions for arithmetic operations */
	private Decimal128 opNaNs(Decimal128 operand, Decimal128Context ctx) {
		// either this or operand is NaN or SNan
		Decimal128 lhs = this;
		if ((this.flags & DEC_SNAN) != 0) {
			ctx.status |= Decimal128Context.DEC_Invalid_operation | Decimal128Context.DEC_sNaN;
		} 
		else if (operand != null && ((operand.flags & DEC_SNAN) != 0)) {
			lhs = operand;
			ctx.status |= Decimal128Context.DEC_Invalid_operation | Decimal128Context.DEC_sNaN;
		}
		else if ((this.flags & DEC_NAN) == 0) {
			lhs = operand;
		}
		Decimal128 result = new Decimal128(lhs.value, lhs.flags);
		// we've signaled by setting status, now clear SNAN to NAN if necessary
		result.flags &= ~DEC_SNAN;
		result.flags |= DEC_NAN;
		return result;
	}
	
	private void ClampOverflow(Decimal128Context ctx) {
		// check that scale is within valid range, modify as required to get into
		// range or set to proper infinity.
		if ((flags & DEC_SPECIAL) != 0) {
			return; // special ones are not out of range
		}
		// some definitions to keep code below readable
		// scale of BigDecimals are negative of exponents in Decimal128s
		BigDecimal resultVal;
		final int MINSCALE = -(DECIMAL128_Emax - DECIMAL128_Pmax + 1);
		final int MAXSCALE = DECIMAL128_Bias;
		int scale = value.scale();
		if (scale > MAXSCALE) {
			// see if we can change scale withing ctx.precision
			int numPrecision = value.precision();
			// see if we can decrease the precision to lower the scale
			// but can't make precision less than 1
			int delta = scale - MAXSCALE;
			if ((numPrecision - delta) >= 1) {
				resultVal = value.setScale(MAXSCALE, ctx.getRoundingMode());
			}
			else {
				// can't fit.  Return 0 or smallest num, which ever is closer
				/* 754r says return smallest value only if we would round to it
				 * if we had infinite precision, otherwise return 0.
				 * 
				 * TODO: test for possible return of smallest possible non-zero value
				 */
				value = new BigDecimal(BigInteger.ZERO, MAXSCALE);
				return;
			}
		} // scale > MAXSCALE
		else if (scale < MINSCALE) {
			int numPrecision  = value.precision();
			// see if we can increase the precision to raise the scale
			// but can't make precision greater than ctx.precision
			int delta = MINSCALE - scale;
			if ((numPrecision + delta) <= ctx.getPrecision() || 
					// special case for 0 with a big scale
					(value.compareTo(BigDecimalZERO) == 0)) {
				resultVal = value.setScale(MINSCALE, ctx.getRoundingMode());
			}
			else {
				// can't fit.  Return Infinity or largest number, depending on rounding
				RoundingMode mode = ctx.getRoundingMode();
				if ((flags & DEC_NEG) != 0) {
					// negative number
					if (!((mode == RoundingMode.CEILING) || (mode == RoundingMode.DOWN))) {
						flags = DEC_NEG | DEC_INF;
						value = BigDecimalZERO;
						return;
					}
				}
				else {
					// positive number
					if (!((mode == RoundingMode.FLOOR) || (mode == RoundingMode.DOWN))) {
						flags = DEC_INF;
						value = BigDecimalZERO;
						return;
					}
				}
				// otherwise fall through to here and return largest possible number of this precision
				byte nine[] = {(byte)9};
				BigInteger NINE = new BigInteger(nine);
				BigInteger total = NINE;
				for (int i = 1; i < ctx.getPrecision(); i++) {
					total = NINE.add(total.multiply(BigInteger.TEN));
				}
				resultVal = new BigDecimal(total, MINSCALE);
				if ((flags & DEC_NEG) != 0) {
					resultVal = resultVal.negate(ctx.mathCtx());
				}
			}
		}
		else
			return; // it's in range
		
		value = resultVal;
	}
	
	/* arithmetic operations */
	public Decimal128 abs() {
		return this.abs(Decimal128Context.DECIMAL128);
	}
	
	public Decimal128 abs(Decimal128Context ctx) {
		Decimal128 result = new Decimal128();
		result.flags = this.flags;  // in case it's NaN or infinity
		result.flags &= ~DEC_NEG;
		result.value = this.value.abs(ctx.mathCtx());
		return result;
	}

	public Decimal128 add(Decimal128 addend) {
		return this.add(addend, Decimal128Context.DECIMAL128);
	}
	
	public Decimal128 add(Decimal128 addend, Decimal128Context ctx) {
		Decimal128 result;
		int specialbits = ((addend.flags | this.flags) & DEC_SPECIAL);
		if (specialbits != 0) {
			if ((specialbits & (DEC_NAN | DEC_SNAN)) != 0) {
				result = opNaNs(addend, ctx); // one or more NaNs
				return result;
			}
			else {//one or two infinities
				result = new Decimal128();
				if ((this.flags & DEC_INF) != 0) {
					if ((addend.flags & DEC_INF) != 0) {
						if (((this.flags ^ addend.flags) & DEC_NEG) != 0) { // different signs
							ctx.status |= Decimal128Context.DEC_Invalid_operation;
							result.flags = DEC_NAN; // should I do this?
							return result;
						}
					}
					result.flags = this.flags; // get sign and infinity flag from me
				}
				else { // augend is infinite, take its sign
					result.flags = addend.flags;
				}
				return result;
			}
		}
		result = new Decimal128();
		result.value = this.value.add(addend.value, ctx.mathCtx());
		if (result.value.compareTo(BigDecimalZERO) < 0)
			result.flags |= DEC_NEG;
		result.ClampOverflow(ctx);
		return result;
	}
	
	public boolean equals(Decimal128 num) {
		// return true if I'm > num
		Decimal128 comp = this.compareTo(num, Decimal128Context.DECIMAL128);
		if (comp == ZERO)
			return true;
		else
			return false;
	}
	
	public boolean greaterThan(Decimal128 num) {
		// return true if I'm > num
		Decimal128 comp = this.compareTo(num, Decimal128Context.DECIMAL128);
		if (comp == ONE)
			return true;
		else
			return false;
	}

	
	public boolean greaterThanOrEqual(Decimal128 num) {
		// return true if I'm >= num
		Decimal128 comp = this.compareTo(num, Decimal128Context.DECIMAL128);
		if (comp == ONE || comp == ZERO)
			return true;
		else
			return false;
	}

	public boolean lessThan(Decimal128 num) {
		// return true if I'm < num
		Decimal128 comp = this.compareTo(num, Decimal128Context.DECIMAL128);
		if (comp == NEG1)
			return true;
		else
			return false;
	}

	
	public boolean lessThanOrEqual(Decimal128 num) {
		// return true if I'm <= num
		Decimal128 comp = this.compareTo(num, Decimal128Context.DECIMAL128);
		if (comp == NEG1 || comp == ZERO)
			return true;
		else
			return false;
	}

	
	// can't return just an int because could be NaN
	private Decimal128 compareTo(Decimal128 val, Decimal128Context ctx) {
		if (((this.flags | val.flags) & (DEC_NAN | DEC_SNAN)) != 0)
			return opNaNs(val, ctx);
		if ((this.flags & DEC_INF) != 0) {
			// I'm infinite (+|-)
			if ((this.flags & DEC_NEG) != 0) {
				// I'm negative infinity, less than anything except another negative infinity
				if ((val.flags & (DEC_INF | DEC_NEG)) == (DEC_INF | DEC_NEG)) {
					ctx.status |= Decimal128Context.DEC_Invalid_operation;
					return NaN;
				}
				return NEG1;
			}
			else { // I'm infinity, greater than anyone except another infinity
				if ((val.flags & (DEC_INF | DEC_NEG)) == (DEC_INF)) {
					ctx.status |= Decimal128Context.DEC_Invalid_operation;
					return NaN;
				}
				return ONE;
			}
		}
		else if ((val.flags & DEC_INF) != 0) {
			// val is infinite and I'm not
			if ((val.flags & DEC_NEG) != 0)
				return ONE;
			else
				return NEG1;
		}
		else {
			switch(this.value.compareTo(val.value)) {
			case 1:
				return ONE;
			case -1:
				return NEG1;
			default:
				return ZERO;
			}
		}
	}
	
	public Decimal128 divide(Decimal128 divisor) {
		return this.divide(divisor, Decimal128Context.DECIMAL128);
	}
	
	public Decimal128 divide(Decimal128 divisor, Decimal128Context ctx) {
		Decimal128 result;
		boolean differentsigns = ((this.flags ^ divisor.flags) & DEC_NEG) != 0;
		int specialbits = ((divisor.flags | this.flags) & DEC_SPECIAL);
		if (specialbits != 0) {
			if ((specialbits & (DEC_NAN | DEC_SNAN)) != 0) {
				return opNaNs(divisor, ctx); // one or more NaNs
			}
			else {//one or two infinities
				result = new Decimal128();
				if ((this.flags & DEC_INF) != 0) {
					// I'm infinite
					if ((divisor.flags & DEC_INF) != 0) {
						ctx.status |= Decimal128Context.DEC_Invalid_operation;
						result.flags = DEC_NAN; 
						// if (differentsigns) result.flags |= DEC_NEG;
					} else {
						result.flags = DEC_INF;
						if (differentsigns) result.flags |= DEC_NEG; 
					}
				}
				else { // divisor is infinite and I'm not, result is (+|-)0
					result.value = BigDecimalZERO.setScale(DECIMAL128_Bias); // smallest 0
					if (differentsigns) result.flags = DEC_NEG;
				}
				return result;
			}
		}
		result = new Decimal128();
		// now check for divide by 0
		if (divisor.value.compareTo(BigDecimalZERO) == 0) { // don't use equals here
			// divisor is 0, answer is (+|-)infinity unless I'm also 0
			if (this.value.compareTo(BigDecimalZERO) == 0) {
				// oops, 0/0 = NaN
				ctx.status |= Decimal128Context.DEC_Invalid_operation;
				result.flags = DEC_NAN;
			}
			else {
				result.value = BigDecimalZERO;
				result.flags |= DEC_INF;
			}
		}
		else result.value = this.value.divide(divisor.value, ctx.mathCtx());
		if (differentsigns)
			result.flags |= DEC_NEG;
		result.ClampOverflow(ctx);
		return result;
	}
	
	public Decimal128 multiply(Decimal128 multiplicand) {
		return this.multiply(multiplicand, Decimal128Context.DECIMAL128);
	}
	
	public Decimal128 multiply(Decimal128 multiplicand, Decimal128Context ctx) {
		Decimal128 result;
		boolean differentsigns = ((this.flags ^ multiplicand.flags) & DEC_NEG) != 0;
		int specialbits = ((multiplicand.flags | this.flags) & DEC_SPECIAL);
		if (specialbits != 0) {
			if ((specialbits & (DEC_NAN | DEC_SNAN)) != 0) {
				result = opNaNs(multiplicand, ctx); // one or more NaNs
				return result;
			}
			else {  //one or two infinities
				result = new Decimal128();
				if ((this.flags & DEC_INF) != 0) {
					// I'm infinite, all is well unless he is 0
					if (((multiplicand.flags & DEC_INF) == 0) && (multiplicand.value.compareTo(BigDecimalZERO) == 0)) {
						ctx.status |= Decimal128Context.DEC_Invalid_operation; // should I do this?
						result.flags = DEC_NAN;
						return result;
					}
				}
				else { 
					// He's infinite, I'd better not be 0
					if (this.value.compareTo(BigDecimalZERO) == 0) {
						ctx.status |= Decimal128Context.DEC_Invalid_operation; // should I do this?
						result.flags = DEC_NAN;
						return result;
					}
				}
				result.flags = DEC_INF;
				if (differentsigns) result.flags |= DEC_NEG;
				return result;
			}
		}
		result = new Decimal128();
		result.value = this.value.multiply(multiplicand.value, ctx.mathCtx());
		if (differentsigns) result.flags |= DEC_NEG;
		result.ClampOverflow(ctx);
		return result;
	}
	
	public Decimal128 remainder(Decimal128 divisor) {
		return this.remainder(divisor, Decimal128Context.DECIMAL128);
	}
	
	public Decimal128 remainder(Decimal128 divisor, Decimal128Context ctx) {
		Decimal128 result;
		boolean differentsigns = ((this.flags ^ divisor.flags) & DEC_NEG) != 0;
		int specialbits = ((divisor.flags | this.flags) & DEC_SPECIAL);
		if (specialbits != 0) {
			if ((specialbits & (DEC_NAN | DEC_SNAN)) != 0) {
				return opNaNs(divisor, ctx); // one or more NaNs
			}
			else {//one or two infinities
				result = new Decimal128();
				if (((this.flags & DEC_INF) != 0) || 
						((divisor.flags & DEC_INF) == 0) && (divisor.value.compareTo(BigDecimalZERO) == 0)) {
					// Dividend infinite or divisor 0
					ctx.status |= Decimal128Context.DEC_Invalid_operation;
					result.flags = DEC_NAN; 
				}
				else { // d % infinity == d
					result.value = this.value;
					if (differentsigns) result.flags = DEC_NEG;
				}
				return result;
			}
		}
		result = new Decimal128();
		// finite % 0 = NaN
		if (divisor.value.compareTo(BigDecimalZERO) == 0) {
			// doesn't matter whether this.value is non-zero
			result.flags = DEC_NAN;
		}
		else {
			// 0 % finite = 0
			if (this.value.compareTo(BigDecimalZERO) == 0) { 
				result.value = this.value;
			}
			else {
				// for reasons Mike says are "by design", it can blow up
				try {
				if (ctx.mathCtx().getPrecision() == 34)
					result.value = this.value.remainder(divisor.value, ctx.mathCtx());
				else {
					// compute remainder in full precision, round answer to smaller precision
					result.value = this.value.remainder(divisor.value, Decimal128Context.DECIMAL128.mathCtx());
					result.value = result.value.add(BigDecimalZERO, ctx.mathCtx());
				}
				} catch (ArithmeticException e) {
					result.flags = DEC_NAN;
				}
			}
		}
		if (differentsigns)
			result.flags |= DEC_NEG;
		result.ClampOverflow(ctx);
		return result;
		
	}

	
	public Decimal128 subtract(Decimal128 subtrahend) {
		return this.subtract(subtrahend, Decimal128Context.DECIMAL128);
	}
	
	public Decimal128 subtract(Decimal128 subtrahend, Decimal128Context ctx) {
		Decimal128 result;
		int specialbits = ((subtrahend.flags | this.flags) & DEC_SPECIAL);
		if (specialbits != 0) {
			if ((specialbits & (DEC_NAN | DEC_SNAN)) != 0) {
				result = opNaNs(subtrahend, ctx); // one or more NaNs
				return result;
			}
			else {//one or two infinities
				result = new Decimal128();
				if ((this.flags & DEC_INF) != 0) {
					// I'm infinite, only bad value is subtracting one of same sign
					if ((subtrahend.flags & DEC_INF) != 0) {
						if (((this.flags ^ subtrahend.flags) & DEC_NEG) == 0) { // same signs
							ctx.status |= Decimal128Context.DEC_Invalid_operation;
							result.flags = DEC_NAN; // should I do this?
							return result;
						}
					}
					result.flags = this.flags; // get sign and infinity flag from me
				}
				else { // augend is infinite and I'm not, take its sign
					result.flags = subtrahend.flags;
					result.flags ^= DEC_NEG; // toggle the sign bit
				}
				return result;
			}
		}
		result = new Decimal128();
		result.value = this.value.subtract(subtrahend.value, ctx.mathCtx());
		if (result.value.compareTo(BigDecimalZERO) < 0)
			result.flags |= DEC_NEG;
		result.ClampOverflow(ctx);
		return result;
	}

	/* override method from Object */
	public int hashCode() {
		return value.hashCode() ^ flags;
	}
        
        
        //JPEXS
        @Override
        public boolean equals(Object num) {
            if (num instanceof Decimal128) {
                return equals((Decimal128) num);
            }
            return false;
        }
        
        //JPEXS
        public String toPlainString() {
		if ((flags & (DEC_NAN | DEC_SNAN)) != 0) {
			return "NaN"; // what about number after NaN
		}
		else if ((flags & DEC_INF) != 0) {
			return "Infinity";
		}
		else {
			return value.toPlainString();
		}
	}
        
        //JPEXS
        public String toActionScriptString() {
            if ((flags & (DEC_NAN | DEC_SNAN)) != 0) {
			return "NaN"; // what about number after NaN
		}
		else if ((flags & DEC_INF) != 0) {
			return "Infinity";
		}
		else {
			return value.toPlainString() + "m";
		}
        }                
}
