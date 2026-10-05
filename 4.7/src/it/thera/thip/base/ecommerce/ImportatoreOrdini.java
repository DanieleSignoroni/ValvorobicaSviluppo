package it.thera.thip.base.ecommerce;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import com.thera.thermfw.base.*;
import com.thera.thermfw.batch.*;
import com.thera.thermfw.batchload.*;//Fix 20813
import com.thera.thermfw.cbs.*;
import com.thera.thermfw.collector.*;
import com.thera.thermfw.common.*;
import com.thera.thermfw.gui.ScreenData;
import com.thera.thermfw.persist.*;
import com.thera.thermfw.security.*;
import com.thera.thermfw.server.*;
import com.thera.thermfw.type.DateType;
import com.thera.thermfw.type.DecimalType;
import com.thera.thermfw.web.MDVManager;

import it.thera.thip.base.agentiProvv.*;
import it.thera.thip.base.articolo.*;
import it.thera.thip.base.azienda.*;
import it.thera.thip.base.cliente.*;
import it.thera.thip.base.comuniVenAcq.*;
import it.thera.thip.base.documenti.*;
import it.thera.thip.base.generale.*;
import it.thera.thip.cs.*;
import it.thera.thip.datiTecnici.configuratore.*;
import it.thera.thip.magazzino.saldi.*;
import it.thera.thip.magazzino.generalemag.Lotto;   //MG FIX 12472
import it.thera.thip.vendite.documentoVE.DocumentoVendita;
import it.thera.thip.vendite.generaleVE.*;
import it.thera.thip.vendite.ordineVE.*;
import it.thera.thip.vendite.ordineVE.web.*;
import it.thera.thip.base.documentoDgt.TipoDocumentoDigitale;
import it.thera.thip.vendite.sellMore.*;//Fix 20813
import it.thera.thip.base.wpu.admin.*;//Fix 20813
import it.thera.thip.base.documentoDgt.DescrittoreStampaDgt;
import it.thera.thip.base.documentoDgt.DocumentoDigitale;
import it.thera.thip.base.partner.DatiAnagrafici;  //MG FIX 12775
import it.thera.thip.base.partner.Indirizzo;


/**
 * <p>Title: Omnibusiness 1.4 std - Azienda di rif.: svil</p>
 * <p>Description: Progetto Omnibusiness 1.4 Standard Edition</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Thera SpA</p>
 * @author EP 22/10/2003
 * @version 1.0
 */
/*
 * Revisions:
 * Number  Date         Owner    Description
 *         22/10/2003   EP       Prima stesura
 * 02200   01/07/2004   ME       Modificate parecchie cose sia a livello di
 *                               struttura della classe che a livello logico
 * 02263   19/07/2004   PM       Migliorata gestione errori
 * 02317   04/08/2004   EP       Aggiunti controlli sui dati x Genova
 * 02326   23/08/2004   MP       Se la descrizione aggiuntiva dell'articolo non
 *                               è specificata viene impostata quella dell'articolo.
 * 02363   01/09/2004   ME       Controllato il settaggio su ordine di alcuni dati
 * 02372   06/09/2004   ME       Aggiunto metodo salvaTestata, aggiunto nuovo
 *                               messaggio in fase di importazione e modificata asseganzione del
 *                               campo di fatturazione cliente della testata dell'ordine
 * 2431   15/09/2004    PM       Migliorato il modo in cui vengono assegnate le condizioni di vendita
 * 02436  15/09/2004    ME       Sistemate alcune NullPointerException nel
 *                               metodo impostaCondizioniVendita
 * 02439  15/09/2004    ME       Sistemata la gestione degli attributi di servizio
 *                               della provenienza prezzo e del riferimento UM prezzo
 * 02486  23/09/2004    PM       Introdotto metodo assegnaQuantitaOrdine che assegna le quantita
 *                               della riga ordine.Il metodo è protetto in modo che possa essere
 *                               ridefinito.
 * 02489  23/09/2004    ME       Gestione stampa conferma ordine
 * 02521  29/09/2004    ME       Eliminato settaggio inutile del campo GWTOR00F.TORU01
 * 02533  30/09/2004    ME       Aggiunti controlli per presenza righe negli
 *                               ordini da importare
 * 02539  01/10/2004    ME
 * 02589  11/10/2004    ME       Sistemata gestione dei campo sconto fine fattura
 *                               e campi con descrizione doppia
 * 02603  12/10/2004    ME
 * 02629  14/10/2004    ME       Aggiustati i settaggi di tutti i campi con descrizione doppia
 * 02647  18/10/2004    ME       Aggiunta rollback nella finally del metodo esegueStampaConfermaOrdini
 * 02678  20/10/2004    ME       Modificato test su riferimento u.m. prezzo
 *                               e ciclo stampa conferma ordini
 * 02701  20/10/2004    ME       Aggiunto metodo protetto assegnaCondizioniVenditaDaConnettore
 *                               per rendere facile la personalizzazione
 * 02707  25/10/2004    ME       Modificata gestione stampe conferma ordine
 * 02786  05/11/2004    ME       Aggiunto metodo assegnaSequenzaRiga e gestione righe omaggio
 * 02793  04/11/2004    PM       Aggiunto controllo che se il maggazzino, nel connettore, non è valorizzato
 *                               viene lasciato impostato quello che deriva da causale.
 *                               Inoltre al processo di stampa della conferme ordine vioene impostata
 *                               la stessa coda su cui viene eseguito il lavoro di importatazione
 *                               degli ordini.
 *                               Infine migliorata messaggistica nel caso di errori.
 * 02812  10/11/2004    ME       Creata variabile d'istanza per le condizioni di
 *                               vendita e per i nomi degli HDR
 * 02999  15/12/2004    ME       Varie modifiche
 * 03054  29/12/2004    PM       Non veniva gestito correttamente lo scale delle quantita delle righe ordine
 * 03465  26/03/2005    SL       Aggiunto metodo per gestire la condizione di where nel metodo importa(...).
 * 03557  07/04/2005    FCrosa   Classe che da personalizzazione è entrata nello standard
                                 Modicato il modo in cui vengono costriuti i filtri
 * 03893  08/06/2005    PM       per la stampa degli ordini importati
 * 04034  27/06/2005    SL       Aggiunto metodo che distingue se gli errori si riferiscono
 *                               agli oridini vendita(testate/righe) o ai documenti vendita(testate/righe).
 * 04206  01/08/2005    PM       Imposto lo stesso articolo cliente nelle righe ordine che hanno lo stesso articolo
 * 04214  02/08/2005    PM       Quando viene impostata la modalita di pagamento
 *                               deve essere impostato anche lo sconto di fine
 *                               fattura recuperata dalla modalità.
 * 04358  21/09/2005    ME       Aggiunto controllo in preparaStampaConfermaOrdini
 * 04386  28/09/2005    ME       Gestione multidatabase
 * 04472  13/10/2005    ME       Modificata nuovamente gestione stampa conferme ordine
 * 05200  17/03/2006    ME       Sistemata gestione del campo Tipo Evasione Ordine
 * 05288  06/04/2006    ME       Ricerca prezzo da tabella connettori nel caso in cui il flag
 *                               'Riassegna prezzi/sconti' è abilitato e non viene trovato nulla
 *                               nei dati di vendita. Ripulito il codice da tutti i blocchi commentati.
 * 06138  27/10/2006    PM       In caso di dato non significativo nella colonna
 *                               Numero ordine cliente e data ordine cliente devono
 *                               riportare il valore null e non una stringa vuota ""
 * 06505  11/01/2007    PM       Modificata l'impostazione del filtro in modo che
                                 utilizzi la nuova impostazione 'Anno/Numero Ordine'
 * 06411  16/01/2007    LP       Aggiunto gestione filtri per esclusione
 * 06554  05/02/2007    Cha@     Aggiunto gestione divisione
 * 05518  21/03/2007    LP       Aggiunta possibilità di indicare la fonte origine
 * 07212  14/05/2007  Chakhari   Aggiunta possibilità di indicare il codice esterno della configurazione
                                 in altarnativa al codice fisico
 * 08911  21/03/2008  EP				 Cambiato il metodo di reperimento delle condizioni di vendita
 * 															 per il corretto funzionamento di una personalizzazione.
 * 09321  03/06/2008  DZ         esegueStampaConfermaOrdini:
 *                               impostati flag per generazione docDgt e docSSD.
 * 09815  24/09/2008  PM         Devono essere importati solo gli ordini con
 *                               stato ordine 1-(Richiesta) o 2-(Richeista web)
 * 09786  17/09/2008  DBot       Aggiunta la gestione dei "null" nelle condizioni di filtro
 * 09913  14/19/2008  PM         Correzione della fix 9815: La fix 9815 Tale fix metteva a null
 *                               l'anagrafico di base sulla testata dell'ordine
 * 10718  20/04/2009  DZ         assegnaQuantitaOrdine: aggiunti test per prevenire nullPointer
 *                               nel caso in cui le UM indicate siano errate.
 * 10955   17/06/2009  Gscarta   modificate chiamate a convertiUM dell'articolo per passare la versione
 * 11123   17/07/2009  DB
 * 11375   23/09/2009  GScarta   integrazione alla 10955
 * 12472   05/05/2010   MG       rivista gestione qtà
 * 12775   15/06/2010   MG       aggiunta stampa errori
 * 13211   07/10/2010   OC       Aggiunto due paramatri umSecMag e quaSecMag del
 *                               metodo getCondizioniVendita
 * 13700   01/02/2011   BW       correzione del variabile param del metodo importa()
 * 14857   26/09/2011   AYM      dopo l importazione dei ordini di vendita aggiungere nella  "Righe Ordini Cliente " i dati relativi al ordine craeto
                                (campi anno Ordine , numero ordine,numero riga ordine ,riga det ordine).
 * 14727   27/07/2011   RA       Add gestione DescrizioneExtArticolo

 * 15529   27/12/2011   PM       Fix tecnica per ripristinare nei log il messaggio del motivo per cui
 *                               in documento non viene importato
 * 15658   28/02/2012   AYM      elimina l'ordine vendita creato " in caso di errore" .(esempio :nel  caso di fido Estratto)
 * 16029   11/04/2012   AYM      Correga l'impostazione provvigione sui cliente.
 * 19768   09/05/2014   PM       Modifica di ottimizzazione.
 * 20445  06/11/2014    PM       Aggiunto indirizzo di spedizione
 * 18914   18/12/2013   RA       Gestione articolo inesitente
 * 20813   12/12/2014   AYM      Gestione del cliente non presenta in panthera .
 * 21076   21/02/2015   PM       Gestione del cliente non presenta in panthera .
 * 21122   09/04/2015   PM       gestione cambio
 * 22066   31/07/2015   AYM      Correga l'importazione di commenti.
 * 22463   10/11/2015   AYM      Aggiungere la gestione di commenti in lancio da sellMore
 * 22466   04/11/2015   PM       Per gli articoli configurati se la configurazione non è valorizzata imposto la configurazione standard 
 * 22768   05/01/2016   PM       ...
 * 22828   05/01/2016   PM       Se durante l'importazione di un ordine c'è un errore applicativo su una riga e il testo dell'errore supera i 50 caratteri, l'importazione ordine termina con errore.
 * 23100   01/03/2016   PM       Non era gestita correttamente la configurazione nella ricerca prezzi
 * 24273   03/09/2016   LTB      Modifiche per agevolare le personalizzazioni.
 * 24574   28/11/2016   AYM      Vari modificazione .    
 * 24715   16/01/2017   AYM      Aggiunto gestione di  l'ordinamento  nella stampa conferma rdine
 * 26033   29/06/2017   AYM      Corregga l'inserimento nella tabella CM  di WpuRubricaContatti per evitare il problema di "duplicate value for index key " .
 * 26743   14/12/2017   PM       Gestione dei campi Cellulare e Sito Web
 * 26993   15/02/2018   Linda    Trocare il messaggio di errore a 1024 caratteri.
 * 27814   06/08/2018   PM       Gestione dei campi ABI, CAB e IBAN
 * 28105   19/10/2018   LTB      L'importazione ordini da e-commerce non importa correttamente l'aliquota IVA sulle voci di spesa.
 * 28235   14/11/2018   MBH      Sistemare il metodo inserireClienteNellaCM secondo il massimo numero caratteri accettabile dal WpuRubricaContatti
 * 28968   07/03/2018   PM       Se importo un ordine ecommerce in cui non ho ne cab ne abi valorizzati, l'ordine viene creato impostando il flag codice SIA a false, mentre se lo si crea da panthera il flag è a true
 * 29197   21/05/2019  GScarta   Gestione sconti per provvigioni
 * 30979   25/03/2020  Jackal    Fix tecnica per personalizzazione
 * 30871   09/03/2020   SZ		 6 Decimali.
 * 31669   23/07/2020   SZ		 Settare in CM_WPU_RUBRICA_CONTATTI .ID_IND_PR_SIST_EST il valore di GWTOR00F.ID_IND_PR_SIST_EST 
 * 33132   19/03/2021  YBA       Modificare nel metodo impostaCondizioniVenditaDaConnettore(OrdineVenditaTestata, OrdineVenditaRigaPrm, Gwror00f, CondizioniDiVendita), per impostare sulla riga ordine lo sconto articolo 2 nello stesso modo in cui viene impostato lo sconto articolo 1.
 * 33460   23/04/2021	FB		 Aggiunta gestione warnings
 * 35787   06/05/2022   YBA      Aggiunta il metodo esisteBanca
 * 35802   09/05/2022   YBA      Aggiungere la gestione dello sconto finale fattura 
 * 36339   20/07/2022   SZ		 Nel connettore impostare lo sconto zero e non a null
 * 36974   098/11/2022  PM       La creazione dei contatti walcomepage non funziona più durante l'importazione dei ordini da sellmore
 * 38861   31/05/2023   SZ		 Nella stampa che viene prodotta vengano riportiti gli warning del fido.
 * 39218   05/07/2023   PM		 Dopo l'installazione della fix 38861 in alcuni casi l'importattore ordini ecommerce termina con errore
 * 40013   12/10/2023   FB		 Ganci per personalizzazioni
 * 41042   22/01/2024   PM		 Ganci per personalizzazioni
 * 41100   25/01/2024   TA       Valorizzare DESCR_ESTESA
 * 41245   06/02/2024   TA       Considerare gli errori forzabile come di avvisi
 * 42678   20/06/2024	AT		 Fix Problema Descrizione Estesa ordini e documenti e-commerce
 * 42785   02/07/2024	AM		 Ganci per personalizzazioni. 
 * 42893   15/07/2024	AT       Correzione importazione provvigione2 del subagente
 * 43525   08/10/2024   SZ		 Usare il cliente  dati destinatario
 * 47380   16/10/2025   LTB      Introdurre un parametro per pilotare l'attivazione del ricalcolo provvigione2Agenti
 * 47377   20/10/2025   SZ		 Aggiungere il impostazioni per importatore ordine cliente
 * 47909   02/12/2025	BN	     Aggiunto nuovo metodo scattoAutomaticoWorkflow(.,.).
 * 48867   04/03/2026	BN		 modificare e method checkNumeroRigheImportate.
 * 48821   09/03/2026   BN	     Aggiunto calcolo del qta Rif
 * 49250   07/04/206	BN		 correcta aggorna del Doc AI in caso flag eliminare dopo importazione e attivo.
 * 49800   03/06/2026   SZ		 Se si sono errori sul documento, nelle righe dice "Errore Non significativo" ma invece dentro c’è l’errore (che poi sia non significativo...)
 * 50341   21/07/2026	BN		 Cirrecta attrubuti valori length
*/

public class ImportatoreOrdini extends AbstractImportatore {

//MG FIX 12775 inizio 
  protected int iReportNr;
  protected int iRigaJobId = 0;
  protected int iNumeroOrdiniDaImportare = 0;
//MG FIX 12775 fine

  protected PrintWriter iOutputBatch = null;

  protected int iNumeroRigheDaImportare = 0;
  protected int iBatchJobId = 0;
  protected Gwmso00f gwmso00f = null;
  protected Gwmso00fCli gwmso00fOrdineCliente = null;//Fix 47377

  //Fix 2489 - inizio
  protected Map mappaOrdiniDaStampare = new HashMap();
  protected String prop = "it/thera/thip/base/ecommerce/resources/ImportatoreOrdini";

  //Fix 2489 - fine

  //Fix 02793 - inizio
  protected String iBatchQueueId = "";

  //Fix 02793 - fine

  //Fix 02812 - inizio
  protected CondizioniDiVendita iCondVendita;
  protected String iNomeHdrTestata;
  protected String iNomeHdrRighe;

  //Fix 02812 - fine

  //Fix 2999 - inizio
  public static final int SCALE_QTA = 2;

  //Fix 2999 - fine

  //Fix 04034 SL - inizio
  protected static final String ORDINE = "0";
  protected static final String DATA_ORIGIN= "SELLMORE";//Fix 20813
  protected static final int RUN_ID= 0;//Fix 20813
  protected boolean iErroriPresenti = false;

  protected boolean iNuoviClientiInCM = false;
  //Fix 04034 SL - fine


  //Fix 4206 PM - Inizio
  protected Map iMapArtCliente = new HashMap();

  //Fix 4206 PM - Fine

  /**
   * Attributo iCodice
   */
  protected String iCodice; //...FIX 5518

  /**
   * Attributo iSistemaVenBanco
   */
  protected char iSistemaVenBanco; //...FIX 5518

 public int iRowId =0;//20813
//Fix 20813 inizio
	static String SELECT_CLI_SIST_EST = "SELECT   "+WpuRubricaContattiTM.ID +" FROM "+WpuRubricaContattiTM.TABLE_NAME+ 
			                                         " WHERE " +WpuRubricaContattiTM.R_AZIENDA + "= ? AND "
			                                                   +WpuRubricaContattiTM.ID_CLI_SIST_EST + "= ? ";
			                                         
	static CachedStatement cClassifCliSistEst = new CachedStatement(SELECT_CLI_SIST_EST);
	
	static String SELECT_CLI_SIST_EST_CM = "SELECT    COUNT(*)  FROM  "+ 
	                                                  SystemParam.getSchema("THIP")+ CMWpuRubricaContatti.CM_TABLE_NAME+
                                                                 " WHERE " +WpuRubricaContattiTM.R_AZIENDA + "= ? AND "
                                                                   +WpuRubricaContattiTM.ID_CLI_SIST_EST + "= ? ";
      
  static CachedStatement cClassifCliSistEstCM = new CachedStatement(SELECT_CLI_SIST_EST_CM);
  
	static String SELECT_ANAG = "SELECT   "+ WpuRubricaContattiTM.ID_ANAG_BASE +" FROM "+WpuRubricaContattiTM.TABLE_NAME+ 
                          " WHERE " +WpuRubricaContattiTM.R_AZIENDA + "= ? AND "
                           +WpuRubricaContattiTM.ID + "= ? ";
      
  static CachedStatement cAnaga = new CachedStatement(SELECT_ANAG);
  
	static String SELECT_CLI = "SELECT  "+ ClientePrimroseTM.ID_CLIENTE +" FROM "+ClientePrimroseTM.TABLE_NAME+ 
      " WHERE " +ClientePrimroseTM.ID_AZIENDA + "= ? AND "
                +ClientePrimroseTM.R_ANAGRAFICO + "= ? ";
      
  static CachedStatement cCli= new CachedStatement(SELECT_CLI);
  
	static String SELECT_MAX_ROW_ID = "SELECT   MAX("+ CMWpuRubricaContatti.ROW_ID +") FROM "+ 
	                                                         SystemParam.getSchema("THIP")+ CMWpuRubricaContatti.CM_TABLE_NAME+
                                              " WHERE " +CMWpuRubricaContatti.DATA_ORIGIN + "= ? AND "
                                               +  CMWpuRubricaContatti.RUN_ID+ "= ? ";
      
  static CachedStatement cMaxRowId = new CachedStatement(SELECT_MAX_ROW_ID);
  
  //Fix 47377 ini 
  protected static CachedStatement NUM_SERIE_CAU_ASS_STATEMENT = new CachedStatement(
	      "SELECT S."+SerieTM.ID_SERIE+" FROM "+SystemParam.getSchema("THIP") + "NUM_SERIE_CAU_ASS S WHERE S." + CollegamentoSerieCausaliTM.ID_AZIENDA + " = ? AND S." +
	      CollegamentoSerieCausaliTM.ID_NUMERATORE + " = ? AND S.ID_CAU_DOCUMENTO  = ? ");
  //Fix 47377 fini

  public static final String INSERT_WPU_CONT = "INSERT INTO "+
                                                  SystemParam.getSchema("THIP")+ CMWpuRubricaContatti.CM_TABLE_NAME
                                                  +"("+"DATA_ORIGIN ,RUN_ID, ROW_ID, RUN_ACTION, TRASF_STATUS, ID,"
                                                  + WpuRubricaContattiTM.ID_CLI_SIST_EST+","
                                                  + WpuRubricaContattiTM.CONTATTO+","
                                                  + WpuRubricaContattiTM.AZ_RAGIONE_SOCIALE+","
                                                  +WpuRubricaContattiTM.AZ_PARTITA_IVA+","
                                                  + WpuRubricaContattiTM.AZ_INDIRIZZO+","
                                                  +WpuRubricaContattiTM.AZ_LOCALITA+","
                                                  + WpuRubricaContattiTM.AZ_CAP+","
                                                  +WpuRubricaContattiTM.AZ_PROVINCIA+","
                                                  + WpuRubricaContattiTM.TELEFONO +","
                                                  + WpuRubricaContattiTM.FAX+","
                                                  + WpuRubricaContattiTM.EMAIL+","
                                                  +WpuRubricaContattiTM.CODICE_AGENTE+","
                                                  + WpuRubricaContattiTM.SITO_INTERNET+","
                                                  +WpuRubricaContattiTM.CODICE_ZONA+","
                                                  + WpuRubricaContattiTM.AZ_NAZIONE+","
                                                  + WpuRubricaContattiTM.CELLULARE +","
                                                  + WpuRubricaContattiTM.VISIBILITA+","
                                                  + WpuRubricaContattiTM.EXT_BOOL1+","
                                                  + WpuRubricaContattiTM.EXT_BOOL2+","
                                                  + WpuRubricaContattiTM.EXT_BOOL3+","
                                                  + WpuRubricaContattiTM.EXT_BOOL4+","
                                                  + WpuRubricaContattiTM.EXT_BOOL5+","
                                                  + WpuRubricaContattiTM.R_AZIENDA+","
                                                  + WpuRubricaContattiTM.TS
                                                  +"," + WpuRubricaContattiTM.CODICE_FISC //Fix 26743
                                                  +"," + WpuRubricaContattiTM.R_ABI //Fix 27814
                                                  +"," + WpuRubricaContattiTM.R_CAB //Fix 27814
                                                  +"," + WpuRubricaContattiTM.CODICE_IBAN //Fix 27814
												  +"," + WpuRubricaContattiTM.ID_IND_PR_SIST_EST //Fix 31669
												  +"," + WpuRubricaContattiTM.EXT_ENUM1 //Fix 36974
												  +"," + WpuRubricaContattiTM.EXT_ENUM2 //Fix 36974
												  +"," + WpuRubricaContattiTM.EXT_ENUM3 //Fix 36974
												  +"," + WpuRubricaContattiTM.EXT_ENUM4 //Fix 36974
												  +"," + WpuRubricaContattiTM.EXT_ENUM5 //Fix 36974
                                                  // +") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; //Fix 36974
                                                           +") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '-', '-', '-', '-', '-')"; //Fix 36974
  
