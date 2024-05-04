# JavaUno - Webapp

The card game as online game (Webapp)

## Version
3.0.0 \
2024, May, 4th, yeah, may the fourth be with you ;)

## Description
This is a webapp, powered by SpringBoot, Java and VueJS.

## Documentation

### API Documentation
* Swagger
  * API Doc: `/v2/api-docs`
  * UI: `/swagger-ui.html`

### Manual
* `/manual.docx` or
* `/manual.pdf`
    
## How to run
* Pre-requisite: Java (8+, tested with Java 8)
* build it with 'mvn clean package' (you will get a javauno-xx.jar file)
* run following command in terminal: java -jar javauno-xx.jar\
  (xx is the version)

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
* The Backend source is licensed under a CC-BY-SA-NC 4.0 Licence
  * License: https://creativecommons.org/licenses/by-nc-sa/4.0/
  * Creator: Johanna Herrmann ([johannas-kram](https://github.com/johannas-kram))
