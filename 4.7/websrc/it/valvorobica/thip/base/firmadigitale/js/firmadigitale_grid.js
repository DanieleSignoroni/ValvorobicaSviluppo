var pollingTimeout = null;
var pollingDelay = 2000;
var MAX_DELAY = 15000;

$(document).ready(function() {
	scheduleNextPoll();
	document.addEventListener("visibilitychange", function() {
		if (document.hidden) {
			pollingDelay = 15000;
		} else {
			pollingDelay = 2000;
			scheduleNextPoll();
		}
	});
});

//$(document).ready(function() {
//	fetchDocuments();
//
//	setInterval(fetchDocuments, 3000);
//});

function fetchDocuments() {
	$.ajax({
		url: getURLWS() + '/firmaDigitale/documenti/attesaFirma/recupera/lista' +
			'?IdDevice=' + encodeURIComponent(idDevice) +
			'&IdAzienda=' + encodeURIComponent(idAzienda),
		method: 'GET',
		contentType: 'application/json',
		success: function(data) {

			const container = document.getElementById('document-list');
			container.innerHTML = '';

			if (!data.documenti || data.documenti.length === 0) {
				pollingDelay = Math.min(pollingDelay * 2, MAX_DELAY);

			} else {
				pollingDelay = 2000;

				data.documenti.forEach((doc) => {
					var col = document.createElement('div');
					col.className = 'col-md-6 col-lg-4';
					col.innerHTML =
						'<div class="card card-custom h-100">' +
						'<div class="card-body">' +
						'<h5 class="card-title mb-3">' + doc.ragioneSociale + '</h5>' +
						'<p class="card-text mb-1"><strong>Tipo:</strong> ' + doc.tipo + '</p>' +
						'<p class="card-text mb-3"><strong>Documenti:</strong> ' + doc.count + '</p>' +
						'<button class="btn btn-azzurro w-100" onclick="firmaDocumento(\'' + doc.docToSignKeys + '\')">Firma</button>' +
						'</div>' +
						'</div>';
					container.appendChild(col);
				});
			}

			scheduleNextPoll();
		},
		error: function(err) {
			window.alert(err.responseText);
			pollingDelay = Math.min(pollingDelay * 2, MAX_DELAY);
			scheduleNextPoll();
		}
	});
}

function firmaDocumento(docToSignKeys) {
	var form = document.createElement('form');
	form.method = 'POST';
	form.action = 'FirmaDigitaleDocumentoRaggruppato.jsp';

	var input = document.createElement('input');
	input.type = 'hidden';
	input.name = 'docToSignKeys';
	input.value = docToSignKeys;
	form.appendChild(input);

	input = document.createElement('input');
	input.type = 'hidden';
	input.name = 'idDevice';
	input.value = idDevice;

	form.appendChild(input);

	input = document.createElement('input');
	input.type = 'hidden';
	input.name = 'idAzienda';
	input.value = idAzienda;

	form.appendChild(input);

	document.body.appendChild(form);

	form.submit();
}


function getURLWS() {
	var ris;
	var url = window.location.href;
	var cut = url.indexOf(webAppPath);
	ris = url.substring(0, cut);
	ris += webAppPath;
	ris += "/api";
	return ris;
}

function scheduleNextPoll() {
	stopPolling();
	pollingTimeout = setTimeout(fetchDocuments, pollingDelay);
}

function stopPolling() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
		pollingTimeout = null;
	}
}