  public static CachedStatement stmtInsert = new CachedStatement(INSERT_WPU_CONT);
//Fix 20813 fine
  
  private List<ErrorMessage> iWarnings=new ArrayList<ErrorMessage>(); //33460
  /**
   * Costruttore, vado a leggere le impostazioni di importazione degli ordini
   */
  public ImportatoreOrdini() {
    //...FIX 5518 (Spostato nel metodo "importa" perchè quì l'attributo Codice non è ancora valorizzato)
    //leggiImpostazioniOrdini();
    //Fix 2812 - inizio
    setNomeHdrTestata("OrdineVendita");
    setNomeHdrRighe("OrdineVenditaRigaPrm");
    //Fix 2812 - fine
  }

//MG FIX 12775 inizio
  public int getBatchJobId() {
    return iBatchJobId;
  }
  public void setReportNr(int val) {
    iReportNr = val;
  }
  public int getReportNr() {
    return iReportNr;
  }
  public void setRigaJobId(int val) {
    iRigaJobId = val;
  }
  public int getRigaJobId() {
    return iRigaJobId;
  }
  public void setNumeroOrdiniDaImportare(int val) {
    iNumeroOrdiniDaImportare = val;
  }
  public int getNumeroOrdiniDaImportare() {
    return iNumeroOrdiniDaImportare;
  }
//MG FIX 12775 fine


  //...FIX 5518 inizio

  /**
   * Codice di riferimento per le impostazioni di importazione documenti
   * @return String
   */
  public String getCodice() {
    //return ORDINE; //Fix 04034 SL - aggiunta questa costante
    return iCodice; //...FIX 5518
  }

  /**
   * setCodice
   * @param codice String
   */
  public void setCodice(String codice) {
    iCodice = codice;
  }

  /**
   * getSistemaVenBanco
   * @return String
   */
  public char getSistemaVenBanco() {
    return iSistemaVenBanco;
  }

  /**
   * setSistemaVenBanco
   * @param sistemaVenBanco char
   */
  public void setSistemaVenBanco(char sistemaVenBanco) {
    iSistemaVenBanco = sistemaVenBanco;
  }


  //...FIX 5518 fine

  protected void setOutputBatch(PrintWriter output) {
    iOutputBatch = output;
  }

  protected void setBatchJobId(int batchJobId) {
    iBatchJobId = batchJobId;
  }

  //Fix 02793 - inizio
  protected void setBatchQueueId(String batchQueueId) {
    iBatchQueueId = batchQueueId;
  }

  //Fix 02793 - Fine


  /**
   * Esecuzione dell'attività batch.
   * E' il metodo che contiene la logica dell'attività batch.
   */
  public void importa() {
    leggiImpostazioniOrdini(); //...FIX 5518
    List gwtor00fList = null;
    Gwtor00f gwtor00f = null;
    OrdineVenditaTestata testataOrdVen = null;
    String whereRiga = null;
    List gwror00fList = null;
    Gwror00f gwror00f = null;
    List gwErrTesList = null;
    GwErrImpTes gwErrTes = null;
    List gwErrRigList = null;
    GwErrImpRig gwErrRig = null;
    int err = BODataCollector.OK;
    Vector righeSave = new Vector();
    Vector gwrorSave = new Vector();
    int countOrdini = 0;
    initializeRowId();//Fix 20813
    iNuoviClientiInCM = false; //Fix 20813
    String condizTesta = getWhereCondizTesta(); //MOD. 03465 SL - Sostituito con il metodo
    String condizErr = "";
    try {
      getLog().appendMessage(ResourceLoader.getString(prop, "InizioElaborazione"), true); //MG FIX 12775
      //Fix 2999 - inizio
      gwtor00fList = Gwtor00f.retrieveList(condizTesta, getOrderByTestata(), false);
      //Fix 2999 - fine
    }
    catch(Exception eGwtor) {
      getLog().appendMessage(ResourceLoader.getString(prop, "ErroreLetturaTestate"), true); //MG FIX 12775
      eGwtor.printStackTrace(Trace.excStream);
    }

    //Fix 2372 - inizio
//MG FIX 12775 inizio
    setNumeroOrdiniDaImportare(gwtor00fList.size());
//MG FIX 12775 fine

    //Fix 2372 - fine

    for(Iterator iGwtor = gwtor00fList.iterator(); iGwtor.hasNext(); ) {
      try {
        gwtor00f = (Gwtor00f)iGwtor.next();
        gwmso00fOrdineCliente = recuperaGwmso00fOrdineCliente(gwtor00f.getTocliw());//Fix 47377
        preProcessaTestataGw(gwtor00f); //40013 GANCIO PER PERSONALIZZAZIONI 
//MG FIX 12775        getLog().appendMessage("\nElaborazione dell'ordine con data carrello: " + gwtor00f.getTocard() + " e numero carrello " + gwtor00f.getTocarp(), true);
        testataOrdVen = creaTestataOrdine(gwtor00f);
        assegnaDatiTestata(testataOrdVen, gwtor00f);

        //Fix 4386 - inizio
        Database db = ConnectionManager.getCurrentDatabase();
        //Fix 4386 - fine

        // Cancella eventuali errori in gw_err_imp_tes e gw_err_imp_rig
        condizErr = " ID_AZIENDA = '" + gwtor00f.getToidaz();
        condizErr = condizErr + "' AND FONTE_ORIGINE = '" + gwtor00f.getTofoor();
        //Fix 4386 - inizio
        condizErr = condizErr + "' AND DATA_CARR = " + db.getLiteral(gwtor00f.getTocard());
        //Fix 4386 - fine
        condizErr = condizErr + " AND PROG_CARR = '" + gwtor00f.getTocarp() + "'";

        gwErrTesList = GwErrImpTes.retrieveList(condizErr, "", false);
        for(Iterator iGwErr = gwErrTesList.iterator(); iGwErr.hasNext(); ) {
          gwErrTes = (GwErrImpTes)iGwErr.next();
          gwErrTes.delete();
          //ConnectionManager.commit();
        }
        gwErrRigList = GwErrImpRig.retrieveList(condizErr, "", false);
        for(Iterator iGwErrRig = gwErrRigList.iterator(); iGwErrRig.hasNext(); ) {
          gwErrRig = (GwErrImpRig)iGwErrRig.next();
          gwErrRig.delete();
          //ConnectionManager.commit();
        }

         //Fix 41042 PM >
        //whereRiga = "";
        //Fix 4386 - inizio
//        whereRiga = "ROCARD = " + db.getLiteral(gwtor00f.getTocard()) +
//          " AND ROCARP = '" + gwtor00f.getTocarp() +
//          "'" + getWhereCondizRiga(); //MOD. 03465 SL - Sostituito con il metodo;
        //Fix 4386 - fine
        //Tolto questa condiz. di where AND ROSTAT = 'V'";
        gwror00fList = getRighe(gwtor00f);
        //Fix 41042 PM <
        

        //Controlla se i dati inseriti in testata vanno tutti bene
        err = checkOrdineVendita(testataOrdVen, gwtor00f);

        if(err != BODataCollector.ERROR) {
          //iNumeroRigheDaImportare = gwror00fList.size();
        	iNumeroRigheDaImportare = getNumeroRigheDaImportare(gwror00fList);  //Fix 22463
          //Fix 2533 - inizio
          if(iNumeroRigheDaImportare > 0) {
            //Fix 2533 - fine
            //Fix 2786 - inizio
//MG FIX 12775 inizio
/*
            if(gwmso00f != null && gwmso00f.getRiassegnaPrezzi() && gwmso00f.isCreaOmaggi()) {
              getLog().appendMessage("L'ordine possiede " + iNumeroRigheDaImportare + " righe più eventuali righe di omaggio", true);
            }
            else {
              getLog().appendMessage("L'ordine possiede " + iNumeroRigheDaImportare + " righe", true);
            }
*/
//MG FIX 12775 fine
            //Fix 2786 - fine
            for(Iterator iGwror = gwror00fList.iterator(); iGwror.hasNext(); ) {
              gwror00f = (Gwror00f)iGwror.next();
              //Fix 22463 inizo
              if(isRigaCommenti(gwror00f)){
              	gestioneRigaCommenti(gwror00f,testataOrdVen); 
              	continue;
              }
              //Fix 22463 false
              OrdineVenditaRigaPrm rigaOrdVenPrm = creaRigaOrdine(testataOrdVen, gwror00f);
              assegnaDatiRiga(testataOrdVen, rigaOrdVenPrm, gwror00f);
              righeSave.add(rigaOrdVenPrm);
              gwrorSave.add(gwror00f);
            }

            //Controlla che i dati di ciascuna riga vadano tutti bene
            int iNumRigheErr = 0; //Contatore righe con errore
            for(int iRig = 0; iRig < righeSave.size(); iRig++) {
              OrdineVenditaRigaPrm ordVenRigSave = (OrdineVenditaRigaPrm)righeSave.get(iRig);
              Gwror00f gwror00fSave = (Gwror00f)gwrorSave.get(iRig);
              err = checkOrdineVenditaRigaPrm(ordVenRigSave, gwror00fSave);
              if(err == BODataCollector.ERROR) {
            	gwror00fSave.setStatoErrImp('1');//Fix 49800
            	gwror00fSave.save();//Fix 49800
                iNumRigheErr++;
              }
            }

            //Se c'è quanche riga con errore setta opportunamente la variabile
            if(iNumRigheErr > 0) {
              err = BODataCollector.ERROR;
              setErroriPresenti(true); //Fix 04034 SL - aggiunto gestione presenza errori
            }
            //Fix 2533 - inizio
          }
          else {
//MG FIX 12775: riportato in aggiornaTabellaStampa
//            getLog().appendMessage("L'ordine non possiede righe pertanto non sarà importato", true);
          }
          //Fix 2533 - fine
        }

        //Caso in cui sia testata che righe contengono tutti i dati corretti
        if(err != BODataCollector.ERROR) {
          //Fix 2533 - inizio
          if(iNumeroRigheDaImportare > 0) {
            //Fix 2533 - fine
            //Aggiunge commenti a testata
            gestioneCommentiTesta(testataOrdVen, gwtor00f);

            //Aggiunge righe a testata con relativi commenti
            for(int iRig = 0; iRig < righeSave.size(); iRig++) {
              OrdineVenditaRigaPrm ordVenRig = (OrdineVenditaRigaPrm)righeSave.get(iRig);
              Gwror00f gwror00fSave = (Gwror00f)gwrorSave.get(iRig);
              gwror00fSave.setStatoErrImp('2');//Fix 49800
              testataOrdVen.getRighe().add(ordVenRig);
              gestioneCommentiRiga(ordVenRig, gwror00fSave);
            }

            //Salvataggio della testata
            int rcTes = salvaTestata(testataOrdVen);
            if(rcTes > ErrorCodes.OK) {
//MG FIX 12775 inizio
              checkNumeroRigheImportate(testataOrdVen, gwtor00f);
//MG FIX 12775 fine
              //Fix 24574 inizio
              checkBloccoEmissioneOrdImportate(testataOrdVen, gwtor00f);
              
              checkFidoForzatoPerCliente(testataOrdVen,gwtor00f);//Fix 38861
              //Fix 24574 fine
              int errDelTes = 0;
              //Fix 30979 - inizio
//              if(gwmso00f != null && gwmso00f.getEliminaRecord())
              //if(areCondizEliminaRecord()) //Fix 49250
              //Fix 30979 - fine
                //errDelTes = gwtor00f.delete(); //Fix 49250
              //else {//Fix 49250
                gwtor00f.setStatoErrImp('2');
                gwtor00f.setTostdo('5');
                //Fix 2200 - inizio
                gwtor00f.setIdAnnoOrdine(testataOrdVen.getAnnoDocumento());
                gwtor00f.setIdNumeroOrdine(testataOrdVen.getNumeroDocumento());
                //Fix 2521 - inizio
//                            gwtor00f.setToru01(String.valueOf(iBatchJobId));
                //Fix 2521 - fine
                //Fix 2200 - inizio
                if(areCondizEliminaRecord()){//Fix 49250
                	gwtor00f.aggiornaDocumentoAI();//Fix 49250
                	errDelTes = gwtor00f.delete();//Fix 49250
            	}else {//Fix 49250
                gwtor00f.save();
                //Fix 14857 inizio
                completaDatiDaOrdine(testataOrdVen, gwrorSave);
                }//Fix 49250
                //Fix 14857 fine

              //} //Fix 49250
              
              errDelTes = esegueOperazioniPers(errDelTes, gwtor00f, testataOrdVen, gwrorSave, righeSave);	//Fix 30979
              
              if(errDelTes >= ErrorCodes.NO_ROWS_UPDATED) {
                ConnectionManager.commit();
                //Fix 2489 - inizio
                //Fix 47377 ini
                boolean isStampaConfOrd = false;
                if(gwmso00f != null)
                	isStampaConfOrd = gwmso00fOrdineCliente != null ? gwmso00fOrdineCliente.isStampaConfOrd() : gwmso00f.isStampaConfOrd();
                //if(gwmso00f != null && gwmso00f.isStampaConfOrd()) {
                if(isStampaConfOrd) {
            	//Fix 47377 fini
                  raggruppaOrdiniStampa(testataOrdVen);
                }
                //Fix 2489 - fine
//MG FIX 12775                getLog().appendMessage("E' stato generato l'ordine panthera: anno = '" + testataOrdVen.getAnnoDocumento() + "' numero = '" + testataOrdVen.getNumeroDocumento() + "'", true);
                countOrdini = countOrdini + 1;
              }
              else {

                ConnectionManager.rollback();
              }
            }
            else {
//MG FIX 12775              getLog().appendMessage("Errore nel salvataggio dell'ordine che ha data carrello: " + gwtor00f.getTocard() + " e numero carrello " + gwtor00f.getTocarp(), true);
//MG FIX 12775              getLog().appendMessage("Return code : " + rcTes, true);
            	//Fix 15529 PM >
            	ErrorMessage em = daRcAErrorMessage(rcTes, null);
            	try
            	{
                     compilaGwErrorImpTes(em, gwtor00f);
                }
                catch(Exception eGwErrSave)
                {
                     eGwErrSave.printStackTrace(Trace.excStream);
                }
            	//Fix 15529 PM <

            	setErroriPresenti(true); //Fix 04034 SL - aggiunto gestione presenza errori
            }
          }
        }
        //Caso in cui si è verificato qualche errore
        else {
          gwtor00f.setStatoErrImp('1');
          gwtor00f.save();
          setErroriPresenti(true); //Fix 04034 SL - aggiunto gestione presenza errori
          ConnectionManager.commit();
        }
      }
      catch(ThipException e) {
        setErroriPresenti(true); //Fix 04034 SL - aggiunto gestione presenza errori
//MG FIX 12775 inizio
        try {
          eliminaOrdineVenditaCreato(testataOrdVen);//Fix 15658 :elimina l'ordine vendita creato " in caso di errore"
          compilaGwErrorImpTes(e.getErrorMessage(), gwtor00f);
/*
        GwErrImpTes gwError = (GwErrImpTes)Factory.createObject(GwErrImpTes.class);
        ErrorMessage errMess = e.getErrorMessage();
        gwError.setIdAzienda(gwtor00f.getToidaz());
        gwError.setFonteOrigine(gwtor00f.getTofoor());
        gwError.setDataCarr(gwtor00f.getTocard());
        gwError.setProgCarr(gwtor00f.getTocarp());
        gwError.setDescrRidotta(errMess.getText());
        gwError.setDescrizione(errMess.getAttOrGroupName() !=null ? errMess.getAttOrGroupName() + ": " + errMess.getLongText() : errMess.getLongText());
        //Fix 04034 SL - aggiunto messaggio esteso x errore
        getLog().appendMessage(errMess.getLongText(), true);
         gwError.save();
*/
          gwtor00f.setStatoErrImp('1');
          gwtor00f.save();
          ConnectionManager.commit();
        }
        catch(Exception eGwErrSave) {
//MG FIX 12775          getLog().appendMessage("Errore in fase di salvataggio degli errori di importazione testata.", true);
          eGwErrSave.printStackTrace(Trace.excStream);
        }
      }
      catch(Exception eCommit) {
        setErroriPresenti(true); //Fix 04034 SL - aggiunto gestione presenza errori
//MG FIX 12775 inizio
//        getLog().appendMessage("Errore nel salvataggio dell'ordine con data carrello: " + gwtor00f.getTocard() + " e numero carrello " + gwtor00f.getTocarp(), true);
      //Fix 13700 Object[] param = {TimeUtils.getDate(gwtor00f.getTocard()).toString(), new Integer(gwtor00f.getTocarp())};
      Object[] param = {TimeUtils.getDate(gwtor00f.getTocard()).toString(), gwtor00f.getTocarp()};//Fix 13700
      getLog().appendMessage(ResourceLoader.getString(prop, "ErrEccezione", param), true);
//MG FIX 12775 fine

        eCommit.printStackTrace(Trace.excStream);
        try {
          ConnectionManager.rollback();
        }
        catch(Exception eRollback) {
          eRollback.printStackTrace(Trace.excStream);
        }
      }
      finally {
        righeSave.clear();
        gwrorSave.clear();

//MG FIX 12775 inizio
      // salvataggio errori di importazione su tabella di stampa
          aggiornaTabellaStampa(gwtor00f);
//MG FIX 12775 fine

      }
    } //Fine del for

    //Fix 2489 - inizio
    //Fix 47377 ini
    boolean isStampaConfOrd = false;
    if(gwmso00f != null)
    	isStampaConfOrd =  gwmso00fOrdineCliente != null ? gwmso00fOrdineCliente.isStampaConfOrd() : gwmso00f.isStampaConfOrd(); 
    //if(gwmso00f != null && gwmso00f.isStampaConfOrd()) {
    if(isStampaConfOrd) {
    //Fix 47377 fini
      //Fix 2707 - inizio
      preparaStampaConfermaOrdini();
      //Fix 2707 - fine
    }
    //Fix 2489 - fine

//MG FIX 12775 inizio
    getLog().appendMessage(" ", true);

    Object[] params = {new Integer(gwtor00fList.size())};
    getLog().appendMessage(ResourceLoader.getString(prop,"NumeroOrdiniDaImportare", params), true);

    Object[] param = {new Integer(countOrdini)};
    getLog().appendMessage(ResourceLoader.getString(prop,"NumeroOrdiniImportati", param), true);
    if (this.isErroriPresenti()) {
      getLog().appendMessage(ResourceLoader.getString(prop,"AttenzionePresenzaErrori"), true);
    }
    getLog().appendMessage(" ", true);
//MG FIX 12775 fine
    //Fix 20813 inizio
    ErrorMessage error = null;
    if (iNuoviClientiInCM) //Fix 21076 PM
    	error = lancioCMContatto();
    if(error!=null){
    	 getLog().appendMessage(error.getLongText(), true);	
    }
    //Fix 20813 fine
  }

  /**
   * MOD. 03465 - SL
   * Aggiunto questo metodo
   * @return String
   */
  protected String getWhereCondizTesta() {
    return "TOIDAZ = '" + Azienda.getAziendaCorrente() + "'" +
      " AND TOSTAT = '" + DatiComuniEstesi.VALIDO + "'" +
//Fix 9815
//      " AND TOSTDO != '5'"  +
      " AND (TOSTDO = '1' OR  TOSTDO = '2')"  +
//Fix 9815
      " AND TOFOOR = '" + getCodice() + "'"; //...FIX 5518 
  }

  /**
   * MOD. 03465 - SL
   * Aggiunto questo metodo
   * @return String
   */
  protected String getWhereCondizRiga() {
    return " AND ROSTAT = '" + DatiComuniEstesi.VALIDO + "'" +
           " AND ROFOOR = '" + getCodice() + "'"; //...FIX 5518
  }

  /**
   * Metodo che legge le impostazioni per l'importatore ordini
   */
  protected void leggiImpostazioniOrdini() {

    String[] gwmsoKey = {Azienda.getAziendaCorrente(), getCodice()};
    try {
      gwmso00f = Gwmso00f.elementWithKey(KeyHelper.buildObjectKey(gwmsoKey), gwmso00f.NO_LOCK);
      if(gwmso00f == null) {
//MG FIX 12775 inizio
        Object[] param = {getCodice()};
        getLog().appendMessage(ResourceLoader.getString(prop, "NonEsistonoImpostazioni", param), true);
//MG FIX 12775 fine
      }
    }
    catch(Exception e) {
      getLog().appendMessage(ResourceLoader.getString(prop, "ErroreLetturaImpostazioni"), true); //MG FIX 12775
      e.printStackTrace(Trace.excStream);
    }
  }

  /**
   * Metodo che crea la testata ordine assegnando il cliente e la
   * causale ordine di vendita
   */
  protected OrdineVenditaTestata creaTestataOrdine(Gwtor00f gwtor) {
    // Testata
    OrdineVenditaTestata testataOrd = (OrdineVenditaTestata)Factory.createObject(OrdineVendita.class);
    testataOrd.setIdAzienda(gwtor.getToidaz());
    testataOrd.setIdCliente(gwtor.getTocliw());
    testataOrd.setIdCau(gwtor.getTocafi());
    // Fix 06554 inizio
    testataOrd.setIdDivisione(gwtor.getIdDivisione());
    // Fix 06554 fine
    // Numeratore
    testataOrd.getNumeratoreHandler().setDataDocumento(gwtor.getTodaor());
    Serie serie = recuperariIdSerieDaUsare(testataOrd);//Fix 47377
    //if(gwmso00f != null && gwmso00f.getRSerie() != null) {//Fix 47377
    if(serie != null) {//Fix 47377
      //testataOrd.getNumeratoreHandler().setIdSerie(gwmso00f.getRSerie().getIdSerie());//Fix 47377
    	testataOrd.getNumeratoreHandler().setIdSerie(serie.getIdSerie());//Fix 47377
    }
    testataOrd.completaBO();

    //Fix 2629 - inizio
    testataOrd.setIdAzienda(gwtor.getToidaz());
    //Fix 2629 - fine

    return testataOrd;
  }

