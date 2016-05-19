# Native-Messaging
Chrome's native messaging example project for Java.

Project structure:
  - App - contains chrome's extension for native messaging example.
  - App_Native - contains native application (java project).
  - Host - native messaging host (+ registry file).
  - Web - contains a simple website which send and receives message from native app.

If you are using eclipse, use maven's eclipse plugin:
```sh
cmd: cd App_Native
cmd: mvn eclipse:eclipse
```