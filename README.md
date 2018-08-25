[![Build Status](https://travis-ci.org/baschermann/File-Transfer.svg?branch=master)](https://travis-ci.org/baschermann/File-Transfer)

## File-Transfer 

Peer 2 Peer File Transfer program I did as a learning experience.

The goals:
- Knowing JavaFX and the differences between Swing/AWT
  - Finding and using a tool for JavaFX GUI editing (Scene2D)
  - Not getting lost in the created XML

- Network programming
  - Socket programming on top of TCP/IP
  - Selfmade protocol
  - Using IPv4 + IPv6
  - Using a threaded approach (no NIO)

- File manipulation
  - Taking a file apart, sending it in pieces and putting it back together
  - CRC check for file consistency

Known Issues:
- Program does not close correctly (if you find out what Thread-3 is, please tell me)
- When up or download speeds are throttled, it will slow down above ~1 MB (full speed works without throttling)

Features for future additions
- Buffered streams for File IO
- Displaying up and download speeds
- GUI Overhaul
- Tests (and possible refactoring)
  - (rewrite for TDD)
- Threaded CRC check
