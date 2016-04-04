$(document).ready(function () {

	var sendMessageBtn = $('#send-message-button');
	var inputElem = $('#input-text');
	var responseElem = $('#response');
	
	/**
	 * Send message operation
	 */
	sendMessageBtn.click(function () {
		var request = {};
		request.message = inputElem.val();

		var event = new CustomEvent("send-message-event", {
			detail: {
				data: request
			},
			bubbles: true,
			cancelable: true
		});
		document.dispatchEvent(event);
	});
			
	/**
	 * Get message event listener
	 */
	document.addEventListener("get-message-event", function (data) {
		var responseObject = data.detail.data;
		responseElem.text(responseObject.message);
	});
});