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
package ch.gpb.elexis.kgexporter.handlers;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.elexis.data.Brief;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.omnivore.data.DocHandle;
import ch.gpb.elexis.kgexporter.pdf.PdfHandler;
import ch.gpb.elexis.kgexporter.util.DateUtil;
import ch.gpb.elexis.kgexporter.util.KgExportPreference;
import ch.gpb.elexis.kgexporter.wizard.KgExportWizard;
import ch.rgw.tools.MimeTool;

import com.lowagie.text.DocumentException;

/**
 * Krankengeschichte Export
 * 
 */
public class KgExportHandler extends AbstractHandler {
    protected Patient patient;
    Shell activeShell;
    protected static Logger log = LoggerFactory.getLogger(KgExportHandler.class.getName());

    public KgExportHandler() {
    }

    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
	IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
	activeShell = window.getShell();

	// init the selection
	ISelection selection =
		HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
	if (selection instanceof IStructuredSelection) {

	    IStructuredSelection strucSelection = (IStructuredSelection) selection;
	    Object selected = strucSelection.getFirstElement();
	    if (selected instanceof Patient) {
		patient = (Patient) selected;
		String msg = "Exporting Patient with ID: " + patient.getId();
		final KgExportWizard kgExportWizard = new KgExportWizard();

		WizardDialog wizardDialog = new WizardDialog(activeShell, kgExportWizard);
		wizardDialog.setTitle(msg);

		if (wizardDialog.open() == Window.OK) {
		    CoreHub.userCfg.set(KgExportPreference.PREF_KGEXPORT_FOOTERTEXT, kgExportWizard.getFooterText());

		    UiDesk.asyncExec(new Runnable() {
			public void run() {
			    export(kgExportWizard.getExportPath(), kgExportWizard.getSelectedCategories(),
				    kgExportWizard.getFooterText());

			}
		    });
		}

	    }
	}

