.\mvnw.cmd spring-boot:run# PlayMatch Security Explained

## What is Security in PlayMatch?

Hi there! In this document, I'll explain how security works in the PlayMatch application in a way that's easy to understand.

### What Does "Security" Mean?

Think of security like the locks on your house door. It keeps strangers out and only lets people with the right keys come in. In apps like PlayMatch, security helps make sure:

1. Only you can access your account
2. Your personal information stays private
3. Nobody can pretend to be you

## Security Features in PlayMatch

### Password Protection

When you create a password in PlayMatch:
- Your password is never stored as-is
- It gets turned into a secret code using something called "Argon2" (like a super-strong blender for passwords)
- Even if someone looked at the database, they couldn't figure out your password

### Login Tokens (JWT)

When you log in:
- The app gives you a special digital ticket (JWT token)
- This ticket proves it's really you
- The ticket has an expiration time (like a movie ticket that only works for one show)
- You use this ticket to access protected parts of the app

### Protection Against Common Threats

PlayMatch protects against:

1. **Password Guessing**: If someone tries to guess your password too many times, they get blocked
2. **Data Snoopers**: Information sent between your device and the server is encrypted (scrambled)
3. **Cross-Site Attacks**: Special protections stop other websites from tricking you into doing things in PlayMatch
4. **Injection Attacks**: The app carefully checks all information it receives to make sure no one is trying to trick it

## Important Security Terms

- **JWT (JSON Web Token)**: A digital ticket that proves who you are
- **Encryption**: Scrambling information so only the right people can read it
- **Authentication**: Proving you are who you say you are (like showing ID)
- **Authorization**: Checking if you're allowed to do something
- **CORS**: Rules about which websites can talk to our app
- **Password Hashing**: The way we turn your password into a secret code

## How Your Data Stays Safe

1. **In Transit**: When you send information to PlayMatch, it travels through a secure tunnel (HTTPS)
2. **At Rest**: Information stored in our database is protected
3. **Access Control**: Only the parts of the app that need your information can see it

## Security Best Practices for Users

1. Use a strong password that's hard to guess
2. Don't share your login information with others
3. Log out when using shared computers
4. Be careful about clicking links in emails claiming to be from PlayMatch

Remember: Security is like a team sport - we've built strong protections, but we need your help to keep everything safe!
