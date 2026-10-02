<%@page import="org.json.JSONObject"%>
<%@page import="com.thera.thermfw.web.WebJSTypeList"%>
<%@page import="java.math.RoundingMode"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="it.valvorobica.thip.base.generale.ws.YCarrello"%>
<%@page import="it.valvorobica.thip.base.portal.YUserPortalSession"%>
<%@page import="it.valvorobica.thip.base.generale.ws.dati.YClientePortale"%>
<%@page import="com.thera.thermfw.base.Trace"%>
<%@page import="com.thera.thermfw.persist.ConnectionManager"%>
<%@page import="java.util.Map"%>
<%@page import="com.thera.thermfw.web.SessionEnvironment"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="com.thera.thermfw.security.Security"%>
<%@page import="com.thera.thermfw.persist.ConnectionDescriptor"%>
<%@page import="com.thera.thermfw.base.IniFile"%>
<%@page import="it.thera.thip.base.profilo.UtenteAzienda"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.google.gson.Gson"%>
<%@page import="com.thera.thermfw.persist.Factory"%>
<%@page import="com.thera.thermfw.web.ServletEnvironment"%>
<%
YUserPortalSession userPortalSession = (YUserPortalSession) request.getSession().getAttribute("YUserPortal");
String webAppPath = IniFile.getValue("thermfw.ini", "Web", "WebApplicationPath");
String token = userPortalSession.getTokecUID();
ArrayList<YCarrello.ItemCarrello> items = new ArrayList<YCarrello.ItemCarrello>();
Map values = new LinkedHashMap();
boolean isopen = false;
Object[] info = SessionEnvironment.getDBInfoFromIniFile();
String dbName = (String) info[0];
String newsHtml = null;
BigDecimal total = BigDecimal.ZERO;

JSONObject deliveryMethods = null;
JSONObject shipmentMethods = null;
JSONObject shippers = null;
JSONObject shipmentDefaultMethod = null;
JSONObject defaultShipper = null;
JSONObject deafultDeliveryMethod = null;

JSONObject datiDestinazione = null;

