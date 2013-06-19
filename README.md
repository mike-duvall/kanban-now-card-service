# Kanban Now Card Service


This is a basic service for handling Kanban cards for my web app I'm calling KanbanNow.

KanbanNow is currently only for my personal use and use by a few beta testers.

I have a version of KanbanNow that is working, but it's a monolithic design with all

functionality in one WAR file.


Over time I am migrating the service functionality into separate micro-services.

This service is the first such service.


The current implementation only handles card postponement functionality.


##Usage

Build the executable jar with:

gradle shadowBuild

Run with:

java -jar build/libs/kanban-now-card-service.jar server {properties file}



Rest(like) Interface:

    * To retrieve a list of postponed cards for a specific board

        GET /cards/board/{boardId}


Coming next:  The ability to postpone a specific card



