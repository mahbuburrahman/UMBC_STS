package edu.umbc.nlp.tool;

import java.util.ArrayList;
import java.util.Arrays;

public class NumberConverter {

	public static String spell(long number) throws NumberException {
		String text;
		if (number < 0L) {
			text = "Minus " + spell(-number, 1);
		} else {
			text = spell(number, 1);
		}

		int index_amp, index_perc;

		index_amp = text.lastIndexOf("$");
		index_perc = text.lastIndexOf("%");

		if (index_amp >= 0) {
			if (index_perc < 0 || index_amp > index_perc) {

				String text1 = text.substring(0, index_amp);
				String text2 = text.substring(index_amp + 1, text.length());

				text = text1 + " and " + text2;
			}
		}

		text = text.replaceAll("\\$", ", ");
		text = text.replaceAll("%", " and ");

		return text;
	}

	// WithSeparator () function:
	// It converts a number to string using 1000's separator.
	// It uses a simple recursive algorithm.
	public static String WithSeparator(long number) {
		if (number < 0) {
			return "-" + WithSeparator(-number);
		}

		if (number / 1000L > 0) {
			return WithSeparator(number / 1000L) + ","
					+ String.format("%1$03d", number % 1000L);
		} else {
			return String.format("%1$d", number);
		}
	}

	private static String mySuffixText[] = {
			"", // Dummy! no level 0
			"", // Nothing for level 1
			" Thousand", " Million", " Billion", " Trillion",
			" (Thousand Trillion)", " (Million Trillion)",
			" (Billion Trillion)", };

	private static String myTeenText[] = { "Zero", "One", "Two", "Three",
			"Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
			"Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
			"Seventeen", "Eighteen", "Ninteen", };

	// used appropriately for under-cent values:
	private static String myCentText[] = { "Twenty", "Thirty", "Forty",
			"Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };

	// used appropriately for under-mil values.
	private static String myMilText[] = { "One Hundred", "Two Hundred",
			"Three Hundred", "Four Hundred", "Five Hundred", "Six Hundred",
			"Seven Hundred", "Eight Hundred", "Nine Hundred" };

	private static String SpellBelow1000(long number) throws NumberException {
		if (number < 0 || number >= 1000)
			throw new NumberException("Expecting a number between 0 and 999: "
					+ number);

		if (number < 20L) {
			return myTeenText[(int) number];
		} else if (number < 100L) {
			int div = (int) number / 10;
			int rem = (int) number % 10;

			if (rem == 0) {
				return myCentText[div - 2];
			} else {
				return myCentText[div - 2] + " " + SpellBelow1000(rem);
			}
		} else {
			int div = (int) number / 100;
			int rem = (int) number % 100;

			if (rem == 0) {
				return myMilText[div - 1];
			} else {
				return myMilText[div - 1] + "%" + SpellBelow1000(rem);
			}
		}
	}

	private static String spell(long number, int level) throws NumberException {
		long div = number / 1000L;
		long rem = number % 1000L;

		if (div == 0) {
			return SpellBelow1000(rem) + mySuffixText[level];
		} else {
			if (rem == 0) {
				return spell(div, level + 1);
			} else {
				return spell(div, level + 1) + "$" + SpellBelow1000(rem)
						+ mySuffixText[level];
			}
		}
	}

	private static String[] myBelowThousandWords = { "zero", "one", "two",
			"three", "four", "five", "six", "seven", "eight", "nine", "ten",
			"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
			"seventeen", "eighteen", "ninteen", "twenty", "thirty", "forty",
			"fifty", "sixty", "seventy", "eighty", "ninety", "hundred" };

	private static long[] myBelowThousandValuess = { 0, 1, 2, 3, 4, 5, 6, 7, 8,
			9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 30, 40, 50, 60, 70,
			80, 90, 100 };

	private static ArrayList<String> myBelowThousandWordList = new ArrayList<String>(
			Arrays.asList(myBelowThousandWords));

	public static long parseBelow1000(String text) throws NumberException {

		long value = 0;
		String[] words = text.replaceAll(" and ", " ").split("\\s");

		for (String word : words) {
			if (!myBelowThousandWordList.contains(word)) {
				throw new NumberException("Unknown token : " + word);
			}

			long subval = getValueOf(word);

			if (subval == 100) {
				if (value == 0)
					value = 100;
				else
					value *= 100;
			} else
				value += subval;

		}

		return value;
	}

	private static long getValueOf(String word) {

		return myBelowThousandValuess[myBelowThousandWordList.indexOf(word)];
	}

	private static String[] mySuffixWords = { "trillion", "billion", "million",
			"thousand" };

	private static long[] mySuffixValues = { 1000000000000L, 1000000000L,
			1000000L, 1000L };

	public static long parse(String text) throws NumberException {
		
		if (text.equalsIgnoreCase("one") || text.equalsIgnoreCase("1st") || text.equalsIgnoreCase("first"))
			return 1;
		else if (text.equalsIgnoreCase("two") || text.equalsIgnoreCase("2nd") || text.equalsIgnoreCase("second"))
			return 2;
		else if (text.equalsIgnoreCase("three") || text.equalsIgnoreCase("3rd") || text.equalsIgnoreCase("third"))
			return 3;
		else if (text.equalsIgnoreCase("four") || text.equalsIgnoreCase("4th") || text.equalsIgnoreCase("fourth"))
			return 4;
		else if (text.equalsIgnoreCase("five") || text.equalsIgnoreCase("5th") || text.equalsIgnoreCase("fifth"))
			return 5;
		else if (text.equalsIgnoreCase("six") || text.equalsIgnoreCase("6th") || text.equalsIgnoreCase("sixth"))
			return 6;
		else if (text.equalsIgnoreCase("seven") || text.equalsIgnoreCase("7th") || text.equalsIgnoreCase("seventh"))
			return 7;
		else if (text.equalsIgnoreCase("eight") || text.equalsIgnoreCase("8th") || text.equalsIgnoreCase("eighth"))
			return 8;
		else if (text.equalsIgnoreCase("nine") || text.equalsIgnoreCase("9th") || text.equalsIgnoreCase("ninth"))
			return 9;
		else if (text.equalsIgnoreCase("ten") || text.equalsIgnoreCase("10th") || text.equalsIgnoreCase("tenth"))
			return 10;

		text = text.toLowerCase().replaceAll("[\\-,]", " ").replaceAll(" and ",
				" ");

		long totalValue = 0;

		boolean processed = false;

		for (int n = 0; n < mySuffixWords.length; n++) {

			int index = text.indexOf(mySuffixWords[n]);

			if (index >= 0) {
				String text1 = text.substring(0, index).trim();
				String text2 = text
						.substring(index + mySuffixWords[n].length()).trim();

				if (text1.equals(""))
					text1 = "one";

				if (text2.equals(""))
					text2 = "zero";

				totalValue = parseBelow1000(text1) * mySuffixValues[n]
						+ parse(text2);
				processed = true;
				break;

			}
		}

		if (processed)
			return totalValue;
		else
			return parseBelow1000(text);
	}
}