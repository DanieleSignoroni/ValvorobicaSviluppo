var currentStep = 0; // 0 - Summary, 1 - Checkout form
var checkoutForm = null;
var cart = null;
var table = null;
var $summaryDiv = null;

function initCheckout() {
	cart = $('#cart', parent.document);
	cart.addClass('shake').attr('data-totalitems', items);
	setTimeout(() => cart.removeClass('shake'), 1000);

	checkoutForm = document.getElementById('checkoutForm');
	checkoutForm.addEventListener('submit', handleCheckoutFormSubmit);

	table = new DataTable('#table', {
		"pageLength": 50,
		dom: 'Blfrtip',
		buttons: [{
			className: 'btn btn-secondary btn-sm',
			text: 'Procedi',
			action: function() {
				//72676 <
				if (parseFloat(total) < parseFloat(minumunOrder)) {
					openModal('txtWarning', $('#modalWarningClick',
						parent.parent.document)[0], msgOrdineMinimo);
					return;
				//72676 >	
				} else {
					resetProgressAndForm();
					$('#modalWarningCheckOut').modal('show');
				}
			}
		}],
		initComplete: function() {
			let btns = $('.dt-button');
			btns.removeClass('dt-button');
		},
		columnDefs: [
			{ orderable: false },
			{
				targets: -1,
				render: function(data, type, row, meta) {
					return "<a class='btn btn-danger btn-rimuovi-row btn-sm mr-2' onclick='removeItem(this.parentNode.parentNode)'>Rimuovi</a>";
				}
			}
		],
	});

	populateDeliveryMethods();
	populateShipmentMethods();
	populateShippers();
	populateDatiDestinazione();
	initLocationAutocomplete();

	$('#shipmentMethod').on('change', function() {
		const selectedValue = $(this).val();
		//let isVettoreRequired = shipmentMethods.shipmentMethods.some(function(method) {
		//	return method.Id === selectedValue && method.isVettoreRequired;
		//});
		const shipperElement = document.getElementById('shipper');
		//if (isVettoreRequired) {
		//	shipperElement.setAttribute('required', true);
		//}
		let modalitaConsegna = document.getElementById('deliveryMethod').value;
		//Se la modalità' di consegna e' FRANCO/FRANCO ADDEBITO allora vettore e' obbligatorio'
		if (modalitaConsegna != null && (modalitaConsegna === "01" || modalitaConsegna === "05")
			&& selectedValue === "VE") {
			//			shipperElement.setAttribute('required', true);
		} else {
			shipperElement.removeAttribute('required');
			document.getElementById('shipper').parentNode.style.display = 'none';
		}
		//72578 rem <
		/*if (selectedValue === "VE") {
			document.getElementById('shipper').parentNode.style.display = 'revert';
		} else {
			document.getElementById('shipper').parentNode.style.display = 'none';
		}*/
		//72578 rem >
		//shipperElement.parentNode.style.display = 'revert';
		//} else {
		//shipperElement.removeAttribute('required');
		//shipperElement.parentNode.style.display = 'none';
		//}
		document.getElementById('IdModalitaSpedizioneHiddenInputValue').value = selectedValue;
	});

	const nextStepButton = document.getElementById('nextStep');
	const confirmButton = document.getElementById('warningCheckOut');
	const summarySection = document.getElementById('summarySection');
	const checkoutSection = document.getElementById('checkoutSection');
	const progressBar = document.getElementById('checkoutProgressBar');
	const indietroFirstStep = document.getElementById('indietroFirstStep');

	indietroFirstStep.addEventListener('click', function() {
		checkoutSection.classList.add('d-none');
		summarySection.classList.remove('d-none');
		updateProgressBar(0, '0/2');
		currentStep = 0;
		checkoutForm.classList.remove('was-validated');
		confirmButton.classList.add('d-none');
		nextStepButton.classList.remove('d-none');
	});

	nextStepButton.addEventListener('click', function() {
		if (currentStep === 0) {
			summarySection.classList.add('d-none');
			checkoutSection.classList.remove('d-none');
			nextStepButton.classList.add('d-none');
			confirmButton.classList.remove('d-none');
			indietroFirstStep.classList.remove('d-none');
			updateProgressBar(50, '1/2');
			currentStep = 1;
		}
	});

	confirmButton.addEventListener('click', function() {
		if (checkoutForm.checkValidity()) {
			checkOutOrder();
			updateProgressBar(100, '2/2');
		} else {
			checkoutForm.classList.add('was-validated');
		}
	});;

	$summaryDiv = $('#summarySection');
}

