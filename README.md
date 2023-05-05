# distributed-student-platform

<div align="center">

![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Erlang](https://img.shields.io/badge/-Erlang-A90533?logo=erlang&logoColor=white&style=for-the-badge)
![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E)

![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![Glassfish](https://img.shields.io/badge/Glassfish-025E8C.svg?style=for-the-badge&logo=oracle&logoColor=white)
![NGINX](https://img.shields.io/badge/-NGINX-009639?logo=nginx&logoColor=white&style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&style=for-the-badge&logoColor=white)
![LaTeX](https://img.shields.io/badge/latex-%23008080.svg?style=for-the-badge&logo=latex&logoColor=white)

</div>

University project for **Distributed Systems and Middleware Technologies** course (MSc Computer Engineering @ University of Pisa, A.Y. 2022-23)

**Student University Platform** is a **distributed web application** aimed at providing multiple features to students and professors, which are:

- Allow students to **book a meeting** with a professor;
- Provide **chatroom** functionality among students where they can share opinions and suggestions about a course.

Both students and professors have a dedicated page where they can view scheduled meetings and cancel them.
Within the course page, on the other hand, it is possible to access the dedicated chatroom or look at the list of available slots and choose the meeting
to reserve.


## System Architecture

![System Architecture](docs/img/system_architecture/system_architecture.png)

The web application, as we can see in figure, consists of a **Client Application**, a **Jakarta Application Server**, and **multiple Erlang Servers**.

- The **Jakarta EE** Application Server, which implements most of the functionality of the application, uses as its reference implementation, **Glassfish 6.2.5** and also communicates with a **MySql relational database** located on a different machine.
- The **Erlang** Servers handle the Chatrooms of the application through an **HTTP websocket communication**. They are managed by a **Load Balancer** and configured by a **Master Node** and both are on another machine. We chose **Nginx** for the Load Balancer since it natively supports the WebSocket protocol both as **reverse proxy** and **load balancer**.
- The client application was implemented using the combination of **HTML**, **CSS** and **Javascript**.


## Synchronization and communication

- Multiple clients **exchange messages** with each other in chat rooms **concurrently**. The chat of each client node must be synchronized.
- When a **student enters or leave** the chatroom each client **node must be synchronized** in order to see a consistent list of students within the chat (i.e. "online students").
- When a student **books a meeting** in an available time slot or remove a previously booked meeting, each client **node must be synchronized to view the same consistent state** (both for students and professors).
- When a professor creates or delete a course, the server must show the course to all the connected client in the "browse", "search" sections and, in case of deletion, inside the course page.


## Structure of the repository

```text
Distributed-University-Chatroom-Platform
|
├── erlang-server
│   ├── chat_server
│   ├── master_node
│   └── nginx
|
├── glassfish-server
│   ├── ejb
│   │   └── src
│   |       ├── dao
|   |       └── ejb   
│   ├── ejb-interfaces
│   │   └── src
│   |       ├── dto
│   |       ├── enums
|   |       └── interfaces   
│   └── web
│       └── src
│           ├── java/it/unipi/dsmt/student_platform
│           |   ├── servlets
|           |   └── utility  
│           └── webapp    
│               ├── assets
|               |   ├── css
|               |   ├── javascript
|               |   ├── img
|               |   └── libs 
|               └── WEB-INF 
|                   └── jsp  
└── mysql-server 

```


## Authors

- [Riccardo Sagramoni](https://github.com/RiccardoSagramoni)
- [Federico Montini](https://github.com/FedericoMontini98)
- [Fabrizio Lanzillo](https://github.com/FabrizioLanzillo)
