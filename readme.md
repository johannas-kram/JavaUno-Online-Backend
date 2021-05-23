# JavaUno - Online Version - Backend

The card game as online game - the backend
This is the Backend for play.java-uno.de

## Version
2.0 (2021, May, 16th)

## Description
The backend offers the api endpoints for the frontend and controls all game instances.

## Frontend
You can use your own frontend or use my frontend under https://github.com/tomatenmark/JavaUno-Online-Frontend\
If you use own frontend, it has to use the same endpoints, like my frontend

## API Documentation
* Swagger
    * API Doc: /v2/api-docs
    * UI: /swagger-ui.html
    
## How to run the backend
* Pre-requisite: Java (8+)
* build it with 'mvn clean package' (you will get a javauno-xx.jar file)
* run following command in terminal: java -jar javauno-xx.jar\
  (xx is the version)

### Spielanleitung
https://github.com/tomatenmark/JavaUno-Online-Frontend/ ->  manual.docs
(generated to https://play.java-uno.de/manual.pdf)

## tokenized-game-create
* This Feature is optional
  * Disable: java -jar javauno-xx.jar --feature.tokenized_game_create=off
  * Enable: java -jar javauno-xx.jar --feature.tokenized_game_create=on
  * Default: Disabled
* A valid token must be provided to create a game
* Token is given via post field 'token'
* Every authorized person should have their own token
* Token-Syntax: 2 random youtube-video-id-like strings, separated by a dot
  * Example token: Dsnmn7Twqd1.j8yZ15Ji210
* Creation and storing of tokens on backend-server
  * Creation of tokens and storing of hashes happen externally\
    (not concern of this application)
  * For every token, a bcrypt-hash of the second youtube-video-id-like string is stored in ./tokens/\
    while the first youtube-video-id-like string is the filename
    * example: Hash of j8yZ15Ji210 is stored in file named Dsnmn7Twqd1
* Token validation:
  * First youtube-video-id-like string is used to search for a file named like that and load the hash from it.
  * Second youtube-video-id-like string is checked with loaded hash to validate token.
  
## License
* The Backend source code is licensed under a CC-BY-SA-NC 4.0 Licence\
   * License: https://creativecommons.org/licenses/by-nc-sa/4.0/
   * Creator: Mark Herrmann (Nickname: Tomatenmark)

## Compatibility
* version 2.x frontend <-> version 1.x backend
  * NOT compatible
* Version 2.x backend <-> version 1.x frontend
  * Compatible, if tokenized-game-create feature is disabled
  * manual should be modified (due to randomized selection of the beginning player)

## ChangeLog

### Version 2.0
* Bugfix: If next round was started very quick after a bots last turn, the fronted got misleading next turn pushes
* Modified draw behaviour: Draw duties or penalties are drawn all at once
* New optional feature: tokenized-game-create (see #tokenized-game-create) (limited downwards compatibility)
* New feature: You can switch the game to another device
* New feature: You can leave the running game (will become a bot)
* New feature: You can kick players (humans and bots, humans will become bots)
* New feature: You and the other players can commit to stop running game
* New feature: Chat
* Improvement: Beginning Player is selected randomly (or the last winner)
* Improvement: Old game deletion threshold increased to 8 hours.
* Improved card dealing (improved first receiver)
* Various modifications to help the vue.js frontend with transitions