  /**
   * Metodo che crea la riga ordine assegnando la testata e la
   * causale ordine di vendita
   */
  protected OrdineVenditaRigaPrm creaRigaOrdine(OrdineVenditaTestata ordVenTes, Gwror00f gwror) {
    //Fix 2200 - inizio
    OrdineVenditaRigaPrm ordVenRig = (OrdineVenditaRigaPrm)Factory.createObject(OrdineVenditaRigaPrm.class);
    ordVenRig.setIdAzienda(ordVenTes.getIdAzienda());
    ordVenRig.setTestata(ordVenTes); //---------------Necessario per completaBO
    //Fix 2200 - fine
    //FIX 19768 PM >
    //ordVenRig.setIdCauRig(gwror.getRocafi());
    String idCauRiga = gwror.getRocafi();
    //if (gwror.getRos101() == TipoRiga.SPESE_MOV_VALORE) //Fix 24574 
    if (gwror.getRos101() == TipoRiga.SPESE_MOV_VALORE && !isCausaleTipoSpesa(idCauRiga))//Fix 24574     
    	idCauRiga = trovaCausaleSpesa(ordVenTes, idCauRiga);
    ordVenRig.setIdCauRig(idCauRiga);
    //Fix 19768 PM <
    ordVenRig.completaBO();

    return ordVenRig;
  }

  /**
   * Metodo assegna i dati della testata
   */
  protected OrdineVenditaTestata assegnaDatiTestata(OrdineVenditaTestata ordVenTes, Gwtor00f gwtor) {

	  //Fix 47377 ini
	  char statoOrd = StatoAvanzamento.DEFINITIVO;
	  if(gwmso00f != null)
		  statoOrd = gwmso00fOrdineCliente != null ? gwmso00fOrdineCliente.getStatoOrd() : gwmso00f.getStatoOrd();
    //if(gwmso00f != null) {
      //if(gwmso00f.getStatoOrd() == 'P') {
        if(statoOrd == 'P') {
        //Fix 47377 fine	
        ordVenTes.setStatoAvanzamento('1');
      }
      //else if(gwmso00f.getStatoOrd() == 'D') {//Fix 47377
        else if(statoOrd == 'D') {
        ordVenTes.setStatoAvanzamento('2');
      }
    //}//Fix 47377

    ordVenTes.setStatoEvasione('0');
    ordVenTes.setSaldoManuale(false);
    //Fix 04034 SL - inizio
    //docVenTes.setDataOrdineIntestatario(gwtdo.getTodaor());
    if(gwtor.getTodvsr() != null)
      ordVenTes.setDataOrdineIntestatario(gwtor.getTodvsr());
      //Fix 04034 SL - fine
      //Fix 06554 inizio
      //ordVenTes.setDivisione(null);
      //Fix 06554 fine

      //Fix 6138 PM Inizio
      if (gwtor.getTovsri() != null && !gwtor.getTovsri().equals(""))
      ordVenTes.setNumeroOrdineIntestatario(gwtor.getTovsri());
      //ordVenTes.setDataOrdineIntestatario(gwtor.getTodvsr());
      //Fix 6138 PM Inizio

    //Fix 2363 - inizio
    if(gwtor.getToctvw() != null && !gwtor.getToctvw().equals("")) {
      ordVenTes.setIdCatVenCli(gwtor.getToctvw());
    }


    //Fix 9813 PM Inizio
   /*
    //Fix 2363 - fine
    if(gwtor.getToclsp() == null) {
      ordVenTes.setRagioneSocaleDest(gwtor.getTorasp());
      ordVenTes.setIndirizzoDestinatario(gwtor.getToinsp());
      ordVenTes.setLocalitaDestinatario(gwtor.getTolosp());
      ordVenTes.setCAPDestinatario(gwtor.getTocasp());
      ordVenTes.setIdNazioneDen(gwtor.getTocnaz());
      ordVenTes.setIdProvinciaDen(gwtor.getToprsp());
      //Fix 9815 PM Inizio
      ordVenTes.setClienteDestinatario(null);
      ordVenTes.setIndirizzo(null);
      //Fix 9815 PM Fine
    }
    else {
      ordVenTes.setIdDenAbt(gwtor.getToclsp());

      //Fix 9815 PM Inizio
      ordVenTes.setRagioneSocaleDest(null);
      ordVenTes.setIndirizzoDestinatario(null);
      ordVenTes.setLocalitaDestinatario(null);
      ordVenTes.setCAPDestinatario(null);
      ordVenTes.setIdNazioneDen(null);
      ordVenTes.setIdProvinciaDen(null);
      ordVenTes.setIndirizzo(null);
      //Fix 9815 PM Fine
    }
*/
  //Fix 19768 PM >  
 /*  if(gwtor.getToclsp() != null && !gwtor.getToclsp().equals("")) {
       ordVenTes.setIdDenAbt(gwtor.getToclsp());
       ordVenTes.setRagioneSocaleDest(null);
       ordVenTes.setIndirizzoDestinatario(null);
       ordVenTes.setLocalitaDestinatario(null);
       ordVenTes.setCAPDestinatario(null);
       ordVenTes.setIdNazioneDen(null);
       ordVenTes.setIdProvinciaDen(null);
       ordVenTes.setIdSequenzaInd(null);
  }
  else if (gwtor.getTorasp() != null && !gwtor.getTorasp().equals("")){
      ordVenTes.setRagioneSocaleDest(gwtor.getTorasp());
      ordVenTes.setIndirizzoDestinatario(gwtor.getToinsp());
      ordVenTes.setLocalitaDestinatario(gwtor.getTolosp());
      ordVenTes.setCAPDestinatario(gwtor.getTocasp());
      ordVenTes.setIdNazioneDen(gwtor.getTocnaz());
      ordVenTes.setIdProvinciaDen(gwtor.getToprsp());

      ordVenTes.setClienteDestinatario(null);
      ordVenTes.setIdSequenzaInd(null);
  }*/
  //Fix 9913 PM fine
   
    assegnaDatiDestinazione(ordVenTes, gwtor);
    //Fix 19768 PM <    
    //Fix 2372 - inizio
    if(gwtor.getTocpfa() != null && !gwtor.getTocpfa().equals("")) {
      ordVenTes.setIdClienteFat(gwtor.getTocpfa());
    }
    //Fix 2372 - fine

    //Fix 2363 inizio
    if(gwtor.getTodair() != null && !gwtor.getTodair().equals("")) {
      ordVenTes.setDataConsegnaRichiesta(gwtor.getTodair());
    }
    else {
      ordVenTes.setDataConsegnaRichiesta(gwtor.getTodaor());
    }
    if(gwtor.getTodaco() != null && !gwtor.getTodaco().equals("")) {
      ordVenTes.setDataConsegnaConfermata(gwtor.getTodaco());
      ordVenTes.setDataConsegnaProduzione(gwtor.getTodaco());
    }
    else {
      ordVenTes.setDataConsegnaConfermata(gwtor.getTodaor());
      ordVenTes.setDataConsegnaProduzione(gwtor.getTodaor());
    }

    //Fix 04034 SL - sistemato calcolo delle settimane x testata
    //Calcolo settimane
    int[] datiSett = TimeUtils.getISOWeek(ordVenTes.getDataConsegnaRichiesta());
    String sett = DocumentoOrdineTestata.getSettimanaFormattata(datiSett[0], datiSett[1]);
    ordVenTes.setSettConsegnaRichiesta(sett);
    datiSett = TimeUtils.getISOWeek(ordVenTes.getDataConsegnaConfermata());
    sett = DocumentoOrdineTestata.getSettimanaFormattata(datiSett[0], datiSett[1]);
    ordVenTes.setSettConsegnaConfermata(sett);
    //Fix 04034 SL - fine

    //Fix 02793 Inizio
    //ordVenTes.setIdMagazzino(gwtor.getTomagw());
    if(gwtor.getTomagw() != null && !gwtor.getTomagw().equals(""))
      ordVenTes.setIdMagazzino(gwtor.getTomagw());
      //ordVenTes.setIdMagazzinoTra(null);
      //Fix 02793 fine
    ordVenTes.setIdCommessa(gwtor.getTocoms());

    ordVenTes.setIdCentroCosto(gwtor.getToceri());

    ordVenTes.setIdFornitore(null);
    //Fix 2363 - inizio
    if(gwtor.getToling() != null && !gwtor.getToling().equals("")) {
      ordVenTes.setIdLingua(gwtor.getToling());
    }
    if(gwtor.getToczon() != null && !gwtor.getToczon().equals("")) {
      ordVenTes.setIdZona(gwtor.getToczon());
    }
    //Fix 2363 - fine
    if(gwtor.getTolisw() != null && !gwtor.getTolisw().equals("")) {
      ordVenTes.setIdListino(gwtor.getTolisw());
    }
    ordVenTes.setOrdineContratto(false);
    ordVenTes.setGrigliaPeriodiOrdCnt('0');
    //Fix 2363 - inizio
    String idValuta = gwtor.getTocvaw();
    if(idValuta != null && !idValuta.equals("")) {
      ordVenTes.setIdValuta(idValuta);
    }
    
    //Fix 21122 PM >
    BigDecimal cambio = gwtor.getTocamb();
    if(cambio != null && cambio.compareTo(new BigDecimal("0")) != 0) {
    	ordVenTes.setCambio(cambio);
    }
    else if(ordVenTes.getIdValuta() != null && !ordVenTes.getIdValuta().equals("") && ordVenTes.getDataDocumento() != null)
    	ordVenTes.setCambio(DocumentoVendita.recuperaCambio(ordVenTes.getIdValuta(), ordVenTes.getDataDocumento()));
    //Fix 21122 PM <    
    
    
    //Fix 2363 - fine
    if(gwtor.getToasfi() != null && !gwtor.getToasfi().equals("")) {
      ordVenTes.setIdAssogIva(gwtor.getToasfi());
    }
    if(gwtor.getTocopa() != null && !gwtor.getTocopa().equals("")) {
      ordVenTes.setIdModPagamento(gwtor.getTocopa());
      //Fix 4214 PM Inizio
      if(ordVenTes.getModalitaPagamento() != null)
        ordVenTes.setPrcScontoFineFattura(ordVenTes.getModalitaPagamento().getScontoCassaPer());
        //Fix 4214 PM Fine

      //Fix 36339 Inizio
      if(ordVenTes.getPrcScontoFineFattura() == null)
    	  ordVenTes.setPrcScontoFineFattura(new BigDecimal("0"));
      //Fix 36339 Fine
    }
    if(gwtor.getTodipa() != null) {
      ordVenTes.setDataInizioPagamento(gwtor.getTodipa());
    }
    if((gwtor.getTocabi() != null && !gwtor.getTocabi().equals("")) &&
      (gwtor.getToccab() != null && !gwtor.getToccab().equals(""))) {
      ordVenTes.getIdentificativoBanca().setCodificaSIA(true);
      ordVenTes.getIdentificativoBanca().setIdABI(gwtor.getTocabi());
      ordVenTes.getIdentificativoBanca().setIdCAB(gwtor.getToccab());
//
// COMPLETARE DATI BANCA
//
    }
    else {
      //Fix 2539 - inizio - Aggiunto il controllo
      if(ordVenTes.getIdentificativoBanca().getIdABI() == null &&
        ordVenTes.getIdentificativoBanca().getIdCAB() == null) {
        //ordVenTes.getIdentificativoBanca().setCodificaSIA(false);  //Fix 28968 PM
        ordVenTes.getIdentificativoBanca().setCodificaSIA(true);  //Fix 28968 PM 
      }
      //Fix 2539 - fine
    }
//Fix 04034 SL - inizio
    if(gwtor.getToccor() != null)
      ordVenTes.setContoCorrente(gwtor.getToccor());

      /*ordVenTes.setIdSpesa1(null);
             ordVenTes.setIdSpesa2(null);*/
    //if(gwtor.getTocspe() != null && !gwtor.getToscmo().equals("")) //Fix 28968
    if(gwtor.getTocspe() != null && !gwtor.getTocspe().equals("")) //Fix 28968
      ordVenTes.setIdSpesa1(gwtor.getTocspe());
    //if(gwtor.getTocsp1() != null && !gwtor.getToscmo().equals("")) //Fix 28968
    if(gwtor.getTocsp1() != null && !gwtor.getTocspe().equals("")) //Fix 28968
      ordVenTes.setIdSpesa2(gwtor.getTocsp1());
//Fix 04034 SL - fine

      //Fix 2999 - inizio
    if(gwtor.getTosccl() != null && !gwtor.getTosccl().equals("")) {
      ordVenTes.setPrcScontoIntestatario(gwtor.getTosccl());
    }
    if(gwtor.getToscmo() != null && !gwtor.getToscmo().equals("")) {
      ordVenTes.setPrcScontoModalita(gwtor.getToscmo());
    }
    if(gwtor.getTocdsc() != null && !gwtor.getTocdsc().equals("")) {
      Sconto scontoTab = (Sconto)Factory.createObject(Sconto.class);
      scontoTab.setIdSconto(gwtor.getTocdsc());
      ordVenTes.setScontoTabellare(scontoTab);
    }
    //Fix 2999 - fine
    
    //Fix 35802 inizio
    if(gwtor.getScontoFinaleFat() != null && !gwtor.getScontoFinaleFat().equals("")) {
    	ordVenTes.setScontoFinaleFat(gwtor.getScontoFinaleFat());
    }
    if(gwtor.getPercScontoFinaleFat() != null && !gwtor.getPercScontoFinaleFat().equals("") ) {
    	ordVenTes.setPercScontoFinaleFat(gwtor.getPercScontoFinaleFat());
    }
    //Fix 35802 fine
    
    //Fix 2589 - inizio
    //if(gwtor.getToscfa() != null && !gwtor.getToscfa().equals("")) {//Fix 36339
    if(gwtor.getToscfa() != null) {//Fix 36339
      ordVenTes.setPrcScontoFineFattura(gwtor.getToscfa());
    }
    //Fix 2589 - fine
    //Fix 2999 - inizio
    if(gwtor.getToagen() != null && !gwtor.getToagen().equals("")) {
      ordVenTes.setIdAgente(gwtor.getToagen());
    }
    if(gwtor.getTopepr() != null && !gwtor.getTopepr().equals("")) {
      ordVenTes.setProvvigioneAgente(gwtor.getTopepr());
    }
    if(gwtor.getToage1() != null && !gwtor.getToage1().equals("")) {
      ordVenTes.setIdAgenteSub(gwtor.getToage1());
    }
    if(gwtor.getTopep1() != null && !gwtor.getTopep1().equals("")) {
      ordVenTes.setProvvigioneSubagente(gwtor.getTopep1());
    }
    //Fix 2999 - fine
    //Fix 2363 - inizio
    if(gwtor.getTosped() != null && !gwtor.getTosped().equals("")) {
      ordVenTes.setIdModSpedizione(gwtor.getTosped());
      //Fix 2589 e 2629 - inizio
      if(gwtor.getTodesp() == null || gwtor.getTodesp().equals("")) {
        if(ordVenTes.getModalitaSpedizione() != null) {
          ordVenTes.setDescrModalitaSpedizione(ordVenTes.getModalitaSpedizione().getDescrizione().getDescrizione());
        }
      }
      else {
        ordVenTes.setDescrModalitaSpedizione(gwtor.getTodesp());
      }
      //Fix 2589 e 2629 - fine
    }
    //Fix 2363 - fine

    //Fix 2363 - inizio
    if(gwtor.getTocons() != null && !gwtor.getTocons().equals("")) {
      ordVenTes.setIdModConsegna(gwtor.getTocons());
      //Fix 2589 e 2629 - inizio
      if(gwtor.getTodcon() == null || gwtor.getTodcon().equals("")) {
        if(ordVenTes.getModalitaConsegna() != null) {
          ordVenTes.setDescrModalitaConsegna(ordVenTes.getModalitaConsegna().getDescrizione().getDescrizione());
        }
      }
      else {
        ordVenTes.setDescrModalitaConsegna(gwtor.getTodcon());
      }
      //Fix 2589 e 2629 - fine
    }
    //Fix 2363 - fine
    //Fix 2589 e 2629 - inizio
    if(gwtor.getToimba() != null && !gwtor.getToimba().equals("")) {
      ordVenTes.setIdAspettoEsn(gwtor.getToimba());
      if(gwtor.getTodimb() == null || gwtor.getTodimb().equals("")) {
        if(ordVenTes.getAspettoEsteriore() != null) {
          ordVenTes.setDescrAspettoEsteriore(ordVenTes.getAspettoEsteriore().getDescrizione().getDescrizione());
        }
      }
      else {
        ordVenTes.setDescrAspettoEsteriore(gwtor.getTodimb());
      }
    }
    //Fix 2589 e 2629 - fine

    //Fix 2589 - inizio
    //A che cosa serve se è già stato fatto sopra?
//      if (gwtor.getTodesp() != null && gwtor.getTodesp() != "") {
//         ordVenTes.setDescrModalitaSpedizione(gwtor.getTodesp());
//      }
//      ordVenTes.setIdModConsegna(gwtor.getTocons());
//      if (gwtor.getTodcon() != null && gwtor.getTodcon() != "") {
//         ordVenTes.setDescrModalitaConsegna(gwtor.getTodcon());
//      }
//      ordVenTes.setIdAspettoEsn(gwtor.getToimba());
//      if (gwtor.getTodimb() != null && gwtor.getTodimb() != "") {
//         ordVenTes.setDescrAspettoEsteriore(gwtor.getTodimb());
//      }
    //Fix 2589 - fine
    //Fix 2363 - inizio
    if(gwtor.getTovet1() != null && !gwtor.getTovet1().equals("")) {
      ordVenTes.setIdVettore1(gwtor.getTovet1());
      //Fix 2589 e 2629 - inizio
      if(gwtor.getTodev1() != null && !gwtor.getTodev1().equals("")) {
        ordVenTes.setDescrVettore1(gwtor.getTodev1());
      }
      else {
        if(ordVenTes.getVettore1() != null) {
          ordVenTes.setDescrVettore1(ordVenTes.getVettore1().getRagioneSociale());
        }
      }
      //Fix 2589 e 2629 - fine
    }
    if(gwtor.getTovet2() != null && !gwtor.getTovet2().equals("")) {
      ordVenTes.setIdVettore2(gwtor.getTovet2());
      //Fix 2589 e 2629 - inizio
      if(gwtor.getTodev2() != null && !gwtor.getTodev2().equals("")) {
        ordVenTes.setDescrVettore2(gwtor.getTodev2());
      }
      else {
        if(ordVenTes.getVettore2() != null) {
          ordVenTes.setDescrVettore2(ordVenTes.getVettore2().getRagioneSociale());
        }
      }
      //Fix 2589 e 2629 - fine
    }
    //Fix 2363 - fine
    ordVenTes.setIdVettore3(null);
    ordVenTes.setDescrVettore3(null);
    //Fix 5200 - inizio
    if(gwtor.getTotpev() == 'O') {
      ordVenTes.setTipoEvasioneOrdine(OrdineTestata.EVASIONE_INTERO_ORD);
    }
    else if(gwtor.getTotpev() == 'R') {
      ordVenTes.setTipoEvasioneOrdine(OrdineTestata.AMMESSO_FRAZIONAMENTO_NO_RIGA);
    }
    else if(gwtor.getTotpev() == 'P') {
      ordVenTes.setTipoEvasioneOrdine(OrdineTestata.AMMESSO_FRAZIONAMENTO_RIGA);
    }
    else if(gwtor.getTotpev() == '4') {
      ordVenTes.setTipoEvasioneOrdine(OrdineTestata.SALDO_AUTOMATICO);
    }
    //Fix 5200 - fine

    //Fix 4472 - inizio
    ordVenTes.setTipoFonteDoc(DocumentoOrdineTestata.TIPO_FONTE_DOC_ECOMMERCE);
    //Fix 4472 - fine
    return ordVenTes;
  }