function handleCheckoutFormSubmit(event) {
	event.preventDefault();
	if (!checkoutForm.checkValidity()) {
		checkoutForm.classList.add('was-validated');
	} else {
		checkOutOrder();
	}
}

function updateProgressBar(value, text) {
	const progressBar = document.getElementById('checkoutProgressBar');
	progressBar.style.width = value + '%';
	progressBar.setAttribute('aria-valuenow', value);
	progressBar.textContent = text;

	if (value === 0) {
		displaySummary();
	}
}

function displaySummary() {
	$summaryDiv.empty();
	let totale = 0;
	let atLeastOneCriticalProduct = false;
	//let atLeastOneProductAvailableAellebi = false; //72575 rem
	let atLeastOneProductAvailableAltraAzienda = false; //72575 new
	let atLeastOneGreenProduct = false;
	let summaryTable = `
    <table class="table">
        <thead>
            <tr>
            	<th></th>
                <th>Articolo</th>
                <th>Descrizione</th>
                <th>Prezzo</th>
                <th>Quantita</th>
            </tr>
        </thead>
        <tbody>
	`;

	let rows = [];

	table.rows().every(function() {
		var rowNode = $(this.node());
		let Articolo = null;
		let Prezzo = null;
		let Descrizione = null;
		let Quantita = null;
		let isArticoloCritico;
		//let isDisponibileAellebi; //72575 rem
		let isDisponibileAltraAzienda; //72575 new
		let semaforo = "/" + $('#webAppPath').val();

		rowNode.find('td input').each(function() {
			var inputName = $(this).attr('name');
			var inputValue = $(this).val();

			if (inputName == 'ArticoloCatalogo')
				Articolo = inputValue;
			else if (inputName == 'Prezzo')
				Prezzo = inputValue;
			else if (inputName == 'Descrizione')
				Descrizione = inputValue;
			else if (inputName == 'Quantita')
				Quantita = inputValue;
			else if (inputName == 'Critico')
				isArticoloCritico = inputValue;
			//72575 rem <
			/*else if (inputName == 'DisponibileAellebi')
				isDisponibileAellebi = inputValue;*/
			//72575 rem >
			//72575 new <
			else if (inputName == 'DisponibileAltraSede')
				isDisponibileAltraAzienda = inputValue;
			//72575 new >
		});

		rows.push({
			Articolo,
			Descrizione,
			Prezzo,
			Quantita,
			isArticoloCritico,
			semaforo,
			//isDisponibileAellebi 72575 rem,
			isDisponibileAltraAzienda //72575 rem new
		});
	});

	rows.sort(function(a, b) {
		return (b.isArticoloCritico === "true" ? 1 : 0) - (a.isArticoloCritico === "true" ? 1 : 0);
	});

	rows.forEach(function(row) {
		let rowClass = '';
		if (row.isArticoloCritico === "true") {
			rowClass = 'class="articolo-critico"';
			//row.semaforo += "/it/thera/thip/base/generale/images/giallo.gif";
			row.semaforo = "yellow";
			atLeastOneCriticalProduct = true;
		} else {
			//row.semaforo += "/it/thera/thip/base/generale/images/verde.gif";
			row.semaforo = "green";
			atLeastOneGreenProduct = true;
		}
		//72575 rem <
		/*if (row.isDisponibileAellebi === "true") {
			atLeastOneProductAvailableAellebi = true;
		}*/
		//72575 rem >
		//72575 new <
		if (row.isDisponibileAltraAzienda === "true") {
			atLeastOneProductAvailableAltraAzienda = true;
		}
		//72575 new >

		let mlt = parseFloat(row.Prezzo) * parseFloat(row.Quantita);
		totale = totale + mlt;

		summaryTable += `
		  <tr ${rowClass}>
		     ${row.isDisponibileAltraAzienda === "false"
				//? `<td><img src="${row.semaforo}"></td>`
				? `<td>
		            <span class="fa-stack" style="font-size: 1em;width:1.5em;">
		              <i class="fa-solid fa-circle fa-stack-1x" style="color: ${row.semaforo};"></i>
		            </span>
		          </td>`
				: `<td>
		            <span class="fa-stack" style="font-size: 1em;width:1.5em;">
		              <i class="fa-solid fa-circle fa-stack-1x" style="color: green;"></i>
		              <span class="fa-stack-1x" style="color: white; font-weight: bold; font-size: 1em;">A</span>
		            </span>
		          </td>`
			}
			<td>${row.Articolo}</td>
		    <td>${row.Descrizione}</td>
		    <td>${parseFloat(row.Prezzo).toFixed(4)}</td>
		    <td>${parseFloat(row.Quantita).toFixed(4)}</td>
		  </tr>
		`;


	});

	summaryTable += `
        </tbody>
        <tfoot>
            <tr>
                <td colspan="3" style="text-align: right; font-weight: bold;">Totale:</td>
                <td>${totale.toFixed(4)}</td>
            </tr>
        </tfoot>
    </table>
	`;

	//72575 rem <
	//const showOnlyYellow = atLeastOneCriticalProduct && !atLeastOneProductAvailableAellebi;
	//const showOnlyA = !atLeastOneCriticalProduct && atLeastOneProductAvailableAellebi;
	//72575 rem >
	//72575 new <
	const showOnlyYellow = atLeastOneCriticalProduct && !atLeastOneProductAvailableAltraAzienda;
	const showOnlyA = !atLeastOneCriticalProduct && atLeastOneProductAvailableAltraAzienda;
	//72575 new >

	// Nella logica originale, se non è né il caso "solo giallo" né "solo A", si mostrano entrambi.
	const includeYellow = showOnlyYellow || (!showOnlyYellow && !showOnlyA);
	const includeA = showOnlyA || (!showOnlyYellow && !showOnlyA);

	let legendHtml = `
    <div class="legend">
  `;

	if (includeYellow) {
		legendHtml += `
      <div class="legend-item">
        <span class="fa-stack" style="font-size: 1em;width:1.5em;">
          <i class="fa-solid fa-circle fa-stack-1x" style="color: yellow;"></i>
        </span>
        <span><i>Articoli parzialmente disponibili o in fase di riassortimento, procedere con la richiesta per ricevere ulteriori informazioni da parte del nostro commerciale.</i></span>
      </div>
    `;
	}

	if (includeA) {
		legendHtml += `
      <div class="legend-item">
        <span class="fa-stack" style="font-size: 1em;width:1.5em;">
          <i class="fa-solid fa-circle fa-stack-1x" style="color: green;"></i>
          <span class="fa-stack-1x" style="color: white; font-weight: bold; font-size: 1em;">A</span>
        </span>
        <span><i>Materiale disponibile presso altra sede, consegna in 2/3 gg</i></span>
      </div>
    `;
	}

	// Nuovo comportamento combinabile
	if (atLeastOneGreenProduct) {
		legendHtml += `
      <div class="legend-item">
        <span class="fa-stack" style="font-size: 1em;width:1.5em;">
          <i class="fa-solid fa-circle fa-stack-1x" style="color: green;"></i>
        </span>
        <span><i>Consegna a saldo</i></span>
      </div>
    `;
	}

	legendHtml += `
    </div>
  `;
	$summaryDiv.append(legendHtml);
	$summaryDiv.append(summaryTable);

}

