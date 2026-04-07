package it.softre.thip.base.firmadigitale.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.ws.rs.core.Response.Status;

import org.apache.axis.encoding.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.json.JSONArray;
import org.json.JSONObject;

import com.thera.thermfw.base.TimeUtils;
import com.thera.thermfw.base.Trace;
import com.thera.thermfw.persist.CachedStatement;
import com.thera.thermfw.persist.Column;
import com.thera.thermfw.persist.ConnectionManager;
import com.thera.thermfw.persist.Factory;
import com.thera.thermfw.persist.KeyHelper;
import com.thera.thermfw.persist.PersistentObject;
import com.thera.thermfw.util.file.FileUtil;

import it.softre.thip.base.firmadigitale.AssociazioneTipoDocFirma;
import it.softre.thip.base.firmadigitale.DocumentiAttesaFirma;
import it.softre.thip.base.firmadigitale.DocumentiAttesaFirmaTM;
import it.softre.thip.base.firmadigitale.PsnDatiFirmaDigitale;
import it.thera.thip.base.comuniVenAcq.DdtTes;
import it.thera.thip.base.comuniVenAcq.DdtTesTM;
import it.thera.thip.base.documentoDgt.DocumentoDgtOggetto;
import it.thera.thip.base.documentoDgt.DocumentoDigitale;
import it.thera.thip.base.documentoDgt.TipoDocumentoDigitale;
import it.thera.thip.base.generale.PersDatiGen;
import it.thera.thip.base.partner.AnagraficoDiBasePrimroseTM;

/**
 * <h1>Softre Solutions</h1> <br>
 * 
 * @author Daniele Signoroni 04/06/2024 <br>
 *         <br>
 *         <b>71561 DSSOF3 04/06/2024</b>
 *         <p>
 *         Prima stesura.<br>
 * 
 *         </p>
 *         <b>71571 DSSOF3 27/06/2024</b>
 *         <p>
 *         Introduzione stampa del pdf.<br>
 *         Migliorie gestione eccezioni.<br>
 *         </p>
 *         <b>71572 DSSOF3 27/06/2024</b>
 *         <p>
 *         Introduzione gestione CopyNumber.<br>
 *         </p>
 */

public class DocumentiAttesaFirmaService {

	private static DocumentiAttesaFirmaService instance;

	public static DocumentiAttesaFirmaService getInstance() {
		if (instance == null) {
			instance =  new DocumentiAttesaFirmaService() ;//Factory.createObject(DocumentiAttesaFirmaService.class);
		}
		return instance;
	}

	public static final char RAGGRUPPAMENTO_CLIENTE = 'C';
	public static final char RAGGRUPPAMENTO_VETTORE = 'V';

	public static final String CARTELLA_GD = "GD";//Mod.4475
	public static final String CARTELLA_AS = "AS"; //...FIX10998 - DZ

