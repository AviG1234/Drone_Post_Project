# Drone_Post_Project, school project Michael Bramnick and Aviv Guy.
 Drone Post is a system of receiving and managing orders from users and delivering packages by drones.   The current repository contains code based on the given requirements in the Documentation folder.
# DronePost folder contains server code and DronePostUI folder contains users code.
# SQL script is in TableScript.sql in DronePost/DataBaseScript folder (microsoft sql server).
# simulated delivery times are at a rate of 1000 to 1 and order status are logd in DronePost/serverLog.txt.
# if no drones are saved in the SQL server the system will load 20 drones.


Drone Post is a system designed for receiving and managing orders from users and delivering packages using drones. This repository contains the codebase for the Drone Post system based on the given requirements. The Documentation folder provides additional details about the system's functionality.

## Overview
Drone Post is a corce project simulating a system of receiving and managing orders from users and delivering packages by drones. the project is an interaction between two programs a user interface code for interacting with the Drone Post system (DronePostUI Folder) and a server-side code for handling order management and drone deliveries (DronePost Folder).
Simulated delivery times are generated at a rate of 1000 to 1, reflecting a fast-paced delivery system.

    DataBaseScript/TableScript.sql:
        SQL script for setting up the necessary tables in the Microsoft SQL Server. This script is located in the DronePost/DataBaseScript folder.

    Simulation of Delivery Times:
        Simulated delivery times are generated at a rate of 1000 to 1, reflecting a fast-paced delivery system.

    Order Status Logging:
        The order status is logged in the DronePost/serverLog.txt file, providing a record of the system's activities.

    Default Drones:
        If no drones are saved in the SQL server, the system will automatically load 20 drones to ensure smooth operations.

System Components
1. DronePost (Server)

    Manages order processing and drone deliveries.
    Utilizes the SQL Server for data storage and retrieval.
    Logs order status in serverLog.txt.
    Simulates delivery times for efficient testing.

2. DronePostUI (User Interface)

    Provides a user-friendly interface for users to interact with the Drone Post system.
    Allows users to place orders, track deliveries, and manage account settings.