function resetProgressAndForm() {
	var nextStepButton = document.getElementById('nextStep');
	var confirmButton = document.getElementById('warningCheckOut');
	var summarySection = document.getElementById('summarySection');
	var checkoutSection = document.getElementById('checkoutSection');
	var progressBar = document.getElementById('checkoutProgressBar');
	updateProgressBar(0, '0/2');
	currentStep = 0;
	summarySection.classList.remove('d-none');
	checkoutSection.classList.add('d-none');
	nextStepButton.classList.remove('d-none');
	confirmButton.classList.add('d-none');
	checkoutForm.classList.remove('was-validated');
	indietroFirstStep.classList.add('d-none');
}

function populateDatiDestinazione() {
	if (datiDestinazione != null) {
		if (datiDestinazione.Indirizzo != undefined)
			document.getElementById('Indirizzo').value = datiDestinazione.Indirizzo.trim();
		if (datiDestinazione.CAP != undefined)
			document.getElementById('CAP').value = datiDestinazione.CAP.trim();
		if (datiDestinazione.Localita != undefined)
			document.getElementById('Localita').value = datiDestinazione.Localita.trim();
		if (datiDestinazione.Provincia != undefined)
			document.getElementById('Provincia').value = datiDestinazione.Provincia.trim();
	}
}

