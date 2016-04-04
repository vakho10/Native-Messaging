/* content.js */

/**
 * Send message event listener
 * 
 * Client dispatches 'send-message-event' event with data object. 
 * Then the data object is sent to background script.
 */
document.addEventListener("send-message-event", function (data) {
	var request = data.detail.data;
	console.info(request);
	// Send message to the background script
	chrome.runtime.sendMessage(request, null);
});

/**
 * Listens to the background script and dispatches 'get-message-event' 
 * to the client when the data is received.
 */
chrome.runtime.onMessage.addListener(function (response, sender, sendResponse) {
	console.info(response);
	// Send response to the front page
	var event = new CustomEvent("get-message-event", {
		detail: {
			data: response
		},
		bubbles: true,
		cancelable: true
	});
	document.dispatchEvent(event);
});