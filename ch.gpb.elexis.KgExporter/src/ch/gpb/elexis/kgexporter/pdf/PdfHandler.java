/*******************************************************************************
 * Copyright (c) 2015, Daniel Ludin
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Ludin (ludin@hispeed.ch) - initial implementation
 *******************************************************************************/
/**
 * @author daniel ludin ludin@hispeed.ch
 * 27.06.2015
 * 
 */
package ch.gpb.elexis.kgexporter.pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.gpb.elexis.kgexporter.handlers.KgExportHandler;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfHandler {
    protected static Logger log = LoggerFactory.getLogger(KgExportHandler.class.getName());
    static com.lowagie.text.Font fontTimes;
    static com.lowagie.text.Font fontTimesSmall;
    static com.lowagie.text.Font fontTimesTitle;
    static Rectangle rect = new Rectangle(30, 30, 559, 800);

    // 
    static {
	try {
	    BaseFont bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", true);
	    fontTimes = new com.lowagie.text.Font(bf_helv, 10);
	    fontTimesTitle = new com.lowagie.text.Font(bf_helv, 12);
	    fontTimesSmall = new com.lowagie.text.Font(bf_helv, 6);
	} catch (DocumentException e) {
	    //System.out.println("Error while loading PDF font: " + e.getMessage());
	    log.error("Error while loading PDF font: " + e.getMessage());
	} catch (IOException e) {
	    //System.out.println("Error while loading PDF font: " + e.getMessage());
	    log.error("Error while loading PDF font: " + e.getMessage());
	}

    }

    public PdfHandler() {
    }


    public static void createLaborwertTable(Patient patient, String filename, LinkedList<String[]> laborBlatt,
	    String footerText)
	    throws IOException,
	    DocumentException {

	// step 1
	Document document = new Document(PageSize.A4);

	// step 2
	PdfWriter.getInstance(document, new FileOutputStream(filename));
	PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
	// step 3
	writer.setBoxSize("art", rect);

	HeaderFooterPageEvent event = new HeaderFooterPageEvent();
	event.setHeaderText(patient.getVorname() + " " + patient.getName() + " ("
		+ patient.getGeburtsdatum() + ")");
	event.setFooterText(footerText);
	writer.setPageEvent(event);


	document.setMargins(56, 72, 60, 60);

	// step 3
	document.open();

	if (laborBlatt.size() == 0) {
	    Paragraph p = new Paragraph(new Chunk("Keine Laborwerte vorhanden", fontTimesTitle));
	    p.setSpacingBefore(20f);
	    document.add(p);
	}
	else {

	    document.add(createTable2(laborBlatt));
	}

	// step 5
	document.close();

    }

    public static void createPdf(Patient patient, String filename, LinkedList<String[]> laborBlatt)
	    throws IOException, DocumentException {

	BaseFont bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", true);
	fontTimes = new com.lowagie.text.Font(bf_helv, 6);

	// step 1
	Document document = new Document(PageSize.A4.rotate());

	// step 2
	PdfWriter.getInstance(document, new FileOutputStream(filename));
	// step 3

	document.setHeader(getHeader(patient.getVorname() + " " + patient.getName() + "   Geburtsdatum: "
		+ patient.getGeburtsdatum()));

	document.open();

	document.add(new Chunk("")); // << this will do the trick. 

	// step 4
	document.add(createTable2(laborBlatt));
	// step 5
	document.close();
    }

    private static HeaderFooter getHeader(String headerText) {
	Phrase headerPhrase = new Phrase(headerText);
	headerPhrase.setFont(fontTimes);

	HeaderFooter header = new HeaderFooter(headerPhrase, false);
	header.setBorder(Rectangle.BOTTOM);
	header.setBorderWidth(0.5f);
	header.setAlignment(Element.ALIGN_LEFT);

	return header;
    }

    public static void createLaborwertTableOld(Patient patient, String filename, LinkedList<String[]> laborBlatt)
	    throws IOException,
	    DocumentException {
	createPdf(patient, filename, laborBlatt);
    }

    public static void createDiagnosenSheet(Patient patient, String filename, String footerText)
	    throws DocumentException, IOException {

	StringBuffer sb = new StringBuffer("Diagnosen:\n");
	sb.append(patient.getDiagnosen());
	sb.append("\n");
	sb.append("Persönliche Anamnese:\n");
	sb.append(patient.getPersAnamnese());
	sb.append("\n");

	// step 1
	Document document = new Document(PageSize.A4);

	// step 2
	PdfWriter.getInstance(document, new FileOutputStream(filename));
	PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
	// step 3
	//Rectangle rect = new Rectangle(30, 30, 559, 800);
	writer.setBoxSize("art", rect);

	HeaderFooterPageEvent event = new HeaderFooterPageEvent();
	event.setHeaderText(patient.getVorname() + " " + patient.getName() + " ("
		+ patient.getGeburtsdatum() + ")");
	event.setFooterText(footerText);
	writer.setPageEvent(event);

	document.setMargins(36, 72, 60, 60);

	// step 3
	document.open();

	document.add(new Chunk("")); // << this will do the trick. 

	if (patient.getDiagnosen().length() > 0) {
	    Paragraph p = new Paragraph("Diagnosen:", fontTimesTitle);
	    p.setSpacingAfter(10f);
	    document.add(p);

	    String[] chunksDiag = patient.getDiagnosen().toString().split("(?m)^\\s*$");
	    for (String chunk : chunksDiag) {
		document.add(new Paragraph(chunk, fontTimes));
	    }
	} else {
	    document.add(new Paragraph("Keine Diagnosen vorhanden", fontTimes));

	}

	if (patient.getPersAnamnese().length() > 0) {
	    Paragraph p = new Paragraph("Persönliche Anamnese:", fontTimesTitle);
	    p.setSpacingBefore(10f);
	    p.setSpacingAfter(10f);

	    document.add(p);

	    String[] chunksAnam = patient.getPersAnamnese().toString().split("(?m)^\\s*$");
	    for (String chunk : chunksAnam) {
		document.add(new Paragraph(chunk, fontTimes));
	    }
	} else {
	    document.add(new Paragraph("Keine Persönliche Anamnese vorhanden", fontTimes));

	}

	// step 5
	document.close();

    }

    public static void createFixMediSheet(Patient patient, String filename, String footerText)
	    throws DocumentException, IOException {
	Prescription[] prescriptions = patient.getFixmedikation();
	StringBuffer sb = new StringBuffer();
	for (Prescription prescription : prescriptions) {
	    //System.out.println("Prescription: " + prescription.getLabel() + "/" + prescription.getDosis());
	    sb.append(prescription.getLabel());
	    sb.append("\r\n");
	}

	// step 1
	Document document = new Document(PageSize.A4);

	// step 2
	PdfWriter.getInstance(document, new FileOutputStream(filename));
	PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
	// step 3
	//Rectangle rect = new Rectangle(30, 30, 559, 800);
	writer.setBoxSize("art", rect);

	HeaderFooterPageEvent event = new HeaderFooterPageEvent();
	event.setHeaderText(patient.getVorname() + " " + patient.getName() + " ("
		+ patient.getGeburtsdatum() + ")");
	event.setFooterText(footerText);
	writer.setPageEvent(event);

	document.setMargins(36, 72, 60, 60);

	// step 3
	document.open();

	document.add(new Chunk("")); // << this will do the trick. 

	if (sb.length() > 0) {
	    Paragraph p = new Paragraph("Fixmedikation:", fontTimesTitle);
	    p.setSpacingAfter(10f);
	    document.add(p);

	    String[] chunksDiag = sb.toString().split("(?m)^\\s*$");
	    for (String chunk : chunksDiag) {
		document.add(new Paragraph(chunk, fontTimes));
	    }
	} else {

	    //document.add(new Paragraph("Keine Medikationen vorhanden", fontTimes));
	    Paragraph p = new Paragraph(new Chunk("Keine Medikationen vorhanden", fontTimesTitle));
	    p.setSpacingBefore(20f);
	    document.add(p);

	}

	// step 5
	document.close();

    }

    public static PdfPTable createTable2(LinkedList<String[]> laborBlatt) throws DocumentException, IOException {
	int noOfCols = laborBlatt.get(0)[0].length();
	PdfPTable table = new PdfPTable(noOfCols);

	float[] colWidths = new float[noOfCols];
	colWidths[0] = 200f;
	for (int i = 1; i < colWidths.length; i++) {
	    colWidths[i] = 40f;
	}

	table.setTotalWidth(colWidths);
	table.setLockedWidth(true);

	// the cell object
	PdfPCell cell;

	for (String[] strings : laborBlatt) {

	    for (int i = 0; i < strings.length; i++) {
		System.out.print(i + ":" + strings[i]);
		cell = new PdfPCell(new Phrase(strings[i], fontTimesSmall));

		cell.setUseBorderPadding(true);
		//
		// Setting cell's border width and color
		//
		cell.setBorderWidth(0.5f);

		table.addCell(cell);

	    }
	    //System.out.println("----------");
	    //System.out.println();
	}

	// we add a cell with colspan 3
	return table;
    }

}
