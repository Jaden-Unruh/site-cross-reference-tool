package siteCrossReferenceTool;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Possible values of {@link Main#info}
 * @author Jaden
 *
 */
enum InfoText {
	ERROR, DESKTOP, SELECT_PROMPT, INIT, RUNNING, DONE
}

/**
 * Entry class for site-cross-reference-tool
 * 
 * @author Jaden
 */
public class Main {
	
	/**
	 * Formats data from a cell of an excel sheet to a String
	 */
	static final DataFormatter FORMATTER = new DataFormatter();
	
	/**
	 * Main program window
	 */
	static JFrame window;
	
	/**
	 * The selected files. Start null. 0 is the date sheet, 1 is the hierarchy sheet.
	 */
	static File[] selectedFiles = new File[2];
	
	/**
	 * The select buttons for each file.
	 * @see #selectedFiles
	 * @see #selectButtons
	 */
	static SelectButton[] selectButtons = new SelectButton[2];
	
	/**
	 * The drop boxes for each file.
	 * @see #selectedFiles
	 * @see #selectButtons
	 */
	static DropPane[] dropPanes = new DropPane[2];
	
	/**
	 * The info box at the bottom of the main program window
	 */
	static JLabel info = new JLabel();
	
	/**
	 * The text currently showing in {@link #info}
	 */
	static InfoText infoText;
	
	/**
	 * Our two main sheets
	 */
	static XSSFSheet hierarchy, allDates;

	/**
	 * Entry method for site-cross-reference-tool
	 * @param args unused
	 */
	public static void main(String[] args) {
		openWindow();
	}
	
	/**
	 * Opens and populates {@link #window}
	 */
	private static void openWindow() {
		window = new JFrame("Site Cross-Reference Tool");
		window.setLayout(new GridBagLayout());
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.add(new JLabel("FCA Date Sheet"), simpleConstraints(0, 1, 2, 1));
		window.add(new JLabel("Location Hierarchy Sheet"), simpleConstraints(2, 1, 2, 1));
		
		dropPanes[0] = new DropPane(0);
		window.add(dropPanes[0], simpleConstraints(0, 2, 2, 1));
		
		dropPanes[1] = new DropPane(1);
		window.add(dropPanes[1], simpleConstraints(2, 2, 2, 1));
		
		selectButtons[0] = new SelectButton(0);
		window.add(selectButtons[0], simpleConstraints(0, 3, 2, 1));
		
		selectButtons[1] = new SelectButton(1);
		window.add(selectButtons[1], simpleConstraints(2, 3, 2, 1));
		
		window.add(info, simpleConstraints(0, 4, 4, 1));
		
		JButton close = new JButton("Close");
		window.add(close, simpleConstraints(0, 5, 1, 1));
		
		JButton help = new JButton("Help");
		window.add(help, simpleConstraints(1, 5, 1, 1));
		
		JButton run = new JButton("Run");
		window.add(run, simpleConstraints(3, 5, 1, 1));
		
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] choices = {"Close", "Open GitHub"};
				if (JOptionPane.showOptionDialog(window, "Drag-and-drop two files of type *.xlsx into the boxes, or use the select\nbuttons to open file selection dialogs. Then, press Run to start the program.\nFor more help, troubleshooting, and program information, visit the GitHub\nand view the program README.", "Help", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]) == 1) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URL("https://github.com/Jaden-Unruh/site-cross-reference-tool").toURI());
							int num = 0;
							System.out.println(2 / num);
						} catch (Exception e1) {
							updateInfo(InfoText.DESKTOP);
						}
					}
				}
			}
		});
		
		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkCorrectSelections()) {
					SwingWorker<Boolean, Void> sw = new SwingWorker<Boolean, Void>() {
						protected Boolean doInBackground() throws Exception {
							init();
							findSites();
							terminate();
							
							updateInfo(InfoText.DONE);
							
							run.setEnabled(true);
							
							return true;
						}
						
						@Override
						protected void done() {
							try {
								get();
							} catch (InterruptedException | ExecutionException e) {
								run.setEnabled(true);
								showErrorMessage(e);
							}
						}
					};
					run.setEnabled(false);
					sw.execute();
				} else
					updateInfo(InfoText.SELECT_PROMPT);
			}
		});
		
		window.pack();
		window.setVisible(true);
	}
	
	/**
	 * Loads {@link #selectedFiles} into memory
	 * @throws FileNotFoundException if a file is not found
	 * @throws IOException if there's an error accessing a file
	 */
	private static void init() throws FileNotFoundException, IOException {
		updateInfo(InfoText.INIT);
		
		XSSFWorkbook datesBook = new XSSFWorkbook(new FileInputStream(selectedFiles[0]));
		allDates = datesBook.getSheetAt(0);
		
		XSSFWorkbook hierarchyBook = new XSSFWorkbook(new FileInputStream(selectedFiles[1]));
		hierarchy = hierarchyBook.getSheetAt(0);
	}
	
	/**
	 * Finds all location IDs in the hierarchy but not in the dates sheet
	 * @throws IOException if there's an error outputting to a txt file
	 */
	private static void findSites() throws IOException {
		updateInfo(InfoText.RUNNING);
		HashSet<String> allLocations = new HashSet<String>();
		int hierarchyRows = hierarchy.getPhysicalNumberOfRows();
		for (int i = 6; i < hierarchyRows; i++) {
			String value = FORMATTER.formatCellValue(hierarchy.getRow(i).getCell(Values.getValue("colNum.hierarchy.locID")));
			if (value.length() > 0)
				allLocations.add(value);
		}
		
		int datesRows = allDates.getPhysicalNumberOfRows();
		for (int i = 1; i < datesRows; i++) {
			String value = FORMATTER.formatCellValue(allDates.getRow(i).getCell(Values.getValue("colNum.dates.locID")));
			if (value.length() > 0)
				allLocations.remove(value);
		}
		
		Iterator<String> it = allLocations.iterator();
		String output = "";
		while (it.hasNext())
			output += it.next() + "\n";
		
		String[] choices = {"Close", "Write to output file"};
		JTextArea jta = new JTextArea(25, 50);
		jta.setText("These are the unvisited location IDs:\n\n" + output);
		jta.setEditable(false);
		if (JOptionPane.showOptionDialog(window, new JScrollPane(jta), "Done!", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]) == 1) {
			String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS"));
			File outputFile = new File(String.format("%s\\Site Cross-Reference Output %s.txt", selectedFiles[0].getParent(), dateTime));
			outputFile.createNewFile();
			FileWriter writeToOutput = new FileWriter(outputFile);
			writeToOutput.append(output);
			writeToOutput.close();
		}
	}
	
	/**
	 * Closes {@link #selectedFiles}
	 * @throws IOException if there's an error closing
	 */
	private static void terminate() throws IOException {
		allDates.getWorkbook().close();
		hierarchy.getWorkbook().close();
	}
	
	/**
	 * Shows an error in a popup window
	 * @param e the exception to show
	 */
	private static void showErrorMessage(Exception e) {
		e.printStackTrace();
		String[] choices = {"Close", "More Info"};
		updateInfo(InfoText.ERROR);
		if (JOptionPane.showOptionDialog(window, String.format("The program encountered an error: %s\n", e.toString()), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, choices, choices[0]) == 1) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			JTextArea jta = new JTextArea(25, 50);
			jta.setText(String.format("Full Error Stack Trace:\n\n%s", sw.toString()));
			jta.setEditable(false);
			JOptionPane.showMessageDialog(window, new JScrollPane(jta), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Creates a GridBagConstraints object with the given attributes, and all other
	 * values set to defaults
	 * 
	 * @param x      horizontal location in grid bag
	 * @param y      vertical location in grid bag
	 * @param width  columns spanned in grid bag
	 * @param height rows spanned in grid bag
	 * @return the new GridBagConstraints object
	 */
	private static GridBagConstraints simpleConstraints(int x, int y, int width, int height) {
		return new GridBagConstraints(x, y, width, height, 0, 0, GridBagConstraints.CENTER, 0, new Insets(10, 10, 10, 10),
				0, 0);
	}
	
	/**
	 * Returns the string associated with the current value of {@link #infoText}
	 * @return the String
	 */
	static String getInfoText() {
		switch(infoText) {
		case ERROR:
			return "Error encountered.";
		case DESKTOP:
			return "There was a problem opening your browser. To visit the GitHub, manually navigate to https://github.com/Jaden-Unruh/site-cross-reference-tool/";
		case SELECT_PROMPT:
			return "Drag-and-Drop or select two files of type *.xlsx to begin.";
		case INIT:
			return "Initializing...";
		case RUNNING:
			return "Running...";
		case DONE:
			return "Done!";
		}
		return null;
	}
	
	/**
	 * Updates {@link #info} to the given value
	 * @param text the value
	 */
	static void updateInfo(InfoText text) {
		infoText = text;
		info.setText(getInfoText());
		window.pack();
	}
	
	/**
	 * Checks if the user has selected two valid (*.xlsx) files
	 * @return true if so
	 */
	private static boolean checkCorrectSelections() {
		try {
			return FileNameUtils.getExtension(selectedFiles[0].getName()).equals("xlsx") && FileNameUtils.getExtension(selectedFiles[1].getName()).equals("xlsx");
		} catch (NullPointerException e) {
			return false;
		}
	}
}