	@SuppressWarnings("unchecked")
	public JSONObject firmaDocumento(String jsonString) {
		JSONObject response = new JSONObject();
		Status status = Status.INTERNAL_SERVER_ERROR;
		try {
			JSONObject body = new JSONObject(jsonString);
			String chiaveDocumentoDaFirmare = (String) body.get("documentId");
			if(chiaveDocumentoDaFirmare.contains(String.valueOf(KeyHelper.VIDEO_KEY_SEPARATOR))) {
				chiaveDocumentoDaFirmare = chiaveDocumentoDaFirmare.replace(String.valueOf(KeyHelper.VIDEO_KEY_SEPARATOR), KeyHelper.KEY_SEPARATOR);
			}
			DocumentiAttesaFirma documentoAttesaFirma = (DocumentiAttesaFirma) DocumentiAttesaFirma.elementWithKey(DocumentiAttesaFirma.class, chiaveDocumentoDaFirmare, PersistentObject.NO_LOCK);
			if(documentoAttesaFirma.getProcessato()) {
				//se per sbaglio e' gia' processato do' errore
				status = Status.INTERNAL_SERVER_ERROR;
			}
			DocumentoDigitale docOri = documentoAttesaFirma.getDocumentodigitale();
			if (documentoAttesaFirma != null && docOri != null) {
				PersDatiGen persDatiGen = PersDatiGen.getPersDatiGen(docOri.getIdAzienda());
				DocumentoDgtOggetto oggetto = (DocumentoDgtOggetto) docOri.getOggetti().get(0);
				String signatureDataUrl = (String) body.get("signature");
				String base64Signature = signatureDataUrl.split(",")[1];
				byte[] signatureBytes = Base64.decode(base64Signature);
				InputStream inputStream = recuperaFileDocumentoDgtOggettoNoSessionOpened(docOri.getIdAzienda(), oggetto);
				PDDocument document = PDDocument.load(readInputStreamToByteArray(inputStream));

				// Remove security from the document
				document.setAllSecurityToBeRemoved(true);

				PDPage page = document.getPage(document.getNumberOfPages() - 1); // Get the last page

				AssociazioneTipoDocFirma associazione = PsnDatiFirmaDigitale.recuperaAssociazioneTipoDocumento(docOri.getIdTipoDocDgt());
				if (associazione == null) {
					// errore grave
					status = Status.INTERNAL_SERVER_ERROR;
				} else if (associazione.getIdTipoDocumento().equals(docOri.getIdTipoDocDgt())) { // se sono diversi
					// allora qualcosa
					// non va!

					// Draw the signature image onto the page
					PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, signatureBytes, "signature");
					float x = associazione.getXPosition().floatValue(); // Example coordinates
					float y = associazione.getYPosition().floatValue(); // Example coordinates
					PDPageContentStream contentStream = new PDPageContentStream(document, page,
							PDPageContentStream.AppendMode.APPEND, true, true);
					contentStream.drawImage(pdImage, x, y, associazione.getWidth().floatValue(),
							associazione.getHeight().floatValue());

					if(associazione.getPosizioneDataFirmaX() != null 
							&& associazione.getPosizioneDataFirmaY() != null) {

						float xData = associazione.getPosizioneDataFirmaX().floatValue();
						float yData = associazione.getPosizioneDataFirmaY().floatValue();

						contentStream.beginText();
						contentStream.setFont(PDType1Font.COURIER, 8);
						contentStream.newLineAtOffset(xData, yData);
						SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH.mm");
						String dataOra = format.format(TimeUtils.getCurrentTimestamp()).toString();
						contentStream.showText(dataOra);
						contentStream.endText();
					}

					contentStream.close();

					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					document.save(byteArrayOutputStream);
					document.close();

					byte[] signedPdfBytes = byteArrayOutputStream.toByteArray();
					DocumentoDgtOggetto oggNew = DocumentoDigitale.creaDocumentoDgtOggetto(docOri,docOri.getTipoDocDgt(), false);
					oggNew.setOnDB(false);
					oggNew.setProgressivo(docOri.getOggetti().size() + 1);
					String filename = oggetto.getFilename();
					if (filename != null && !filename.equals("")) {
						oggNew.setFilename(getNomeAllegato(oggetto.getNomeFilePdf()));
						oggNew.setSSDMimeType(oggetto.getSSDMimeType());
						String descrizione = oggetto.getDescrizione().getDescrizione();
						descrizione = descrizione + " Firmato";
						if (descrizione.length() > 35) {
							descrizione = descrizione.substring(descrizione.length() - 35);
						}
						oggNew.getDescrizione().setDescrizione(descrizione);
						oggNew.getDescrizione().setDescrizioneRidotta(oggetto.getDescrizione().getDescrizioneRidotta());
						if (persDatiGen.getMemorizzazioneDocDgt() == PersDatiGen.MEM_DOC_DGT_SU_DATABASE) {
							oggNew.setBlobBytes(signedPdfBytes);
						}
						oggNew.setCompresso(docOri.getTipoDocDgt().isZipAttachment());
						if (persDatiGen.getMemorizzazioneDocDgt() == PersDatiGen.MEM_DOC_DGT_SU_FILESYSTEM) {
							salvaSuFileSystem(persDatiGen,oggetto,signedPdfBytes,true);
						}
					}
					docOri.getOggetti().add(oggNew);
					int rc = docOri.save();

					// flaggare il documento come processato
					documentoAttesaFirma.setProcessato(true);
					rc = documentoAttesaFirma.save();
					if (rc > 0) {
						status = Status.OK;
						ConnectionManager.commit();
					} else {
						ConnectionManager.rollback();
					}

					// Se sull'associazione tipo documento ho abilitato la stampa e il device ce
					// l'ha specificata allora lancio la stampa del pdf
					if (associazione.isStampaAbilitata() && documentoAttesaFirma.getDevice().getStampante() != null) {

						// Recuper il nome del device
						String printerName = documentoAttesaFirma.getDevice().getStampante().getDevice();

						// Recuper i bytes
						InputStream textStream = new ByteArrayInputStream(signedPdfBytes);

						// Creo il documento da stampare
						DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;

						// Lookup di tutte le stampanti per trovare il mio device
						PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

						PrintService selectedPrintService = null;
						for (PrintService printService : printServices) {
							if (printService.getName().equalsIgnoreCase(printerName)) {
								selectedPrintService = printService;
								break;
							}
						}

						if (selectedPrintService != null) {
							// Lancio effettivo del pdf sul printer device
							int copyNumber = documentoAttesaFirma.getCopyNumber() != null
									? documentoAttesaFirma.getCopyNumber()
											: 0;
							try {
								for (int i = 0; i < copyNumber; i++) {
									textStream = new ByteArrayInputStream(signedPdfBytes); // Reinitialize for each
									// print
									DocPrintJob printJob = selectedPrintService.createPrintJob();
									PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
									printJob.print(new SimpleDoc(textStream, flavor, null), attributes);
									Trace.excStream.println(" --> Softre : Firma documento {" + docOri.getKey()
									+ "}, lancio stampa avvenuto con successo, stampante {" + printerName
									+ "}");
								}
							} catch (PrintException e) {
								Trace.excStream.println(" --> Softre : Firma documento {" + docOri.getKey()
								+ "}, lancio stampa terminato con errori :" + e.getMessage());
							}
						} else {
							Trace.excStream.println(" --> Softre : Firma documento {" + docOri.getKey()
							+ "}, stampante non trovata {" + printerName + "}");
						}
					}
				} else {
					status = Status.INTERNAL_SERVER_ERROR;
					Trace.excStream.println(" --> Softre : Firma documento {" + docOri.getKey()
					+ "}, tipo documento DGT = " + docOri.getIdTipoDocDgt() + ","
					+ " mentre il tipo documento associazione = " + associazione.getIdTipoDocumento());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(Trace.excStream);
		} catch (IOException e) {
			e.printStackTrace(Trace.excStream);
		} catch (Exception e) {
			e.printStackTrace(Trace.excStream);
		}
		response.put("status", status);
		return response;
	}

	public String salvaSuFileSystem(PersDatiGen persDatiGen, DocumentoDgtOggetto oggetto, byte[] buffer, boolean annullaBlob) throws SQLException, IOException {
		if (oggetto.isOnDB())
			oggetto.cancellaAllFile();
		String percorso = percorsoSalvataggioFile(persDatiGen, oggetto, oggetto.getDocumentoDgt(), false);
		String nomeFileCalcolato = oggetto.creaNomeFile();
		File path = new File(percorso);
		if (!path.exists())
			com.thera.thermfw.util.file.FileUtil.makeDirs(percorso);
		File f = new File(percorso+File.separator+nomeFileCalcolato);
		FileOutputStream fileoutputstream = new FileOutputStream(f);
		if(buffer != null)//Fix 24821
			fileoutputstream.write(buffer);
		fileoutputstream.close();
		if (annullaBlob)
			oggetto.getOggettoDigitale().setBytes(null);

		oggetto.salvaPerArchivSost(buffer, nomeFileCalcolato);  //...FIX10998 - DZ

		return percorso;
	}

	public String getNomeAllegato(String path) {
		String str = path;
		if (path.lastIndexOf(".") != -1) {
			str = path.substring(0, path.lastIndexOf("."));
			String extension = path.substring(path.lastIndexOf("."));
			str = str + "_Firmato" + extension;
		}
		return str;
	}

	public JSONObject recuperaDocumentoDaFirmare(String idDevice, String idAzienda) {
		JSONObject infoDocumento = new JSONObject();
		DocumentiAttesaFirma documentoAttesaFirma = recuperaChiaveDocumentoDigitale(idDevice, idAzienda);
		if (documentoAttesaFirma != null) {
			infoDocumento = recuperaJsonDocumentoDaFirmare(documentoAttesaFirma);
		}
		return infoDocumento;
	}

	protected JSONObject recuperaJsonDocumentoDaFirmare(DocumentiAttesaFirma documentoAttesaFirma) {
		JSONObject infoDocumento = new JSONObject();
		String kDocDgt = documentoAttesaFirma.getDocumentodigitaleKey();
		if (kDocDgt != null) {
			try {
				DocumentoDigitale docDgt = (DocumentoDigitale) DocumentoDigitale.elementWithKey(DocumentoDigitale.class, kDocDgt, PersistentObject.NO_LOCK);
				if (docDgt != null) {
					if (docDgt.getOggetti().size() > 0) {
						DocumentoDgtOggetto oggetto = (DocumentoDgtOggetto) docDgt.getOggetti().get(0);
						InputStream inputStream = recuperaFileDocumentoDgtOggettoNoSessionOpened(docDgt.getIdAzienda(), oggetto);
						if(inputStream == null) { //Non ho trovato il file quindi cancello altrimenti i prossimi (che magari hanno il file) non vanno
							int rc = documentoAttesaFirma.delete();
							if(rc > 0) {
								ConnectionManager.commit();
							}else {
								ConnectionManager.rollback();
							}
							infoDocumento.put("file",new byte[1]);
						}else {
							infoDocumento.put("file", Base64.encode(readInputStreamToByteArray(inputStream)));
						}
						infoDocumento.put("chiaveDocumentoDigitale", documentoAttesaFirma.getKey());
					}
				}
			} catch (SQLException e) {
				e.printStackTrace(Trace.excStream);
			} catch (IOException e) {
				e.printStackTrace(Trace.excStream);
			} catch (Exception e) {
				e.printStackTrace(Trace.excStream);
			}
		}
		return infoDocumento;
	}

	@SuppressWarnings("rawtypes")
	protected InputStream recuperaFileDocumentoDgtOggettoNoSessionOpened(String idAzienda, DocumentoDgtOggetto oggetto) throws Exception {
		InputStream inputStream = null;
		PersDatiGen persDatiGen = PersDatiGen.getPersDatiGen(idAzienda);
		if(persDatiGen != null) {
			if (persDatiGen.getMemorizzazioneDocDgt() == PersDatiGen.MEM_DOC_DGT_SU_FILESYSTEM) {
				File f = null;
				String percorso = percorsoSalvataggioFile(persDatiGen,oggetto,oggetto.getDocumentoDgt(),false);
				String nome = oggetto.creaNomeFile();
				if (percorso != null && nome != null) {
					f = new File(percorso+File.separator+nome);
					if (f.exists()) {
						if (!oggetto.isCompresso()) {
							//Mod.4834 - inizio
							if (f.length() == 0)
								return null;
							//Mod.4834 - fine
							return new FileInputStream(f);
						}
						else {
							File ft = new File(percorso+File.separator+nome);
							if (ft.length() == 0)
								return null;
							ZipFile zf = new ZipFile(ft);
							Enumeration e = zf.entries();
							ZipEntry entry;
							if (e.hasMoreElements()) {
								entry = (ZipEntry)e.nextElement();
								InputStream is = zf.getInputStream(entry);
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								byte[] buffer = new byte[32000];
								int i;
								while ((i = is.read(buffer)) > 0)
									baos.write(buffer,0,i);
								is.close();
								zf.close();
								inputStream = new ByteArrayInputStream(baos.toByteArray());
							}
						}
					}
				}
			} else {
				inputStream = oggetto.getOggettoBlob(); //.Legge da db quindi nessun problema
			}
		}
		return inputStream;
	}

	public String percorsoSalvataggioFile(PersDatiGen persDatiGen, DocumentoDgtOggetto oggetto,DocumentoDigitale dgt, boolean archivSost) {
		StringBuffer sb = new StringBuffer();
		String pathPersDatiGen = persDatiGen.getPercorsoMemDocDgt();
		if (pathPersDatiGen != null && !pathPersDatiGen.equals("")) {
			sb.append(pathPersDatiGen);
			if (pathPersDatiGen.charAt(pathPersDatiGen.length()-1) != File.separatorChar)
				sb.append(File.separator);
			//File di = new File(sb.toString());
			if (archivSost){ //...FIX10998 - DZ
				sb.append(File.separator).append(CARTELLA_AS);
			}
			else{ //...FIX11650 - DZ
				sb.append(CARTELLA_GD);
			}
			String pathTipoDocDgt = dgt.getTipoDocDgt().getPercorsoMemDocDgt();
			if (pathTipoDocDgt != null && !pathTipoDocDgt.equals("")) {
				if (pathTipoDocDgt.charAt(0) != File.separatorChar)
					sb.append(File.separator);
				sb.append(pathTipoDocDgt);
				//....Fix 17687 inizio
				//if (persDatiGen.getMemorizzazioneDocDgt() == PersDatiGen.MEM_DOC_DGT_SU_FILESYSTEM || archivSost){ //...FIX16835 - DZ 17301
				char FileSys = persDatiGen.getMemorizzazioneDocDgt();
				try {
					if(!oggetto.isForzaSuFileSystem()){//Fix 21334
						FileSys = (oggetto.getOggettoBlob() == null) ? PersDatiGen.MEM_DOC_DGT_SU_FILESYSTEM : PersDatiGen.MEM_DOC_DGT_SU_DATABASE;
					}
					//Fix 35000 inizio
					char isPersDatiFileSys = persDatiGen.getMemorizzazioneDocDgt();
					if(isPersDatiFileSys == PersDatiGen.MEM_DOC_DGT_SU_FILESYSTEM && oggetto.getOggettoBlob() != null && dgt.getIdVersione() >1)
					{
						FileSys =  PersDatiGen.MEM_DOC_DGT_SU_FILESYSTEM;
					}
					//Fine 35000
				}
				catch (Exception ex) {
					ex.printStackTrace(Trace.excStream);
				}
				if (FileSys == PersDatiGen.MEM_DOC_DGT_SU_FILESYSTEM || archivSost){ //....Fix 17687 fine
					if (pathTipoDocDgt.charAt(pathTipoDocDgt.length()-1) != File.separatorChar)
						sb.append(File.separator);
					//					 if (dgt.getNomeCartella() == null || dgt.getNomeCartella().equals("")) {//Fix 19319
					if (oggetto.getNomeCartella() == null || oggetto.getNomeCartella().equals("")) {//Fix 19319
						String cartella = ultimaCartellaDisponibile(persDatiGen,oggetto, sb.toString(), dgt.getTipoDocDgt(), archivSost);
						if (cartella == null)
							return null;
						sb.append(cartella);
						//						 dgt.setNomeCartella(cartella);//Fix 19319
						oggetto.setNomeCartella(cartella);//Fix 19319
					}
					else
						//sb.append(dgt.getNomeCartella());//Fix 19319
						sb.append(oggetto.getNomeCartella());//Fix 19319
				}
				//Mod.4764 - inizio
				if (!FileUtil.exists(sb.toString()))
					FileUtil.makeDirs(sb.toString());
				//Mod.4764 - fine
				return sb.toString();
			}
			return null;
		}
		return null;
	}

	public String ultimaCartellaDisponibile(PersDatiGen persDatiGen, DocumentoDgtOggetto oggetto,String path, TipoDocumentoDigitale tpDocDgt, boolean archivSost) {
		if (tpDocDgt.getNumeroCartellaMem() == 0) {
			tpDocDgt.setNumeroCartellaMem(tpDocDgt.getNumeroCartellaMem()+1);
			if (!oggetto.salvaTpDocDgt(tpDocDgt))
				return null;
		}
		String cartella = oggetto.cartellaToString(tpDocDgt.getNumeroCartellaMem());
		File f = new File(path+cartella);
		if (!f.exists())
			return cartella;
		if (f.isDirectory()) {
			String[] s = f.list();
			int numeroMaxDocDgt = (persDatiGen.getNumeroMaxDocDgt() == 0) ? 10000 : persDatiGen.getNumeroMaxDocDgt();//17301
			if (s.length < numeroMaxDocDgt)
				return cartella;
			else {
				tpDocDgt.setNumeroCartellaMem(tpDocDgt.getNumeroCartellaMem()+1);
				if (!oggetto.salvaTpDocDgt(tpDocDgt))
					return null;
				return ultimaCartellaDisponibile(persDatiGen,oggetto, path, tpDocDgt, archivSost);
			}
		}
		return "";
	}

	private byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int numRead;
		try {
			while ((numRead = inputStream.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, numRead);
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return byteBuffer.toByteArray();
	}

	public DocumentiAttesaFirma recuperaChiaveDocumentoDigitale(String idDevice, String idAzienda) {
		DocumentiAttesaFirma doc = null;
		String key = null;
		ResultSet rs = null;
		CachedStatement cs = null;
		String stmt = " SELECT " + DocumentiAttesaFirmaTM.ID_AZIENDA + "," + DocumentiAttesaFirmaTM.ID + " " + "FROM "
				+ DocumentiAttesaFirmaTM.TABLE_NAME + " WHERE " + DocumentiAttesaFirmaTM.ID_AZIENDA + " = '" + idAzienda
				+ "' " + "AND " + DocumentiAttesaFirmaTM.R_DEVICE + " = '" + idDevice + "' " + "AND "
				+ DocumentiAttesaFirmaTM.PROCESSATO + " = '" + Column.FALSE_CHAR + "' ORDER BY ID DESC ";
		try {
			cs = new CachedStatement(stmt);
			rs = cs.executeQuery();
			if (rs.next()) {
				key = KeyHelper.buildObjectKey(new String[] { rs.getString(DocumentiAttesaFirmaTM.ID_AZIENDA),
						rs.getString(DocumentiAttesaFirmaTM.ID) });
				doc = (DocumentiAttesaFirma) DocumentiAttesaFirma.elementWithKey(DocumentiAttesaFirma.class, key,
						PersistentObject.NO_LOCK);
			}
		} catch (SQLException e) {
			e.printStackTrace(Trace.excStream);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (cs != null) {
					cs.free();
				}
			} catch (SQLException e) {
				e.printStackTrace(Trace.excStream);
			}
		}
		return doc;
	}

	@SuppressWarnings({ "rawtypes" })
	public JSONObject recuperaListaDocumentiDaFirmareRaggruppati(String idDevice, String idAzienda) throws Exception {
		Vector documenti = documentiAttesaFirmaNonProcessati(idDevice, idAzienda);
		JSONObject jsonDocumenti = new JSONObject();
		JSONObject errors = new JSONObject();
		JSONArray jsonArrayDocumenti = new JSONArray();
		if(documenti.size() > 0) {
			Map<String, DocumentoAttesaFirmaRaggruppato> documentiRaggruppati = (Map<String, DocumentoAttesaFirmaRaggruppato>) new HashMap<String,DocumentoAttesaFirmaRaggruppato>();
			for (Iterator iterator = documenti.iterator(); iterator.hasNext();) {
				DocumentiAttesaFirma doc = (DocumentiAttesaFirma) iterator.next();
				//jsonArrayDocumenti.put(recuperaJsonDocumentoDaFirmare(doc));
				DocumentoDigitale docDgt = doc.getDocumentodigitale();
				if(docDgt != null) {
					ResultSet rs = null;
					CachedStatement cs = null;
					try {
						//DdtTes ddt = null;
						String select = "SELECT "
								+ "	T.R_MOD_SPEDIZIONE, "
								+ "	T.VETTORE1, "
								+ "	T.R_CLIENTE, "
								+ "	T.RAGIONE_SOC_DEN , "
								+ "	A.ANARASOC "
								+ "FROM "
								+ "	THIP.DDT_TES T "
								+ "LEFT OUTER JOIN THIP.FORNITORI_ACQ V "
								+ "ON "
								+ "	V.ID_AZIENDA = T.ID_AZIENDA "
								+ "	AND V.ID_FORNITORE = T.VETTORE1 "
								+ "LEFT OUTER JOIN FINANCE.BBANAPT A "
								+ "ON "
								+ "	A.ANACD = V.R_ANAGRAFICO ";
						String where = "WHERE T."+DdtTesTM.ID_AZIENDA+" = '"+docDgt.getIdAzienda()+"' ";
						where += "AND T."+DdtTesTM.ID_ANNO_DDT+" = '"+docDgt.getAnnoDoc()+"' ";
						where += "AND T."+DdtTesTM.ID_NUMERO_DDT+" = '"+docDgt.getNumeroDoc()+"' ";
						where += "AND T."+DdtTesTM.TIPO_DDT+" = '"+DdtTes.TIPO_DDT_VEN+"' ";
						/*try {
						ddt = DdtTes.elementWithKey(KeyHelper.buildObjectKey(new String[] {
								docDgt.getIdAzienda(),docDgt.getAnnoDoc(),docDgt.getNumeroDoc(),String.valueOf(DdtTes.TIPO_DDT_VEN)
						}), PersistentObject.NO_LOCK);
					}catch (NullPointerException e) {
						// exc su getDescrVettoreFormattata()
					}*/
						//if(ddt != null) {
						cs = new CachedStatement(select+where);
						rs = cs.executeQuery();
						if(rs.next()) {
							String idModSpedizione = rs.getString(DdtTesTM.R_MOD_SPEDIZIONE) != null ? rs.getString(DdtTesTM.R_MOD_SPEDIZIONE).trim() : "";
							String idVettore1 = rs.getString(DdtTesTM.VETTORE1) != null ? rs.getString(DdtTesTM.VETTORE1).trim() : "";
							String idCliente = rs.getString(DdtTesTM.R_CLIENTE) != null ? rs.getString(DdtTesTM.R_CLIENTE).trim() : "";
							String ragioneSocialeDen = rs.getString(DdtTesTM.RAGIONE_SOC_DEN) != null ? rs.getString(DdtTesTM.RAGIONE_SOC_DEN).trim() : "";
							String ragioneSocialeVet = rs.getString(AnagraficoDiBasePrimroseTM.RAGIONE_SOCIALE) != null ? rs.getString(AnagraficoDiBasePrimroseTM.RAGIONE_SOCIALE).trim() : "";
							String c = null;
							if(idModSpedizione != null && idModSpedizione.equals("VE") && idVettore1 != null) {
								c = KeyHelper.buildObjectKey(new Object[] {RAGGRUPPAMENTO_VETTORE,idVettore1});
							}else {
								c = KeyHelper.buildObjectKey(new Object[] {RAGGRUPPAMENTO_CLIENTE,idCliente});
							}
							c = KeyHelper.formatKeyString(c);
							if(documentiRaggruppati.containsKey(c)) {
								DocumentoAttesaFirmaRaggruppato oggetto = documentiRaggruppati.get(c);
								//oggetto.getDdt().add(ddt);
								oggetto.getDocumenti().add(doc);
								oggetto.setCount(oggetto.getCount()+1);
							}else {
								DocumentoAttesaFirmaRaggruppato oggetto = creaOggettoAppoggio(c, doc, null);
								oggetto.setRagioneSocialeDestinatario(ragioneSocialeDen);
								oggetto.setRagioneSocialeVettore(ragioneSocialeVet);
								oggetto.setCount(1);
								documentiRaggruppati.put(oggetto.getChiave(), oggetto);
							}
							//}
							/*} catch (SQLException e) {
						e.printStackTrace(Trace.excStream);
					}*/
						}
					}catch (SQLException e) {
						e.printStackTrace(Trace.excStream);
					}finally {
						try {
							if(rs != null) {
								rs.close();
							}
							if(cs != null) {
								cs.free();
							}
						}catch (SQLException e) {
							e.printStackTrace(Trace.excStream);
						}
					}
				}
			}
			for (Map.Entry<String, DocumentoAttesaFirmaRaggruppato> entry : documentiRaggruppati.entrySet()) {
				String key = entry.getKey();
				DocumentoAttesaFirmaRaggruppato val = entry.getValue();
				JSONObject documento = new JSONObject();

				String ragioneSociale = null;
				char tipo = key.charAt(0);
				switch (tipo) {
				case RAGGRUPPAMENTO_CLIENTE:
					//ragioneSociale = val.getDdt().get(0).getRagioneSocDen();
					ragioneSociale = val.getRagioneSocialeDestinatario();
					break;
				case RAGGRUPPAMENTO_VETTORE:
					ragioneSociale = val.getRagioneSocialeVettore();
					//ragioneSociale = val.getDdt().get(0).getVettore1().getRagioneSociale();
				default:
					break;
				}
				documento.put("ragioneSociale", ragioneSociale);
				documento.put("count", val.getCount());
				documento.put("chiave", key);
				documento.put("tipo",(tipo == RAGGRUPPAMENTO_CLIENTE ? "Cliente" : "Fornitore"));

				List<String> keys = new ArrayList<>();
				for (Iterator iterator = val.getDocumenti().iterator(); iterator.hasNext();) {
					DocumentiAttesaFirma doc = (DocumentiAttesaFirma) iterator.next();
					keys.add(KeyHelper.formatKeyString(doc.getKey()));
				}
				documento.put("docToSignKeys", String.join(",", keys));

				jsonArrayDocumenti.put(documento);
			}
		}else {
			errors.put("message", "Nessun documento in attesa di firma");
		}
		jsonDocumenti.put("documenti", jsonArrayDocumenti);
		jsonDocumenti.put("errors", errors);
		return jsonDocumenti;
	}

	protected DocumentoAttesaFirmaRaggruppato creaOggettoAppoggio(String chiave, DocumentiAttesaFirma documento, DdtTes ddt) {
		DocumentoAttesaFirmaRaggruppato oggetto = (DocumentoAttesaFirmaRaggruppato) Factory.createObject(DocumentoAttesaFirmaRaggruppato.class);
		oggetto.setChiave(KeyHelper.formatKeyString(chiave));
		oggetto.getDocumenti().add(documento);
		//oggetto.getDdt().add(ddt);
		return oggetto;
	}

	@SuppressWarnings("rawtypes")
	public Vector documentiAttesaFirmaNonProcessati(String idDevice, String idAzienda) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
		String where = " "+ DocumentiAttesaFirmaTM.ID_AZIENDA + " = '" + idAzienda
				+ "' " + "AND " + DocumentiAttesaFirmaTM.R_DEVICE + " = '" + idDevice + "' " + "AND "
				+ DocumentiAttesaFirmaTM.PROCESSATO + " = '" + Column.FALSE_CHAR + "'";
		//try {
		return DocumentiAttesaFirma.retrieveList(DocumentiAttesaFirma.class, where, "ID DESC", true);
		/*} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
			e.printStackTrace(Trace.excStream);
		}
		return new Vector();*/
	}

}
