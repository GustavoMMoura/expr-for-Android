// Syntax-error exception.
// Copyright 1996 by Darius Bacon; see the file COPYING.

package expr;

import android.content.Context;

/**
 * An exception indicating a problem in parsing an expression.  It can
 * produce a short, cryptic error message (with getMessage()) or a
 * long, hopefully helpful one (with explain()).
 */
public class SyntaxException extends Exception
{

	/**
	 * An error code meaning the input string couldn't reach the end
	 * of the input; the beginning constituted a legal expression,
	 * but there was unparsable stuff left over.
	 */
	public static final int INCOMPLETE = 0;

	/**
	 * An error code meaning the parser ran into a non-value token
	 * (like "/") at a point it was expecting a value (like "42" or
	 * "x^2").
	 */
	public static final int BAD_FACTOR = 1;

	/**
	 * An error code meaning the parser hit the end of its input
	 * before it had parsed a full expression.
	 */
	public static final int PREMATURE_EOF = 2;

	/**
	 * An error code meaning the parser hit an unexpected token at a
	 * point where it expected to see some particular other token.
	 */
	public static final int EXPECTED = 3;

	/**
	 * An error code meaning the expression includes a variable not
	 * on the `allowed' list.
	 */
	public static final int UNKNOWN_VARIABLE = 4;

	/**
	 * Make a new instance.
	 *
	 * @param complaint short error message
	 * @param parser    the parser that hit this snag
	 * @param reason    one of the error codes defined in this class
	 * @param expected  if nonnull, the token the parser expected to
	 *                  see (in place of the erroneous token it did see)
	 */
	public SyntaxException(String complaint,
						   Parser parser,
						   int reason,
						   String expected)
	{
		super(complaint);
		this.reason = reason;
		this.parser = parser;
		this.scanner = parser.tokens;
		this.expected = expected;
	}

	/**
	 * Give a long, hopefully helpful error message.
	 *
	 * @return the message
	 */
	public String explain(Context context)
	{
		StringBuffer sb = new StringBuffer();

		sb.append(context.getString(R.string.calculator_error_intro, scanner.getInput()));
		sb.append("\n\n");

		explainWhere(sb, context);
		explainWhy(sb, context);
		explainWhat(sb, context);

		return sb.toString();
	}

	private Parser parser;
	private Scanner scanner;

	private int reason;
	private String expected;

	private String fixedInput = "";

	private void explainWhere(StringBuffer sb, Context context)
	{
		if(scanner.isEmpty())
			sb.append(context.getString(R.string.calculator_error_empty));
		else if(scanner.atStart())
		{
			if(isLegalToken())
				sb.append(context.getString(R.string.calculator_error_start_illegal_symbol_location, theToken()));
			else
				sb.append(context.getString(R.string.calculator_error_start_meaningless_symbol, theToken()));
		}
		else
		{
			if(scanner.atEnd())
				sb.append(context.getString(R.string.calculator_error_ends_unexpectedly, asFarAs()));
			else if(isLegalToken())
				sb.append(context.getString(R.string.calculator_error_ends_with, asFarAs(), theToken()));
			else
				sb.append(context.getString(R.string.calculator_error_ends_with_unrecognised, asFarAs(), theToken()));
		}

		sb.append('\n');
	}

	private void explainWhy(StringBuffer sb, Context context)
	{
		switch(reason)
		{
			case INCOMPLETE:
				if(isLegalToken())
					sb.append(context.getString(R.string.calculator_error_incomplete));
				break;
			case BAD_FACTOR:
			case PREMATURE_EOF:
				if(scanner.atStart())
					sb.append(context.getString(R.string.calculator_error_expected_value));
				else
					sb.append(context.getString(R.string.calculator_error_expected_value_to_follow));
				break;
			case EXPECTED:
				sb.append(context.getString(R.string.calculator_error_expected_x, expected));
				break;
			case UNKNOWN_VARIABLE:
				sb.append(context.getString(R.string.calculator_error_unknown_variable));
				break;
			default:
				throw new Error("Can't happen");
		}

		sb.append('\n');
	}

	private void explainWhat(StringBuffer sb, Context context)
	{
		fixedInput = tryToFix();
		if(null != fixedInput)
		{
			sb.append(context.getString(R.string.calculator_error_parse_example, fixedInput));
			sb.append('\n');
		}
	}

	private String tryToFix()
	{
		return (parser.tryCorrections() ? scanner.toString() : null);
	}

	private String asFarAs()
	{
		Token t = scanner.getCurrentToken();
		int point = t.location - t.leadingWhitespace;
		return scanner.getInput().substring(0, point);
	}

	private String theToken()
	{
		return scanner.getCurrentToken().sval;
	}

	private boolean isLegalToken()
	{
		Token t = scanner.getCurrentToken();
		return t.ttype != Token.TT_EOF
				&& t.ttype != Token.TT_ERROR;
	}
}
