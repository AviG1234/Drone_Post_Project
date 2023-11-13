## Overview
Drone Post is a corce project simulating a system of receiving and managing orders from users and delivering packages by drones. the project is an interaction between two programs a user interface code for interacting with the Drone Post system (DronePostUI Folder) and a server-side code for handling order management and drone deliveries (DronePost Folder).

## System parameters and follow up
- Simulated delivery times are generated at a rate of 1000 to 1, 
- The order status is logged in the DronePost/serverLog.txt file, providing a record of the system's activities.
- If no drones are saved in the SQL server, the system will automatically load 20 drones to ensure smooth operations.

## SQL Server
- SQL script for setting up the necessary tables in the Microsoft SQL Server. This script is located in the DronePost/DataBaseScript folder.
