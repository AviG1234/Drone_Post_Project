## Overview
Drone Post is a comprehensive project that simulates a system for receiving, managing orders from users, and executing deliveries using drones. The project encompasses an interaction between two programs: the user interface code (DronePostUI Folder) facilitating user interactions with the Drone Post system, and the server-side code (DronePost Folder) responsible for order management and drone deliveries.
System Parameters and Follow-Up

The Drone Post system operates based on the following parameters:

# Simulated Delivery Times:
- Delivery times are simulated at a rate of 1000 to 1, ensuring a dynamic and efficient testing environment.

# Order Status Logging:
- The system maintains a detailed log of order statuses, accessible in the DronePost/serverLog.txt file. This log provides a comprehensive record of the system's activities.

# Automatic Drone Loading:
- In cases where no drones are saved in the SQL server, the system takes proactive measures by automatically loading 20 drones. This ensures the system's smooth operations and responsiveness.

## SQL Server
- The SQL Server component is crucial for data storage and retrieval within the Drone Post system. The necessary tables are set up using the provided SQL script located in the DronePost/DataBaseScript folder. Executing this script in Microsoft SQL Server establishes the foundation for seamless operation and efficient data management.
