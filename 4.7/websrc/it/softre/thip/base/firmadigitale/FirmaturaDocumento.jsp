<!DOCTYPE html>
<%@page import="com.thera.thermfw.base.IniFile"%>
<%
String webAppPath = IniFile.getValue("thermfw.ini", "Web", "WebApplicationPath");
//Lo recupero dalla request e lo uso per filtrare i documenti da firmare
String idDevice = request.getParameter("IdDevice");
String idAzienda = request.getParameter("IdAzienda");
%>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<title>Firma Documento</title>
<link rel="icon" type="image/x-icon" href="/<%=webAppPath%>/thermweb/image/nav/favicon.ico">
 <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.4.1/dist/css/bootstrap.min.css"
    integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">
<link
	href="/<%=webAppPath%>/it/softre/thip/base/firmadigitale/css/modal.css"
	rel="stylesheet">
<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
	integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
	crossorigin="anonymous"></script>
<script
	src="https://cdn.jsdelivr.net/npm/popper.js@1.14.7/dist/umd/popper.min.js"
	integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1"
	crossorigin="anonymous"></script>
<script
	src="https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/js/bootstrap.min.js"
	integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
	crossorigin="anonymous"></script>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<style>
        html, body {
            height: 100%;
            margin: 0;
            touch-action: manipulation; /* Prevent zooming */
        }
        .container-fluid {
            height: 100%;
        }
        .row {
            height: 100%;
        }
        .col-left {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        .col-right {
            height: 100%;
        }
        .pdf-viewer {
            width: 100%;
            height: 99vh;
            border: none;
        }
         .button-container {
        margin-top: 10px;
        display: flex;
        gap: 10px;
    }
    .btn-custom {
        background-color: rgb(117, 144, 166);
        color: white;
        border: none;
        padding: 10px 20px;
        font-size: 16px;
        border-radius: 5px;
        cursor: pointer;
        transition: background-color 0.3s;
    }
    .btn-custom:hover {
        background-color: rgb(117, 144, 166);
    }
     #signature-pad { 
/*         border: 2px solid #000; */
/*         border-radius: 10px; */
/*         box-shadow: 0 4px 8px rgba(0,0,0,0.1); */
	width:500px;
	height:500px;
     } 
    
    canvas {
touch-action: none; /* Prevent touch events from being handled by the browser */
}
    </style>
