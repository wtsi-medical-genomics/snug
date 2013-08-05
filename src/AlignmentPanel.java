import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import net.sf.picard.reference.IndexedFastaSequenceFile;
import net.sf.picard.reference.ReferenceSequence;
import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class AlignmentPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<SAMRecord> reads;
	IndexedFastaSequenceFile reference;
	int ALIGNMENT_PIX_PER_BASE;
	Variant v;
	
	public void setVariant(Variant variant) {
		this.v = variant;
	}

	public AlignmentPanel() {
		super();
		setPreferredSize(new Dimension(900, 600));
		setBackground(Color.white);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		FontMetrics fm = getFontMetrics(getFont());
		ALIGNMENT_PIX_PER_BASE = fm.charWidth('M');
		
		drawAlignment(g2);
	}

	private void drawAlignment(Graphics2D g2) {
    	
    	if (reads != null && reference != null) {
    		
    		int ypos = 15;
    		g2.setColor(Color.blue);
    		
    		int width = this.getWidth();
    		int height = this.getHeight();
    		// how many bases can we fit in the current window
    		int visableBases = width / ALIGNMENT_PIX_PER_BASE;
    		// assume the middle base is the variant - how many bases are either side of it
    		int surroundingBases = visableBases -1;
    		// distance in pix to the variant site
    		int offset = (surroundingBases / 2) * ALIGNMENT_PIX_PER_BASE;
    		int refStart = v.getPos() - (surroundingBases/2);
    		int refEnd = v.getPos() + (surroundingBases/2);
    		
    		drawRuler(g2, refStart, refEnd, ypos);
    		ypos += 20;
    		drawReference(g2, refStart, refEnd, ypos);
    		
    		// draw lines before and after to show the position of the variant
    		g2.drawLine(offset-1, 0, offset-1, height);
    		g2.drawLine(offset + ALIGNMENT_PIX_PER_BASE, 0, offset + ALIGNMENT_PIX_PER_BASE, height);
    		
    		ypos += 15;
    		g2.setColor(Color.black);
            for (SAMRecord read : reads) {            	
            	drawSequence(g2, read, ypos, offset);
            	ypos += 15;
    		}            
    	}
    }
	
	private void drawReference(Graphics2D g2, int refStart, int refEnd, int ypos) {
		
		ReferenceSequence refSeq = reference.getSubsequenceAt(v.getChr(), refStart, refEnd);
		byte[] bases = refSeq.getBases();
		
		g2.setColor(Color.blue);
		for(int i=0; i<bases.length; i++) {
			String base = String.valueOf((char) bases[i]);
			g2.drawString(base, i * ALIGNMENT_PIX_PER_BASE, ypos);	
		}		
	}

	private void drawRuler(Graphics2D g2, int refStart, int refEnd, int ypos) {
		
		ReferenceSequence refSeq = reference.getSubsequenceAt(v.getChr(), refStart, refEnd);
		byte[] bases = refSeq.getBases();
		
		g2.setColor(Color.black);		
		for(int i=0; i<bases.length; i++) {			
			if ((i+1) % 10 == 0) {
				g2.drawString("|", i * ALIGNMENT_PIX_PER_BASE + 2, ypos);
			} else {
				g2.drawString("_", i * ALIGNMENT_PIX_PER_BASE + 1, ypos);
			}
		}
	}
	
	private void drawSequence(final Graphics2D g2, final SAMRecord samRecord, int ypos, int offset) {
		
		int xpos;
		int basePosition;
		final String readSeq = samRecord.getReadString();

		List<AlignmentBlock> blocks = samRecord.getAlignmentBlocks();
		
		for(int i=0; i<blocks.size(); i++) {
			
			AlignmentBlock block = blocks.get(i);
			int blockStart = block.getReadStart();
			int distance2Variant = block.getReferenceStart() - v.getPos();			
			
			for(int j=0; j<block.getLength(); j++) {
			
				int readPos = blockStart-1+j;
				int currentPosition = ((distance2Variant + j) + v.getPos());
				ReferenceSequence refSeq = reference.getSubsequenceAt(v.getChr(), currentPosition, currentPosition);
				byte[] bases = refSeq.getBases();				
				String refBase = String.valueOf((char) bases[0]);
				String readBase = readSeq.substring(readPos, readPos+1);
				
				// check read base against reference base - colour or bold if different
				if (readBase.matches(refBase)) {
					g2.setColor(Color.black);
					readBase = "-";
				} else {
					g2.setColor(Color.red);
				}
				
				basePosition = (distance2Variant + j) * ALIGNMENT_PIX_PER_BASE;
				xpos = offset + basePosition;
				g2.drawString(readBase, xpos, ypos);				
			}
		}
	}

	public void setReads(ArrayList<SAMRecord> reads) {
		this.reads = reads;
	}

	public void setReference(IndexedFastaSequenceFile indexedFastaSequenceFile) {
		this.reference = indexedFastaSequenceFile;

	}

}