//72676 <
function initLocationAutocomplete() {
	var $inputs = $('.location-search');
	var $results = $('#locationAutocompleteResults');
	var defaultLocation = {
		CAP: $('#CAP').val(),
		Localita: $('#Localita').val(),
		Provincia: $('#Provincia').val()
	};
	var queryCache = {};
	var pendingRequest = null;
	var debounceTimer = null;
	var locationChanged = false;

	function closeResults() {
		$results.empty();
		$inputs.attr('aria-expanded', 'false');
	}

	function selectLocation(location) {
		$('#CAP').val(location.CAP || '');
		$('#Localita').val(location.localita || '');
		$('#Provincia').val(location.idProvincia || '');
		locationChanged = false;
		$inputs.each(function() { this.setCustomValidity(''); });
		closeResults();
	}

	function restoreDefaultLocation() {
		if (!locationChanged) {
			return;
		}
		clearTimeout(debounceTimer);
		if (pendingRequest) {
			pendingRequest.abort();
			pendingRequest = null;
		}
		$('#CAP').val(defaultLocation.CAP);
		$('#Localita').val(defaultLocation.Localita);
		$('#Provincia').val(defaultLocation.Provincia);
		locationChanged = false;
		$inputs.each(function() { this.setCustomValidity(''); });
		closeResults();
	}

	function showResults(locations) {
		closeResults();
		var containerBounds = $results.parent()[0].getBoundingClientRect();
		var capBounds = document.getElementById('CAP').getBoundingClientRect();
		var provinciaBounds = document.getElementById('Provincia').getBoundingClientRect();
		$results.css({
			left: capBounds.left - containerBounds.left,
			right: 'auto',
			width: provinciaBounds.right - capBounds.left
		});
		locations.forEach(function(location) {
			var label = [location.CAP, location.localita, location.idProvincia]
				.filter(function(value) { return value; }).join(' - ');
			$('<div>', {
				'class': 'location-autocomplete-option',
				'role': 'option',
				'tabindex': 0,
				'text': label
			}).on('mousedown', function(event) {
				event.preventDefault();
				selectLocation(location);
			}).on('keydown', function(event) {
				if (event.key === 'Enter' || event.key === ' ') {
					event.preventDefault();
					selectLocation(location);
				}
			}).appendTo($results);
		});
		$inputs.attr('aria-expanded', locations.length > 0 ? 'true' : 'false');
	}

	function search(query) {
		var cacheKey = query.toUpperCase();
		if (queryCache[cacheKey]) {
			showResults(queryCache[cacheKey]);
			return;
		}
		if (pendingRequest) {
			pendingRequest.abort();
		}
		var request = $.ajax({
			url: getURLREST() + "/api/customersPortal/capparioSusa",
			method: 'GET',
			dataType: 'json',
			data: { query: query, limit: 20 }
		});
		pendingRequest = request;
		request.done(function(response) {
			var locations = response && response.cappario ? response.cappario : [];
			queryCache[cacheKey] = locations;
			showResults(locations);
		}).fail(function(xhr, status) {
			if (status !== 'abort') {
				closeResults();
			}
		}).always(function() {
			if (pendingRequest === request) {
				pendingRequest = null;
			}
		});
	}

	$inputs.on('input', function() {
		var query = $.trim(this.value);
		locationChanged = true;
		$inputs.each(function() {
			this.setCustomValidity("Selezionare una localita' dall'elenco proposto.");
		});
		clearTimeout(debounceTimer);
		if (pendingRequest) {
			pendingRequest.abort();
			pendingRequest = null;
		}
		if (query.length < 2) {
			closeResults();
			return;
		}
		debounceTimer = setTimeout(function() { search(query); }, 300);
	}).on('keydown', function(event) {
		if (event.key === 'Escape') {
			closeResults();
		}
	});

	$(document).on('mousedown', function(event) {
		if (!$(event.target).closest('.location-search, #locationAutocompleteResults').length) {
			restoreDefaultLocation();
		}
	});
}
//72676 >

