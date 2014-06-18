package edu.umbc.nlp.tool;

import java.util.Scanner;


public class NumberConvertTest {

	public static void main(String[] args) throws NumberException {

		long value = 0L;

		for (long i = 0; i <= 10L; i++) {
			value = value * 10 + i;
			readOutAgain(value);
		}

		readOut(100001);

		value = 0L;

		for (long i = 10; i >= 0L; i--) {
			value = value * 10 + i;
			readOutAgain(value);
		}

		Scanner in = new Scanner(System.in);

		for (;;) {
			try {
				System.out.print("Number in words : ");
				String numberWordsText = in.nextLine();

				if (numberWordsText.equals("."))
					break;
				else if (numberWordsText.equals(""))
					continue;

				System.out.println("Value : "
						+ NumberConverter.WithSeparator(NumberConverter
								.parse(numberWordsText)));

			} catch (Exception e) {
				System.err.println(e.getMessage());

			}
		}

	}

	private static void readOut(long value) {
		String text = "Error";
		try {
			text = NumberConverter.spell(value);

		} catch (NumberException e) {
			text = e.getMessage();

		} catch (Exception e) {
			text = e.getMessage();
		}

		System.out.println(NumberConverter.WithSeparator(value) + " : " + text);
	}

	private static void readOutAgain(long value) throws NumberException {
		String text = "Error";
		long readValue = 0;
		try {
			text = NumberConverter.spell(value);
			readValue = NumberConverter.parse(text);

		} catch (NumberException e) {
			text = e.getMessage();

		} catch (Exception e) {
			text = e.getMessage();

		}

		System.out.println(NumberConverter.WithSeparator(value) + " : " + text
				+ "  (" + readValue + ")");
	}
}
