# JavaUno - Online Version - Backend

Das beliebte Kartenspiel als Multiplayer-Browser-Game

## Aufgaben des Backends
Dies ist das Backend des Browser-Games, eine Springboot-Application.\
Die gesamte Spiel-Logik passiert innerhalb des Backends auf einem SSD-vRoot-Server.\
Die Springboot-Application wird über entsprechende Requests vom Frontent angesprochen,
eine einfache Single-Page-Application auf einem nginx, vermutlich mit VUE.js\

## Trennung von Backend und Frontend
Backend und Frontend sind so voneinander getrennt, dass sie beliebig ausgetauscht werden können.\
So ist es beispielsweise möglich, statt dem ebenfalls von mir geschriebenen Web-Frontend
ein eigenes Desktop-UI Frontend zu schreiben.

## Dokumentation
* Swagger
    * API Doc: /v2/api-docs
    * UI: /swagger-ui.html