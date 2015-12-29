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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class HeaderFooterPageEvent extends PdfPageEventHelper {
    public float offset = 5;
    String sbHeader = new String("Default Text. use setHeaderText");
    String sbFooter = new String("Default Text. use setFooterText");
    String sDate = null;

    public HeaderFooterPageEvent() {
	super();

	DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
	Date date = new Date();
	sDate = df.format(date);
    }

    public void onStartPage(PdfWriter writer, Document document) {
	Rectangle rect = writer.getBoxSize("art");

	ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(sbHeader),
		rect.getLeft(), rect.getTop(), 0);

	ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase(sDate),
		rect.getRight(), rect.getTop(), 0);

	PdfContentByte cb = writer.getDirectContentUnder();

	//cb.rectangle(document.left(), document.top(),
	//	document.right() - document.left(), document.top() - 25);
	/*
	System.out.println("l: " + document.left());
	System.out.println("r: " + document.right());
	System.out.println("t: " + document.top());
	System.out.println("b: " + document.bottom());
	l: 36.0
	r: 559.0
	t: 806.0
	b: 36.0
	*/

	//Rectangle rect2 = new Rectangle(document.top() - 36, document.top() - 36, 559, 1);

	/*
	float l = 36f;
	float r = 36f;
	float t = 559f;
	float b = 2f;

	Rectangle rect2 = new Rectangle(l, r, t, b);

	//Rectangle rect2 = new Rectangle(36, 36, 559, 1);
	rect2.setBorder(Rectangle.BOTTOM);
	rect2.setBorderWidth(0.5f);
	cb.rectangle(rect2);

	//cb.setColorStroke(Color.BLACK);
	*/

	/*
	for (int i = 30; i > 0; i--) {
	    System.err.println((float) i / 10);
	    cb.setLineWidth((float) i / 10);
	    cb.moveTo(36, 806 - (5 * i));
	    cb.lineTo(400, 806 - (5 * i));
	    cb.stroke();
	}
	cb.moveTo(10, 50);
	cb.lineTo(559, 50);
	 */
	cb.setLineWidth(0.5f);
	cb.moveTo(30, 791);
	cb.lineTo(559, 791);

	cb.stroke();

    }

    public void onEndPage(PdfWriter writer, Document document) {
	Rectangle rect = writer.getBoxSize("art");
	ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(this.sbFooter),
		rect.getLeft(), rect.getBottom(), 0);

	/*
	ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Bottom Right"),
		rect.getRight(), rect.getBottom(), 0);
	*/
	PdfContentByte cb = writer.getDirectContentUnder();

	cb.setLineWidth(0.5f);

	cb.moveTo(30, 50);
	cb.lineTo(559, 50);
	cb.stroke();

    }

    @Override
    public void onParagraph(PdfWriter paramPdfWriter, Document paramDocument, float paramFloat) {
	//super.onParagraph(paramPdfWriter, paramDocument, paramFloat);

	System.out.println("paramFloat:" + paramFloat);
	if (paramFloat < 200f) {
	    paramDocument.newPage();

	}

    }

    public void setHeaderText(String header) {
	this.sbHeader = header;
    }

    public void setFooterText(String footer) {
	this.sbFooter = footer;
    }

}