function populateShippers() {
	let select = document.getElementById('shipper');
	let data = JSON.stringify(shippers);
	data = JSON.parse(data);

	select.innerHTML = '';

	let defaultOption = document.createElement('option');
	defaultOption.value = '';
	defaultOption.textContent = 'Seleziona un vettore';
	defaultOption.disabled = true;
	defaultOption.selected = true;
	select.appendChild(defaultOption);

	if (data.shippers != undefined) { //72578 check
		data.shippers.forEach(function(method) {
			let option = document.createElement('option');
			option.value = method.Id;
			option.textContent = method.Description;
			select.appendChild(option);
		});
	} //72578
}

function populateShipmentMethods() {
	let select = document.getElementById('shipmentMethod');
	let id = null;
	let descr = null;

	shipmentMethods.shipmentMethods.forEach(function(method) {
		let option = document.createElement('option');
		option.value = method.Id;
		option.textContent = method.Description;
		select.appendChild(option);
	});

	if (shipmentDefaultMethod != null) {
		id = shipmentDefaultMethod.Id;
		descr = shipmentDefaultMethod.Description;

		select.value = id;

		$('#IdModalitaSpedizioneHiddenInputValue').val(id);
	}

	//	if (!isVettoreRequired) {
	//		document.getElementById('shipper').removeAttribute('required');
	//		document.getElementById('shipper').parentNode.style.display = 'none';
	//	}
	const shipperElement = document.getElementById('shipper');
	let modalitaConsegna = document.getElementById('deliveryMethod').value;
	//Se la modalità' di consegna e' FRANCO/FRANCO ADDEBITO allora vettore e' obbligatorio'
	//	if (modalitaConsegna != null && (modalitaConsegna === "01" || modalitaConsegna === "05")
	//		&& id === "VE") {
	//		shipperElement.setAttribute('required', true);
	//	} else {
	//		shipperElement.removeAttribute('required');
	//		document.getElementById('shipper').parentNode.style.display = 'none';
	//	}
	//	if (id === "VE") {
	//		document.getElementById('shipper').parentNode.style.display = 'revert';
	//	} else {
	//		document.getElementById('shipper').parentNode.style.display = 'none';
	//	}

}

function populateDeliveryMethods() {
	let select = document.getElementById('deliveryMethod');

	let id = null;
	let descr = null;

	if (deafultDeliveryMethod != null) {
		id = deafultDeliveryMethod.Id;
		descr = deafultDeliveryMethod.Description;
		if (id == "02") {
			document.getElementById('shipper').removeAttribute('required');
			document.getElementById('shipper').parentNode.style.display = 'none';
		}
	} else {
		id = deliveryMethods.deliveryMethods[0].Id;
		descr = deliveryMethods.deliveryMethods[0].Description;
	}

	select.innerHTML = '';

	let defaultOption = document.createElement('option');
	defaultOption.value = id;
	defaultOption.textContent = descr;
	defaultOption.disabled = true;
	defaultOption.selected = true;
	select.appendChild(defaultOption);

	$('#IdModalitaConsegnaHiddenInputValue').val(id);

	// 		data.deliveryMethods.forEach(function(method) {
	// 			let option = document.createElement('option');
	// 			option.value = method.Id; 
	// 			option.textContent = method.Description; 
	// 			select.appendChild(option); 
	// 		});
}