  /**
   * Metodo assegna i dati della riga
   */
  protected OrdineVenditaRigaPrm assegnaDatiRiga(OrdineVenditaTestata ordVenTes, OrdineVenditaRigaPrm ordVenRig, Gwror00f gwror) {
  	//Fix 19768 PM >
  	if (ordVenRig.getTipoRiga() == TipoRiga.SPESE_MOV_VALORE)
  		return assegnaDatiRigaSpesa(ordVenTes, ordVenRig, gwror);
  	else
  	{
      //Fix 19768 PM <
  	  //Fix 47377 ini
  	  char statoOrd = StatoAvanzamento.DEFINITIVO;
  	  if(gwmso00f != null)
  		  statoOrd = gwmso00fOrdineCliente != null ? gwmso00fOrdineCliente.getStatoOrd() : gwmso00f.getStatoOrd();
      //if(gwmso00f != null) {
        //if(gwmso00f.getStatoOrd() == 'P') {
          if(statoOrd == 'P') {
          //Fix 47377 fine	
        ordVenRig.setStatoAvanzamento('1');
        }
        //else if(gwmso00f.getStatoOrd() == 'D') {//Fix 47377
          else if(statoOrd == 'D') {
        ordVenRig.setStatoAvanzamento('2');
    	}
      //}//Fix 47377

    ordVenRig.setStatoEvasione('0');
    ordVenRig.setSaldoManuale(false);
    //Fix 2786 - inizio
    assegnaSequenzaRiga(ordVenRig, gwror);
    //Fix 2786 - fine
    // riga contratto non trovata ???
    if(gwror.getRocarr().intValue() >= 5000 && gwror.getRocarr().intValue() < 9000) {
      int numRiga = (gwror.getRocarr().intValue() - 5000) * 10;
      ordVenRig.setIdRigaCollegata(Integer.decode(String.valueOf(numRiga)));
    }
    else {
      ordVenRig.setIdRigaCollegata(null);
    }
    ordVenRig.setIdDettaglioRigaCollegata(null);

    // Dati Bozza non trovati ???

    ordVenRig.setIdCauRig(gwror.getRocafi());
    //Fix 5288 - inizio
    //String idMgazzino = gwror.getRocafi();
    String idMgazzino = gwror.getRomagw();
    //Fix 5288 - fine
    if(idMgazzino != null && !idMgazzino.equals(""))
      ordVenRig.setIdMagazzino(gwror.getRomagw());
    String idArticolo = gwror.getRocoar();
    //PM Fix 2326 - inizio
    Articolo articolo = null;
    //PM Fix 2326 - Fine
    if(idArticolo != null && !idArticolo.equals("")) {
      ordVenRig.setIdArticolo(idArticolo);
      //PM Fix 2326 - inizio
      //Articolo articolo = ordVenRig.getArticolo();
      articolo = ordVenRig.getArticolo();
      //PM Fix 2326 - Fine

      Integer numVersione = null;
      Integer numVersioneSaldo = null;
      //Fix 04034 SL - tengo traccia della versione precedente della gestione versioni
      /*if (articolo.getVersioneAtDate(new Date(System.currentTimeMillis())).getIdVersioneSaldi() != null)
                   {
          numVersioneSaldo = articolo.getVersioneAtDate(new Date(System.currentTimeMillis())).getIdVersioneSaldi();
          ordVenRig.setIdVersioneSal(numVersioneSaldo);
                   }
                   else if (ordVenRig.getArticolo().getVersioneAtDate(new Date(System.currentTimeMillis())).getIdVersione() != null)
                   {
          numVersione = ordVenRig.getArticolo().getVersioneAtDate(new Date(System.currentTimeMillis())).getIdVersione();
          ordVenRig.setIdVersioneSal(numVersione);
                   }*/

      //Fix 04034 SL - sistemato gestione articolo versione saldi e richiesta
      if(articolo != null) {

      	// fix 11375 >
        ArticoloVersione versioneRcs = null;
      	if (articolo.hasVersioneEstesa()) {
        	String idVersione = gwror.getRocdve();
        	if (idVersione != null) {
        		List vers = articolo.getVersioni();
        		Iterator iter = vers.iterator();
        		while (iter.hasNext()) {
        			versioneRcs = (ArticoloVersione)iter.next();
        			if (versioneRcs.getIdVersione().toString().equals(idVersione)) {
                ordVenRig.setIdVersioneRcs(new Integer(idVersione));
                ordVenRig.setIdVersioneSal(ordVenRig.getIdVersioneRcs());
                break;
        			}
        		}
        	}
      	}
      	if (versioneRcs == null) {
      		versioneRcs = articolo.getVersioneAtDate(new
      				Date(System.currentTimeMillis()));
      		if(versioneRcs != null) {
      			numVersione = versioneRcs.getIdVersione();
      			ordVenRig.setIdVersioneRcs(numVersione);
      			numVersioneSaldo = versioneRcs.getIdVersioneSaldi();
      			if(numVersioneSaldo != null)
      				ordVenRig.setIdVersioneSal(numVersioneSaldo);
      			else
      				ordVenRig.setIdVersioneSal(numVersione);
      		}
      	}
      	// fix 11375 <

      }
      //Fix 04034 SL - fine
    // Fix 07212 inizio CH/A
    String idEsternoConfig = gwror.getIdEsternoConfigurazione();
    if (idEsternoConfig != null && !idEsternoConfig.equals(""))
      ordVenRig.setIdEsternoConfig(idEsternoConfig);
    else {
    //Fix 2200 - inizio
      String idConfig = gwror.getRobars();
    //Fix 2539 - inizio - Aggiunto controllo su conf. DUMMY
      if (idConfig != null &&
          !idConfig.equals(Configurazione.CONFIGURAZIONE_DUMMY.toString()))

    //Fix 2539 - fine
        ordVenRig.setIdConfigurazione(new Integer(idConfig));
    //Fix 2200 - fine
      //Fix 22466 PM >
      else
      {
    	  if (articolo!=null &&(idConfig == null || idConfig.equals("")) && articolo.isConfigurato())
    		  ordVenRig.setConfigurazione(articolo.getConfigurazioneStd());
      }
      //Fix 22466 PM <
    }
    // Fix 07212 fine

    // PM Fix 2326 - inizio
    // ordVenRig.setDescrizioneArticolo(gwror.getRodear());
      String descrArt = gwror.getRodear();
      if((descrArt == null || descrArt.equals("")) && articolo != null)
        descrArt = articolo.getDescrizioneArticoloNLS().getDescrizione();
      ordVenRig.setDescrizioneArticolo(descrArt);
      //PM Fix 2326 - Fine

      //Fix14727 Inizio RA
      if(PersDatiVen.getCurrentPersDatiVen().getGestioneDescExtArticolo() == PersDatiVen.GESTITA && articolo != null) {
        ArticoloCliente articoloCliente = (ArticoloCliente)ordVenRig.recuperaArticoloIntestatario();
        //Fix 18914 inizio
        //String descExtRiga = (articoloCliente != null && articoloCliente.getDescrizioneEst() != null && !isEm(articoloCliente.getDescrizioneEst().getDescrizioneEstesa())) ? articoloCliente.getDescrizioneEst().getDescrizioneEstesa() : articolo.getDescrizioneArticoloNLS().getDescrizioneEstesa();
        String descExtRiga = "";
        if(articoloCliente != null && articoloCliente.getDescrizioneEst() != null && !isEm(articoloCliente.getDescrizioneEst().getDescrizioneEstesa())) 
        	descExtRiga = articoloCliente.getDescrizioneEst().getDescrizioneEstesa();
        else if(articolo != null && articolo.getDescrizioneArticoloNLS() != null)
        	descExtRiga = articolo.getDescrizioneArticoloNLS().getDescrizioneEstesa();
        //Fix 18914 fine
	//ordVenRig.setDescrizioneExtArticolo(descExtRiga);//Fix 41100

      //Fix Inizio 41100 
        // inizio fix 42678

//        if(gwror.getRodear() != null && !Utils.areEqual(articolo.getDescrizioneArticoloNLS().getDescrizione(), gwror.getRodear()) )
//        	ordVenRig.setDescrizioneExtArticolo(gwror.getRodear());  
        if(gwror.getRodear() != null 
        		&& !Utils.areEqual("", gwror.getRodear())
        		&& !Utils.areEqual(articolo.getDescrizioneArticoloNLS().getDescrizione(), gwror.getRodear()) )
        	ordVenRig.setDescrizioneExtArticolo(gwror.getRodear());  
        // fine fix 42678
        else
        	ordVenRig.setDescrizioneExtArticolo(descExtRiga);
      //Fix Fine 41100
      }
      //Fix14727 Fine RA

      //Fix 02486 - PM Inizio
      assegnaQuantitaOrdine(ordVenRig, gwror);
      //Fix 02486 - PM Fine
    }
    ordVenRig.setIdOperazione(null);
     //ordVenRig.setImportoPercentualeSpesa(gwror.getRoprez());Fix 29197
    //ordVenRig.setSpesaPercentuale(gwror.getRotmov());Fix 29197

    //Fix 2363 - inizio
    if(gwror.getRodric() != null && !gwror.getRodric().equals("")) {
      ordVenRig.setDataConsegnaRichiesta(gwror.getRodric());
    }
    if(gwror.getRodaco() != null && !gwror.getRodaco().equals("")) {
      ordVenRig.setDataConsegnaConfermata(gwror.getRodaco());
      ordVenRig.setDataConsegnaProduzione(gwror.getRodaco());
    }

    //Fix 04034 SL - sistemato calcolo delle settimane sulle righe
    //Calcolo settimane
    int[] datiSett = TimeUtils.getISOWeek(ordVenRig.getDataConsegnaRichiesta());
    String sett = DocumentoOrdineTestata.getSettimanaFormattata(datiSett[0], datiSett[1]);
    ordVenRig.setSettConsegnaRichiesta(sett);
    datiSett = TimeUtils.getISOWeek(ordVenRig.getDataConsegnaConfermata());
    sett = DocumentoOrdineTestata.getSettimanaFormattata(datiSett[0], datiSett[1]);
    ordVenRig.setSettConsegnaConfermata(sett);
    ordVenRig.setSettConsegnaProduzione(sett);
    //Fix 04034 SL - fine

    //Fix 2363 - fine
    if(gwror.getRolisw() != null && !gwror.getRolisw().equals(""))
      ordVenRig.setIdListino(gwror.getRolisw());

      /*        try
              {
                  OrdineVenditaRigaImp ordVenRigaImp = new OrdineVenditaRigaImp();
                  ordVenRigaImp.calcolaDatiVenditaImp((OrdineVendita)ordVenTes);
              }
              catch (Exception eOVRI)
              {
                  eOVRI.printStackTrace(Trace.excStream);
              }
       */
    if(gwror.getRoasfi() != null && !gwror.getRoasfi().equals(""))
      ordVenRig.setIdAssogIVA(gwror.getRoasfi());
    else if(articolo != null)
      ordVenRig.setIdAssogIVA(articolo.getIdAssoggettamentoIVA());
    //Fix 16029 inizio
    impostaProvvigioniAgente(ordVenTes, ordVenRig);
    //Fix 16029 fine
    impostaCondizioniVendita(ordVenTes, ordVenRig, gwror);

    //Fix 04034 SL - Sistemata la gestione delle provvigioni di agente e subAgente
    Agente age = ordVenRig.getAgente();
    if(age != null) {
      BigDecimal prvAge = age.getPrcProvvigione();
      if(prvAge != null) {
        BigDecimal prv1Age = ordVenRig.getProvvigione1Agente();
        if(prv1Age != null)
          prv1Age = prv1Age.add(prvAge);
        else
          prv1Age = prvAge;
        ordVenRig.setProvvigione1Agente(prv1Age);
      }
    }

    Agente subAge = ordVenRig.getSubagente();
    if(subAge != null) {
      BigDecimal prvAge = subAge.getPrcProvvigione();
      if(prvAge != null) {
        BigDecimal prv1Age = ordVenRig.getProvvigione1Subagente();
        if(prv1Age != null)
          prv1Age = prv1Age.add(prvAge);
        else
          prv1Age = prvAge;
        ordVenRig.setProvvigione1Subagente(prv1Age);
      }
    }
    //Fix 04034 SL - fine
    completaDatiDaRiga(ordVenRig,gwror);//Fix 16029
    ordVenRig.setIdCommessa(gwror.getRocoms());
    //Fix 04034 SL - Aggiunta gestione costo unitario
    impostaCostoUnitario(ordVenRig);
    //Fix 04034 SL - fine
    if(gwror.getRotpev() == 'O' || gwror.getRotpev() == 'R')
      ordVenRig.setRigaNonFrazionabile(true);
    else
      ordVenRig.setRigaNonFrazionabile(false);

      //      gestioneCommentiRiga(ordVenRig, gwror);

      //Fix 4206 PM Inizio
    impostaArticoloCliente(ordVenRig);
    //Fix 4206 PM Fine
  	 } //Fix 19768 PM
    return ordVenRig;
  }

  /**
   * Controllo della testata
   * Fix 2200: cambiato nome e modificata logica
   */
  public int checkOrdineVendita(OrdineVenditaTestata ordVen, Gwtor00f gwtor00f) {
    int err = BODataCollector.OK;

    //Fix 2200 - inizio
    OrdineVenditaDataCollector bo = (OrdineVenditaDataCollector)
      Factory.createObject(OrdineVenditaDataCollector.class);
    //Fix 2812 - inizio
    bo.initialize(getNomeHdrTestata());
    //Fix 2812 - fine
    bo.setBo(ordVen);
    bo.loadAttValue();
    bo.impostaSecondoCausale();
    //Fix 2200 - fine
    //Fix 20813 inizio
    err = checkEsistenzaCliente(gwtor00f,ordVen);
    if(err== BODataCollector.ERROR) { 
    	return err;
    }
   //Fix 20813 fine
	bo.setForceableErrorsToWarnings(true);//Fix 41245
    err = bo.check();
    if(err == BODataCollector.ERROR) {
//MG FIX 12775      getLog().appendMessage("Errore in fase di salvataggio della testate dell'ordine.", true);
//MG FIX 12775      getLog().stampaListaErrori(bo.getErrorList());

      for(int iBoErr = 0; iBoErr < bo.getErrorList().getErrors().size(); iBoErr++) {
//MG FIX 12775 inizio
        try {
          compilaGwErrorImpTes(bo.getErrorList().errorAt(iBoErr), gwtor00f);
        }
        catch(Exception eGwErrSave) {
          eGwErrSave.printStackTrace(Trace.excStream);
        }
/*
        gwError = (GwErrImpTes)Factory.createObject(GwErrImpTes.class);
        ErrorMessage errMess = bo.getErrorList().errorAt(iBoErr);
        gwError.setIdAzienda(gwtor00f.getToidaz());
        gwError.setFonteOrigine(gwtor00f.getTofoor());
        gwError.setDataCarr(gwtor00f.getTocard());
        gwError.setProgCarr(gwtor00f.getTocarp());
        gwError.setDescrRidotta(errMess.getText());
        gwError.setDescrizione(errMess.getAttOrGroupName() !=null ? errMess.getAttOrGroupName() + ": " + errMess.getLongText() : errMess.getLongText());
        try {
          gwError.save();
        }
        catch(Exception eGwErrSave) {
//MG FIX 12775          getLog().appendMessage("Errore in fase di salvataggio degli errori di importazione testata.", true);
          eGwErrSave.printStackTrace(Trace.excStream);
        }
  */
      }
    }
    return err;
  }

  /**
   * Salvataggio della riga
   * Fix 2200: cambiato nome e modificata logica
   */
  public int checkOrdineVenditaRigaPrm(OrdineVenditaRigaPrm ordVenRigPrm, Gwror00f gwror00f) {
    int err = BODataCollector.OK;

    //Fix 2200 - inizio
    OrdineVenditaRigaPrmDataCollector bo = (OrdineVenditaRigaPrmDataCollector)
      Factory.createObject(OrdineVenditaRigaPrmDataCollector.class);
    //Fix 2812 - inizio
    bo.initialize(getNomeHdrRighe());
    //Fix 2812 - fine

//MG FIX 12472 inizio
    //disabilito momentaneamente il ricalcolo automatico delle qtà di magazzino
    boolean oldValueRicQta = ordVenRigPrm.isRicalcoloQtaFattoreConv();
    ordVenRigPrm.setRicalcoloQtaFattoreConv(false);

    //if (ordVenRigPrm.getArticolo().isArticLotto() && // Fix 18914
    if (ordVenRigPrm.getArticolo() != null && ordVenRigPrm.getArticolo().isArticLotto() && // Fix 18914
        (gwror00f.getIdLotto() != null && !gwror00f.getIdLotto().equals(Lotto.LOTTO_DUMMY))) {
      OrdineVenditaRigaLottoPrm lottoD;
      lottoD = (OrdineVenditaRigaLottoPrm)Factory.createObject(OrdineVenditaRigaLottoPrm.class);
      lottoD.setFather(ordVenRigPrm);
      lottoD.setIdArticolo(ordVenRigPrm.getIdArticolo());
      lottoD.setIdLotto(gwror00f.getIdLotto());
      lottoD.setQtaInUMRif(ordVenRigPrm.getQtaInUMRif());
      lottoD.setQtaInUMPrmMag(ordVenRigPrm.getQtaInUMPrmMag());
      lottoD.setQtaInUMSecMag(ordVenRigPrm.getQtaInUMSecMag());
      lottoD.setQtaAttesaEvasione(ordVenRigPrm.getQtaAttesaEvasione());
      lottoD.setQtaPropostaEvasione(ordVenRigPrm.getQtaPropostaEvasione());
      ordVenRigPrm.getRigheLotto().add(lottoD);
    }

//MG FIX 12472 fine

    bo.setBo(ordVenRigPrm);
    bo.loadAttValue();
    bo.impostaSecondoCausale();
    bo.getComponentManager("AnnoOrdine").setMandatory(false);
    bo.getComponentManager("NumeroOrdine").setMandatory(false);
    //Fix 2200 - fine
    //fix 11123
    bo.setForceableErrorForced(true);
    // fine fix 11123

    err = bo.check();

//MG FIX 12472 inizio : aggiunti controlli
    ordVenRigPrm.setRicalcoloQtaFattoreConv(oldValueRicQta);
    List errorUM = checkUMRigaImport(gwror00f.getArticolo(), gwror00f.getIdUMRif(),
                                     gwror00f.getIdUMPrm(), gwror00f.getIdUMSec());
    if (errorUM != null && errorUM.size() > 0) {
      bo.getErrorList().getErrors().addAll(errorUM);
      err = BODataCollector.ERROR;
    }
    //if (ordVenRigPrm.getArticolo().isArticLotto()) {// Fix 18914
    if (ordVenRigPrm.getArticolo() != null && ordVenRigPrm.getArticolo().isArticLotto()) {// Fix 18914
       if (gwror00f.getIdLotto() != null && !gwror00f.getIdLotto().equals(Lotto.LOTTO_DUMMY)) {
         List errorLotto = checkCodiceLotto(gwror00f);
         if (errorLotto != null && errorLotto.size() > 0) {
           bo.getErrorList().getErrors().addAll(errorLotto);
           err = BODataCollector.ERROR;
         }
       }
    }
 //MG FIX 12472 fine

    if(err == BODataCollector.ERROR) {
      //Fix 02793 Inizio
      //getLog().appendMessage("Errore in fase di salvataggio delle righe", true);
//MG FIX 12775      getLog().appendMessage("Errore in fase di salvataggio delle riga carrello: " + gwror00f.getRocarr().intValue(), true);
      //Fix 02793 Fine
//MG FIX 12775      getLog().stampaListaErrori(bo.getErrorList());

      for(int iBoErr = 0; iBoErr < bo.getErrorList().getErrors().size(); iBoErr++) {
//MG FIX 12775 inizio
        try {
          compilaGwErrorImpRig(bo.getErrorList().errorAt(iBoErr), gwror00f);
        }
        catch(Exception eGwErrSave) {
          eGwErrSave.printStackTrace(Trace.excStream);
        }
/*
        gwError = (GwErrImpRig)Factory.createObject(GwErrImpRig.class);
        ErrorMessage errMess = bo.getErrorList().errorAt(iBoErr);
        gwError.setIdAzienda(gwror00f.getRoidaz());
        gwError.setFonteOrigine(gwror00f.getRofoor());
        gwError.setDataCarr(gwror00f.getRocard());
        gwError.setProgCarr(gwror00f.getRocarp());
        //Fix 2200 - inizio
        gwError.setProgRigaCarr(gwror00f.getRocarr());
        //Fix 2200 - fine
        gwError.setDescrRidotta(errMess.getText());
//MG FIX 12472 inizio
//        gwError.setDescrizione(errMess.getAttOrGroupName() + ": " + errMess.getLongText());
        gwError.setDescrizione(errMess.getAttOrGroupName() !=null ? errMess.getAttOrGroupName() + ": " + errMess.getLongText() : errMess.getLongText());
//MG FIX 12472 fine
        try {
          gwError.save();
        }
        catch(Exception eGwErrSave) {
//MG FIX 12775          getLog().appendMessage("Errore in fase di salvataggio degli errori di importazione righe.", true);
          eGwErrSave.printStackTrace(Trace.excStream);
        }
*/
      }
    }
    return err;
  }

  protected void gestioneCommentiTesta(OrdineVenditaTestata ordVenTes, Gwtor00f gwtor) {
    //...FIX 5518 inizio
    //...Se sulle impostazioni di default dell'importatore dell'ordine non è stato indicato
    //...nessun commentUse allora quello che c'è scritto nella colonna TOCOMM (debitamente
    //...troncato se più lungo di 250 caratteri) viene scritto nelle note dell'ordine
	  //Fix 47377
	  String idCommentUse = null;
	  if(gwmso00f != null)
		  idCommentUse = gwmso00fOrdineCliente != null ? gwmso00fOrdineCliente.getIdCommentUse() :  gwmso00f.getIdCommentUse();
    //if(gwmso00f != null && gwmso00f.getIdCommentUse() != null) {
	  if(idCommentUse != null) {
	  //Fix 47377 fine
      //...FIX 5518 fine
      if(gwtor.getTocomm() != null && !gwtor.getTocomm().equals("")) {
        Vector commentoVectorTesta = new Vector();
        Comment commentoOrdTesta = (Comment)Factory.createObject(Comment.class);
        Comment commentoOrdTesta2 = (Comment)Factory.createObject(Comment.class);
        String commentoStringa = "";
        if(gwtor.getTocomm().length() <= 35) {
          commentoOrdTesta.setDescription(gwtor.getTocomm());
        }
        else {
          commentoOrdTesta.setDescription(gwtor.getTocomm().substring(0, 29) + ".....");
        }
        if(gwtor.getTocomm().length() <= 400) {
          commentoStringa = gwtor.getTocomm().substring(0, gwtor.getTocomm().length());
        }
        else {
          commentoStringa = gwtor.getTocomm().substring(0, 400);
        }
        //String[] commentoArr = {commentoStringa};//Fix 22066
        String[] commentoArr = {commentoStringa, ""}; //Fix 22066
        commentoOrdTesta.getTextNLSHandler().setTextsForLanguage(commentoArr, "it");
        if(gwtor.getTocomm().length() > 400) {
          commentoOrdTesta2.setDescription(gwtor.getTocomm().substring(0, 29) + ".....");
          String commentoStringa2 = gwtor.getTocomm().substring(400);
         // String[] commentoArr2 = {commentoStringa2};//Fix 22066
          String[] commentoArr2 = {commentoStringa2, ""}; //Fix 22066
          commentoOrdTesta2.getTextNLSHandler().setTextsForLanguage(commentoArr2, "it");
        }
        try {
          commentoOrdTesta.save();
          commentoVectorTesta.add(commentoOrdTesta);
          if(gwtor.getTocomm().length() > 400) {
            commentoOrdTesta2.save();
            commentoVectorTesta.add(commentoOrdTesta2);
          }
        }
        catch(Exception eSComm) {
          eSComm.printStackTrace(Trace.excStream);
        }

        CommentHandlerManager ordTestaCHM = ordVenTes.getCommentHandlerManager();
        CommentHandler ordTestaCH = ordVenTes.getCommentHandler();

        for(int iCT = 0; iCT < commentoVectorTesta.size(); iCT++) {
          Comment commentoOrd = (Comment)commentoVectorTesta.get(iCT);

          CommentHandlerLink ordTestaCHL = (CommentHandlerLink)Factory.createObject(CommentHandlerLink.class);
          ordTestaCHL.setComment(commentoOrd);

          CommentUse ordTestaCU = null;
          //String[] ordTestaCUKey = {gwmso00f.getIdCommentUse()};//Fix 47377
          String[] ordTestaCUKey = {idCommentUse};//Fix 47377
          try {
            ordTestaCU = (CommentUse)CommentUse.elementWithKey(KeyHelper.buildObjectKey(ordTestaCUKey), ordTestaCU.NO_LOCK);
          }
          catch(Exception eOTCU) {
            eOTCU.printStackTrace(Trace.excStream);
          }
          CommentUseLink ordTestaCUL = (CommentUseLink)Factory.createObject(CommentUseLink.class);
          if(ordTestaCU != null) {
            ordTestaCUL.setCommentUse(ordTestaCU);
          }
          else {
            getLog().appendMessage(ResourceLoader.getString(prop, "ErroreCommentUse"), true); //MG FIX 12775
          }

          List commentULList = ordTestaCHL.getCommentUseLinks();
          commentULList.add(ordTestaCUL);
          List commentHLList = ordTestaCH.getCommentHandlerLinks();
          commentHLList.add(ordTestaCHL);
          try {
            ordTestaCH.save();
          }
          catch(Exception eCUL) {
            eCUL.printStackTrace(Trace.excStream);
          }
        }
      }
    }
    //...FIX 5518 inizio
    else {
     /*42785 if(gwtor.getTocomm() != null && !gwtor.getTocomm().equals("")) {
        String nota = "";
        if(gwtor.getTocomm().length() <= 250)
          nota = gwtor.getTocomm();
        else
          nota = gwtor.getTocomm().substring(0, 249);
        ordVenTes.setNota(nota);
      }*/
      valorizzareNotaOrdini(ordVenTes,gwtor);//42785
    }
    //...FIX 5518 fine
  }