	return null;
    }

    private void export(String exportPath, HashMap<String, Boolean> selectedCategories, String footerText) {

	// EXPORT DIAGNOSELISTE
	if (selectedCategories.get(KgExportWizard.EXPORTCATEGORY_DIAGNOSEPROBLEMLISTE)) {

	    try {
		File fDiagnosen = new File(exportPath, "Diagnosen " + patient.getName() + " "
			+ patient.getVorname() + ".pdf");

		PdfHandler.createDiagnosenSheet(patient, fDiagnosen.getAbsolutePath(), footerText);

	    } catch (DocumentException e) {
		log.error("error while creating Medikationsliste pdf: " + e.getMessage());
		//System.out.println("error while creating Medikationsliste pdf: " + e.getMessage());
	    } catch (IOException e) {
		log.error("error while creating Medikationsliste pdf: " + e.getMessage());
		//System.out.println("error while creating Medikationsliste pdf: " + e.getMessage());
	    }
	}

	// EXPORT BRIEFE
	// get all letters (only the general ones)
	// Date and Betreff are not always unique, so we put a number to the filename.
	if (selectedCategories.get(KgExportWizard.EXPORTCATEGORY_BRIEFE)) {

	    int count = 1;
	    List<Brief> briefe = loadBriefe(false);
	    for (Brief brief : briefe) {
		//System.out.println("brief:" + brief.getDatum() + " / " + brief.getBetreff());
		byte[] bBrief = brief.loadBinary();
		FileOutputStream fOut = null;
		File subdirBriefe = new File(exportPath, "Briefe");
		subdirBriefe.mkdir();

		try {

		    String ext;
		    if (brief.getMimeType().startsWith("doc")) {
			ext = brief.getMimeType();
		    } else {
			ext = MimeTool.getExtension(brief.getMimeType());
		    }

		    if (ext.length() == 0) {
			System.out
				.println("TextView.openDocument no extension found for mime type: " + brief.getMimeType() + "  Titel:" + brief.getBetreff()); //$NON-NLS-1$
			ext = "odt";
		    }

		    File fDestBrief = new File(subdirBriefe, String.valueOf(count++) + "-" + brief.getBetreff().trim()
			    + "." + ext);
		    fOut = new FileOutputStream(fDestBrief);
		    fOut.write(bBrief);

		    String dateBrief = brief.getDatum();
		    //System.out.println("datum / mimetype: " + dateBrief + " / " + brief.getMimeType());
		    Date dBrief = DateUtil.getDateFromGermanFormat(dateBrief);

		    Date now = new Date();
		    BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(fDestBrief.getPath()),
			    BasicFileAttributeView.class);
		    FileTime timeCreated = FileTime.fromMillis(dBrief.getTime());
		    FileTime timeMod = FileTime.fromMillis(now.getTime());
		    attributes.setTimes(timeCreated, timeMod, timeCreated);

		} catch (Exception e) {
		    log.error("Fehler beim speichern eines Briefs: (ID, Betreff: " + brief.getId() + ", "
			    + brief.getBetreff() + ")" + e.getMessage());
		    //System.out.println("Fehler beim speichern eines Briefs: (ID, Betreff: " + brief.getId() + ", "
		    //    + brief.getBetreff() + ")" + e.getMessage());
		} finally {
		    close(fOut);
		}

	    }
	}

	// EXPORT LABORBLATT
	if (selectedCategories.get(KgExportWizard.EXPORTCATEGORY_LABORBLATT)) {

	    LinkedList<String[]> laborBlatt = getLaborblatt(patient);
	    File pdfFile = new File(exportPath, "Laborblatt " + patient.getName() + " " + patient.getVorname() + ".pdf");

	    try {
		PdfHandler.createLaborwertTable(patient, pdfFile.getAbsolutePath(), laborBlatt, footerText);
	    } catch (IOException e) {
		log.error("error while creating laborblatt pdf: " + e.getMessage());
		//System.out.println("error while creating laborblatt pdf: " + e.getMessage());
	    } catch (DocumentException e) {
		log.error("error while writing laborblatt pdf: " + e.getMessage());
		//System.out.println("error while writing laborblatt pdf: " + e.getMessage());
	    }
	}

	// EXPORT MEDIKATIONSLISTE
	if (selectedCategories.get(KgExportWizard.EXPORTCATEGORY_MEDIKATIONSLISTE)) {
	    try {
		File fMedikationsliste = new File(exportPath, "Medikationsliste " + patient.getName() + " "
			+ patient.getVorname() + ".pdf");

		PdfHandler.createFixMediSheet(patient, fMedikationsliste.getAbsolutePath(), footerText);

	    } catch (DocumentException e) {
		log.error("error while creating Medikationsliste pdf: " + e.getMessage());
		//System.out.println("error while creating Medikationsliste pdf: " + e.getMessage());
	    } catch (IOException e) {
		log.error("error while creating Medikationsliste pdf: " + e.getMessage());
		//System.out.println("error while creating Medikationsliste pdf: " + e.getMessage());
	    }
	}

	// EXPORT OMNIVORE DOKUMENTE
	// read all selected omnivore documents and store them in  folders corresponding to their category
	// TODO: sollten die omniv. docs auch nach mandant gelesen werden?
	List<DocHandle> docList = loadOmnivoreDocs(patient);

	List<DocHandle> catList = DocHandle.getMainCategories();

	for (DocHandle item : catList) {
	    ////System.out.println("Cat title: " + /*item.getCategory() + "/" + */item.getTitle());
	    List<DocHandle> docsOfCategory = getOmnivoreDocsOfCategory(docList, item.getTitle());

	    if (docsOfCategory.size() > 0) {
		// create directory

		// get docs and put them in the newly created folder
		for (DocHandle docHandle : docsOfCategory) {
		    Boolean sSel = selectedCategories.get(docHandle.getCategory());

		    if (sSel) {

			String sSubDir = docHandle.getCategory();
			sSubDir = sSubDir.replaceAll("/", "_");
			sSubDir = sSubDir.replaceAll("\\\\", "_");

			File subdirExport = new File(exportPath, sSubDir);
			if (!subdirExport.exists()) {
			    subdirExport.mkdir();
			}

			String fileName = docHandle.getTitle();
			String ext = docHandle.getMimetype().substring(docHandle.getMimetype().lastIndexOf("."));

			// if the filename doesn't end with the same ext anyway, then replace with the one from the mimetype
			if (!docHandle.getTitle().endsWith(ext)) {
			    fileName += ext;
			}
			fileName = sanitizeFilename(fileName);

			//System.out.println("title:\t" + docHandle.getTitle());
			//System.out.println("mimetype:\t" + docHandle.getMimetype());
			//System.out.println("filename:\t" + fileName);

			File fExport = new File(subdirExport, fileName);

			try {
			    docHandle.storeExternal(fExport.getAbsolutePath());
			} catch (Exception e) {
			    log.error("Error storing omnivore doc id: (" + docHandle.getId() + ") "
				    + docHandle.getMimetype() + " / " + e.getMessage());
			    //System.out.println("Error storing omnivore doc id: (" + docHandle.getId() + ") "
			    //    + docHandle.getMimetype() + " / " + e.getMessage());
			}
		    }

		}
	    }
	}

    }

    public static void close(Closeable c) {
	if (c == null)
	    return;
	try {
	    c.close();
	} catch (IOException e) {
	    //log the exception
	}
    }

    private LinkedList<String[]> getLaborblatt(Patient patient) {
	final LaborResultsComposite resultsComposite = new LaborResultsComposite(activeShell, 0);
	resultsComposite.selectPatient(patient);

	String[] header = resultsComposite.getPrintHeaders();
	int[] skipColumnsIndex = resultsComposite.getSkipIndex();
	TreeItem[] rows = resultsComposite.getPrintRows();

	LinkedList<String[]> usedRows = new LinkedList<String[]>();

	if (rows == null || rows.length == 0) {
	    //usedRows.add(new String[] { "Keine Laborwerte vorhanden" });
	    //System.out.println("Keine Laborwerte vorhanden für Patient: " + patient.getWrappedId() + "/"
	    //	    + patient.getName());
	    return usedRows;
	}

	Tree tree = rows[0].getParent();
	int cols = tree.getColumnCount() - skipColumnsIndex.length;
	int[] colsizes = new int[cols];
	float first = 25;
	float second = 10;
	if (cols > 2) {
	    int rest = Math.round((100f - first - second) / (cols - 2f));
	    for (int i = 2; i < cols; i++) {
		colsizes[i] = rest;
	    }
	}
	colsizes[0] = Math.round(first);
	colsizes[1] = Math.round(second);

	usedRows.add(header);
	for (int i = 0; i < rows.length; i++) {
	    boolean used = false;
	    String[] row = new String[cols];
	    for (int j = 0, skipped = 0; j < cols + skipped; j++) {
		if (skipColumn(j, skipColumnsIndex)) {
		    skipped++;
		    continue;
		}
		int destIndex = j - skipped;
		row[destIndex] = rows[i].getText(j);
		if ((destIndex > 1) && (row[destIndex].length() > 0)) {
		    used = true;
		    // break;
		}
	    }
	    if (used == true) {
		usedRows.add(row);
	    }
	}

	return usedRows;

    }

    private boolean skipColumn(int index, int[] skip) {
	for (int i : skip) {
	    if (index == i) {
		return true;
	    }
	}
	return false;
    }

    private List<Brief> loadBriefe(boolean excludeCst) {

	if (patient != null) {

	    Query<Brief> qbe = new Query<Brief>(Brief.class);
	    qbe.add(Brief.FLD_PATIENT_ID, Query.EQUALS, patient.getId());

	    if (excludeCst) {
		// TODO: the crit. should be configurable/dynamic
		qbe.add(Brief.FLD_SUBJECT, Query.NOT_EQUAL, "%CST%");
	    }
	    qbe.orderBy(false, Brief.FLD_DATE);

	    List<Brief> listResult = new ArrayList<>();
	    List<Brief> list = qbe.execute();
	    for (Brief brief : list) {
		listResult.add(brief);
	    }

	    return listResult;
	}

	return new ArrayList<Brief>();
    }

    private List<DocHandle> getOmnivoreDocsOfCategory(List<DocHandle> list, String category) {
	List<DocHandle> ret = new ArrayList<DocHandle>();
	for (DocHandle item : list) {
	    if (item.getCategory().equals(category)) {
		ret.add(item);

	    }
	}

	return ret;
    }

    public static List<DocHandle> loadOmnivoreDocs(Patient patient) {

	if (patient == null) {
	    ArrayList<DocHandle> emptyList = new ArrayList<DocHandle>();
	    return emptyList;
	}

	List<DocHandle> ret = new LinkedList<DocHandle>();

	Query<DocHandle> qbe = new Query<DocHandle>(DocHandle.class);
	qbe.add(DocHandle.FLD_PATID, Query.EQUALS, patient.getId());
	//qbe.add(DocHandle.FLD_CAT, Query.EQUALS, cat);
	qbe.orderBy(false, new String[] { DocHandle.FLD_CAT, DocHandle.FLD_DATE, DocHandle.FLD_TITLE });

	List<DocHandle> root = qbe.execute();

	ret.addAll(root);

	return ret;
    }

    /**
     * Eliminate characters illegal in windows filenames
     * @param name
     * @return
     */
    public String sanitizeFilename(String name) {
	return name.replaceAll("[:\\\\/*?|<>]", "_");
    }

}
