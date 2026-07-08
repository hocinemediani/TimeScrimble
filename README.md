# 🎮 TimeScrimble, an online pictionary-like game
> A project built entirely with **Java Spring Boot, HTML and Javascript**.  
> ✏️ Draw. ✍️ Guess. 🏆 Win. Every game is its own fun !
---


## 📌 Project Overview
Pictionary is a great game. You draw, show people what you've drawn and hope that they'll get you.
But what's even more fun than drawing and guessing on paper ? Drawing and guessing on the internet !
In TimeScrimble, every move matters. As a drawer, you'll have 60 seconds to draw the best representation of the word you have to
make people guess. And as a guesser, every second counts, as the faster you discover the drawer's word,
the more points you'll be able to grab. Rise to the top by being the best all-around drawer and guesser !

---


## 🚀 Features
| Feature | Status | Notes |
|---|---|---|
| Real-time Drawing | 🟩 Done ! | Communication and sync via WebSockets |
| Session & State Management | 🟩 Done ! | Handles multiple concurrent games, reconnections, and refreshes |
| Role Cycling | 🟩 Done ! | Automatic rotation of the drawer each round |
| Anti-Cheat Security | 🟩 Done ! | Target word is strictly routed to the drawer only |
| Word Dictionary | 🟩 Done ! | Built-in pool of 100 playable words |
| Player Authentication | 🟩 Done ! | Password login or guest mode (nickname only) |
| End Game Podium | 🟩 Done ! | Final screen displaying the Top 3 players |
| Custom Word Lists | 🟧 Planned | Allow hosts to create custom themes |
| Player Avatars | 🟧 Planned | Custom profile pictures for authenticated users |


## 🖼️ Screenshots & Demos

### 📷​ The main gameplay page.

<img width="1920" height="925" alt="Nouveau projet" src="https://github.com/user-attachments/assets/9a092c30-af95-4b71-bdcd-85fbd0560fde" />

### 📷​ Lobby with existing games and the option to create a new one.

<img width="1920" height="926" alt="image" src="https://github.com/user-attachments/assets/d23ef8b3-72d6-4d21-a54a-b83614fe9602" />


### 📷​ Endscreen after winning a game.

<img width="1854" height="963" alt="image" src="https://github.com/user-attachments/assets/edabaf17-56ab-4108-a866-228e88956655" />

---


## 🗓️ Development Timeline
| Date       | Milestone                   | Status    |
|------------|-----------------------------|-----------|
| 2026-02 | Project initialized          | ✅ Done     |
| 2026-05 | Project Due                  | ✅ Done     |
| 2026    | Continuation of the project  | 🟧 To do    |
---


## ▶️ Running the Game
```bash
# Compile & Run
./mvnw spring-boot:run
```
>💡 Make sure you're using JDK 14+ !
---


## 🧪 TODO
- [ ] A lot.
---


## 🐞 Known Issues
- [ ] None for now.
---


## 🤝 Contributors
| Names        | Role           | Github Profile|
|-------------|----------------|---------------|
| *Hocine Mediani* | 👨‍💻 Developer  | [> hocinemediani](https://github.com/hocinemediani) |
| *Ilian Kraifi* | 👨‍💻 Developer   | [> iki389](https://github.com/ik389) |
| *Benjamin Krief* | 👨‍💻 Developer   | [> Banshai012](https://github.com/Banshai012) |
---


## 📜 License
This project is open-source under the [MIT License](LICENSE).

---

## 💬 Feedback & Contact
📬 Found a bug? Have suggestions?  
Open an issue or contact me directly via [hocine.mediani7@gmail.com](mailto:hocine.mediani7@gmail.com)

---

Shield: [![CC BY-NC-ND 4.0][cc-by-nc-nd-shield]][cc-by-nc-nd]

This work is licensed under a
[Creative Commons Attribution-NonCommercial-NoDerivs 4.0 International License][cc-by-nc-nd].

[![CC BY-NC-ND 4.0][cc-by-nc-nd-image]][cc-by-nc-nd]

[cc-by-nc-nd]: http://creativecommons.org/licenses/by-nc-nd/4.0/
[cc-by-nc-nd-image]: https://licensebuttons.net/l/by-nc-nd/4.0/88x31.png
[cc-by-nc-nd-shield]: https://img.shields.io/badge/License-CC%20BY--NC--ND%204.0-lightgrey.svg