  protected void gestioneCommentiRiga(OrdineVenditaRigaPrm ordVenRig, Gwror00f gwror) {

    //...FIX 5518 inizio
    //...Se sulle impostazioni di default dell'importatore dell'ordine non è stato indicato
    //...nessun commentUse allora quello che c'è scritto nella colonna TOCOMM (debitamente
    //...troncato se più lungo di 250 caratteri) viene scritto nelle note dell'ordine
	  //Fix 47377
	  String idCommentUse = null;
	  if(gwmso00f != null)
		  idCommentUse = gwmso00fOrdineCliente != null ? gwmso00fOrdineCliente.getIdCommentUse() :  gwmso00f.getIdCommentUse();
    //if(gwmso00f != null && gwmso00f.getIdCommentUse() != null) {
	  if(idCommentUse != null) {
	  //Fix 47377 fine
      //...FIX 5518 fine
      if(gwror.getRocomm() != null && !gwror.getRocomm().equals("")) {
        Vector commentoVector = new Vector();
        Comment commentoOrdRiga = (Comment)Factory.createObject(Comment.class);
        Comment commentoOrdRiga2 = (Comment)Factory.createObject(Comment.class);
        String commentoStringa = "";
        if(gwror.getRocomm().length() <= 35) {
          commentoOrdRiga.setDescription(gwror.getRocomm());
        }
        else {
          commentoOrdRiga.setDescription(gwror.getRocomm().substring(0, 29) + ".....");
        }
        if(gwror.getRocomm().length() <= 400) {
          commentoStringa = gwror.getRocomm().substring(0, gwror.getRocomm().length());
        }
        else {
          commentoStringa = gwror.getRocomm().substring(0, 400);
        }
        //String[] commentoArr = {commentoStringa};//Fix 22066
        String[] commentoArr = {commentoStringa, ""}; //Fix 22066
        commentoOrdRiga.getTextNLSHandler().setTextsForLanguage(commentoArr, "it");
        if(gwror.getRocomm().length() > 400) {
          commentoOrdRiga2.setDescription(gwror.getRocomm().substring(0, 29) + ".....");
          String commentoStringa2 = gwror.getRocomm().substring(400);
          //String[] commentoArr2 = {commentoStringa2};//Fix 22066
          String[] commentoArr2 = {commentoStringa2, ""}; //Fix 22066
          commentoOrdRiga2.getTextNLSHandler().setTextsForLanguage(commentoArr2, "it");
        }
        try {
          commentoOrdRiga.save();
          commentoVector.add(commentoOrdRiga);
          if(gwror.getRocomm().length() > 400) {
            commentoOrdRiga2.save();
            commentoVector.add(commentoOrdRiga2);
          }
        }
        catch(Exception eSComm) {
          eSComm.printStackTrace(Trace.excStream);
        }

        CommentHandlerManager ordRigaCHM = ordVenRig.getCommentHandlerManager();
        CommentHandler ordRigaCH = ordVenRig.getCommentHandler();

        for(int i = 0; i < commentoVector.size(); i++) {
          Comment commentoOrd = (Comment)commentoVector.get(i);

          CommentHandlerLink ordRigaCHL = (CommentHandlerLink)Factory.createObject(CommentHandlerLink.class);
          ordRigaCHL.setComment(commentoOrd);

          CommentUse ordRigaCU = null;
          //String[] ordRigaCUKey = {gwmso00f.getIdCommentUse()};//Fix 47377
          String[] ordRigaCUKey = {idCommentUse};//Fix 47377
          try {
            ordRigaCU = (CommentUse)CommentUse.elementWithKey(KeyHelper.buildObjectKey(ordRigaCUKey), ordRigaCU.NO_LOCK);
          }
          catch(Exception eORCU) {
            eORCU.printStackTrace(Trace.excStream);
          }
          CommentUseLink ordRigaCUL = (CommentUseLink)Factory.createObject(CommentUseLink.class);
          if(ordRigaCU != null) {
            ordRigaCUL.setCommentUse(ordRigaCU);
          }
          else {
            getLog().appendMessage(ResourceLoader.getString(prop, "ErroreCommentUse"), true); //MG FIX 12775
          }
          List commentULList = ordRigaCHL.getCommentUseLinks();
          commentULList.add(ordRigaCUL);
          List commentHLList = ordRigaCH.getCommentHandlerLinks();
          commentHLList.add(ordRigaCHL);

          try {
            ordRigaCH.save();
          }
          catch(Exception eCH) {
            eCH.printStackTrace(Trace.excStream);
          }

        }
      }
    }
    //...FIX 5518 inizio
    else {
      if(gwror.getRocomm() != null && !gwror.getRocomm().equals("")) {
        String nota = "";
        if(gwror.getRocomm().length() <= 250)
          nota = gwror.getRocomm();
        else
          nota = gwror.getRocomm().substring(0, 250);
        ordVenRig.setNota(nota);
      }
    }
    //...FIX 5518 fine
  }

  //Fix 2372 - inizio
  protected int salvaTestata(OrdineVenditaTestata testata) throws SQLException {
    if(iNumeroRigheDaImportare != testata.getRighe().size()) {
      testata.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
//MG FIX 12775 inizio
//      getLog().appendMessage("L'ordine sarà salvato in stato provvisorio poichè il numero di righe importate è diverso da quelle dell'ordine da importare", true);
//      getLog().appendMessage("Numero righe importate = " + testata.getRighe().size(), true);
      Object[] param = {new Integer(testata.getRighe().size())};
      getLog().appendMessage(ResourceLoader.getString(prop,"WarningOrdineProvvisorio", param), true);
//MG FIX 12775 fine
    }
    //Fix 24574 inizio
    MotivoBloccoOrdine motivoBlocco = testata.getMotivoBloccoImmissioneDocumento(); 
    if (motivoBlocco !=null && motivoBlocco.getTipoBlocco() == MotivoBloccoOrdine.ACQUISIZIONE_AMMESSA_CON_APPROVAZIONE){//Evasione ammessa ma soggetta ad accettazione
    	testata.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
    }  	
    //Fix 24574 fine 
    //47909 ini    
    //return testata.save();    
    int rc =  testata.save();
    if(testata != null && rc >0){ 
		  scattoAutomaticoWorkflow(testata);
	 }            
    return rc;
    //47909 fine
  }

  //Fix 2372 - fine



//Fix 2431 - PM inizio
  protected OrdineVenditaRigaPrm impostaCondizioniVendita(OrdineVenditaTestata
    ordVenTes, OrdineVenditaRigaPrm ordVenRig, Gwror00f gwror) {
    ordVenRig.setServizioCalcDatiVendita(false);
//    RicercaCondizioniDiVendita ricercaVen = (RicercaCondizioniDiVendita) // Fix 08911
    Factory.createObject(RicercaCondizioniDiVendita.class);
    // Fix 08911 ini

    String idConfigurazione = "";
    //Fix 23100 PM >
    /*if (ordVenRig.getIdConfigurazione() != null) {
    	idConfigurazione = ordVenRig.getIdConfigurazione().toString();
    }*/
    
    idConfigurazione = ordVenRig.getIdEsternoConfig();
    if ((idConfigurazione == null || idConfigurazione.equals("")) && ordVenRig.getConfigurazione() != null) {
    	idConfigurazione = ordVenRig.getConfigurazione().getIdEsternoConfig();
    }
    //Fix 23100 PM <

    String dataDoc = "";
    if (ordVenTes.getDataDocumento() != null) {
      DateType dateType = new DateType();
      dataDoc = dateType.objectToString(ordVenTes.getDataDocumento());
    }
    DecimalType decType = new DecimalType();
    String qtaVendita = "";
    if (ordVenRig.getQtaInUMRif() != null) {
    	qtaVendita = decType.objectToString(ordVenRig.getQtaInUMRif());
    }
    String qtaUMMag = "";
    if (ordVenRig.getQtaInUMPrmMag() != null) {
    	qtaUMMag = decType.objectToString(ordVenRig.getQtaInUMPrmMag());
    }
	  // Fix 13211 inzio
    String qtaUMSecMag = "";
    if (ordVenRig.getQtaInUMSecMag() != null) {
      qtaUMSecMag = decType.objectToString(ordVenRig.getQtaInUMSecMag());
    }
    // Fix 13211 fine

    try {
      //Fix 24273 inizio
      /*iCondVendita = RicercaCondizioniDiVendita.getCondizioniVendita(
      		ordVenRig.getIdListino(),
      		ordVenTes.getIdCliente(),
      		ordVenRig.getIdArticolo(),
      		idConfigurazione,
      		ordVenRig.getIdUMRif(),
      		qtaVendita,
      		qtaUMMag,
          ordVenTes.getIdModPagamento(),
          dataDoc,
          dataDoc,
          ordVenRig.getIdAgente(),
          ordVenRig.getIdSubagente(),
          ordVenRig.getIdUMPrm(),
          ordVenTes.getIdValuta(),
  				"",
          "",
          "",
          "",
		      ordVenRig.getIdUMSec(), qtaUMSecMag // Fix 13211
        );*/
    	//Fix 29197 Inizio
    	String prcScontoIntestatario = decType.objectToString(ordVenRig.getPrcScontoIntestatario());
    	String prcScontoModalita = decType.objectToString(ordVenRig.getPrcScontoModalita());
    	String idScontoModalita = ordVenRig.getIdScontoMod() == null ? "" : ordVenRig.getIdScontoMod();
    	//Fix 29197 Fine
      CondizioniDiVenditaParams condVenParams = (CondizioniDiVenditaParams)Factory.createObject(CondizioniDiVenditaParams.class);
      condVenParams = condVenParams.impostaParamsCondizioniDiVendita(null,
      		ordVenRig.getIdAzienda(),
      		ordVenRig.getIdListino(),
      		ordVenTes.getIdCliente(),
      		ordVenRig.getIdArticolo(),    		
      		idConfigurazione,
      		ordVenRig.getIdUMRif(),
      		qtaVendita,
      		qtaUMMag,
      		ordVenTes.getIdModPagamento(),
      		dataDoc,
      		dataDoc,
      		ordVenRig.getIdAgente(),
      		ordVenRig.getIdSubagente(),
      		ordVenRig.getIdUMPrm(),
      		ordVenTes.getIdValuta(),
    			"",
    			false,
    			//"",// Fix 29197
    			prcScontoIntestatario, // Fix 29197
    			//"",// Fix 29197
    			prcScontoModalita,//Fix 29197
    			//"",//Fix 29197
    			idScontoModalita,//Fix 29197
    			"1", 
    			null,
    			ordVenRig.getIdUMSec(), 
    			qtaUMSecMag
      		); 
	  condVenParams.setTipoScontoRiga("A");//Fix29197
      impostaParamsCVPers(condVenParams);
      iCondVendita = RicercaCondizioniDiVendita.getCondizioniVendita(condVenParams);
      //Fix 24273 fine  
    }
    catch(Exception eCV) {
      eCV.printStackTrace(Trace.excStream);
    }
    /**
    try {
      iCondVendita = ricercaVen.ricercaCondizioniDiVendita(
        ordVenRig.getIdAzienda(),
        ordVenRig.getListinoVendita(),
        ordVenTes.getCliente(),
        ordVenRig.getArticolo(),
        ordVenRig.getUMRif(),
        ordVenRig.getQtaInUMRif(),
        new BigDecimal(0.0), //Prima era 1000. Perchè??????
        ordVenTes.getModalitaPagamento(),
        ordVenTes.getDataDocumento(),
        ordVenRig.getAgente(),
        ordVenRig.getSubagente(),
        ordVenRig.getUMPrm(),
        ordVenRig.getQtaInUMPrmMag(),
        ordVenTes.getValuta()
        );
    }
    catch(Exception eCV) {
      eCV.printStackTrace(Trace.excStream);
    }
		**/
    // Fix 08911 fin
    if(iCondVendita != null) {
      ordVenRig.setListinoVendita(iCondVendita.getListinoVendita());
      ordVenRig.setPrezzo(iCondVendita.getPrezzo());
      ordVenRig.setPrezzoExtra(iCondVendita.getPrezzoExtra());
      ordVenRig.setPrezzoListino(iCondVendita.getPrezzo());
      ordVenRig.setScontoArticolo1(iCondVendita.getScontoArticolo1());
      ordVenRig.setScontoArticolo2(iCondVendita.getScontoArticolo2());
      ordVenRig.setMaggiorazione(iCondVendita.getMaggiorazione());
      //Fix 2436 - inizio
      Sconto sconto = iCondVendita.getSconto();
      if(sconto != null) {
        ordVenRig.setIdSconto(sconto.getIdSconto());
      }
      Agente agente = iCondVendita.getAgente();
      if(agente != null) {
        ordVenRig.setIdAgente(agente.getIdAgente());
      }
      //Fix 2436 - fine
      ordVenRig.setProvvigione2Agente(iCondVendita.getProvvigioneAgente2());
      //Fix 2436 - inizio
      Agente subagente = iCondVendita.getSubAgente();
      if(subagente != null) {
        ordVenRig.setIdSubagente(subagente.getIdAgente());
      }
      //Fix 2436 - fine
      ordVenRig.setProvvigione2Subagente(iCondVendita.getProvvigioneSubagente2());
      ordVenRig.setProvenienzaPrezzo(iCondVendita.getTipoTestata());
      ordVenRig.setServizioProvenienzaPrezzo(iCondVendita.getTipoTestata());
      ordVenRig.setRiferimentoUMPrezzo(iCondVendita.getUMPrezzo());
      ordVenRig.setServizioRiferimUMPrezzo(iCondVendita.getUMPrezzo());
      ordVenRig.setTipoRigaListino(iCondVendita.getTipologiaRiga());
      //Fix 2786 - inizio
      //Fix 47377 ini
      boolean riassegnaPrezzi = false;
      boolean creaOmaggi = false;
      if(gwmso00f != null) {
    	  if(gwmso00fOrdineCliente != null) {
    	       riassegnaPrezzi = gwmso00fOrdineCliente.getRiassegnaPrezzi();
    	       creaOmaggi = gwmso00fOrdineCliente.isCreaOmaggi();  
    	  }else {
    	       riassegnaPrezzi = gwmso00f.getRiassegnaPrezzi();
    	       creaOmaggi = gwmso00f.isCreaOmaggi();
    	  }
      }
      //if(gwmso00f != null && gwmso00f.getRiassegnaPrezzi() && gwmso00f.isCreaOmaggi()) {
      if(riassegnaPrezzi && creaOmaggi) {
	  //Fix 47377 fini
        ordVenRig.setServizioListVendScaglione(iCondVendita.getListinoVenditaScaglioneKey());
      }
      //Fix 2786 - fine
      if(iCondVendita.getListinoVenditaScaglione() != null) {
        AssoggettamentoIVA assIVA = iCondVendita.getListinoVenditaScaglione().
          getListinoVenditaRiga().getAssoggettamentoIVA();
        if(assIVA != null)
          ordVenRig.setAssoggettamentoIVA(assIVA);
      }
      
     //Fix 21122 PM
     if (iCondVendita.getAzzeraScontiCliFor())	
	 {
		ordVenRig.setPrcScontoIntestatario(new BigDecimal("0"));
		ordVenRig.setPrcScontoModalita(new BigDecimal("0"));
		ordVenRig.setScontoModalita(null);
	 }
     //Fix 21122 PM
     
    }

    //Fix 5288 - inizio
    //Fix 47377 ini
    boolean riassegnaPrezzi = false;
    if(gwmso00f != null) {
  	  if(gwmso00fOrdineCliente != null)
  	       riassegnaPrezzi = gwmso00fOrdineCliente.getRiassegnaPrezzi();
  	  else 
  	       riassegnaPrezzi = gwmso00f.getRiassegnaPrezzi(); 
    }
    //if(gwmso00f != null && gwmso00f.getRiassegnaPrezzi()) {
    if(riassegnaPrezzi) {
	//Fix 47377
      if(ordVenRig.getPrezzo() == null) {
        ordVenRig.setPrezzo(gwror.getRoprez());
      }
      
    }
    else {
      //Fix 2701 - PM Inizio
      impostaCondizioniVenditaDaConnettore(ordVenTes, ordVenRig, gwror, iCondVendita);
      //Fix 2701 - PM Fine
    }
    //Fix 5288 - fine

    return ordVenRig;
  }

//Fix 2431 - PM fine


//Fix 2701 - PM Inizio
  protected void impostaCondizioniVenditaDaConnettore(OrdineVenditaTestata ordVenTes, OrdineVenditaRigaPrm ordVenRig, Gwror00f gwror, CondizioniDiVendita iCondVendita) {
    if(gwror.getRolisw() != null && !gwror.getRolisw().equals(""))
      ordVenRig.setIdListino(gwror.getRolisw());
//Fix 2603 - inizio
    if(gwror.getRoprez() != null && !gwror.getRoprez().equals(new BigDecimal(0.0)))
//Fix 2603 - fine
    {
      ordVenRig.setPrezzo(gwror.getRoprez());
    }
    if(iCondVendita != null && iCondVendita.getPrezzo() != null && gwror.getRoprez() != null) {
      if(iCondVendita.getPrezzo().compareTo(gwror.getRoprez()) != 0) {
        ordVenRig.setProvenienzaPrezzo('0');
        ordVenRig.setServizioProvenienzaPrezzo('0');
      }
    }
    if(gwror.getRoscar() != null)
      ordVenRig.setScontoArticolo1(gwror.getRoscar());
    //Fix 33132 inizio
    if(gwror.getScontoArt2() != null)
        ordVenRig.setScontoArticolo2(gwror.getScontoArt2()); 
    //Fix 33132 fine
    if(gwror.getRosccm() != null)
      ordVenRig.setMaggiorazione(gwror.getRosccm());
    if(gwror.getRocdsc() != null)
      ordVenRig.setIdSconto(gwror.getRocdsc());
    if(gwror.getRosccl() != null)
      ordVenRig.setPrcScontoIntestatario(gwror.getRosccl());
    if(gwror.getRoscmo() != null)
      ordVenRig.setPrcScontoModalita(gwror.getRoscmo());
    if(gwror.getRoagen() != null)
      ordVenRig.setIdAgente(gwror.getRoagen());
    // Fix 29197 Inizio
    /*
    if(gwror.getRopepr() != null)
      ordVenRig.setProvvigione2Agente(gwror.getRopepr());
    */
    if(gwror.getRopepr() != null)
      ordVenRig.setProvvigione1Agente(gwror.getRopepr());
    if(gwror.getRopecr() != null)
      ordVenRig.setProvvigione2Agente(gwror.getRopecr());
    // Fix 29197 Fine
    if(gwror.getRoage1() != null)
      ordVenRig.setIdSubagente(gwror.getRoage1());
    // inizio fix 42893
//    if(gwror.getRopep1() != null)
//      ordVenRig.setProvvigione2Subagente(gwror.getRopep1());
    if(gwror.getRopec1() != null)
        ordVenRig.setProvvigione2Subagente(gwror.getRopec1());
    // fine fix 42893

    if(gwror.getRotpre() == '0' || gwror.getRotpre() == '1') {
      ordVenRig.setTipoPrezzo(gwror.getRotpre());
    }
//Fix 2678 - inizio
    if(gwror.getRopzum() == 'V' || gwror.getRopzum() == 'M') {
      ordVenRig.setRiferimentoUMPrezzo(gwror.getRopzum());
//Fix 2439 - inizio
      ordVenRig.setServizioRiferimUMPrezzo(gwror.getRopzum());
//Fix 2439 - fine
    }
//Fix 2678 - fine
//Fix 2439 - inizio
//      ordVenRig.setServizioProvenienzaPrezzo(ordVenRig.getRiferimentoUMPrezzo());
    ordVenRig.setServizioProvenienzaPrezzo(ordVenRig.getProvenienzaPrezzo());
//Fix 2439 - fine
	  //47380 inizio
	  if(IsAttivaRicalcoloProvvigione2Agenti() && !esisteUnaValoreProvvigione2AgentiValorizzati(gwror) && esisteUnaValoreScontiValorizzati(gwror)) {
		  ordVenRig.setServeRicalProvvAg(true);
		  ordVenRig.setServeRicalProvvSubag(true);			
	  }
	  //47380 fine
  }

//Fix 2701 - PM fine


//Fix 02486 - PM Inizio
  protected void assegnaQuantitaOrdine(OrdineVenditaRigaPrm ordVenRig, Gwror00f gwror) {
    Articolo articolo = ordVenRig.getArticolo();
    if(articolo != null) {
      BigDecimal rifQta = null;
      BigDecimal prmQta = null;
      //Fix 2200 - inizio
      BigDecimal secQta = null;
      BigDecimal zero = new BigDecimal("0.00");
      String rifUM = gwror.getRocumv();
      String prmUM = gwror.getRocumm();
      String secUM = gwror.getIdUMSec(); //MG FIX 12472

//MG FIX 12472 inizio
      if(secUM == null || secUM.equals(""))
        secUM = articolo.getIdUMSecMag();
//MG FIX 12472 fine


      if(prmUM == null || prmUM.equals(""))
        prmUM = articolo.getIdUMPrmMag();

      if(rifUM == null || rifUM.equals("")) {
        UnitaMisura umVendita = articolo.getUMPrimariaVendita();
        if(umVendita != null)
          rifUM = umVendita.getIdUnitaMisura();
        else
          rifUM = prmUM;
      }

      List errorUM = this.checkUMRigaImport(articolo, rifUM, prmUM, secUM);
      if (errorUM != null && errorUM.size() > 0) {
        return ;
      }

      ordVenRig.setIdUMRif(rifUM);
      ordVenRig.setIdUMPrm(prmUM);
      ordVenRig.setIdUMSec(secUM);   //MG FIX 12472

      rifQta = gwror.getRoqtor();

//MG FIX 12472 inizio
      boolean calcolaQtaPrmMag = true;
      prmQta = gwror.getRoqt2o();
      if (ordVenRig.getUMPrm() != null && prmQta != null && prmQta.compareTo(new BigDecimal(0))!=0) {
          calcolaQtaPrmMag = false;
      }
      boolean calcolaQtaSecMag = true;
      secQta = gwror.getRoqt3o();
      if (ordVenRig.getUMSec() != null && secQta != null && secQta.compareTo(new BigDecimal(0))!=0) {
          calcolaQtaSecMag = false;
      }
      //Fix 48821 ini
      boolean calcolaQtaVen = true;
      if (ordVenRig.getUMRif() != null && rifQta != null && rifQta.compareTo(new BigDecimal(0))!=0) {
    	  calcolaQtaVen = false;
      }
      
      if (calcolaQtaVen) {
          if (prmQta != null && (prmQta.compareTo(new BigDecimal("0.0")) != 0)&& ordVenRig.getUMRif() != null && ordVenRig.getUMPrm() != null){
        	  
            rifQta = articolo.convertiUM(prmQta, ordVenRig.getUMPrm(),
                                         ordVenRig.getUMRif(), ordVenRig.getArticoloVersRichiesta()); 
            if (rifQta == null) { 
              Object[] param = {gwror.getRocard().toString(), gwror.getRocarp()};
              getLog().appendMessage(ResourceLoader.getString(prop, "ErrQtaUmRif1", param), true);  
            }
            else {
              rifQta = Q6Calc.get().setScale(rifQta,SCALE_QTA, BigDecimal.ROUND_HALF_DOWN);
              if (rifQta.compareTo(zero) == 0) {
                Object[] param = {gwror.getRocard().toString(), gwror.getRocarp()};
                getLog().appendMessage(ResourceLoader.getString(prop, "ErrQtaUmRif2", param), true);  
              }
            }
          }
          else if (secQta != null && (secQta.compareTo(new BigDecimal("0.0")) != 0) && ordVenRig.getUMRif() != null && ordVenRig.getUMSec() != null){
				rifQta = articolo.convertiUM(secQta, ordVenRig.getUMSec(), ordVenRig.getUMRif(),
						ordVenRig.getArticoloVersRichiesta());
				if (rifQta == null) {
					Object[] param = { gwror.getRocard().toString(), gwror.getRocarp() };
					getLog().appendMessage(ResourceLoader.getString(prop, "ErrQtaUmRif1", param), true);
				} else {
					rifQta = Q6Calc.get().setScale(rifQta, SCALE_QTA, BigDecimal.ROUND_HALF_DOWN);
					if (rifQta.compareTo(zero) == 0) {
						Object[] param = { gwror.getRocard().toString(), gwror.getRocarp() };
						getLog().appendMessage(ResourceLoader.getString(prop, "ErrQtaUmRif2", param), true);
					}
				}    	  
          }
        }
      //Fix 48821 fine

      if (calcolaQtaPrmMag) {
        if (ordVenRig.getUMRif() != null && ordVenRig.getUMPrm() != null){//...FIX10718 - DZ
          prmQta = articolo.convertiUM(rifQta, ordVenRig.getUMRif(),
                                       ordVenRig.getUMPrm(), ordVenRig.getArticoloVersRichiesta()); // fix 10955
          if (prmQta == null) { //...FIX10718 - DZ
            Object[] param = {gwror.getRocard().toString(), gwror.getRocarp()};
            getLog().appendMessage(ResourceLoader.getString(prop, "ErrQtaUmPrm1", param), true);  //MG FIX 12775
          }
          else {
            //prmQta = prmQta.setScale(SCALE_QTA, BigDecimal.ROUND_HALF_DOWN);//Fix 30871
			prmQta = Q6Calc.get().setScale(prmQta,SCALE_QTA, BigDecimal.ROUND_HALF_DOWN);//Fix 30871
            if (prmQta.compareTo(zero) == 0) {
              Object[] param = {gwror.getRocard().toString(), gwror.getRocarp()};
              getLog().appendMessage(ResourceLoader.getString(prop, "ErrQtaUmPrm2", param), true);  //MG FIX 12775
            }
          }
        }
      }

      if (calcolaQtaSecMag) {
        if (ordVenRig.getUMRif() != null && ordVenRig.getUMSec() != null){
          secQta = articolo.convertiUM(rifQta, ordVenRig.getUMRif(),
                                       ordVenRig.getUMSec(), ordVenRig.getArticoloVersRichiesta());
          if (secQta == null) {
            Object[] param = {gwror.getRocard().toString(), gwror.getRocarp()};
            getLog().appendMessage(ResourceLoader.getString(prop, "ErrQtaUmSec1", param), true);  //MG FIX 12775
          }
          else {
            //secQta = secQta.setScale(SCALE_QTA, BigDecimal.ROUND_HALF_DOWN);//Fix 30871
			secQta = Q6Calc.get().setScale(secQta,SCALE_QTA, BigDecimal.ROUND_HALF_DOWN);//Fix 30871
            if (secQta.compareTo(zero) == 0) {
              Object[] param = {gwror.getRocard().toString(), gwror.getRocarp()};
              getLog().appendMessage(ResourceLoader.getString(prop, "ErrQtaUmSec2", param), true);  //MG FIX 12775
            }
          }
        }
      }

      ordVenRig.setQtaInUMRif(rifQta);
      ordVenRig.setQtaInUMPrmMag(prmQta);
      ordVenRig.setQtaInUMSecMag(secQta!=null ? secQta : new BigDecimal(0));
      /* ricalcola il valore del flag ricalcolqQta*/
      ordVenRig.attivaRicalcoloQtaFattoreConv();

      boolean isGesQtaIntera = UnitaMisura.isPresentUMQtaIntera(ordVenRig.getUMRif(),
          ordVenRig.getUMPrm(), ordVenRig.getUMSec(), articolo);
      if (isGesQtaIntera && ordVenRig.isRicalcoloQtaFattoreConv()) {
        QuantitaInUMRif qtaCalc = articolo.calcolaQuantitaArrotondate(rifQta, ordVenRig.getUMRif(),
          ordVenRig.getUMPrm(), ordVenRig.getUMSec(), articolo.UM_RIF);
        rifQta = qtaCalc.getQuantitaInUMRif();
        prmQta = qtaCalc.getQuantitaInUMPrm();
        secQta = qtaCalc.getQuantitaInUMSec();
        ordVenRig.setQtaInUMRif(rifQta);
        ordVenRig.setQtaInUMPrmMag(prmQta);
        ordVenRig.setQtaInUMSecMag(secQta!=null ? secQta : new BigDecimal(0));
      }
//MG FIX 12472 fine
    }
    //Fix 2200 - fine
  }

//Fix 02486 - PM Fine