</head>
<body>
	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-6 col-sm-6 col-left p-2">
			<h3>Firma qui</h3>
				<canvas id="signature-pad"
					style="border: 1px solid #000000;"></canvas>
				<div class="button-container">
				<button class="btn-custom" onclick="clearSignature()">Pulisci</button>
				<button class="btn-custom" onclick="saveSignature()">Salva</button>
				</div>

			</div>
			<div class="col-lg-6 col-sm-6 col-right">
				<iframe class="pdf-viewer" id="pdfViewer" src="" frameborder="0"></iframe>
				<input type="hidden" id="chiaveDocumentoDigitale"></input>
				<input type="hidden" id="chiaveDocumentoDaFirmare"></input>
			</div>
		</div>
	</div>
	<div style="display: none" class="text-center">
				<a href="#modalWarning" id="modalWarningClick"
					class="trigger-btn" data-toggle="modal"></a>
	</div>
	<div id="modalWarning" class="modal fade">
			<div class="modal-dialog modal-confirm">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title" id="modalTitle"></h4>
					</div>
					<div class="modal-body">
						<p id="txtWarning" class="text-center"></p>
					</div>
					<div class="modal-footer" style="flex-wrap: unset;">
						<button class="btn btn-block"
							onClick="$('#modalWarning').modal('toggle');"
							data-dismiss="modal">OK</button>
					</div>
				</div>
			</div>
	</div>
	 <script>
        $(document).ready(function() {
           // fetchDocument();
        	scheduleNextPoll();
        	document.addEventListener("visibilitychange", function() {
        	    if (document.hidden) {
        	        pollingDelay = 15000;
        	    } else {
        	        pollingDelay = 5000;
        	        scheduleNextPoll();
        	    }
        	});
        });
        
        document.addEventListener('touchstart', function(event) {
            if (event.touches.length > 1) {
                event.preventDefault();
            }
        }, { passive: false });

        var lastTouchEnd = 0;
        document.addEventListener('touchend', function(event) {
            var now = (new Date()).getTime();
            if (now - lastTouchEnd <= 300) {
                event.preventDefault();
            }
            lastTouchEnd = now;
        }, false);
        
        function openModal(paragraphId,elementToClick,text,titleId,titleText){
        	$('#'+paragraphId).html(text);	
        	$('#'+titleId).html(titleText);
        	elementToClick.click();	
        }

        function fetchDocument() {
            $.ajax({
                url: getURLWS() + '/firmaDigitale/documenti/attesaFirma/recupera' +
                     '?IdDevice=' + encodeURIComponent('<%=idDevice%>') +
                     '&IdAzienda=' + encodeURIComponent('<%=idAzienda%>'),
                method: 'GET',
                contentType: 'application/json',
                success: function(data) {

                    if (data.info != 'Nessun documento') {
                        pollingDelay = 5000;
                        var base64PDF = data.file;
                        var chiaveDocumentoDigitale = data.info;

                        $('#chiaveDocumentoDigitale').val(chiaveDocumentoDigitale);

                        var pdfDataUrl = 'data:application/pdf;base64,' + base64PDF;
                        $('#pdfViewer').attr('src', pdfDataUrl);
                        stopPolling();

                    } else {
                        $('#pdfViewer').attr('src','');

                        pollingDelay = Math.min(pollingDelay * 2, MAX_DELAY);

                        scheduleNextPoll();
                    }
                },
                error: function(err) {
                    console.error('Error fetching document:', err);
                    pollingDelay = Math.min(pollingDelay * 2, MAX_DELAY);
                    scheduleNextPoll();
                }
            });
        }
        
        function scheduleNextPoll() {
            stopPolling(); // evita duplicati
            pollingTimeout = setTimeout(fetchDocument, pollingDelay);
        }

        function stopPolling() {
            if (pollingTimeout) {
                clearTimeout(pollingTimeout);
                pollingTimeout = null;
            }
        }

        //function startPolling() {
		//	if (!pollingInterval) { // Only set the interval if it's not already set
		//	pollingInterval = setInterval(fetchDocument, 5000); // Poll every 5 seconds
		//	}
		//}

        function getURLWS() {
            var ris;
            var url = window.location.href;
            var cut = url.indexOf("<%=webAppPath%>");
            ris = url.substring(0, cut);
            ris += "<%=webAppPath%>";
            ris += "/api";
            return ris;
        }

        var canvas = document.getElementById('signature-pad');
        var ctx = canvas.getContext('2d');
        var drawing = false;
        var pollingTimeout = null;
        var pollingDelay = 5000; // start veloce
        var MAX_DELAY = 15000;
        
        var scale = 2; // Change this scale based on your desired canvas size.

        canvas.width = canvas.clientWidth * scale;
        canvas.height = canvas.clientHeight * scale;
        ctx.scale(scale, scale);
        ctx.lineWidth = 6;
        // Function to get the mouse or touch position relative to the canvas
        function getPosition(event) {
            var rect = canvas.getBoundingClientRect();
            var x, y;
            if (event.touches) {
            	 x = event.touches[0].pageX - rect.left - window.scrollX;
                 y = event.touches[0].pageY - rect.top - window.scrollY;
            } else {
            	x = event.pageX - rect.left - window.scrollX;
                y = event.pageY - rect.top - window.scrollY;
            }
            return { x: x, y: y };
        }

        // Start drawing
        function startDrawing(event) {
            drawing = true;
            ctx.beginPath();  // Start a new path each time drawing starts
            var position = getPosition(event);
            ctx.moveTo(position.x, position.y);
        }

        // Draw on the canvas
        function draw(event) {
            if (drawing) {
                var position = getPosition(event);
                ctx.lineTo(position.x, position.y);
                ctx.stroke();
            }
        }

        // Stop drawing
        function stopDrawing() {
            drawing = false;
        }

        // Event listeners for mouse events
        canvas.addEventListener('mousedown', startDrawing);
        canvas.addEventListener('mousemove', draw);
        canvas.addEventListener('mouseup', stopDrawing);
        canvas.addEventListener('mouseout', stopDrawing);

        // Event listeners for touch events
        canvas.addEventListener('touchstart', function(event) {
            event.preventDefault(); // Prevent scrolling when touching the canvas
            startDrawing(event);
        });
        canvas.addEventListener('touchmove', function(event) {
            event.preventDefault(); // Prevent scrolling when touching the canvas
            draw(event);
        });
        canvas.addEventListener('touchend', stopDrawing);
        canvas.addEventListener('touchcancel', stopDrawing);

        // Clear the canvas
        function clearSignature() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.beginPath();  // Reset the path
        }

        // Check if the canvas is empty
        function isCanvasEmpty() {
            const blankCanvas = document.createElement('canvas');
            blankCanvas.width = canvas.width;
            blankCanvas.height = canvas.height;
            return canvas.toDataURL() === blankCanvas.toDataURL();
        }


        // Save the signature
        function saveSignature() {
        	var chiaveDocumentoDigitale = $('#chiaveDocumentoDigitale').val();
        	if(chiaveDocumentoDigitale == ""){
        		openModal('txtWarning',$('#modalWarningClick')[0],'Nessun documento da firmare','modalTitle','Attenzione!');
                return;
        	}
            if (isCanvasEmpty()) {
            	openModal('txtWarning',$('#modalWarningClick')[0],'La firma e vuota!','modalTitle','Attenzione!');
                return;
            }
            
            var canvas = document.getElementById('signature-pad');
            var signatureDataUrl = canvas.toDataURL('image/png');
            
            var requestData = {
                documentId: chiaveDocumentoDigitale,
                signature: signatureDataUrl,
            };

            $.ajax({
                url: getURLWS() + '/firmaDigitale/documenti/attesaFirma/firma',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(requestData),
                success: function(response) {
                	//openModal('txtWarning',$('#modalWarningClick')[0],'Documento firmato correttamente!','modalTitle','Congratulazioni!');
                    location.reload();
                },
                error: function(err) {
                    openModal('txtWarning',$('#modalWarningClick')[0],'Errori nel salvataggio!','modalTitle','Attenzione!');
                }
            });
        }
    </script>
</body>
</html>