String salesConditionsPDFKey = null;
boolean isVettoreRequired = false;
try {
	if (!Security.isCurrentDatabaseSetted()) {
		Security.setCurrentDatabase(dbName, null);
	}
	Security.openDefaultConnection();
	isopen = true;
	ConnectionDescriptor cd = ConnectionManager.getCurrentConnectionDescriptor();
	YCarrello ws = new YCarrello();
	ws.setUseAuthentication(false);
	ws.setUseAuthorization(false);
	ws.setUseLicence(false);
	ws.setUserPortalSession(userPortalSession);
	ws.setConnectionDescriptor(cd);
	values = ws.send();
	items = (ArrayList<YCarrello.ItemCarrello>) values.get("items");
	total = (BigDecimal) values.get("total");
	deliveryMethods = (JSONObject) values.get("deliveryMethods");
	salesConditionsPDFKey = (String) values.get("salesConditionsPDFKey");
	deliveryMethods = (JSONObject) values.get("deliveryMethods");
	shipmentMethods = (JSONObject) values.get("shipmentMethods");
	shippers = (JSONObject) values.get("shippers");
	shipmentDefaultMethod = (JSONObject) values.get("shipmentDefaultMethod");
	defaultShipper = (JSONObject) values.get("defaultShipper");
	deafultDeliveryMethod = (JSONObject) values.get("deafultDeliveryMethod");
	datiDestinazione = (JSONObject) values.get("shipmentData");
	isVettoreRequired = (Boolean) values.get("isVettoreRequired");
} catch (Throwable t) {
	t.printStackTrace(Trace.excStream);
} finally {
	if (isopen) {
		Security.closeDefaultConnection();
	}
}
%>
<html>
<head>
<meta charset="utf-8">
<meta content="width=device-width, initial-scale=1.0" name="viewport">
<title>Carrello</title>
<!-- CSS Files -->
<%=com.thera.thermfw.web.WebJSTypeList.getImportForCSS("it/valvorobica/thip/base/portal/css/bootstrap.min.css", request)%>
<%=com.thera.thermfw.web.WebJSTypeList.getImportForCSS("it/valvorobica/thip/base/portal/css/frames.css", request)%>
<%=WebJSTypeList.getImportForCSS("it/valvorobica/thip/base/portal/css/cart.css", request)%>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
<%=WebJSTypeList.getImportForCSS("it/valvorobica/thip/assets/DataTables/datatables.css", request)%>
<%=WebJSTypeList.getImportForCSS("it/valvorobica/thip/assets/DataTables/Select/css/select.dataTables.css", request)%>
<%=WebJSTypeList.getImportForCSS("it/valvorobica/thip/assets/DataTables/Buttons/css/buttons.dataTables.css", request)%>
<%=WebJSTypeList.getImportForCSS("it/valvorobica/thip/base/portal/css/carrello.css", request)%>
<style>
.location-autocomplete { position: relative; }
.location-autocomplete-results {
	position: absolute; z-index: 1060; top: 100%; right: 0; left: 0;
	max-height: 240px; overflow-y: auto; background: #fff;
	border: 1px solid #ced4da; border-radius: 0 0 .25rem .25rem;
	box-shadow: 0 .25rem .5rem rgba(0, 0, 0, .15);
}
.location-autocomplete-results:empty { display: none; }
.location-autocomplete-option { padding: .5rem .75rem; cursor: pointer; }
.location-autocomplete-option:hover,
.location-autocomplete-option.active { color: #fff; background: #007bff; }
.location-search-help { position: relative; display: inline-block; margin-left: .35rem; vertical-align: middle; }
.location-search-help-icon {
	display: inline-flex; align-items: center; justify-content: center;
	width: 1.1rem; height: 1.1rem;
	color: #007bff; border: 1px solid #007bff; cursor: help;
	border-radius: 50%; font-size: .75rem; font-weight: 700; line-height: 1;
}
.location-search-help-text {
	display: none; position: absolute; z-index: 1070; bottom: calc(100% + .5rem); left: 50%;
	width: 280px; padding: .65rem .75rem; transform: translateX(-50%);
	color: #fff; background: #343a40; border-radius: .25rem;
	box-shadow: 0 .25rem .5rem rgba(0, 0, 0, .2); font-size: .8rem; font-weight: 400;
}
.location-search-help:hover .location-search-help-text,
.location-search-help:focus-within .location-search-help-text { display: block; }
</style>
<%=com.thera.thermfw.web.WebJSTypeList.getImportForJSLibrary("it/valvorobica/thip/base/portal/js/jquery.js", request)%>
<%=com.thera.thermfw.web.WebJSTypeList.getImportForJSLibrary("it/valvorobica/thip/base/portal/js/modal_utils.js", request)%>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
<%=com.thera.thermfw.web.WebJSTypeList.getImportForJSLibrary("it/valvorobica/thip/base/portal/js/main_1.js", request)%>
<%=com.thera.thermfw.web.WebJSTypeList.getImportForJSLibrary("it/valvorobica/thip/assets/DataTables/datatables.js", request)%>
<%=com.thera.thermfw.web.WebJSTypeList.getImportForJSLibrary("it/valvorobica/thip/assets/DataTables/Select/js/dataTables.select.js", request)%>
<%=com.thera.thermfw.web.WebJSTypeList.getImportForJSLibrary("it/valvorobica/thip/assets/DataTables/Buttons/js/dataTables.buttons.js", request)%>
<%=com.thera.thermfw.web.WebJSTypeList.getImportForJSLibrary("it/valvorobica/thip/base/portal/js/carrello.js", request)%>
</head>
<body class="noscrollbar" onload="rimuoviSpinner()">
			<input type="hidden" name="urlWS" id="urlWS" value=""> <input
			type="hidden" name="webAppPath" id="webAppPath"
			value="<%=webAppPath%>">
			<input
				type="hidden" name="Azienda" id="Azienda"
				value="<%=userPortalSession.getIdAzienda()%>"> <input type="hidden" name="token" id="token" value="<%=userPortalSession.getTokecUID()%>"> 
		<div class="row">
			<div class="col-sm-2 mb-2">
				<h3 class="pointer">Carrello</h3>
			</div>
		</div>
		<div class="table-responsive">
			<table class="table table-striped table-hover table-bordered"
				id="table">
				<thead class="thead-dark">
					<tr>
						<th scope="col">Articolo</th>
						<th scope="col">Descrizione</th>
						<th scope="col">Unita misura</th>
						<th scope="col">Quantita</th>
						<th scope="col">Prezzo</th>
						<th scope="col">Comandi</th>
					</tr>
				</thead>
				<tbody>
					<%
					Iterator<YCarrello.ItemCarrello> iter = items.iterator();
					while (iter.hasNext()) {
						YCarrello.ItemCarrello item = (YCarrello.ItemCarrello) iter.next();
					%>
					<tr>
						<td>
							<input type="hidden" name="Critico" value="<%=item.isArticoloCritico()%>">
<%-- 							<!-- <input type="hidden" name="DisponibileAellebi" value="<%=item.isDisponibileAellebi()%>">  --> --%>
							<input type="hidden" name="DisponibileAltraSede" value="<%=item.isDisponibileAltraSede()%>">
				            <input type="hidden" name="key" value="<%=item.getKey()%>">
				            <input type="hidden" name="Prezzo" value="<%=item.getPrezzo()%>">
				            <%=item.getIdArticoloCatalogo()%>
				            <input type="hidden" name="Articolo" value="<%=item.getIdArticolo()%>"/>
				            <input type="hidden" name="ArticoloCatalogo" value="<%=item.getIdArticoloCatalogo()%>"/>
        				</td>
        				
						<td><%=item.getDescrExtArticolo()%>
							<input type="hidden" name="Descrizione" value="<%=item.getDescrExtArticolo()%>"/>
						</td>
						
						<td style="width: 15%;"><%=item.getUmDefVen()%></td>
						
						<td style="width: 15%;" data-sort="<%=item.getQuantita()%>">
							<input type="number" class="form-control" onChange="ricalcolaTotale(this)" name="Quantita" value="<%=item.getQuantita()%>"></input>
						</td>
						
						<td><%=item.getPrezzo()%></td>
						
						<td>
							<!-- Qui ci sara' il bottone di rimozione -->
						</td>
					</tr>
					<%
					}
					%>
				</tbody>
				<tfoot>
          			  <tr>
               			 <th colspan="4" style="text-align:right">Totale Provvisorio:</th>
              			 <th id="total"><%=total.setScale(2, RoundingMode.DOWN).toString() %></th>
              			 <th></th>
            		</tr>
        		</tfoot>
			</table>
		</div>
		<div style="display: none" class="text-center">
			<a href="#modalRemoveItem" id="removeItemClick" class="trigger-btn"
				data-toggle="modal"></a>
		</div>
		<div id="modalRemoveItem" class="modal fade">
			<div class="modal-dialog modal-confirm">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title">Attenzione!</h4>
					</div>
					<div class="modal-body">
						<p id="removeItemTxt" class="text-center"></p>
					</div>
					<div class="modal-footer" style="flex-wrap:unset;">
						<button type="button" class="btn btn-block" id="removeItemBtn"
							data-dismiss="modal">SI</button>
							<button type="button" class="btn btn-block" onclick="$('#modalRemoveItem').modal('toggle');"
							data-dismiss="modal">NO</button>
					</div>
				</div>
			</div>
		</div>
		
		<div style="display: none" class="text-center">
			<a href="#modalFirstStep" id="modalFirstStepClick" class="trigger-btn"
				data-toggle="modal"></a>
		</div>
		<div id="modalFirstStep" class="modal fade">
			<div class="modal-dialog modal-confirm">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title">RIEPILOGO ARTICOLI SCELTI</h4>
					</div>
					<div class="modal-body">
						<p id="riepilogo" class="text-center"></p>
					</div>
					<div class="modal-footer" style="flex-wrap:unset;">
						<button type="button" onclick="procediSecondStep()" class="btn btn-block" id="removeItemBtn"
							data-dismiss="modal">PROCEDI</button>
							<button type="button" class="btn btn-block" onclick="$('#modalFirstStep').modal('toggle');"
							data-dismiss="modal">TORNA INDIETRO</button>
					</div>
				</div>
			</div>
		</div>
		
		<div style="display: none" class="text-center">
			<a href="#modalWarningCheckOut" id="warningCheckOutClick" class="trigger-btn"
				data-toggle="modal"></a>
		</div>
					<div id="modalWarningCheckOut" class="modal fade">
  <div class="modal-dialog modal-confirm">
    <form id="checkoutForm">
      <div class="modal-content">
        <div class="modal-body" id="summarySection">
        </div>

        <div class="modal-body d-none" id="checkoutSection">
          <div class="row alert alert-danger" style="margin:0;">
	          <div class="col-8">
	          </div>
	         <div class="col">
<!-- 72578 remmo il link al sito e prendo il pdf da doc dgt -->
<!-- 				    <a style="text-decoration: none;" href="https://www.valvorobica.eu/wp-content/uploads/Condizioni-generali-di-vendita-Valvorobica_02_Ottobre-2023.pdf" -->
<!-- 				     title="Condizioni di vendita" target="_blank" id="condizioniVen"> -->
						<h3 onclick="parent.download('<%=userPortalSession.getTokecUID()%>','<%=salesConditionsPDFKey %>')" class="pointer">
				        <button type="button" class="btn btn-block">Condizioni di vendita</button>
				        </h3>
<!-- 				    </a> -->
			</div>
	       </div>
          <div class="row mt-4">
            <div class="col">
              <label class="form-label" for="CAP">CAP</label>
              <span class="location-search-help">
                <span class="location-search-help-icon" tabindex="0" aria-label="Come cercare CAP e localit&agrave;" aria-describedby="locationSearchHelp">i</span>
                <span id="locationSearchHelp" class="location-search-help-text" role="tooltip">Digita almeno 2 caratteri in uno dei campi CAP, Localit&agrave; o Provincia, poi seleziona un risultato: il sistema compiler&agrave; automaticamente tutti e tre i campi. Non &egrave; necessario inserire manualmente il CAP.</span>
              </span>
              <div id="locationAutocompleteResults" class="location-autocomplete-results" role="listbox" aria-label="Localit&agrave; disponibili"></div>
              <label class="form-label" for="Localita">Localit&agrave;</label>
              <label class="form-label" for="Provincia">Provincia</label>
            <div class="col">
              <label class="form-label">Modalità  di spedizione</label>
              <select class="form-select mb-2" name="IdModalitaSpedizione" id="shipmentMethod" required></select>
              <input type="hidden" name="IdModalitaSpedizione" id="IdModalitaSpedizioneHiddenInputValue">
            </div>
            <div class="col" style="display:none;">
              <label class="form-label">Vettore</label>
					<!-- 72578 remmo il required -->
				    <!-- <select class="form-select mb-2" name="IdVettore1" id="shipper" required></select> -->
					<select class="form-select mb-2" name="IdVettore1" id="shipper" ></select> 
            </div>
          </div>
          <div class="row">
            <div class="col">
              <label class="form-label">Indirizzo</label>
              <input class="form-control" type="text" name="Indirizzo" id="Indirizzo" required>
            </div>
            <div class="col location-autocomplete">
              <label class="form-label">CAP</label>
              <input class="form-control location-search" type="text" name="CAP" id="CAP" required autocomplete="off" aria-autocomplete="list" aria-controls="locationAutocompleteResults">
            </div>
            <div class="col location-autocomplete">
              <label class="form-label">Località </label>
              <input class="form-control location-search" type="text" name="Localita" id="Localita" required autocomplete="off" aria-autocomplete="list" aria-controls="locationAutocompleteResults">
            </div>
            <div class="col location-autocomplete">
              <label class="form-label">Provincia</label>
              <input class="form-control location-search" type="text" name="Provincia" id="Provincia" required autocomplete="off" aria-autocomplete="list" aria-controls="locationAutocompleteResults">
              <div id="locationAutocompleteResults" class="location-autocomplete-results" role="listbox" aria-label="LocalitÃ  disponibili"></div>
            </div>
          </div>
          <input name="vsNr" id="vsNr" class="form-control mt-2" placeholder="Vs. Numero Ordine" required maxlength="15" />
          <input name="Email" id="Email" class="form-control mt-2" placeholder="Email" required />
          <textarea name="note" id="note" class="form-control mt-2" placeholder="NOTE (es. vettore, urgenze, stato materiale...)"></textarea>
          <div id="responseMessage" class="mt-3"></div>
        </div>

        <div class="modal-footer" style="flex-wrap:unset;">
          <button type="button" class="btn btn-block" id="nextStep">Avanti</button>
          <button type="button" class="btn btn-block d-none" id="warningCheckOut">Conferma ordine</button>
          <button type="button" class="btn btn-block d-none" id="indietroFirstStep">Indietro</button>
          <button type="button" class="btn btn-block" onclick="annullaCheckoutOrder()" data-dismiss="modal">Annulla</button>
        </div>

        <!-- Progress bar -->
        <div class="progress mt-2">
          <div class="progress-bar bg-success" role="progressbar" id="checkoutProgressBar" 
            style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">0/2
          </div>
        </div>
      </div>
    </form>
  </div>
</div>	
	
		<div style="display: none" class="text-center">
			<a href="#modalSuccessCheckOut" id="successCheckOutClick" class="trigger-btn"
				data-toggle="modal"></a>
		</div>
		<div id="modalSuccessCheckOut" class="modal fade">
			<div class="modal-dialog modal-confirm">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title">Congratulazioni!</h4>
					</div>
					<div class="modal-body">
						<p id="successTxt" class="text-center"></p>
					</div>
					<div class="modal-footer" style="flex-wrap:unset;">
						<button type="button" class="btn btn-block" onClick="window.location.reload();"
							data-dismiss="modal">OK</button>
					</div>
				</div>
			</div>
		</div>

	<script>
    var deliveryMethods = <%= deliveryMethods %>;
    var shipmentMethods = <%= shipmentMethods %>;
    var shippers = <%= shippers %>;
    var shipmentDefaultMethod = <%= shipmentDefaultMethod %>;
    var defaultShipper = <%= defaultShipper %>;
    var deafultDeliveryMethod = <%= deafultDeliveryMethod %>;
    var datiDestinazione = <%= datiDestinazione %>;
    var isVettoreRequired = <%= isVettoreRequired %>;
    var items = <%=YUserPortalSession.getNumeroItemsCarrelloUtente(userPortalSession.getIdAzienda(), userPortalSession.getIdUtente())%>;
    var company = "<%=userPortalSession.getIdAzienda()%>";
    var idCliente = "<%=userPortalSession.getIdCliente()%>";

    $(document).ready(function() {
        // Initialize your JavaScript file
        initCheckout();
    });
</script>

</body>
</html>