  //Fix 2489 - inizio
  protected void raggruppaOrdiniStampa(OrdineVenditaTestata testata) throws ThipException {
    //Fix 2707 - inizio
    riempieMappa(testata, mappaOrdiniDaStampare);
    //Fix 2707 - fine
  }

  //Fix 2707 - inizio
  protected void riempieMappa(OrdineVenditaTestata testata, Map mappa) {
    //Fix 2999 - inizio (in chiave anno anzichè data)
    String annoOrdine = testata.getAnnoDocumento(); //Chiave
    Object valore = mappa.get(annoOrdine); //Valore
    List listaOrdini =
      (valore == null) ? new ArrayList() : (List)valore;
    listaOrdini.add(testata);
    mappa.put(annoOrdine, listaOrdini);
    //Fix 2999 - fine
  }

  protected void preparaStampaConfermaOrdini() {
    preparaStampaConfermaOrdini(
      mappaOrdiniDaStampare,
      ResourceLoader.getString(prop, "IdReport")
      );
  }

  //Fix 2707 - fine


  //Fix 2707 - inizio (cambiato nome e aggiunti parametri)
  protected void preparaStampaConfermaOrdini(Map mappa, String nomeReport) {
    Collection listeOrdini = mappa.values();
    //Fix 2707 - fine
    Iterator iterListeOrdini = listeOrdini.iterator();
    while(iterListeOrdini.hasNext()) {
      List ordiniPerData = (List)iterListeOrdini.next();

      //Fix 4472 - inizio (commentata tutta la suddivisione in gruppi)
      esegueStampaConfermaOrdini(ordiniPerData, nomeReport);
      //Fix 4472 - fine
    }
  }

  /**
   * FIX09321 - DZ: aggiunta impostazione dei flag di creazione docDgt e docSSD secondo la
   * seguente logica:
   *
   * @param ordini List
   * @param nomeReport String
   */
  protected void esegueStampaConfermaOrdini(List ordini, String nomeReport) {//Fix 2707 (aggiunto parametro nomeReport)
    boolean pushDone = false;
    boolean res = false;
    ReportConfermaOrdVenBatch confermaOrdine =
      (ReportConfermaOrdVenBatch)Factory.createObject(ReportConfermaOrdVenBatch.class);
    confermaOrdine.setBatchExecution(true);
    confermaOrdine.setReportId(nomeReport);//Fix 2707
    confermaOrdine.setStatoConferma(ReportConfermaOrdVen.DA_EMETTERE);//Fix 22768 PM
    creaFiltroStampa(confermaOrdine.getFiltri(), ordini);
    //Fix 24715 inizio
    creaOrderByConfermaOrdine(confermaOrdine);
    //Fix 24715 fine
//    confermaOrdine.impostaFiltroNumeroDataOrd(testata.getNumeroDocumento(), testata.getDataDocumento());
    BatchOptions opt = (BatchOptions)Factory.createObject(BatchOptions.class);
    try {
      ConnectionManager.pushConnection();
      pushDone = true;
      String entityId = Entity.findEntityId(ReportConfermaOrdVenBatch.class);
      String taskId = "RUN";
      opt.initDefaultValues(ReportConfermaOrdVenBatch.class, entityId, taskId);
      opt.getBatchJob().setBatchQueueId(iBatchQueueId); //Fix 02793
      confermaOrdine.setBatchJob(opt.getBatchJob());
      confermaOrdine.setStampaDaImportatore(true);  //Fix 4472
      impostaFlagGenerazioneDgtSSD(confermaOrdine, nomeReport); //...FIX09321 - DZ
      int resSave = confermaOrdine.save();
      if(resSave >= ErrorCodes.NO_ROWS_UPDATED) {
        ConnectionManager.commit();
        res = BatchService.submitJob(confermaOrdine.getBatchJob().getBatchJobId());
      }
      else {
        ConnectionManager.rollback();
      }
    }
    catch(SQLException exc1) {
      exc1.printStackTrace(Trace.excStream);//...FIX09321 - DZ
    }
    catch(ConnectionException exc2) {
      exc2.printStackTrace(Trace.excStream);//...FIX09321 - DZ
    }
    catch(Exception exc3) {
      exc3.printStackTrace(Trace.excStream);//...FIX09321 - DZ
    }
    finally {
      //Fix 2647 - inizio
      try {
        ConnectionManager.rollback();
      }
      catch(SQLException exc4) {
        exc4.printStackTrace(Trace.excStream);//...FIX09321 - DZ
      }
      //Fix 2647 - fine
      if(pushDone)
        ConnectionManager.popConnection();
    }
  }

  /**
   * FIX09321 - DZ.
   * Recupero il descrittore distampa digitale, se esiste, per leggere le impostazioni
   * di generazione docDgt/SSD.
   * Mi interessano solo i flag di "AttivaDocDgt/SSD", e non quelli di "RichiestaDocDgt/SSD"
   * (che rendono visibili i flag di "AttivaDocDgt/SSD" nel reportModelChooser), perchè
   * scelgo di mettere a true la generazione docDgt/SSD in base a quale è il default impostato
   * sul descrittore di stampa. Non mi interessa se poi in GUI (interattivo) l'utente ha
   * la possibilità di attivare/disattivare.
   * @param runnable ThipElaboratePrintRunnable
   */
  protected void impostaFlagGenerazioneDgtSSD(ReportConfermaOrdVenBatch runnable, String nomeReport){
    runnable.setDocDgtEnabled(false);
    runnable.setSSDEnabled(false);
    String idAzienda = Azienda.getAziendaCorrente();
    Object[] keyParts = {idAzienda, nomeReport};
    String key = KeyHelper.buildObjectKey(keyParts);
    try{
      //...recupero il descrittore di stampaDgt associato alla stampa confermaOrdVen
      DescrittoreStampaDgt descrStpDgt = DescrittoreStampaDgt.elementWithKey(key, PersistentObject.NO_LOCK);
      if (descrStpDgt != null && descrStpDgt.getDatiComuniEstesi().getStato() == DatiComuniEstesi.VALIDO) {
      	boolean docDgtEnabled = descrStpDgt.isAttivaGenDocDgt();
        boolean docSSDEnabled = descrStpDgt.isAttivaGenSSD();
        runnable.setDocDgtEnabled(docDgtEnabled);
        runnable.setSSDEnabled(docSSDEnabled);
      }
    }
    catch(SQLException e){
      e.printStackTrace(Trace.excStream);
    }
  }

  protected void creaFiltroStampa(CondizioniFiltri condFiltro, List ordini) {
    OrdineVenditaTestata primaTestata = (OrdineVenditaTestata)ordini.get(0);
    //Fix 4472 - inizio
    OrdineVenditaTestata ultimaTestata = (OrdineVenditaTestata)ordini.get(ordini.size() - 1);
    //Fix 4472 - fine
    String annoOrdine = primaTestata.getAnnoDocumento();

    Vector colonne = condFiltro.getColonneFiltro();
    for(int i = 0; i < colonne.size(); i++) {
      ColonneFiltri colFiltro = (ColonneFiltri)colonne.elementAt(i);
      if(colFiltro.getClassAdName().equals("NumeroDocumentoPerOrdine")) {
        //Fix 4472 - inizio
        /*
                String listaStringa = new String();
                 Iterator iterOrdini = ordini.iterator();
                 while (iterOrdini.hasNext()) {
          OrdineVendita testata = (OrdineVendita)iterOrdini.next();
          //Fix 3893 PM Inizio
          //listaStringa += testata.getNumeroDocumento() + "+;";
          listaStringa += testata.getNumeroDocumento() + ColonneFiltri.COD_DESC_SEP + ColonneFiltri.LISTA_SEP;
          //Fix 3893 PM Fine
                 }
                 listaStringa = listaStringa.substring(0, listaStringa.length() - 2);
                 colFiltro.setListaString(listaStringa);
         */
       // Fix 06505 PM Inizio
        //colFiltro.setFrom(primaTestata.getNumeroDocumento());
        //colFiltro.setTo(ultimaTestata.getNumeroDocumento());

        try {
         int size = colFiltro.getAdditionalClassAd().getType().getSize();
         while(annoOrdine.length() < size)
           annoOrdine += " ";
       }
       catch (Exception ex) {
         ex.printStackTrace(Trace.excStream);
       }
       //Fix 9786 inizio
       String filtroFrom = annoOrdine + ColonneFiltri.ANNO_SEP + primaTestata.getNumeroDocumento();
       String filtroTo = annoOrdine + ColonneFiltri.ANNO_SEP + ultimaTestata.getNumeroDocumento();
       CondizioniFiltri.svuotaColonnaFiltro(colFiltro);
       CondizioniFiltri.impostaFiltroFrom(colFiltro, filtroFrom);
       CondizioniFiltri.impostaFiltroTo(colFiltro, filtroTo);
       /*
       colFiltro.setFrom(annoOrdine + ColonneFiltri.ANNO_SEP + primaTestata.getNumeroDocumento());
       colFiltro.setTo(annoOrdine + ColonneFiltri.ANNO_SEP + ultimaTestata.getNumeroDocumento());
       // Fix 06505 PM Fine

        //Fix 4472 - fine
        //...FIX 6411 inizio
        //...Imposto questi booleani a false perchè nei test ho verificato che se
        //...è presente l'MDV questi falg potrebbero essere erroneamente impostati a true
        colFiltro.setRangeEsclusione(false);
        colFiltro.setListaEsclusione(false);
        //...FIX 6411 fine
        */
       //Fix 9786 fine
      }
      // Fix 06505 PM Inizio
      //Fix 3054 Inizio
      //else if (colFiltro.getClassAdName().equals("DataOrdine")) {
      /*else if (colFiltro.getClassAdName().equals("AnnoOrdine")) {
        //DateType dt = new DateType();
        //colFiltro.setFrom(dt.objectToString(dataOrdine));
        //colFiltro.setTo(dt.objectToString(dataOrdine));

       colFiltro.setFrom(annoOrdine);
       colFiltro.setTo(annoOrdine);
       //Fix 3054 Fine
      }*/
      // Fix 06505 PM Fine
    }
  }

  //Fix 2489 - fine


  //Fix 2786 - inizio
  /**
   * Assegna alla riga dell'ordine di Panthera un numero calcolato in base al
   * campo ROCARR memorizzato nella tabella del connettore (GWROR00F)
   */
  protected void assegnaSequenzaRiga(OrdineVenditaRigaPrm ordVenRig, Gwror00f gwror) {
    int seqRiga = 0;

    if(gwror.getRocarr().intValue() >= 5000 && gwror.getRocarr().intValue() < 9000) {
      seqRiga = ((gwror.getRocarr().intValue() - 5000) * 10) + 1;
      ordVenRig.setSequenzaRiga(seqRiga);
    }
    else {
      seqRiga = gwror.getRocarr().intValue() * 10;
    }

    ordVenRig.setSequenzaRiga(seqRiga);
  }

  //Fix 2786 - fine


  //Fix 2812 - inizio
  public void setCondVendita(CondizioniDiVendita condVendita) {
    iCondVendita = condVendita;
  }

  public CondizioniDiVendita getCondVendita() {
    return iCondVendita;
  }

  public void setNomeHdrTestata(String s) {
    iNomeHdrTestata = s;
  }

  public String getNomeHdrTestata() {
    return iNomeHdrTestata;
  }

  public void setNomeHdrRighe(String s) {
    iNomeHdrRighe = s;
  }

  public String getNomeHdrRighe() {
    return iNomeHdrRighe;
  }

  //Fix 2812 - fine

  //Fix 2999 - inizio
  protected String getOrderByTestata() {
    return "TODAOR";
  }

  //Fix 2999 - fine

  //Fix 04034 SL - inizio
  protected void setErroriPresenti(boolean erroriPresenti) {
    iErroriPresenti = erroriPresenti;
  }

  protected boolean isErroriPresenti() {
    return iErroriPresenti;
  }

  //Fix 04034 SL - Aggiunta gestione costo riferimento e costo unitario
  protected void impostaCostoUnitario(OrdineVenditaRigaPrm riga) {
    PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();

    char tipoCostoRiferimento = pdv.getTipoCostoRiferimento();
    riga.setTipoCostoRiferimento(tipoCostoRiferimento);
    switch(tipoCostoRiferimento) {
      case TipoCostoRiferimento.NESSUN_COSTO:
        riga.setCostoUnitario(new BigDecimal(0.0));
        break;
      case TipoCostoRiferimento.COSTO_STANDARD:
      case TipoCostoRiferimento.COSTO_MEDIO:
      case TipoCostoRiferimento.COSTO_ULTIMO:
        impostaCostoUnitarioSaldiMagazzino(riga, tipoCostoRiferimento);
        break;
      case TipoCostoRiferimento.COSTO_DA_ARTICOLO:
        impostaCostoUnitarioDaArticolo(riga, pdv.getTipoCosto().getIdTipoCosto());
        break;
    }
  }

  protected void impostaCostoUnitarioSaldiMagazzino(OrdineVenditaRigaPrm riga, char tipoCostoRiferimento) {
    BigDecimal costoUnitario = null;
    String idConfigurazione = (riga.getIdConfigurazione() == null) ? "0" : riga.getIdConfigurazione().toString();

    String key =
      KeyHelper.buildObjectKey(
      new String[] {
      Azienda.getAziendaCorrente(),
      riga.getIdMagazzino(),
      riga.getIdArticolo(),
      riga.getIdVersioneSal() != null ? riga.getIdVersioneSal().toString() : null, //Mod. 04039 SL
      idConfigurazione,
      SaldoMag.OPERAZIONE_DUMMY
    }
      );

    SaldoMag saldoMag = null;
    try {
      saldoMag = SaldoMag.elementWithKey(key, PersistentObject.NO_LOCK);
    }
    catch(SQLException e) {}
    if(saldoMag != null) {
      DatiSaldo datiSaldo = saldoMag.getDatiSaldo();

      switch(tipoCostoRiferimento) {
        case TipoCostoRiferimento.COSTO_STANDARD:
          costoUnitario = datiSaldo.getCostoStandard();
          break;
        case TipoCostoRiferimento.COSTO_MEDIO:
          costoUnitario = datiSaldo.getCostoMedio();
          break;
        case TipoCostoRiferimento.COSTO_ULTIMO:
          costoUnitario = datiSaldo.getCostoUltimo();
          break;
      }
    }
    else {
      costoUnitario = new BigDecimal(0.0);
    }
    riga.setCostoUnitario(costoUnitario);
    Trace.print("costo unitario=" + costoUnitario);
  }

  protected void impostaCostoUnitarioDaArticolo(OrdineVenditaRigaPrm riga, String idTipoCosto) {
    BigDecimal costoUnitario = new BigDecimal(0.0);

    Integer idVersione = (riga.getIdVersioneSal() == null ? ContenitoreRiga.VERSIONE_DUMMY : riga.getIdVersioneSal());
    Integer idConfigurazione = (riga.getIdConfigurazione() == null) ? null : riga.getIdConfigurazione();
    ArticoloCosto articoloCosto =
      ArticoloCosto.elementWithKey(
      Azienda.getAziendaCorrente(),
      riga.getIdArticolo(),
      idVersione,
      idConfigurazione,
      idTipoCosto
      );
    if(articoloCosto != null)
      costoUnitario = articoloCosto.getCosto();
    riga.setCostoUnitario(costoUnitario);
  }

  //Fix 04034 SL - fine

  //Fix 4206 PM - Inizio
  protected void impostaArticoloCliente(OrdineVenditaRigaPrm ordVenRig) {
    ArticoloCliente artCli = (ArticoloCliente)ordVenRig.getArticoloIntestatario();
    if(artCli == null)
      return;
    String keyArticoloCliente = artCli.getKey();
    if(!iMapArtCliente.containsKey(keyArticoloCliente))
      iMapArtCliente.put(keyArticoloCliente, artCli);
    //else { 72676 SOF3 remmo
    if(iMapArtCliente.containsKey(keyArticoloCliente)) { //72676 SOF3 nuovo blocco che corregge lo std
      artCli = (ArticoloCliente)iMapArtCliente.get(keyArticoloCliente);
      ordVenRig.setArticoloIntestatario(artCli);
      ordVenRig.setAlfanumRiservatoUtente1(artCli.getArticoloPerCliente()); //72676 SOF3
    }
  }