function modifyRow(btn) {
	btn.parentElement.parentNode.querySelector('[name=Quantita]').removeAttribute('readonly');
}

function ricalcolaTotale(input) {
	parent.mostraSpinner();
	let key = [];
	key = input.parentElement.parentNode.querySelector('[name=key]').value.split('\x16');
	if (parseFloat(input.value) <= 0) {
		let txt = "Inserire una quantita positiva, o rimuovere l'articolo";
		openModal('txtWarning', $('#modalWarningClick', parent.document)[0], txt);
		return;
	}
	let idArticolo = input.parentElement.parentNode.querySelector('[name=Articolo]').value;
	input.value = parseFloat(input.value).toFixed(4);
	getPrezzo(getURLWS(), idArticolo, input.value).done(function(response) {
		input.parentNode.nextElementSibling.innerHTML = parseFloat(response['prezzo']).toFixed(4);
	}).fail(function(jqXHR) {
		jqXHR.responseJSON.errors.forEach(function(obj) {
			if (obj[0].includes('token expired')) {
				parent.document.getElementById('tokenExpiredClick').click();
			}
		});
	});
	let queryUpdate = "UPDATE THIPPERS.YCARRELLO_PORTALE SET QUANTITA = '" + input.value + "' WHERE ID_AZIENDA = '" + key[0] + "' AND R_UTENTE_PORTALE = '" + key[1] + "' AND PROGRESSIVO = '" + key[2] + "' ";
	let url = getURLWS();
	url += '?id=GSQ&tokenUID=' + $('#token').val();
	url += '&company=' + company;
	url += '&query=' + queryUpdate;
	$.ajax({
		url: url,
		method: 'POST',
		dataType: 'json',
		data: '',
		contentType: 'application/json; charset=utf-8',
		success: function() {
			window.location.reload();
		},
		error: function(xhr) {
			xhr.responseJSON.errors.forEach(function(obj) {
				openModal('txtWarning', $('#modalWarningClick',
					parent.parent.document)[0], obj[0]);
			});
		},
		complete: function() {
			parent.rimuoviSpinner();
		}
	});
	let tot = 0;
	let total = $('#total');
	table.rows().every(function(rowIdx) {
		let trNode = table.row(rowIdx).node();
		let prezzo = 0;
		if (trNode.querySelector('[name=prezzo]') != null)
			prezzo = trNode.querySelector('[name=Prezzo]').value;
		let qta = trNode.querySelector('[name=Quantita]').value;
		let mlt = parseFloat(prezzo) * parseFloat(qta);
		tot = tot + mlt;
	});
	total[0].innerHTML = tot.toFixed(4);
}

function toggle() {
	$('#modalRemoveItem').modal('toggle');
}

function confirmCheckOutOrder() {
	$('#warningCheckOutClick')[0].click();
}

function procediSecondStep() {
	$('#modalFirstStep').modal('toggle');
	$('#warningCheckOutClick')[0].click();
}

function annullaCheckoutOrder() {
	$('#modalWarningCheckOut').modal('toggle');
	table.rows().deselect();
}

