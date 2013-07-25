# Kanban Now Card Service


This is a basic service for handling Kanban cards for KanbanNow, my personal Kanban web app.

KanbanNow is currently only for my personal use and use by a few beta testers.

I have a version of KanbanNow that is working, but it's a monolithic design with all

functionality in one WAR file.


Over time I am migrating the service functionality into separate micro-services.

This service is the first such service.


The current implementation of the card-service only handles card postponement functionality.


##Usage

Build the executable jar with:

gradle shadowBuild

Run with:

java -jar build/libs/kanban-now-card-service.jar server {properties file}



Rest(like*) Interface:

    * To retrieve a list of postponed cards for a specific board

        GET /cards/board/{boardId}

    * To postpone a card

        POST /cards/{cardId}/postpone

        pass number of days to postpone in the body


Coming next:

    * The ability to get all cards for a board (not just postponed one)
    * The ability to add cards to a board