  //Fix 4206 PM - Inizio

//MG FIX 12472 inizio
  protected List checkUMRigaImport(Articolo articolo, String gwUmVen, String gwUmPrm, String gwUmSec) {
    List errorList = new Vector();
    boolean errore = false;
    if (articolo == null)
      return null;
    String artUmVen = (articolo.getUMDefaultVendita() != null) ? articolo.getUMDefaultVendita().getIdUnitaMisura() : null;
    String artUmPrm = articolo.getIdUMPrmMag();
    String artUmSec = articolo.getIdUMSecMag();

/* test su unità di misura di riferimento */
    if (gwUmVen == null) {
/*
ErrorMessage errMessage = new ErrorMessage("THIP300268");
errorList.add(errMessage);
*/
      gwUmVen = artUmVen;
    }
    else {
      List ll = new ArrayList(articolo.getArticoloDatiVendita().getUMVenditeSecondarie());
      if (ll != null && !UnitaMisura.isUMNellaLista(gwUmVen, ll)) {
        ErrorMessage errMessage = new ErrorMessage("THIP300269");
        errorList.add(errMessage);
      }
    }

/* test su unità di misura prm di magazzino */
    if (gwUmPrm == null) {
/*
ErrorMessage errMessage = new ErrorMessage("THIP300270");
errorList.add(errMessage);
*/
      gwUmPrm = artUmPrm;
    }
    else {
      if (artUmPrm != null && !artUmPrm.equals(gwUmPrm)) {
        ErrorMessage errMessage = new ErrorMessage("THIP300271");
        errorList.add(errMessage);
      }
    }

/* test su unità di misura sec di magazzino */
    if (artUmSec != null) {
      if (gwUmSec == null) {
        gwUmSec = artUmSec;
/*
ErrorMessage errMessage = new ErrorMessage("THIP300272");
errorList.add(errMessage);
*/
      }
      else {
        if (!artUmSec.equals(gwUmSec)) {
          ErrorMessage errMessage = new ErrorMessage("THIP300273");
          errorList.add(errMessage);
        }
      }
    }
    return errorList;
  }

  protected List checkCodiceLotto(Gwror00f gwror00f) {
    List errorList = new Vector();
    if (gwror00f.getIdLotto() != null && !gwror00f.getIdLotto().equals(Lotto.LOTTO_DUMMY)) {
      String codiceAzienda = gwror00f.getRoidaz();
      String codiceArticolo = gwror00f.getRocoar();
      String codiceLotto = gwror00f.getIdLotto();
      Object[] keyParts = {codiceAzienda, codiceArticolo, codiceLotto};
      try {
        Lotto lotto = Lotto.elementWithKey(KeyHelper.buildObjectKey(keyParts), PersistentObject.NO_LOCK);
        if (lotto == null)
          errorList.add(new ErrorMessage("THIP300267"));
      } catch(SQLException e) {
      }
    }
    return errorList;
  }

//MG FIX 12472 fine
  
//MG FIX 12775 inizio
  protected void aggiornaTabellaStampa(Gwtor00f gwtor00f) {
    StringBuffer errBuff = new StringBuffer();
    try {
      RptErrorImportOrd rptErr = (RptErrorImportOrd)Factory.createObject(RptErrorImportOrd.class);
      rptErr.setBatchJobId(getBatchJobId());
      rptErr.setReportNr(getReportNr());
      int rigaJob = getRigaJobId() + 1;
      rptErr.setRigaJobId(rigaJob);
      setRigaJobId(rigaJob);

      // dati di testata
      rptErr.setIdAzienda(gwtor00f.getToidaz());
      rptErr.setFonte(gwtor00f.getTofoor());
      rptErr.setDescrFonte(gwmso00f.getFonteOrigine().getDescrizione());
      rptErr.setDataCarrello(gwtor00f.getTocard());
      rptErr.setNumeroCarrello(gwtor00f.getTocarp());
      rptErr.setIdCliente(gwtor00f.getTocliw());
      if (gwtor00f.getTocliw() != null && gwtor00f.getCliente() != null) {
        DatiAnagrafici datiAnag = gwtor00f.getCliente().getDatiAnagraficiValidiAl(gwtor00f.getTocard());
        if (datiAnag != null)
          rptErr.setRagioneSociale(datiAnag.getRagioneSociale());
      }
      rptErr.setAnnoOrdine(gwtor00f.getIdAnnoOrdine());
      rptErr.setNumeroOrdine(gwtor00f.getIdNumeroOrdine());

      //recupero errori di testata
      List errTestata = gwtor00f.getErrori();
      if (errTestata != null && errTestata.size() > 0) {
        errBuff.append(ResourceLoader.getString(prop, "ErroriSuTestata") + "\n");
        Iterator it = errTestata.iterator();
        while(it.hasNext()) {
          GwErrImpTes elem = (GwErrImpTes)it.next();
          errBuff.append(elem.getDescrizione() + "\n");
        }
      }
      //recupero errori righe
      List righe = gwtor00f.getRighe();
      if (righe != null && righe.size() > 0) {
        Iterator iterRighe = righe.iterator();
        while(iterRighe.hasNext()) {
          Gwror00f riga = (Gwror00f)iterRighe.next();
          List errRiga = riga.getErroriRiga();
          if (errRiga != null && errRiga.size() > 0) {
            errBuff.append(ResourceLoader.getString(prop, "ErroriSuRighe") + "\n");
            Iterator it = errRiga.iterator();
            while(it.hasNext()) {
              GwErrImpRig elem = (GwErrImpRig)it.next();
              Object[] param = {new Integer(elem.getProgRigaCarr().intValue())};
              errBuff.append(ResourceLoader.getString(prop, "NumeroRigaCarrello", param));
              errBuff.append(elem.getDescrizione() + "\n");
            }
          }
        }
      }
      else {
        errBuff.append(ResourceLoader.getString(prop, "ErroreNoRigheOrdine"));
      }
      if (errBuff.length() > 0){
    	  //Fix 26993 inizio
    	  String str = errBuff.toString();
    	  if(str.length()> 1024)
    		  str = str.substring(0,1024);
    	  rptErr.setTesto(str);
    	  //rptErr.setTesto(errBuff.toString());
    	  //Fix 26993 fine
      }
      else {
        if (gwtor00f.getIdNumeroOrdine() == null || gwtor00f.getIdNumeroOrdine().equals("")) {
          errBuff.append(ResourceLoader.getString(prop, "ErroreInGenerazioneOrdine"));
          rptErr.setTesto(errBuff.toString());
        }
      }
      rptErr.save();
      ConnectionManager.commit();
    } catch(Exception e) {
      e.printStackTrace(Trace.excStream);
    }
  }

  protected void checkNumeroRigheImportate(OrdineVenditaTestata testata, Gwtor00f gwtor00f) throws SQLException{
    //if (iNumeroRigheDaImportare != testata.getRighe().size())//Fix 48867
	if (iNumeroRigheDaImportare != (testata.getRighe().size() - testata.getNumeroRigheSpesaGenerateAutomaticamente()))//Fix 48867
      compilaGwErrorImpTes(new ErrorMessage("THIP300300", (new Integer(testata.getRighe().size())).toString()), gwtor00f);
  }
 //Fix 24574 inizio
  protected void checkBloccoEmissioneOrdImportate(OrdineVenditaTestata testata, Gwtor00f gwtor00f) throws SQLException{
	    MotivoBloccoOrdine motivoBlocco= testata.getMotivoBloccoImmissioneDocumento();
	    if (motivoBlocco!=null && motivoBlocco.getTipoBlocco() ==MotivoBloccoOrdine.ACQUISIZIONE_AMMESSA_CON_APPROVAZIONE){//Evasione ammessa ma soggetta ad accettazione
	    	compilaGwErrorImpTes(new ErrorMessage("THIP40T512",motivoBlocco.getDescrizione().getDescrizione()), gwtor00f);
	    }  	
  }
  //Fix 24574 fine
  //Fix 38861 Inizio
  protected void checkFidoForzatoPerCliente(OrdineVenditaTestata testata , Gwtor00f gwtor00f) throws SQLException{
	  List righe = testata.getRighe();
	  Iterator i = righe.iterator();
	  while(i.hasNext()) {
		  OrdineVenditaRiga riga = (OrdineVenditaRiga) i.next();
		  //for(int j=0 ; j <= riga.getWarningList().size() ; j++) { //FIx 39218 PM
		  for(int j=0 ; j < riga.getWarningList().size() ; j++) { //FIx 39218 PM
			  ErrorMessage  err = (ErrorMessage) riga.getWarningList().get(j);
			  if(err.getId().equals("THIP200030"))//Worning controllo fido
			  {
				  compilaGwErrorImpTes(err , gwtor00f);
				  getWarnings().add(err);
				  return;
			  }
		  }
	  }
  }
  //Fix 38861 Fine
  protected GwErrImpTes compilaGwErrorImpTes(ErrorMessage errMess, Gwtor00f gwtor00f) throws SQLException{
    GwErrImpTes gwError = (GwErrImpTes)Factory.createObject(GwErrImpTes.class);
    gwError.setIdAzienda(gwtor00f.getToidaz());
    gwError.setFonteOrigine(gwtor00f.getTofoor());
    gwError.setDataCarr(gwtor00f.getTocard());
    gwError.setProgCarr(gwtor00f.getTocarp());
    String errore = errMess.getText();
    if(errore.length() > 50)
      errore = errore.substring(0, 50);
    gwError.setDescrRidotta(errore);
    gwError.setDescrizione(errMess.getAttOrGroupName() !=null ? errMess.getAttOrGroupName() + ": " + errMess.getLongText() : errMess.getLongText());
    getLog().appendMessage(errMess.getLongText(), true);
    gwError.save();
    return gwError;
  }

  protected GwErrImpRig compilaGwErrorImpRig(ErrorMessage errMess, Gwror00f gwror00f) throws SQLException{
    GwErrImpRig gwError = (GwErrImpRig)Factory.createObject(GwErrImpRig.class);
    gwError.setIdAzienda(gwror00f.getRoidaz());
    gwError.setFonteOrigine(gwror00f.getRofoor());
    gwError.setDataCarr(gwror00f.getRocard());
    gwError.setProgCarr(gwror00f.getRocarp());
    gwError.setProgRigaCarr(gwror00f.getRocarr());
    String errore = errMess.getText();
    if(errore.length() > 50)
      errore = errore.substring(0, 50);
    //gwError.setDescrRidotta(errMess.getText()); //Fix 22828 PM
    gwError.setDescrRidotta(errore);//Fix 22828 PM
    gwError.setDescrizione(errMess.getAttOrGroupName() !=null ? errMess.getAttOrGroupName() + ": " + errMess.getLongText() : errMess.getLongText());
    gwError.save();
    return gwError;
  }

//MG FIX 12775 fine

  //Fix14727 Inizio RA
  public boolean isEm(String tmp){
    return (tmp != null && !tmp.equals("")) ? false :true;
  }
  //Fix14727 Inizio RA
  //Fix 14857 inizio
  public void completaDatiDaOrdine(OrdineVenditaTestata testata, List listRigGwror00f) throws SQLException {
    for (int iRigGwror00f = 0; iRigGwror00f < listRigGwror00f.size(); iRigGwror00f++) {
      Gwror00f gwror00fSave = (Gwror00f) listRigGwror00f.get(iRigGwror00f);
      gwror00fSave.setIdAnnoOrdine(testata.getAnnoDocumento());
      gwror00fSave.setIdNumeroOrdine(testata.getNumeroDocumento());
      OrdineVenditaRigaPrm ord= (OrdineVenditaRigaPrm)(testata.getRighe().get(iRigGwror00f));
      gwror00fSave.setIdDetRigaOrd(ord.getDettaglioRigaDocumento().intValue());
      gwror00fSave.setIdRigaOrdine(ord.getNumeroRigaDocumento().intValue());
      gwror00fSave.save();
    }

  }
  //Fix 14857 fine
  //Fix 15658 inizio
  public int eliminaOrdineVenditaCreato(OrdineVenditaTestata ordVen) throws SQLException{
     int ret=0;
       if(ordVen.retrieve())
         ret=ordVen.delete();
     return ret;
  }

  //Fix 15658 fine
  //Fix 16029 inizio
public void impostaProvvigioniAgente(OrdineVenditaTestata ordVenTes, OrdineVenditaRigaPrm ordVenRig)  {
  try{

      DecimalType dt = new DecimalType();
      Double provvAgente = null;
      Double provvSubagente = null;
      BigDecimal provvAgenteBD = null;
      String idLineaProdotto = null;
      if (ordVenRig.getArticolo()!=null )
    	  idLineaProdotto = ordVenRig.getArticolo().getIdLineaProdotto();
       if (idLineaProdotto != null && !idLineaProdotto.equals("") ){
         RicercatoreAgentiProvvigioni rap = new RicercatoreAgentiProvvigioni();

         if (ordVenTes.getProvvigioneAgente() != null && !ordVenTes.getProvvigioneAgente().equals(""))
           provvAgente = (Double) (dt.stringToObject(ordVenTes.getProvvigioneAgente().toString()));

         if (ordVenTes.getProvvigioneSubagente() != null && !ordVenTes.getProvvigioneSubagente().equals(""))
           provvSubagente = (Double) (dt.stringToObject(ordVenTes.getProvvigioneSubagente().toString()));

         if (provvAgente != null) {
           provvAgenteBD = new BigDecimal(provvAgente.doubleValue());
           provvAgenteBD = provvAgenteBD.setScale(2, BigDecimal.ROUND_HALF_UP);
         }
         BigDecimal provvSubagenteBD = null;
         if (provvSubagente != null) {
           provvSubagenteBD = new BigDecimal(provvSubagente.doubleValue());
           provvSubagenteBD = provvSubagenteBD.setScale(2, BigDecimal.ROUND_HALF_UP);
         }
         AgentiProvvigioni agentiProvvigioni =
           rap.ricercaAgentiProvvigioni(
             ordVenTes.getIdCliente(),
             ordVenTes.getIdDivisione(),
             idLineaProdotto,
             ordVenTes.getIdAgente(),
             provvAgenteBD,
             ordVenTes.getIdAgenteSub(),
             provvSubagenteBD
           );

         Agente agente = agentiProvvigioni.getAgente();
         if (agente != null) {
           ordVenRig.setIdAgente(agente.getIdAgente());
           ordVenRig.setProvvigione1Agente(agentiProvvigioni.getProvvigioniAgente());
         }
         Agente subAgente = agentiProvvigioni.getSubagente();
         if (subAgente != null) {
           ordVenRig.setIdSubagente(subAgente.getIdAgente());
           ordVenRig.setProvvigione1Subagente(agentiProvvigioni.getProvvigioniSubagente());
         }
       }
  }
  catch (Exception ex){
     ex.printStackTrace();
  }
}

public void completaDatiDaRiga(OrdineVenditaRigaPrm ordVenRig,Gwror00f gwror){
  if(gwror.getRoagen() != null && !gwror.getRoagen().equals("")) {
   ordVenRig.setIdAgente(gwror.getRoagen());
  }
  if(gwror.getRopepr() != null && !gwror.getRopepr().equals("")) {
    ordVenRig.setProvvigione1Agente(gwror.getRopepr());
  }
  if(gwror.getRoage1() != null && !gwror.getRoage1().equals("")) {
    ordVenRig.setIdSubagente(gwror.getRoage1());
  }
  if(gwror.getRopep1() != null && !gwror.getRopep1().equals("")) {
   ordVenRig.setProvvigione1Subagente(gwror.getRopep1());
  }
}
//Fix 16029 fine

//Fix 19768 PM >
protected void assegnaDatiDestinazione(OrdineVenditaTestata ordVenTes, Gwtor00f gwtor)
{
   if(gwtor.getToclsp() != null && !gwtor.getToclsp().equals("")) {
       ordVenTes.setIdDenAbt(gwtor.getToclsp());
       ordVenTes.setRagioneSocaleDest(null);
       ordVenTes.setIndirizzoDestinatario(null);
       ordVenTes.setLocalitaDestinatario(null);
       ordVenTes.setCAPDestinatario(null);
       ordVenTes.setIdNazioneDen(null);
       ordVenTes.setIdProvinciaDen(null);
       ordVenTes.setIdSequenzaInd(null);
  }
  //Fix 20445 PM > 
  else if (gwtor.getIdSeqIndirizzo() != null)
  {
      ordVenTes.setIdDenAbt(null);
      ordVenTes.setRagioneSocaleDest(null);
      ordVenTes.setIndirizzoDestinatario(null);
      ordVenTes.setLocalitaDestinatario(null);
      ordVenTes.setCAPDestinatario(null);
      ordVenTes.setIdNazioneDen(null);
      ordVenTes.setIdProvinciaDen(null);
      ordVenTes.setIdSequenzaInd(gwtor.getIdSeqIndirizzo());	  
      ordVenTes.getIndirizzo();	  
  }
  //Fix 20445 PM < 
  else if (gwtor.getTorasp() != null && !gwtor.getTorasp().equals("")){
	  //Fix 43525 Inizio
	  String ragioneSocialeCli = troncaStringa(ordVenTes.getCliente().getRagioneSociale(),50);
	  String indCliente = ordVenTes.getCliente().getIndirizzo();
	  if(Utils.areEqual(ragioneSocialeCli, gwtor.getTorasp()) && Utils.areEqual(indCliente, gwtor.getToinsp()))
	  {
	      ordVenTes.setRagioneSocaleDest(null);
	      ordVenTes.setIndirizzoDestinatario(null);
	      ordVenTes.setLocalitaDestinatario(null);
	      ordVenTes.setCAPDestinatario(null);
	      ordVenTes.setIdNazioneDen(null);
	      ordVenTes.setIdProvinciaDen(null);
	  }
	  else
	  {
	  //Fix 43525 Fine
		  if(gwtor.getTorasp().length() > 35)//50341
			  ordVenTes.setRagioneSocaleDest(gwtor.getTorasp().substring(0, 35));//50341
		  //ordVenTes.setRagioneSocaleDest(gwtor.getTorasp());//50341
		  if(gwtor.getToinsp() != null && gwtor.getToinsp().length() > 35)//50341
		  	ordVenTes.setIndirizzoDestinatario(gwtor.getToinsp().substring(0, 35));//50341
		  //ordVenTes.setIndirizzoDestinatario(gwtor.getToinsp());//50341
	      ordVenTes.setLocalitaDestinatario(gwtor.getTolosp());
	      ordVenTes.setCAPDestinatario(gwtor.getTocasp());
	      ordVenTes.setIdNazioneDen(gwtor.getTocnaz());
	      ordVenTes.setIdProvinciaDen(gwtor.getToprsp());
	  }//Fix 43525
      ordVenTes.setClienteDestinatario(null);
      ordVenTes.setIdSequenzaInd(null);
  }
   //Fix 20813 inizio
  else if(gwtor.getIdSeqIndirizzo()!=null && !gwtor.getIdSeqIndirizzo().equals("")){
      ordVenTes.setIdSequenzaInd(gwtor.getIdSeqIndirizzo());
      ordVenTes.setRagioneSocaleDest(null);
      ordVenTes.setIndirizzoDestinatario(null);
      ordVenTes.setLocalitaDestinatario(null);
      ordVenTes.setCAPDestinatario(null);
      ordVenTes.setIdNazioneDen(null);
      ordVenTes.setIdProvinciaDen(null); 	
  }
   //Fix 20813 fine

}


protected OrdineVenditaRigaPrm assegnaDatiRigaSpesa(OrdineVenditaTestata ordVenTes, OrdineVenditaRigaPrm ordVenRig, Gwror00f gwror) 
{
 //Implementare settado la qta il prezzo
	ordVenRig.setQtaInUMRif(gwror.getQtaInUMRif());
	ordVenRig.setPrezzo(gwror.getRoprez());
	ordVenRig.setImportoPercentualeSpesa(gwror.getRoprez());
	ordVenRig.setSpesaPercentuale(gwror.getRotmov());
	if(gwror.getRoasfi() != null) //28105	
		ordVenRig.setIdAssogIVA(gwror.getRoasfi()); //28105	
	return ordVenRig;
}

protected String trovaCausaleSpesa(OrdineVenditaTestata ordVenTes, String idCauRiga)
{
	//cercare la prima causale di riga di spesa nelle causali associate alla causale di testata
	CausaleOrdineVendita cauOrd = ordVenTes.getCausale();
	String idCausale = null;
	if (cauOrd== null) return null;
	if (idCauRiga == null)
	    idCauRiga = "";
	Iterator i = cauOrd.getCausaliRiga().iterator();
	CausaleRigaOrdVen cauRiga = null;
	while (i.hasNext())
	{
		cauRiga = (CausaleRigaOrdVen)i.next();
		if (cauRiga.getTipoRiga() == TipoRiga.SPESE_MOV_VALORE )
		{
			String idCau = cauRiga.getIdCausaleRigaOrdineVen();
	        if (idCau.trim().equals(idCauRiga))
	          return idCau;
	        else 
	          idCausale = idCau;
	    }  
	}
    return idCausale;

}





//Fix 19768 PM <
//Fix 20813 inizio
 public int checkEsistenzaCliente(Gwtor00f gwtor00f,OrdineVenditaTestata ordVen){
	 int ret =BODataCollector.OK;
	 if (!esisteClienteInPth(gwtor00f)){
		  if(gwmso00f.isImportaCliCont()){
			ret =BODataCollector.ERROR;
		  	String idRubricaContatti = recuperaIdRubricaContatti(gwtor00f.getTocliw());
		  	   
		  	if(idRubricaContatti == null) {
	
		  		 boolean esisteInCM = verificaEsistanzaInCMContatti((gwtor00f));
		  		 String tabellaContatti = WpuRubricaContattiTM.TABLE_NAME;		  		 
		  		 if(!esisteInCM)
		  			 tabellaContatti =SystemParam.getSchema("THIP")+ CMWpuRubricaContatti.CM_TABLE_NAME;
		  		  String params[]={gwtor00f.getTocliw(),tabellaContatti};
		  		  compilaError(new ErrorMessage("THIP40T335",params),gwtor00f);	 
		  	}	
		  	
		  	else{
		  	    String idAnagrafica = recuperaIdAnagrafico(idRubricaContatti);
		  	    if(idAnagrafica == null) {
		  	    	  String params[]={idRubricaContatti};
		  	    	  compilaError(new ErrorMessage("THIP40T336",params), gwtor00f);
		  	    	}
		  	    
		  	    else{
		  	    	String idCliente = recuperaIdCliente(idAnagrafica);
		  	    	if(idCliente ==null){ 
		  	    		compilaError(new ErrorMessage("THIP40T337",idAnagrafica), gwtor00f);
		  	    	}
		  	    	
		  	      else {
		  	    	   ordVen.setIdCliente(idCliente);
		  	    	   ordVen.setIdAnagrafico(new Integer(idAnagrafica));
		  	    	   gwtor00f.setTocliw(idCliente);
		  	    	   //gwtor00f.setTorags(GestioneEsportaOrdVenDaSellMore.ESIST_OB+idCliente);
		  	    	   ret = BODataCollector.OK;
		  	    		 		  	    	
		  	    	}
		  	    }
		   }	
		  }		 
	 }
	 if(ret == BODataCollector.OK) 
	  ret = checkEsistenzaIndirizzo(ordVen ,gwtor00f,ret); 
	 return ret;
 }
 