function checkOutOrder() {
	const responseMessage = document.getElementById('responseMessage');
	responseMessage.innerHTML = '';

	if ($('#IdModalitaSpedizioneHiddenInputValue').val() === '') {
		$('#IdModalitaSpedizioneHiddenInputValue').val(document.getElementById('shipmentMethod').value);
	}

	parent.mostraSpinner();
	//Loop tra le righe selezionate per costruire un json che contiene le chiavi dei record da cancellare
	let keys = [];
	let ret = false;
	table.rows().every(function(rowIdx, tableLoop, rowLoop) {
		let trNode = table.row(rowIdx).node();
		let key = trNode.querySelector('[name=key]').value;
		let qta = trNode.querySelector('[name=Quantita]');
		if (parseFloat(qta.value) > parseFloat(qta.parentNode.previousElementSibling.innerHTML)) {
			let txt = "Non e' possibile ordinare piu di quanto disponibile";
			openModal('txtWarning', $('#modalWarningClick', parent.document)[0], txt);
			ret = true;
		}
		keys.push({
			'id': rowIdx,
			'key': key,
			'quantita': qta.value
		});
	});
	if (ret) {
		parent.rimuoviSpinner();
		return;
	}
	let formData = {};
	$('#checkoutForm').serializeArray().forEach(function(field) {
		formData[field.name] = field.value;
	});
	let payload = {
		items: keys,
		formData: formData
	};
	$.ajax({
		url: getURLWS() + '?id=YCKOC&tokenUID=' + $('#token').val(),
		method: 'POST',
		dataType: 'json',
		data: JSON.stringify(payload),
		contentType: 'application/json; charset=utf-8',
		success: function(response) {
			responseMessage.innerHTML = '<div class="alert alert-success">Richiesta inviata con successo!</div>';
			checkoutForm.reset();
			setTimeout(() => {
				parent.rimuoviSpinner();
				window.location.reload();
			}, 3000);
		},
		error: function(xhr, status, error) {
			xhr.responseJSON.errors.forEach(function(obj) {
				if (obj[0].includes('token expired')) {
					parent.document.getElementById('tokenExpiredClick').click();
				} else {
					openModal('txtWarning', $('#modalWarningClick',
						parent.parent.document)[0], obj[0]);
				}
				responseMessage.innerHTML = '<div class="alert alert-danger">Errore durante l\'invio della richiesta. Riprova.</div>';
			});
			parent.rimuoviSpinner();
		},
	});
}

function removeItem(item) {
	let key = item.querySelector('input[name="key"]').value;
	let keys = [];
	keys.push({
		'id': 0,
		'value': key
	});
	removeItems(keys);
}

function removeItems(keys) {
	parent.mostraSpinner();
	$.ajax({
		url: getURLWS() + '?id=YREMC&tokenUID=' + $('#token').val(),
		method: 'POST',
		dataType: 'json',
		data: "{keys : '" + JSON.stringify(keys) + "'}",
		contentType: 'application/json; charset=utf-8',
		success: function(response) {
			if (response.errors.length > 0) {
				parent.rimuoviSpinner();
			} else {
				window.location.reload();
			}
		},
		error: function(xhr, status, error) {
			xhr.responseJSON.errors.forEach(function(obj) {
				try {
					if (obj[0].includes('token expired')) {
						parent.document.getElementById('tokenExpiredClick').click();
					}
				} catch (e) {
					window.location.reload();
				}
			});
			parent.rimuoviSpinner();
		}
	});
}

function getPrezzo(url, articolo, qta) {
	return $.ajax({
		url: encodeURI(url + '?id=RPEC&tokenUID=' + $('#token').val() + '&company=' + company + '&tipoUMVendita=V&codCliente=' + idCliente + '&codArticolo=' + articolo + '&qtaRichiesta=' + qta), // Replace with the actual URL
		method: 'GET',
		dataType: 'json'
	});
}

function confirmDelete(btn) {
	if (table.rows('.selected').data().length > 0) { //solo se ne ho selezionata almeno 1
		let txt = "Sei sicuro di rimuovere gli articoli selezionati dal carrello? ";
		$('#removeItemBtn').attr('onClick', 'removeItems()');
		openModal('removeItemTxt', $('#removeItemClick')[0], txt);
	}
}

function getURLWS() {
	let ris;
	let url = window.location.href;
	let wbAppPth = parent.document.getElementById('webAppPath').value;
	let cut = url.indexOf(wbAppPth);
	ris = url.substring(0, cut);
	ris += wbAppPth;
	ris += "/ws";
	return ris;
}

//72676 <
function getURLREST() {
	let ris;
	let url = window.location.href;
	let wbAppPth = parent.document.getElementById('webAppPath').value;
	let cut = url.indexOf(wbAppPth);
	ris = url.substring(0, cut);
	ris += wbAppPth;
	return ris;
}
//72676 >