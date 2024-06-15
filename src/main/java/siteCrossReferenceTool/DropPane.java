package siteCrossReferenceTool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class DropPane extends JTextPane{
	DropPane(final int whichSelect) {
		super();
		setText("\n\n\n\nDrop");
		StyledDocument doc = getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		setEditable(false);
		setFocusable(false);
		setBackground(Color.LIGHT_GRAY);
		setPreferredSize(new Dimension(250, 250));
		setDropTarget(new DropTarget() {
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>)
					evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					for (File file : droppedFiles) {
						Main.selectedFiles[whichSelect] = file;
						Main.selectButtons[whichSelect].rename(file.getName());
						rename("\n\n\n\n:)");
					}
					evt.dropComplete(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	void rename(String text) {
		setText(text);
	}
}