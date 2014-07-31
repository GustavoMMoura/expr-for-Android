import expr.Parser;
import expr.SyntaxException;
import expr.Variable;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GraphApplet extends Applet implements ActionListener
{

	public String getAppletInfo()
	{
		return "Draws a graph of a formula you enter.";
	}

	private GraphCanvas canvas;
	private TextField input;

	public void init()
	{
		input = new TextField(36);
		input.addActionListener(this);

		Panel inputBox = new Panel();
		inputBox.setLayout(new FlowLayout());
		inputBox.add(new Label("f(x) = ", Label.RIGHT));
		inputBox.add(input);

		canvas = new GraphCanvas();

		setLayout(new BorderLayout());
		add("North", inputBox);
		add("Center", canvas);
		validate();
	}

	public void actionPerformed(ActionEvent evt)
	{
		drawGraph();
	}

	private void drawGraph()
	{
		try
		{
			canvas.setCurve(parseFofX(input.getText(), Color.black));
		}
		catch(SyntaxException e)
		{
			showStatus(e.explain());
			return;
		}
		showStatus("");
		canvas.repaint();
	}

	private Curve parseFofX(String text, Color color) throws SyntaxException
	{
		Variable x = Variable.make("x");
		Parser parser = new Parser();
		parser.allow(x);
		return new FunctionOfX(canvas, color, x, parser.parseString(text));
	}
}
