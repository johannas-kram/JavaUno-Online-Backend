# JavaUno - Webapp

The card game as online game (Webapp)

## Version
3.0.0 \
2024, May, 10th

## Description
This is a webapp, powered by SpringBoot and VueJS.

## Documentation

### API Documentation
* Swagger
  * API Doc: `/v2/api-docs`
  * UI: `/swagger-ui.html`

### Manual
* `/manual.docx` or
* `/manual.pdf`
    
## How to run
* Pre-requisite: Java (17+, tested with Java 17)
* build it with 'mvn clean package' (you will get a javauno-xx.jar file)
* run following command in terminal: java -jar javauno-xx.jar\
  (xx is the version)

## Application Arguments
* `--server.port`: Sets the port the applications listens to (default: 9001)
* `--server.address`: Sets the ip addresses the application will be reachable from (default: 0.0.0.0 meaning 'all')
* `--data.path`: Sets the path to the data directory (default: ./data)
* `--feature.tokenized_game_create`:
  Enables or disables the feature [tokenized-game-create](#tokenized-game-create) (default: off)

## tokenized-game-create
With this feature enabled, a valid token must be provided to create a new game.

### Enable / Disable
* Disable: java -jar javauno-xx.jar --feature.tokenized_game_create=off
* Enable: java -jar javauno-xx.jar --feature.tokenized_game_create=on
* Default: Disabled

### Token
* A valid token must be provided to create a game, via URL.
  * /api/game/create/{token}
* Every authorized person should have their own token
* Token-Syntax: 2 random youtube-video-id-like strings, separated by a dot
  * Example token: Dsnmn7Twqd1.j8yZ15Ji210
* Creation and storing of tokens on backend-server
  * Creation of tokens and storing of hashes happen externally\
    (not concern of this application)
  * For every token, a bcrypt-hash of the second token part is stored under `${data.path}/tokens`,
    while the filename is the first token part.
    * example: Hash of j8yZ15Ji210 is stored in file named Dsnmn7Twqd1
* Token validation:
  * First youtube-video-id-like string is used to search for a file named like that and load the hash from it.
  * Second youtube-video-id-like string is checked with loaded hash to validate token.
  
## License
* The source code is licensed under a CC-BY-SA-NC 4.0 Licence
  * License: https://creativecommons.org/licenses/by-nc-sa/4.0/
  * Creator: Johanna Herrmann ([johannas-kram](https://github.com/johannas-kram))
