
Kevin is intented to be the overseer logic for my home automation efforts.

I belive that high level logic should be implemented in some real language; and use mqtt to deliver commands to operational devices.

It right now does the following:
* autoclean
  * when nobody at home; but someone was there ~5 minutes ago it starts the vacuum
  * ...and sends it back home if someone came back in an hour
* some alexa skills
  * control kodi
  * control mira


mqtt channels

 * mirobo/lastClean
	last clean time timestamp 