 public boolean esisteClienteInPth(Gwtor00f gwtor00f){
   //return (gwtor00f.getTorags()!=null &&
  //		     gwtor00f.getTorags().equals(GestioneEsportaOrdVenDaSellMore.ESIST_OB+ gwtor00f.getTocliw()));
	 
	   return gwtor00f.getCliente() != null;

 }
 
 public synchronized String  recuperaIdRubricaContatti (String idCliente){
   Database db = ConnectionManager.getCurrentDatabase();
   try{
 	  PreparedStatement ps = cClassifCliSistEst.getStatement();
	  db.setString(ps,1,Azienda.getAziendaCorrente()); 
	  db.setString(ps,2,idCliente);  
	  ResultSet rs = cClassifCliSistEst.executeQuery();
	  if(rs.next()) return rs.getString(1);
   }
   catch (Exception ex){
  	 ex.printStackTrace(Trace.excStream);
   }
	 return null;
 }
 
 public boolean  verificaEsistanzaInCMContatti(Gwtor00f gwtor00f){
	  //Fix 28235 inizio
	  boolean esistInCM = esisteClienteInCMContatti(gwtor00f.getTocliw()); 
	  if (esistInCM)
		  iNuoviClientiInCM = true;
 	  //Fix 28235 Fine
	  if(!esistInCM && gwtor00f.getTorags() != null && !gwtor00f.getTorags().equals("")){
	  	  inserireClienteNellaCM(gwtor00f); 
	  	 return false;   
	  }
	 return true; 	 
 }

 
 public synchronized boolean  esisteClienteInCMContatti(String idCliente){
   Database db = ConnectionManager.getCurrentDatabase();
   int num =0;
   try{
   PreparedStatement ps = cClassifCliSistEstCM.getStatement();
   db.setString(ps,1,Azienda.getAziendaCorrente()); 
   db.setString(ps,2,idCliente);  
   ResultSet rs = cClassifCliSistEstCM.executeQuery();
   if(rs.next())
     num = rs.getInt(1);
   return num>0;
   }
   
   catch (Exception ex){
  	 ex.printStackTrace(Trace.excStream);
   }
	 return false;
 }
 
 public  synchronized void inserireClienteNellaCM(Gwtor00f gwtor00f){
  try{
   PreparedStatement ps = stmtInsert.getStatement();
   Database db = ConnectionManager.getCurrentDatabase();
   db.setString(ps,1,DATA_ORIGIN);
   //iRowId +=iRowId ;
   iRowId = Numerator.getNextInt("CONTATTI_IMP_ORD");
   Trace.print("inserireClienteNellaCM  iRowId " + iRowId);
   ps.setInt(2,RUN_ID);
   ps.setInt(3, iRowId);
   db.setString(ps,4,"I");
   db.setString(ps,5,"0");
   //ps.setInt(6, 0); //Fix 26033
   ps.setInt(6, iRowId); //Fix 26033
   db.setString(ps,7,gwtor00f.getTocliw());
   db.setString(ps,8,gwtor00f.getTocliw());
   db.setString(ps,9,troncare(gwtor00f.getTorags(),35));//Fix 28235 add la chiamata di troncare 
   db.setString(ps,10,gwtor00f.getTopaiv());
   db.setString(ps,11,troncare(gwtor00f.getToindi(),35));//Fix 28235 add la chiamata di troncare
   db.setString(ps,12,gwtor00f.getToloca());
   db.setString(ps,13,gwtor00f.getTocavp());
   db.setString(ps,14,gwtor00f.getToprov());
   db.setString(ps,15,gwtor00f.getTotelf());
   db.setString(ps,16,gwtor00f.getTotfax());
   db.setString(ps,17,gwtor00f.getTomail());  
   db.setString(ps,18,troncare(gwtor00f.getToagen(),3));//Fix 28235 add la chiamata di troncare
   //db.setString(ps,19,"WEB");  //Fix 26743 PM
   db.setString(ps,19,gwtor00f.getSitoWeb()); //Fix 26743 PM
   db.setString(ps,20,troncare(gwtor00f.getToczon(),3));//Fix 28235 add la chiamata di troncare
   db.setString(ps,21,troncare(gwtor00f.getTocnaz(),3));//Fix 28235 add la chiamata di troncare
   //db.setString(ps,22,"CellularPhone");  //Fix 26743 PM
   db.setString(ps,22,gwtor00f.getCellulare()); //Fix 26743 PM
   db.setString(ps,23,"A");
   db.setString(ps,24,"N");
   db.setString(ps,25,"N"); 
   db.setString(ps,26,"N");
   db.setString(ps,27,"N");
   db.setString(ps,28,"N"); 
   db.setString(ps,29,Azienda.getAziendaCorrente());
   ps.setTimestamp(30,getCurrentTimestamp());
   ps.setString(31,gwtor00f.getTocofi()); //Fix 26743 PM
   
 //Fix 35787 inizio
   if(esisteBanca(gwtor00f.getTocabi(),gwtor00f.getToccab()))
   {
	   ps.setString(32,gwtor00f.getTocabi()); //Fix 27814 PM
	   ps.setString(33,gwtor00f.getToccab()); //Fix 27814 PM
   }
   else
   {
	   ps.setString(32,null);
	   ps.setString(33,null);
   }
 //Fix 35787 fine
   
   ps.setString(34,troncare(gwtor00f.getTodcab(),34)); //Fix 27814 PM //Fix 28235 add la chiamata di troncare
   ps.setLong(35, gwtor00f.getIdIndPrSistEst());//Fix 31669
   int rc = ps.executeUpdate();
   Trace.println("inserireClienteNellaCM  rc " + rc);
   iNuoviClientiInCM = true;
  }
   catch (Exception ex){
  	 ex.printStackTrace(Trace.excStream);
   }
 }
 
 //Fix 28235 inizio
 public static String troncare(String val, int numCar)
 {
	 if (val == null || val.length()<= numCar)
		 return val;
	 
	 return val.substring(0, numCar);
 }
 // Fix 28235
 
 public void compilaError(ErrorMessage error ,Gwtor00f gwtor00f){
	 try{
     compilaGwErrorImpTes(error, gwtor00f);
   }
	 
  catch(SQLException ex)
  {
     ex.printStackTrace(Trace.excStream);
  }	 
 }
 
 public  synchronized String recuperaIdAnagrafico(String idRubCont){
   Database db = ConnectionManager.getCurrentDatabase();
   ResultSet rs = null;
   try{
 	  PreparedStatement ps = cAnaga.getStatement();
	  db.setString(ps,1,Azienda.getAziendaCorrente()); 
	  db.setString(ps,2,idRubCont);  
	  rs = cAnaga.executeQuery();
	  if(rs.next()) return rs.getString(1);
   }
   catch (Exception ex){
  	 ex.printStackTrace(Trace.excStream);
   }
   
   finally {
			if (rs != null) {

				try {
					
					rs.close();
				}
				
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
	 }	
	 return null;	 
	 
 }
 
 public  synchronized String recuperaIdCliente(String idAnag){
   Database db = ConnectionManager.getCurrentDatabase();
   ResultSet rs = null;
   try{
 	  PreparedStatement ps = cCli.getStatement();
	  db.setString(ps,1,Azienda.getAziendaCorrente()); 
	  db.setString(ps,2,idAnag);  
	  rs = cCli.executeQuery();
	  if(rs.next()) return rs.getString(1);
   }
   catch (Exception ex){
  	 ex.printStackTrace(Trace.excStream);
   }
   
   finally {
		if (rs != null) {
			try {					
			     rs.close();
				}
				
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
	 }	  

	 return null;	 
	 
 }
 
 public int checkEsistenzaIndirizzo(OrdineVenditaTestata ordVen ,Gwtor00f gwtor00f, int ret){
	 return ret;
 }
 
 
 public ErrorMessage lancioCMContatto(){

	 BatchLoader  cmwContatto = null;
	 ErrorMessage err =null;
   BatchOptions batchOptions = (BatchOptions)Factory.createObject(BatchOptions.class);
   cmwContatto = (CMWpuRubricaContatti)Factory.createObject(CMWpuRubricaContatti.class);
   cmwContatto.getRunParameter().setDataOrigin(DATA_ORIGIN);
   cmwContatto.getRunParameter().setRunId(RUN_ID);
   cmwContatto.getRunParameter().setPrintError(true);
   cmwContatto.getRunParameter().setPrintValidData(true);
   try{
       batchOptions.initDefaultValues(CMWpuRubricaContatti.class, "CMWpuRubContat", "RUN");
       cmwContatto.setReportId("CMErrContatto");// to do in persdati
       cmwContatto.setBatchExecution(batchOptions.getExecutionMode() != 'S');
       cmwContatto.setBatchJob(batchOptions.getBatchJob());
       cmwContatto.setScheduledJob(batchOptions.getScheduledJob());
       cmwContatto.setExecutePrint(false);
       int rc = cmwContatto.save();
       if(rc < 0) {
         ConnectionManager.rollback();
         err = new ErrorMessage("BAS0000078", "Errore durante la salva di CM Contatto ");
       }
       else
         ConnectionManager.commit();
			 	 
	      boolean ritorno = false;
	      if(cmwContatto.getBatchJob() != null)
	        ritorno = BatchService.submitJob(cmwContatto.getBatchJob());
	      if(!ritorno)
	        Trace.excStream.println("Errore durante la sottomissione di CM Contatto  ");
	      if(cmwContatto.getBatchJob().retrieve()) {
	        if(cmwContatto.getBatchJob().getStatus() == BatchJob.COMPLETED_ERROR) {
	          String msgErr = "Errore durante la stampa (vedi log batch)";
	          err = new ErrorMessage("BAS0000078", msgErr);
	        }
	      }		 
		}
			 
			 catch(Exception ex){
				 ex.printStackTrace();
			 }
  return err;
 }
 
  public int initializeRowId(){
    Database db = ConnectionManager.getCurrentDatabase();
    int rowId=0;

    try{
  	 PreparedStatement ps = cMaxRowId.getStatement();
 	  db.setString(ps,1,DATA_ORIGIN); 
 	  ps.setInt(2, RUN_ID);
 	  ResultSet rs = cMaxRowId.executeQuery();
 	  if(rs.next()){ 
 	  	iRowId =rs.getInt(1); 	  
 	  }
    }
    catch (Exception ex){
   	 ex.printStackTrace(Trace.excStream);
    }
 	 return rowId;	  	
  }
  
	public Timestamp getCurrentTimestamp() {
		java.util.Date date = new java.util.Date();
		return new Timestamp(date.getTime());
	}
	//Fix 20813 fine
	//Fix 22463 inizio
	 public boolean isRigaCommenti(Gwror00f gwror00f){
		 return false;
	 }
	 
	 public void gestioneRigaCommenti(Gwror00f gwror00f,OrdineVenditaTestata ordVen){
		 
	 }
	 
	 public int getNumeroRigheDaImportare(List listRighe){
		return listRighe.size();
	}
	//Fix 22463 fine

   //24273 inizio
   public void impostaParamsCVPers(CondizioniDiVenditaParams cdvParams) {
   }  
   //24273 fine	 
	//Fix 24574 inizio
	 protected boolean isCausaleTipoSpesa(String idCausale){
	   String[] causaleKey = {Azienda.getAziendaCorrente(),idCausale};
	   CausaleRigaOrdVen causale =null;
	    try {
		causale = CausaleRigaOrdVen.elementWithKey(KeyHelper.buildObjectKey(causaleKey), CausaleRigaOrdVen.NO_LOCK);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		if(causale!=null && causale.getTipoRiga()==TipoRiga.SPESE_MOV_VALORE)
			  return true;
		return false ; 
	 }
   //Fix 24574 fine	 
	//Fix 24715 inizio
	 public void creaOrderByConfermaOrdine(ReportConfermaOrdVenBatch confermaOrdine){
		 	 
		   BODataCollector intDC = (BODataCollector)Factory.createObject(BODataCollector.class);
	       intDC.initialize("ReportConfermaOrdVen", true);
	       intDC.loadAttValue();
           Vector colonneOrderBySel = new Vector();
           
	       ScreenData screenData = MDVManager.getScreenData(null, null, intDC.getClassADCollection().getClassName());
	       if ((screenData != null) && (screenData.getCompValues("CondizioniOrdinamento") != null)) {
	    	   colonneOrderBySel = getListaColonniOrderByDaMdv(screenData,confermaOrdine,intDC);
       	           
	        }
	       
	       else{
	    	   ColonneFiltri col = getColonnaOrderByDef(confermaOrdine.condOrdinamento,"NumeroOrdine");
	    	   if(col!=null)
	    	   colonneOrderBySel.add(col);
	       }
	    	  
	    confermaOrdine.condOrdinamento.setColonneSelezionate(colonneOrderBySel);  
		 
	 }
	
	  public ColonneFiltri  getColonnaOrderByDef(CondizioniOrdinamento condOrd , String classAdOrderBy){
		
		  ColonneFiltri Col =null;
		  Iterator iter = condOrd.getColonneOrderBy().iterator();
		  while (iter.hasNext()){
		      ColonneFiltri icf = (ColonneFiltri)iter.next();
		      if (icf.getClassAdName().equals(classAdOrderBy))
		         return icf;
		  }
		  
		   return Col;
	 }
	  
	 public Vector getListaColonniOrderByDaMdv(ScreenData screenData,ReportConfermaOrdVenBatch confermaOrdine,BODataCollector bodc){
  	   Vector keys = screenData.getCompValues("CondizioniOrdinamento");
  	   Vector  colonneOrderBySel = new Vector();
  	   String values = "";
       for (int y = 0; y < keys.size(); y++) {
      	 values = getVideoFormat((String)keys.elementAt(y));
      	 String colonneOrderBy =Utils.replace(values, "OrdineVendita", "") ;	        	           	   
      	 ColonneFiltri col = getColonnaOrderByDef(confermaOrdine.condOrdinamento,colonneOrderBy);
      	 if(col!=null)
      	  colonneOrderBySel.add(col);        		         	   
       }
      return colonneOrderBySel;   
	 }
	 
	 public String getVideoFormat(String txt) {
		 return txt.replace(PersistentObject.KEY_SEPARATOR.charAt(0),PersistentObject.VIDEO_KEY_SEPARATOR);
	 }
	//Fix 24715 fine


	 //Fix 30979 - inizio
	 protected boolean areCondizEliminaRecord() {
		//Fix 47377 ini
		 boolean eliminaRecord = false;
		 if(gwmso00f != null)
			 eliminaRecord = gwmso00fOrdineCliente != null ? gwmso00fOrdineCliente.getEliminaRecord() : gwmso00f.getEliminaRecord();
		 //return gwmso00f != null && gwmso00f.getEliminaRecord();//Fix 47377
		 return eliminaRecord;
		//Fix 47377 fini
	 }

	 
	 protected int esegueOperazioniPers(int rcOperStd, Gwtor00f gwtor, OrdineVenditaTestata testataOrdVen, List elencoGwrorSalvati, List elencoRigheOrdVenSalvate) throws SQLException {
		 return rcOperStd;
	 }
	 //Fix 30979 - fine

	 //33460 inizio
	 public List<ErrorMessage> getWarnings() {
		 return iWarnings;
	 }
	 public boolean hasWarnings() {
		 return !iWarnings.isEmpty();
	 }
	 //33460 fine
	 
	 //Fix 35787 inizio
	 public boolean esisteBanca(String abi, String cab){
		 BancaEsternaABICABPrimrose banca = null;
		 String bancaKey = KeyHelper.buildObjectKey(new String[] {abi,cab});
		try {
			banca = (BancaEsternaABICABPrimrose) BancaEsternaABICABPrimrose.elementWithKey(bancaKey, PersistentObject.NO_LOCK);
			if(banca != null)
				 return true;
			 else 
				 return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		 return true;
	 }
	 //Fix 35787 fine
	 
	 //40013 >
	 
	 protected void preProcessaTestataGw(Gwtor00f gwtor00f) {
		 
	 }
	 
	 //40013 <
	 
     // Fix 41042 PM
	 protected String getWhereRighe(Gwtor00f gwtor00f)
	 {
		Database db = ConnectionManager.getCurrentDatabase();
        String whereRiga = "ROCARD = " + db.getLiteral(gwtor00f.getTocard()) +
          " AND ROCARP = '" + gwtor00f.getTocarp() +
          "'" + getWhereCondizRiga(); 
        return whereRiga;
	 }

	 protected String getOrderByRighe(Gwtor00f gwtor00f)
	 {
        return "";
	 }
	 
	 protected List getRighe(Gwtor00f gwtor00f) throws Exception
	 {
        return Gwror00f.retrieveList(getWhereRighe(gwtor00f), getOrderByRighe(gwtor00f), false);
	 }
     // Fix 41042 PM
     //42785 inizio
	public void valorizzareNotaOrdini(OrdineVenditaTestata ordVenTes, Gwtor00f gwtor) {
		if(gwtor.getTocomm() != null && !gwtor.getTocomm().equals("")) {
	        String nota = "";
	        if(gwtor.getTocomm().length() <= 250)
	          nota = gwtor.getTocomm();
	        else
	          nota = gwtor.getTocomm().substring(0, 249);
	        ordVenTes.setNota(nota);
	      }
	}
	//42785 fine
	  //Fix 43525
	  protected String troncaStringa(String s, int lunghezza)
	  {
		  if (s != null && lunghezza > -1 && s.length() > lunghezza)
			  s = s.substring(0, lunghezza - 1);
		  return s;
	  }
	  //Fix 43525
	  //47380 inizio
	  public boolean IsAttivaRicalcoloProvvigione2Agenti() {
		  String atvRicalcoloProvvigione2Agenti = ParametroPsn.getValoreParametroPsn("std.PreOrdini", "AttivaRicalcoloProvvigione2Agenti");    
		  if(atvRicalcoloProvvigione2Agenti == null || atvRicalcoloProvvigione2Agenti.equals("N")) {
			  return false;
		  }
		  return true;
	  }
	  
	  public boolean esisteUnaValoreProvvigione2AgentiValorizzati(Gwror00f gwror) {
		  if((gwror.getRoagen() != null && !gwror.getRoagen().isEmpty()) || 
				  (gwror.getRopepr() != null && gwror.getRopepr().compareTo(new BigDecimal(0)) != 0) ||
				  (gwror.getRopecr() != null && gwror.getRopecr().compareTo(new BigDecimal(0)) != 0) ||
				  (gwror.getRoage1() != null && !gwror.getRoage1().isEmpty()) ||
				  (gwror.getRopep1() != null && gwror.getRopep1().compareTo(new BigDecimal(0)) != 0) ||
				  (gwror.getRopec1() != null && gwror.getRopec1().compareTo(new BigDecimal(0)) != 0)) {
			  return true;
		  }
		  return false;			  
	  }

	  public boolean esisteUnaValoreScontiValorizzati(Gwror00f gwror) {
		  if((gwror.getRoscar() != null && gwror.getRoscar().compareTo(new BigDecimal(0)) != 0) ||
				  (gwror.getScontoArt2() != null && gwror.getScontoArt2().compareTo(new BigDecimal(0)) != 0) ||
				  (gwror.getRosccm() != null && gwror.getRosccm().compareTo(new BigDecimal(0)) != 0) ||
				  (gwror.getRocdsc() != null && !gwror.getRocdsc().isEmpty()) ||
				  (gwror.getRosccl() != null && gwror.getRosccl().compareTo(new BigDecimal(0)) != 0)) {
			  return true;
		  }
		  return false;			  
	  }
	  //47380 fine	 
	  //Fix 47377 ini
	  public Gwmso00fCli recuperaGwmso00fOrdineCliente(String idCliente) {
		  Gwmso00fCli record = null;
		  String recordKey = KeyHelper.buildObjectKey(new String[] {gwmso00f.getIdAzienda() , gwmso00f.getIdFonteOrigine(),idCliente});
		  try {
			 record = Gwmso00fCli.elementWithKey(recordKey, Gwmso00fCli.NO_LOCK);
			if(record != null)
				return record;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
		  return record;
	  }
	  
	  public Serie recuperariIdSerieDaUsare(OrdineVenditaTestata testataOrd) {
		  if(gwmso00fOrdineCliente != null)
			  return gwmso00fOrdineCliente.getRSerie();
		  
		  if(!getParamRicercaSeriePrimaDaAssociazioneSerieCausali() && gwmso00f !=null)
			  return gwmso00f.getRSerie();
		  
		  Serie serie = unicoSeriePerNumeratoreCausaliOrdVendita(testataOrd);
		  if(serie != null)
			  return serie;
		  
		  if(gwmso00f != null)
			  return gwmso00f.getRSerie();
		  
		  return null;
	  }
	  
		 public boolean getParamRicercaSeriePrimaDaAssociazioneSerieCausali() {
			 String ricercaSeriePrimaDaAssociazioneSerieCausali = ParametroPsn.getValoreParametroPsn("std.ecommerce", "RicercaSeriePrimaDaAssociazioneSerieCausali");
			 if(ricercaSeriePrimaDaAssociazioneSerieCausali != null && ricercaSeriePrimaDaAssociazioneSerieCausali.equals("Y"))
				 return true;
			 return false;
		 }
		 
		 public Serie unicoSeriePerNumeratoreCausaliOrdVendita(OrdineVenditaTestata testataOrd) {
			 String idAzienda = testataOrd.getIdAzienda();
			 String idNumerator = testataOrd.getNumeratoreHandler().getIdNumeratore();
			 String idCausaleOrd = testataOrd.getIdCau();
			 String idSerie = getSerieCausaliAssociate(idAzienda,idNumerator,idCausaleOrd);
			 if(idSerie != null) {
				 String serieKey = KeyHelper.buildObjectKey(new String[] {idAzienda,idNumerator,idSerie});
				 try {
					return (Serie) Serie.elementWithKey(Serie.class, serieKey, Serie.NO_LOCK);
				} catch (SQLException e) {e.printStackTrace();}
			 }
			 
			 return null;
		 }
		 
		  public synchronized static String getSerieCausaliAssociate(String idAzienda, String idNumeratore, String idCausale) {//toDo
			    try {
			      PreparedStatement s = NUM_SERIE_CAU_ASS_STATEMENT.getStatement();
			      Database db = ConnectionManager.getCurrentDatabase();
			      db.setString(s, 1, idAzienda);
			      db.setString(s, 2, idNumeratore);
			      db.setString(s, 3, idCausale);
			      ResultSet r = s.executeQuery();
			      if(r.next()) {
			    	  String idSerie = r.getString(1);
			    	  if(!r.next()) {
			    		  return idSerie;
			    	  }
			    	  return null;
			      }
			    }
			    catch (Exception ex) {
			      ex.printStackTrace(Trace.excStream);
			    }
			    return null;
		}
	  //Fix 47377 fine
	  // Fix 47909 inizio
		protected List scattoAutomaticoWorkflow(OrdineVenditaTestata ordVen) throws SQLException {
			List errors = ordVen.postSave();
			return errors;
		}
	  //Fix 47909 fine
}
